/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.Action;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
import uk.ac.imperial.presage2.util.fsm.FSM;
import uk.ac.imperial.presage2.util.fsm.Transition;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.fsm.StateType;
import uk.ac.imperial.presage2.util.fsm.TransitionCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationSpawnEvent;
import uk.ac.imperial.presage2.util.protocols.FSMProtocol;
import uk.ac.imperial.presage2.util.protocols.MessageAction;
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
	private final HashMap<FSMConversation,Role> myRoles;
	/**
	 * The most recent ballots voted for by the agent
	 */
	private final HashMap<String,IPConVoteData> hnb;
	
	public static enum Role {
		LEADER, ACCEPTOR, LEARNER, PROPOSER, INVALID
	};
	
	public static enum State {
		START, IDLE, PRE_VOTE, OPEN_VOTE, CHOSEN, SYNC, ERROR
	};

	public static enum MessageType {
		ARROGATE, RESIGN, ADD_ROLE, REM_ROLE, PROPOSE, PREPARE, RESPONSE, SUBMIT, VOTE, SYNC_REQ, SYNC_ACK, REVISE, LEAVE,
		/* Add this message for convenience of keeping track */ CHOSEN,
		/* Internal message to help with transitioning on broadcasts you send (since you don't receive them) */ INTERNAL_ROLE_CHANGE,
		/* Internal message to help with detecting a revise transition */ INTERNAL_NEED_REVISION,
		/* Internal message to end the conversation (go to error) */ INTERNAL_END
	};
	
	public IPConProtocol(final UUID myId, final UUID authkey,
			EnvironmentConnector environment, NetworkAdaptor network)
			throws FSMException {
		super("IPConProtocol", FSM.description(), network);
		this.environment = environment;
		this.myId = myId;
		this.authkey = authkey;
		this.myRoles = new HashMap<FSMConversation, Role>();
		this.hnb = new HashMap<String,IPConVoteData>();

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
		 * TODO Slightly concerned about overlapping ballots/issues/conversations...
		 * TODO Include checks that the ballotnum of the message isnt lower than the one you know about..
		 * TODO work out whether/when ballotnum should be updated, especially in revision sending bits
		 * TODO Also maybe explicitly include clusters in the msgdata ? Was planning on using the conversation for this (yay overloading) but... ?
		 * TODO Need to ignore duplicate messages... (or messages from the same agent saying different things in the same phase)
		 * TODO Where do revision numbers fit in ? I was going with the conversation key being the revision number (sort of) but they dont have an ordering ? And revisions don't start a new conversation... :s
		 * TODO perhaps remove all the duplication of code (ie rechecking things in the action after the condition has checked already...
		 * 
		 */
		
		/*
		 * Transition: Start -> IDLE.
		 * Happens immediately after creating FSM. FIXME work out if this is how to init ?
		 * Sends an ARROGATE message to form new cluster/issue
		 */
		this.description.addTransition(MessageType.ARROGATE,
			TransitionCondition.ALWAYS,
			State.START, State.IDLE,
			new SpawnAction() {

				@Override
				public void processSpawn(ConversationSpawnEvent event, FSMConversation conv, Transition transition) {
					// send arrogate message
					ArrogateSpawnEvent e = (ArrogateSpawnEvent) event;
					NetworkAddress from = conv.getNetwork().getAddress();
					//NetworkAddress to = conv.recipients.get(0);
					logger.debug("Arrogating: " + e.issue);
					myRoles.put(conv, Role.LEADER);
					Integer hnbBallot = hnb.get(e.issue).getBallotNum();
					Integer hnbRevision = hnb.get(e.issue).getRevision();
					// If you don't know about any previous ballots or revisions, start with 0
					// otherwise, try a higher ballot number in the same revision
					// make sure the actual hnb doesn't change
					if (hnbBallot.equals(null)) { hnbBallot = 0; } else hnbBallot++;
					if (hnbRevision.equals(null)) { hnbRevision = 0; }
					if (hnbBallot.equals(hnb.get(e.issue).getBallotNum())) {
						logger.error("INADVERTANTLY CHANGED HNB !");
						System.exit(1);
					}
					conv.entity = new IPConConvDataStore(myId, e.issue, hnbBallot, hnbRevision, Role.LEADER);
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
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.PROPOSER),
				new MessageTypeCondition(MessageType.PROPOSE.name()),
				new ConversationCondition()
			),
			State.IDLE, State.PRE_VOTE,
			new MessageAction() {

				@Override
				public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
					// send the prepare message
					if (message.getData() instanceof IPConVoteData) {
						IPConVoteData msgData = (IPConVoteData) message.getData();
						IPConConvDataStore dataStore = ((IPConConvDataStore)conv.getEntity());
						Integer hnbBal = getHNB(msgData.getIssue()).getBallotNum();
						Integer hnbRev = getHNB(msgData.getIssue()).getRevision();
						if ( (dataStore.getRevision() >= hnbRev) && (dataStore.getBallotNum() >= hnbBal) ) {
							conv.getNetwork().sendMessage(
								new BroadcastMessage<IPConVoteData>(
									Performative.REQUEST, MessageType.PREPARE.name(), SimTime.get(), conv.getNetwork().getAddress(),
									/* Send prepare message at the correct ballot and revision number. Value doesn't matter */
									new IPConVoteData(msgData.getIssue(), null, hnbBal, hnbRev)
								)
							);
						}
					}
					else {
						logger.warn("Tried to process a bad message on a PROPOSE transition: " + message);
					}
					
				}
					
		});
		
		/*
		 * Transition: PRE_VOTE -> PRE_VOTE
		 * Happens on receiving a response message, but not enough of them
		 * Do nothing other than update own data
		 */
		this.description.addTransition(MessageType.RESPONSE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.RESPONSE.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						if (entity instanceof IPConConvDataStore) {
							Integer recCount = (Integer)((IPConConvDataStore)entity).getData("ReceiveCount")+1;
							Integer quorum = (Integer)((IPConConvDataStore)entity).getData("Quorum");
							return (recCount < quorum);
						}
						else return false;
					}
					
				}
			),
			State.PRE_VOTE, State.PRE_VOTE,
			new Action() {

				@Override
				public void execute(Object event, Object entity,Transition transition) {
					if (entity instanceof IPConConvDataStore) {
						IPConConvDataStore store = ((IPConConvDataStore)entity);
						Integer recCount = (Integer)store.getData("ReceiveCount")+1;
						store.getDataMap().put("ReceiveCount", recCount);
						logger.trace("Updated recCount to " + recCount + " on issue " + store.getIssue());
					}
				}
		
		});
		
		/*
		 * Transition: PRE_VOTE -> OPEN_VOTE
		 * Happens after receiving a quorum of responses
		 * Decide on safe values and send submit message
		 */
		this.description.addTransition(MessageType.RESPONSE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.RESPONSE.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						if (entity instanceof IPConConvDataStore) {
							Integer recCount = (Integer)((IPConConvDataStore)entity).getData("ReceiveCount")+1;
							Integer quorum = (Integer)((IPConConvDataStore)entity).getData("Quorum");
							return (recCount >= quorum);
						}
						else return false;
					}
					
				}
			),
			State.PRE_VOTE, State.OPEN_VOTE,
			new MessageAction() {

			@Override
			public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
				if (conv.getEntity() instanceof IPConConvDataStore) {
					// TODO FIXME // decide safe value, send the submit message with correct ballot number, reset the recCount in the datastore, set voteCount to 0
					
					
					IPConConvDataStore dataStore = (IPConConvDataStore) conv.getEntity();
					dataStore.getDataMap().put("ReceiveCount", 0);
					dataStore.getDataMap().put("VoteCount", 0);
					logger.trace("Sent a submit message on issue " + dataStore.getIssue() + " with value " + dataStore.getValue());
				}
				else {
					logger.warn("Tried to process a bad RESPONSE message: " + message);
				}
			}
		
		});
		
		/*
		 * Transition: OPEN_VOTE -> OPEN_VOTE
		 * Happens after receiving not a quorum of votes (excluding votes with incorrect values or ballot)
		 * Update own knowledge 
		 */
		this.description.addTransition(MessageType.VOTE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.VOTE.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConVoteData) && (entity instanceof IPConConvDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConVoteData msgData = (IPConVoteData) m.getData();
							IPConConvDataStore dataStore = (IPConConvDataStore)entity;
							Integer voteCount = (Integer)(dataStore.getData("VoteCount"))+1;
							Integer quorum = (Integer)(dataStore.getData("Quorum"));
							return ( ( msgData.getBallotNum().equals(dataStore.getData("BallotNum")) ) && ( msgData.getValue().equals( ((IPConConvDataStore)entity).getValue() ) ) && (voteCount < quorum) );
						}
						else return false;
					}
				}
			),
			State.OPEN_VOTE, State.CHOSEN,
			new Action() {

			@Override
			public void execute(Object event, Object entity,Transition transition) {
				if (entity instanceof IPConConvDataStore) {
					IPConConvDataStore store = ((IPConConvDataStore)entity);
					Integer voteCount = (Integer)store.getData("VoteCount")+1;
					store.getDataMap().put("VoteCount", voteCount);
					logger.trace("Updated voteCount to " + voteCount + " on issue " + store.getIssue());
				}
			}
		
		});
		
		/*
		 * Transition: OPEN_VOTE -> CHOSEN
		 * Happens after receiving a quorum of votes (of correct values in right ballot)
		 * Update own knowledge and send chosen message (we're adding this for clarity)
		 */
		this.description.addTransition(MessageType.VOTE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.VOTE.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConVoteData) && (entity instanceof IPConConvDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConVoteData msgData = (IPConVoteData) m.getData();
							IPConConvDataStore dataStore = (IPConConvDataStore)entity;
							Integer voteCount = (Integer)(dataStore.getData("VoteCount"))+1;
							Integer quorum = (Integer)(dataStore.getData("Quorum"));
							return ( ( msgData.getBallotNum().equals(dataStore.getData("BallotNum")) ) && ( msgData.getValue().equals( dataStore.getValue() ) ) && (voteCount >= quorum) );
						}
						else return false;
					}
				}
			),
			State.OPEN_VOTE, State.CHOSEN,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
					// update knowledge and send chosen message
					if (		(message.getData() instanceof IPConVoteData)
							&&	(conv.getEntity() instanceof IPConConvDataStore) ) {
						IPConVoteData msgData = (IPConVoteData)message.getData();
						IPConConvDataStore store = ((IPConConvDataStore)conv.getEntity());
						Integer voteCount = (Integer)store.getData("VoteCount")+1;
						store.getDataMap().put("VoteCount", voteCount);
						conv.getNetwork().sendMessage(
								new BroadcastMessage<IPConVoteData>(
									Performative.INFORM, MessageType.CHOSEN.name(), SimTime.get(), conv.getNetwork().getAddress(),
									/* Send chosen message with correct ballot number. Value should also be correct, obviously */
									new IPConVoteData(msgData.getIssue(), store.getValue(), (Integer) store.getData("BallotNum"), null)
								)
							);
						logger.trace("Updated voteCount to " + voteCount + " on issue " + store.getIssue() + " and issued a CHOSEN message with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
					}
					
				}
		});
		
		/*
		 * Transition: CHOSEN -> SYNC
		 * Happens after a new acceptor joins (you send the addrole message then you do this)
		 * Send sync_req message and make note of who is syncing (do other acceptors need to know we're syncing?)
		 */
		this.description.addTransition(MessageType.INTERNAL_ROLE_CHANGE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				//new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.INTERNAL_ROLE_CHANGE.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						// check syncCount is 0, check msg is right type, then get the data and make sure it's correct/valid
						// Should be the first agent being synched, should be the right msgtype, should be adding an agent to acceptor with the right ballotnum
						IPConConvDataStore dataStore = (IPConConvDataStore)entity;
						return (	( (dataStore.getData("SyncSet").equals(null)) || (dataStore.getData("SyncSet").equals(Collections.emptySet())) ) && 
								( (event instanceof InternalRoleChangeMessage) && (((InternalRoleChangeMessage)event).getData() instanceof IPConRoleChangeMessageData ) &&
								( (((InternalRoleChangeMessage)event).getData().getAddRole()))  ) &&
								( (((InternalRoleChangeMessage)event).getData().getNewRole().equals(Role.ACCEPTOR))) && 
								( (((InternalRoleChangeMessage)event).getData().getBallotNum().equals(dataStore.getData("BallotNum"))) )
								);
					}
					
				}
			),
			State.CHOSEN, State.SYNC,
			new SyncReqMessageAction()
		);
		
		/*
		 * Transition: SYNC -> SYNC
		 * Happens after a new acceptor joins (you send the addrole message before this, they join, then you do this)
		 * Send sync_req message and make note of who is syncing (do other acceptors need to know we're syncing?)
		 */
		this.description.addTransition(MessageType.INTERNAL_ROLE_CHANGE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.INTERNAL_ROLE_CHANGE.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						// check syncCount is not 0 (or null), check msg is right type, then get the data and make sure it's correct/valid
						// Should be the first agent being synched, should be the right msgtype, should be adding an agent to acceptor with the right ballotnum
						IPConConvDataStore dataStore = (IPConConvDataStore)entity;
						return  ( ( (!dataStore.getData("SyncSet").equals(null)) && (!dataStore.getData("SyncSet").equals(Collections.emptySet())) ) && 
								( (event instanceof InternalRoleChangeMessage) && (((InternalRoleChangeMessage)event).getData() instanceof IPConRoleChangeMessageData ) &&
								( (((InternalRoleChangeMessage)event).getData().getAddRole()))  ) &&
								( (((InternalRoleChangeMessage)event).getData().getNewRole().equals(Role.ACCEPTOR))) && 
								( (((InternalRoleChangeMessage)event).getData().getBallotNum().equals(dataStore.getData("BallotNum"))) )
								);
					}
					
				}
			),
			State.SYNC, State.SYNC,
			new SyncReqMessageAction()
		);
		
		/*
		 * Transition: SYNC -> SYNC
		 * Happens after receiving a sync_ack message (of yes or no) but safety is still ok
		 * Update knowledge of who is syncing (do other acceptors need to know we're syncing?)
		 */
		this.description.addTransition(MessageType.SYNC_ACK, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.SYNC_ACK.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						// check the message is correct, the agent is being synched, the agent isn't the last agent and safety is ok
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConVoteData) && (entity instanceof IPConConvDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConVoteData msgData = (IPConVoteData) m.getData();
							IPConConvDataStore dataStore = (IPConConvDataStore)entity;
							if ( ( ((HashSet<NetworkAddress>)(dataStore.getData("SyncSet"))).size()!=1 ) && ((HashSet<NetworkAddress>)(dataStore.getData("SyncSet"))).contains(m.getFrom()) ) {
								// TODO FIXME Check safety here
								
							}
							else return false;
						}
						else return false;
					}
					
				}
			),
			State.SYNC, State.SYNC,
			new MessageAction() {

				@Override
				public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
					// TODO check safety is ok, update own knowledge
					
					if (	(message instanceof Message<?>)
							&& (message.getData() instanceof IPConVoteData)
							&& (conv.getEntity() instanceof IPConConvDataStore) ) {
						IPConVoteData msgData = (IPConVoteData)message.getData();
						IPConConvDataStore store = ((IPConConvDataStore)conv.getEntity());
						HashSet<NetworkAddress> syncSet = (HashSet<NetworkAddress>) store.getData("SyncSet");
						if (!syncSet.equals(null) && (syncSet.size()!=1) && syncSet.contains(message.getFrom()) ) {
							syncSet.remove(message.getFrom());
							store.getDataMap().put("SyncSet", syncSet); //FIXME will this always be overwriting or do I actually need to do this?
							// TODO recheck safety is ok ?
							// TODO add vote fact for sender
							logger.trace("Removed " + message.getFrom() + " from SyncSet and updated to " + store.getDataMap().get("SyncSet") + " after confirming safety is still ok on issue " + store.getIssue() + " and with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
						}
						else {
							logger.warn("SyncSet didn't contain the sender");
						}
					}
					
				}
		
		});
		
		/*
		 * Transition: SYNC -> CHOSEN
		 * Happens after receiving last sync_ack message (of yes or no) but safety is still ok 
		 * Update knowledge (if other acceptors needed to know we're syncing then tell them we're not anymore)
		 */
		this.description.addTransition(MessageType.SYNC_ACK, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.SYNC_ACK.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						// check the message is correct, the agent is being synched, the agent is the last agent and safety is ok
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConVoteData) && (entity instanceof IPConConvDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConVoteData msgData = (IPConVoteData) m.getData();
							IPConConvDataStore dataStore = (IPConConvDataStore)entity;
							if ( ( ((HashSet<NetworkAddress>)(dataStore.getData("SyncSet"))).size()==1 ) && ((HashSet<NetworkAddress>)(dataStore.getData("SyncSet"))).contains(m.getFrom()) ) {
								// TODO FIXME Check safety ok here
								
							}
							else return false;
						}
						else return false;
					}
					
				}
			),
			State.SYNC, State.CHOSEN,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
					// TODO check safety is ok, update knowledge (, send chosen message?)
					if (	(message instanceof Message<?>)
							&& (message.getData() instanceof IPConVoteData)
							&& (conv.getEntity() instanceof IPConConvDataStore) ) {
						IPConVoteData msgData = (IPConVoteData)message.getData();
						IPConConvDataStore store = ((IPConConvDataStore)conv.getEntity());
						HashSet<NetworkAddress> syncSet = (HashSet<NetworkAddress>) store.getData("SyncSet");
						if (!syncSet.equals(null) && (syncSet.size()==1) && syncSet.contains(message.getFrom()) ) {
							syncSet.remove(message.getFrom());
							store.getDataMap().put("SyncSet", syncSet); //FIXME will this always be overwriting or do I actually need to do this?
							// TODO recheck safety is ok ?
							// TODO add vote fact for sender
							logger.trace("Removed last sync from " + message.getFrom() + " from SyncSet and updated to " + store.getDataMap().get("SyncSet") + " after confirming safety is still ok on issue " + store.getIssue() + " and with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
						}
						else {
							logger.warn("SyncSet didn't contain the sender");
						}
					}
				}
		});
		
		/*
		 * Transition: SYNC -> IDLE
		 * Happens after receiving sync_ack message which violates safety - dont think this can be a yes, but doesnt necessarily have to be the last expected sync message 
		 * Send revise message
		 */
		this.description.addTransition(MessageType.SYNC_ACK, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
				new MessageTypeCondition(MessageType.SYNC_ACK.name()),
				new ConversationCondition(),
				new TransitionCondition() {

					@Override
					public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
						// check the message is correct, the agent is being synched, and safety is not ok
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConVoteData) && (entity instanceof IPConConvDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConVoteData msgData = (IPConVoteData) m.getData();
							IPConConvDataStore dataStore = (IPConConvDataStore)entity;
							if (((HashSet<NetworkAddress>)(dataStore.getData("SyncSet"))).contains(m.getFrom())) {
								// TODO FIXME Check safety violated here
								
							}
							else return false;
						}
						else return false;
					}
					
				}
			),
			State.SYNC, State.IDLE,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO check safety is broken, send revise message, wipe all counters/sets
					if (	(message instanceof Message<?>)
							&& (message.getData() instanceof IPConVoteData)
							&& (conv.getEntity() instanceof IPConConvDataStore) ) {
						IPConVoteData msgData = (IPConVoteData)message.getData();
						IPConConvDataStore store = ((IPConConvDataStore)conv.getEntity());
						HashSet<NetworkAddress> syncSet = (HashSet<NetworkAddress>) store.getData("SyncSet");
						if (syncSet.contains(message.getFrom()) ) {
							syncSet.clear();
							store.getDataMap().put("SyncSet", syncSet); //FIXME will this always be overwriting or do I actually need to do this?
							store.getDataMap().put("VoteCount", 0);
							store.getDataMap().put("ReceiveCount", 0);
							// leave BallotNum as it is for ordering
							// TODO recheck safety is not ok ?
							conv.getNetwork().sendMessage(
									new BroadcastMessage<IPConVoteData>(
										Performative.INFORM, MessageType.REVISE.name(), SimTime.get(), conv.getNetwork().getAddress(),
										/* Send revise message with correct ballot number. Value should also be correct, obviously */
										new IPConVoteData(msgData.getIssue(), null, (Integer) store.getData("BallotNum"), null)
									)
								);
							
							logger.trace("Revised issue " + store.getIssue() + " - previously with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
						}
						else {
							logger.warn("SyncSet didn't contain the sender");
						}
					}
				}
		});
		
		/*
		 * Transition: CHOSEN -> IDLE
		 * Could be lots of reasons (including agent loss)... better make it an event ? 
		 * Send revise message
		 */
		this.description.addTransition(MessageType.REVISE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new MessageTypeCondition(MessageType.INTERNAL_NEED_REVISION.name()),
				new ConversationCondition()
			),
			State.CHOSEN, State.IDLE,
			new MessageAction() {
		
				@Override
				public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
					// send revise message if conversation in event is same
					if (	(message instanceof InternalNeedRevisionMessage)
							&& (message.getData() instanceof IPConVoteData)
							&& (conv.getEntity() instanceof IPConConvDataStore) ) {
						IPConVoteData msgData = (IPConVoteData)message.getData();
						IPConConvDataStore store = ((IPConConvDataStore)conv.getEntity());
						// reset everything
						store.getDataMap().put("VoteCount", 0);
						store.getDataMap().put("ReceiveCount", 0);
						// leave BallotNum as it is for ordering
						conv.getNetwork().sendMessage(
								new BroadcastMessage<IPConVoteData>(
									Performative.INFORM, MessageType.REVISE.name(), SimTime.get(), conv.getNetwork().getAddress(),
									/* Send revise message with correct ballot number. Value should also be correct, obviously */
									new IPConVoteData(msgData.getIssue(), null, (Integer) store.getData("BallotNum"), null)
								)
							);
						logger.trace("Revised issue " + store.getIssue() + " - previously with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
					}
					else {
						logger.warn("Couldn't find a valid internal revision change message to process");
					}
					
				}
		});
		
		/*
		 * Transition: ANY -> ERROR
		 * Happens when you receive an internal_end message
		 * Go to ERROR state, do nothing else.
		 */
		for (State state : State.values()) {
			this.description.addTransition(MessageType.INTERNAL_END, new AndCondition(
					new MessageTypeCondition(MessageType.INTERNAL_END.name()),
					new ConversationCondition()
				),
				state, State.ERROR,
				new MessageAction() {

					@Override
					public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
						logger.trace("Ending conversation " + conv + " on issue " + ((IPConVoteData)message.getData()).issue);
					}

			});
		}
		
		/*this.description.addTransition(MessageType.PROPOSE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.PROPOSER),
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
		});*/
		
		/*
		 * Transition: CHOSEN -> PRE_VOTE
		 * Happens when you receive a propose message (in theory)
		 * Send prepare message
		 */
		/*this.description.addTransition(MessageType.PROPOSE, new AndCondition(
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.PROPOSER),
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
		});*/

	
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
	 * Call this function when a revision is needed
	 */
	public void internalNeedRevision(FSMConversation conv, String issue, Integer ballotNum, String reason) {
		logger.trace("Signalling to self that a revision is needed in conversation " + conv + " in issue " + issue);
		this.handle(new InternalNeedRevisionMessage(conv, issue, ballotNum));
	}
	
	/**
	 * Message to indicate need for revision
	 * @author dws04
	 *
	 */
	class InternalNeedRevisionMessage extends Message<IPConVoteData> {

		/**
		 * 
		 * @param conv
		 * @param issue
		 * @param ballotNum 
		 */
		public InternalNeedRevisionMessage(FSMConversation conv, String issue, Integer ballotNum) {
			super(Performative.INFORM, MessageType.INTERNAL_NEED_REVISION.name(), SimTime.get(), conv.getNetwork().getAddress(),new IPConVoteData(issue, null, ballotNum, null));
		}

		

	}
	
	/**
	 * Call this function when changing the role of an agent
	 * @param conv
	 * @param agentId
	 * @param issue
	 * @param ballotNum
	 * @param addOrRem
	 * @param oldRole
	 * @param newRole
	 */
	public void internalRoleChange(FSMConversation conv, NetworkAddress agentId, String issue, Integer ballotNum, Boolean addOrRem, Role oldRole, Role newRole) {
		this.handle(new InternalRoleChangeMessage(conv, agentId, issue, ballotNum, addOrRem, oldRole, newRole));
	}
	
	protected IPConVoteData getHNB(String issue) {
		return this.hnb.get(issue);
	}
	
	protected void setHNB(String issue, Object value, Integer ballotNum, Integer revision) {
		this.hnb.get(issue).setBallotNum(ballotNum);
		this.hnb.get(issue).setRevision(revision);
	}
	
	/**
	 * Internal "fake" message to indicate a rolechange and trigger a transition
	 * @author dws04
	 *
	 */
	class InternalRoleChangeMessage extends Message<IPConRoleChangeMessageData> {
		
		/**
		 * @param conv
		 * @param agentId
		 * @param issue
		 * @param ballotNum
		 * @param addOrRem
		 * @param oldRole
		 * @param newRole
		 */
		public InternalRoleChangeMessage(FSMConversation conv, NetworkAddress agentId, String issue, Integer ballotNum, Boolean addOrRem, Role oldRole, Role newRole) {
			super(Performative.INFORM, MessageType.INTERNAL_ROLE_CHANGE.name(), SimTime.get(), conv.getNetwork().getAddress(),new IPConRoleChangeMessageData(agentId, issue, ballotNum, addOrRem, oldRole, newRole));
		}
	}
	
	/**
	 * MessageAction to process an InternalRoleChangeMessage and send a sync_req message
	 */
	class SyncReqMessageAction extends MessageAction {
		
		@Override
		public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
			// update syncCount, send sync_req message to agent, and a broadcast to let others know
			if (	(message instanceof InternalRoleChangeMessage)
					&& (message.getData() instanceof IPConRoleChangeMessageData)
					&& (conv.getEntity() instanceof IPConConvDataStore) ) {
				IPConRoleChangeMessageData msgData = (IPConRoleChangeMessageData)message.getData();
				IPConConvDataStore store = ((IPConConvDataStore)conv.getEntity());
				// Check it's not null since we're reusing this block of code...
				HashSet<NetworkAddress> syncSet = (HashSet<NetworkAddress>) store.getData("SyncSet");
				if (syncSet.equals(null)) { syncSet = new HashSet<NetworkAddress>(); }
				syncSet.add(msgData.getAgentId());
				store.getDataMap().put("SyncSet", syncSet); //FIXME will this always be overwriting or do I actually need to do this?
				/* Send SyncReq message with correct ballot number. Value should also be correct, obviously */
				final IPConVoteData newData = new IPConVoteData(msgData.getIssue(), store.getValue(), (Integer) store.getData("BallotNum"), null);
				conv.getNetwork().sendMessage(
						new BroadcastMessage<IPConVoteData>(
							Performative.INFORM, MessageType.SYNC_REQ.name(), SimTime.get(), conv.getNetwork().getAddress(),
							newData
						)
					);
				conv.getNetwork().sendMessage(
						new UnicastMessage<IPConVoteData>(
							Performative.INFORM, MessageType.SYNC_REQ.name(), SimTime.get(),
							conv.getNetwork().getAddress(), msgData.getAgentId(),
							newData
						)
					);
				logger.trace("Updated syncSet to " + syncSet + " on issue " + store.getIssue() + " and issued a SYNC_REQ message to the agent (Addr:"+ msgData.getAgentId() + "/UUID:"+msgData.getAgentId().getId() + ") and broadcast group with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
			}
		}
	}
	
	/**
	 * Class to hold the data to identify a vote (issue, value, ballot)
	 * @author dws04
	 *
	 */
	private class IPConVoteData {
		private String issue;
		private Object value;
		private Integer ballotNum;
		private Integer revision;
		/**
		 * @param issue
		 * @param value
		 */
		public IPConVoteData(String issue, Object value) {
			super();
			this.issue = issue;
			this.value = value;
			this.ballotNum = null;
			this.revision = null;
		}
		/**
		 * @param issue
		 * @param value
		 * @param revision TODO
		 */
		public IPConVoteData(String issue, Object value, Integer ballotNum, Integer revision) {
			super();
			this.issue = issue;
			this.value = value;
			this.ballotNum = null;
			this.revision = null;
		}
		/**
		 * @return the issue
		 */
		public String getIssue() {
			return issue;
		}
		/**
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}
		/**
		/**
		 * @return the ballotNum
		 */
		public Integer getBallotNum() {
			return ballotNum;
		}
		/**
		 * @return the revision
		 */
		public Integer getRevision() {
			return revision;
		}
		/**
		 * @param ballotNum the ballotNum to set
		 */
		public void setBallotNum(Integer ballotNum) {
			this.ballotNum = ballotNum;
		}
		/**
		 * @param revision the revision to set
		 */
		public void setRevision(Integer revision) {
			this.revision = revision;
		}
	}
	
	/**
	 * TransitionCondition to check your role
	 */
	private class IPConOwnRoleCondition implements TransitionCondition {
		
		private final Role role;
		
		public IPConOwnRoleCondition(Role role) {
			this.role = role;
		}

		@Override
		public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
			if (entity instanceof IPConConvDataStore) {
				// FIXME this should use myRoles or change entity.roleMap to have a list of roles each
				return ((IPConConvDataStore)entity).getRole(myId).equals(role);
			}
			else return false;
		}
	}
	
	/**
	 * TransitionCondition to check the role of the sender
	 */
	private class IPConSenderRoleCondition implements TransitionCondition {
		
		private final Role role;
		
		public IPConSenderRoleCondition(Role role) {
			this.role = role;
		}

		@Override
		public boolean allow(Object event, Object entity, uk.ac.imperial.presage2.util.fsm.State state) {
			if (event instanceof Message) {
				Message<?> m = (Message<?>) event;
				if (entity instanceof IPConConvDataStore) {
					// FIXME change entity.roleMap to have a list of roles each
					return ((IPConConvDataStore)entity).getRole(m.getFrom().getId()).equals(role);
				}
				else return false;
			}
			else return false;
		}
	}
	
	
}
