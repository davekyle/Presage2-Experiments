/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.Action;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
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
		ARROGATE, RESIGN, ADD_ROLE, REM_ROLE, PROPOSE, PREPARE, RESPONSE, SUBMIT, VOTE, SYNC_REQ, SYNC_ACK, REVISE, LEAVE,
		/* Add this message for convenience of keeping track */ CHOSEN,
		/* Add this message for convenience on transitions and keeping track */ JOIN_ACK
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
		 * TODO ALSO NEED TO CHECK VALID ROLES OF SENDER
		 */
		
		/*
		 * Transition: Start -> IDLE.
		 * Happens immediately after creating FSM.
		 * Sends an ARROGATE message to form new cluster/issue
		 */
		this.description.addTransition(MessageType.ARROGATE,
			TransitionCondition.ALWAYS,
			State.START, State.IDLE,
			new SpawnAction() {

				@Override
				public void processSpawn(ConversationSpawnEvent event,
						FSMConversation conv, Transition transition) {
					// send arrogate message
					ArrogateSpawnEvent e = (ArrogateSpawnEvent) event;
					NetworkAddress from = conv.getNetwork().getAddress();
					//NetworkAddress to = conv.recipients.get(0);
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

				@Override
				public void processMessage(Message<?> message,
						FSMConversation conv, Transition transition) {
					// TODO // send the prepare message
					
				}
					
		});
		
		/*
		 * Transition: PRE_VOTE -> PRE_VOTE
		 * Happens on recieving a response message, but not enough of them
		 * Do nothing other than update own data
		 */
		this.description.addTransition(MessageType.RESPONSE, new AndCondition(
				new MessageTypeCondition(MessageType.RESPONSE.name()),
				new ConversationCondition()),
			State.PRE_VOTE, State.PRE_VOTE,
			new Action() {

				@Override
				public void execute(Object event, Object entity,
						Transition transition) {
					// TODO update own data
					
				}
		
		});
		
		/*
		 * Transition: PRE_VOTE -> OPEN_VOTE
		 * Happens after receiving a quorum of responses
		 * Decide on safe values and send submit message
		 */
		this.description.addTransition(MessageType.RESPONSE, new AndCondition(
				new MessageTypeCondition(MessageType.RESPONSE.name()),
				new ConversationCondition()),
			State.PRE_VOTE, State.OPEN_VOTE,
			new MessageAction() {

			@Override
			public void processMessage(Message<?> message,
					FSMConversation conv, Transition transition) {
				// TODO // decide safe value and send the submit message
				
			}
		
		});
		
		/*
		 * Transition: OPEN_VOTE -> OPEN_VOTE
		 * Happens after receiving not a quorum of votes
		 * Update own knowledge 
		 */
		this.description.addTransition(MessageType.VOTE, new AndCondition(
				new MessageTypeCondition(MessageType.VOTE.name()),
				new ConversationCondition()
			),
			State.OPEN_VOTE, State.CHOSEN,
			new Action() {

				@Override
				public void execute(Object event, Object entity,
						Transition transition) {
					// TODO update own knowlegde
					
				}
		
		});
		
		/*
		 * Transition: OPEN_VOTE -> CHOSEN
		 * Happens after receiving a quorum of votes
		 * Update own knowledge and send chosen message (we're adding this for clarity)
		 */
		this.description.addTransition(MessageType.VOTE, new AndCondition(
					new MessageTypeCondition(MessageType.VOTE.name()),
					new ConversationCondition()
				),
				State.OPEN_VOTE, State.CHOSEN,
				new MessageAction() {
			
					@Override
					public void processMessage(Message<?> message, FSMConversation conv,
							Transition transition) {
						// TODO update knowledge and send chosen message
						
					}
		});
		
		/*
		 * Transition: CHOSEN -> SYNC
		 * Happens after a new acceptor joins (you send the addrole message before this, they join, then you do this)
		 * Send sync_req message and make note of who is syncing (do other acceptors need to know we're syncing?)
		 */
		this.description.addTransition(MessageType.JOIN_ACK, new AndCondition(
				new MessageTypeCondition(MessageType.JOIN_ACK.name()),
				new ConversationCondition()
			),
			State.CHOSEN, State.SYNC,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO send sync_req message
					
				}
		});
		
		/*
		 * Transition: SYNC -> SYNC
		 * Happens after a new acceptor joins (you send the addrole message before this, they join, then you do this)
		 * Send sync_req message and make note of who is syncing (do other acceptors need to know we're syncing?)
		 */
		this.description.addTransition(MessageType.JOIN_ACK, new AndCondition(
				new MessageTypeCondition(MessageType.JOIN_ACK.name()),
				new ConversationCondition()
			),
			State.SYNC, State.SYNC,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO send sync_req message
					
				}
		});
		
		/*
		 * Transition: SYNC -> SYNC
		 * Happens after receiving a sync_ack message (of yes or no) but safety is still ok
		 * Update knowledge of who is syncing (do other acceptors need to know we're syncing?)
		 */
		this.description.addTransition(MessageType.SYNC_ACK, new AndCondition(
				new MessageTypeCondition(MessageType.SYNC_ACK.name()),
				new ConversationCondition()
			),
			State.SYNC, State.SYNC,
			new Action() {

				@Override
				public void execute(Object event, Object entity,
						Transition transition) {
					// TODO check safety is ok, update own knowlegde
					
				}
		
		});
		
		/*
		 * Transition: SYNC -> CHOSEN
		 * Happens after receiving last sync_ack message (of yes or no) but safety is still ok 
		 * Update knowledge (if other acceptors needed to know we're syncing then tell them we're not anymore)
		 */
		this.description.addTransition(MessageType.SYNC_ACK, new AndCondition(
				new MessageTypeCondition(MessageType.SYNC_ACK.name()),
				new ConversationCondition()
			),
			State.SYNC, State.CHOSEN,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO check safety is ok, update knowledge, send sync_req message
					
				}
		});
		
		/*
		 * Transition: SYNC -> IDLE
		 * Happens after receiving sync_ack message which violates safety - dont think this can be a yes, but doesnt necessarily have to be the last expected sync message 
		 * Send revise message
		 */
		this.description.addTransition(MessageType.SYNC_ACK, new AndCondition(
				new MessageTypeCondition(MessageType.SYNC_ACK.name()),
				new ConversationCondition()
			),
			State.SYNC, State.IDLE,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO check safety is broken, send revise message
					
				}
		});
		
		/*
		 * Transition: CHOSEN -> IDLE
		 * Could be lots of reasons (including agent loss)... better make it an event ? 
		 * Send revise message
		 */
		this.description.addTransition(MessageType.REVISE, new AndCondition(
				new EventTypeCondition(NeedToReviseEvent.class),
				new ConversationCondition()
			),
			State.CHOSEN, State.IDLE,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO send revise message if conversation in event is same
					
				}
		});
		
		/*
		 * Transition: CHOSEN -> PRE_VOTE
		 * Happens when you recieve a propose message (in theory)
		 * Send prepare message
		 */
		this.description.addTransition(MessageType.PROPOSE, new AndCondition(
				new MessageTypeCondition(MessageType.PROPOSE.name()),
				new ConversationCondition()
			),
			State.CHOSEN, State.PRE_VOTE,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO send prepare message
					
				}
		});
		
		
		
		
	}
	
	/**
	 * Event to spawn the conversation on an issue.
	 * @author dws04
	 *
	 */
	class ArrogateSpawnEvent extends ConversationSpawnEvent {

		final String issue;

		ArrogateSpawnEvent(String issue) {
			super(Collections.<NetworkAddress>emptySet());
			this.issue = issue;
		}

	}
	
	/**
	 * Event to indicate need of a revision.
	 * @author dws04
	 *
	 */
	class NeedToReviseEvent extends ConversationSpawnEvent {

		final String issue;
		final FSMConversation conv;

		NeedToReviseEvent(FSMConversation conv, String issue) {
			super(Collections.<NetworkAddress>emptySet());
			this.issue = issue;
			this.conv = conv;
		}

	}
}
