/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.FSM;
import uk.ac.imperial.presage2.util.fsm.Transition;
import uk.ac.imperial.presage2.util.fsm.FSMDescription;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.fsm.StateType;
import uk.ac.imperial.presage2.util.fsm.TransitionCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationSpawnEvent;
import uk.ac.imperial.presage2.util.protocols.FSMProtocol;
import uk.ac.imperial.presage2.util.protocols.MessageAction;
import uk.ac.imperial.presage2.util.protocols.MessageTypeAndContentsCondition;
import uk.ac.imperial.presage2.util.protocols.MessageTypeCondition;
import uk.ac.imperial.presage2.util.protocols.SpawnAction;
import uk.ac.imperial.presage2.util.protocols.ConversationSpawnEvent;
import uk.ac.imperial.presage2.util.protocols.FSMConversation;

/**
 * @author dws04
 *
 */
public abstract class IPConProtocol extends FSMProtocol {

	private final Logger logger;
	protected final EnvironmentConnector environment;
	private final UUID myId;
	private final UUID authkey;
	
	enum State {
		START, IDLE, PRE_VOTE, OPEN_VOTE, CHOSEN, SYNC, ERROR
	};

	enum MessageType {
		ARROGATE, RESIGN, ADD_ROLE, REM_ROLE, PROPOSE, PREPARE, RESPONSE, SUBMIT, VOTE, SYNC_REQ, SYNC_ACK, REVISE, LEAVE
	};
	
	public IPConProtocol(final UUID myId, final UUID authkey,
			EnvironmentConnector environment, NetworkAdaptor network)
			throws FSMException {
		super("IPConProtocol", FSM.description(), network);
		this.environment = environment;
		this.myId = myId;
		this.authkey = authkey;

		this.logger = Logger.getLogger("IPCon for " + myId);
		
		// leader side of protocol
		this.description.addState(State.START, StateType.START)
				.addState(State.IDLE)
				.addState(State.PRE_VOTE)
				.addState(State.OPEN_VOTE)
				.addState(State.CHOSEN)
				.addState(State.SYNC)
				.addState(State.ERROR, StateType.END);
		
		/*
		 * Need to do all transitions with AND ( ConversationCondition(), MessageTypeAndContentsCondition([msgType],[msgData]) )
		 * which means I need to write a MessageTypeAndContentsCondition...
		 */
		
		/*
		 * Transition: Start -> IDLE.
		 * Happens immediately after creating FSM.
		 * Sends an ARROGATE message to form new cluster/issue
		 */
		this.description.addTransition(MessageType.ARROGATE,
				TransitionCondition.ALWAYS, State.START,
				State.IDLE, new SpawnAction() {

					@Override
					public void processSpawn(ConversationSpawnEvent event,
							FSMConversation conv, Transition transition) {
						// send arrogate message
						ArrogateSpawnEvent e = (ArrogateSpawnEvent) event;
						NetworkAddress from = conv.getNetwork().getAddress();
						NetworkAddress to = conv.recipients.get(0);
						logger.debug("Arrogating: " + e.issue);
						conv.entity = e.issue;
						conv.getNetwork().sendMessage(
								new BroadcastMessage<String>(
										Performative.INFORM, MessageType.ARROGATE.name(), SimTime.get(), from, e.issue));
					}
				});
		
		/*
		 * Transition: IDLE -> PRE_VOTE.
		 * Happens after receiving a propose message.
		 * Sends a prepare message.
		 */
		this.description.addTransition(MessageType.PROPOSE, new AndCondition(
					new MessageTypeCondition(MessageType.PROPOSE.name()),
					new ConversationCondition()
					),
				State.IDLE, State.PRE_VOTE,
				new MessageAction() {
					// send the prepare message
		});
		
		/*
		 * Transition: PRE_VOTE -> PRE_VOTE
		 * Happens on recieving a response message, but not enough of them
		 * Do nothing other than update own data
		 */
		
		/*
		 * Transition: PRE_VOTE -> OPEN_VOTE
		 * Happens after receiving a quorum of responses
		 * Decide on safe values and send submit message
		 */
		
		/*
		 * Transition: OPEN_VOTE -> CHOSEN
		 * Happens after receiving a quorum of votes
		 * Update own knowledge and send chosen message (we're adding this for clarity)
		 */
		
		/*
		 * Transition: CHOSEN -> SYNC
		 * Happens after a new acceptor joins (you send the addrole message before this, they join, then you do this)
		 * Send sync_req message and make note of who is syncing (do other acceptors need to know we're syncing?)
		 */
		
		/*
		 * Transition: SYNC -> SYNC
		 * Happens after a new acceptor joins (you send the addrole message before this, they join, then you do this)
		 * Send sync_req message and make note of who is syncing (do other acceptors need to know we're syncing?)
		 */
		
		/*
		 * Transition: SYNC -> SYNC
		 * Happens after receiving a sync_ack message (of yes or no) but safety is still ok
		 * Update knowledge of who is syncing (do other acceptors need to know we're syncing?)
		 */
		
		/*
		 * Transition: SYNC -> CHOSEN
		 * Happens after receiving last sync_ack message (of yes or no) but safety is still ok 
		 * Update knowledge (if other acceptors needed to know we're syncing then tell them we're not anymore)
		 */
		
		/*
		 * Transition: SYNC -> IDLE
		 * Happens after receiving sync_ack message which violates safety - dont think this can be a yes, but doesnt necessarily have to be the last expected sync message 
		 * Send revise message
		 */
		
		/*
		 * Transition: CHOSEN -> IDLE
		 * Could be lots of reasons... better make it an event ? 
		 * Send revise message
		 */
		
		/*
		 * Transition: CHOSEN -> PRE_VOTE
		 * Happens when you recieve a propose message (in theory)
		 * Send propose message
		 */
		
		
		/**
		 * Event to spawn the conversation on an issue.
		 * FIXME TODO Need to work out how to have it spawn a broadcast
		 * @author dws04
		 *
		 */
		class ArrogateSpawnEvent extends ConversationSpawnEvent {

			final String issue;

			ArrogateSpawnEvent(String issue) {
				super(null); // FIXME TODO fix this for broadcasts
				this.issue = issue;
			}

		}
		
	}
		
}
