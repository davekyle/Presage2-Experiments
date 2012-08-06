/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

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
	
	enum Role {
		LEADER, ACCEPTOR, LEARNER, PROPOSER, INVALID
	};
	
	enum State {
		START, IDLE, PRE_VOTE, OPEN_VOTE, CHOSEN, SYNC, ERROR
	};

	enum MessageType {
		ARROGATE, RESIGN, ADD_ROLE, REM_ROLE, PROPOSE, PREPARE, RESPONSE, SUBMIT, VOTE, SYNC_REQ, SYNC_ACK, REVISE, LEAVE,
		/* Add this message for convenience of keeping track */ CHOSEN,
		/* Internal message to help with transitioning on broadcasts you send (since you don't receive them) */ INTERNAL_ROLE_CHANGE
	};
	
	public IPConProtocol(final UUID myId, final UUID authkey,
			EnvironmentConnector environment, NetworkAdaptor network)
			throws FSMException {
		super("IPConProtocol", FSM.description(), network);
		this.environment = environment;
		this.myId = myId;
		this.authkey = authkey;
		this.myRoles = new HashMap<FSMConversation, Role>();

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
		 * TODO Also maybe explicitly include clusters in the msgdata ? Was planning on using the conversation for this (yay overloading) but... ?
		 * TODO Need to ignore duplicate messages... (or messages from the same agent saying different things in the same phase)
		 * TODO Where do revision numbers fit in ? I was going with the conversation key being the revision number (sort of) but they dont have an ordering ?
		 * 
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
				public void processSpawn(ConversationSpawnEvent event, FSMConversation conv, Transition transition) {
					// send arrogate message
					ArrogateSpawnEvent e = (ArrogateSpawnEvent) event;
					NetworkAddress from = conv.getNetwork().getAddress();
					//NetworkAddress to = conv.recipients.get(0);
					logger.debug("Arrogating: " + e.issue);
					myRoles.put(conv, Role.LEADER);
					conv.entity = new IPConDataStore(myId, e.issue, Role.LEADER);
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
				new MessageTypeCondition(MessageType.PROPOSE.name()),
				new ConversationCondition()
			),
			State.IDLE, State.PRE_VOTE,
			new MessageAction() {

				@Override
				public void processMessage(Message<?> message, FSMConversation conv, Transition transition) {
					// send the prepare message
					if (message.getData() instanceof IPConMsgData) {
						IPConMsgData msgData = (IPConMsgData) message.getData();
						IPConDataStore dataStore = ((IPConDataStore)conv.getEntity());
						Integer balNum = ((Integer)dataStore.getData("BallotNum"));
						if (balNum==null) {
							balNum = 0;
						}
						conv.getNetwork().sendMessage(
							new BroadcastMessage<IPConMsgData>(
								Performative.REQUEST, MessageType.PREPARE.name(), SimTime.get(), conv.getNetwork().getAddress(),
								/* Send prepare message with a higher ballot number than you think exists. Value doesn't matter */
								new IPConMsgData(msgData.getIssue(), null, balNum+1)
							)
						);
						dataStore.getDataMap().put("BallotNum", balNum+1);
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
						if (entity instanceof IPConDataStore) {
							Integer recCount = (Integer)((IPConDataStore)entity).getData("ReceiveCount")+1;
							Integer quorum = (Integer)((IPConDataStore)entity).getData("Quorum");
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
					if (entity instanceof IPConDataStore) {
						IPConDataStore store = ((IPConDataStore)entity);
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
						if (entity instanceof IPConDataStore) {
							Integer recCount = (Integer)((IPConDataStore)entity).getData("ReceiveCount")+1;
							Integer quorum = (Integer)((IPConDataStore)entity).getData("Quorum");
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
				if (conv.getEntity() instanceof IPConDataStore) {
					// TODO // decide safe value, send the submit message with correct ballot number, reset the recCount in the datastore, set voteCount to 0
					
					
					IPConDataStore dataStore = (IPConDataStore) conv.getEntity();
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
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConMsgData) && (entity instanceof IPConDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConMsgData msgData = (IPConMsgData) m.getData();
							IPConDataStore dataStore = (IPConDataStore)entity;
							Integer voteCount = (Integer)(dataStore.getData("VoteCount"))+1;
							Integer quorum = (Integer)(dataStore.getData("Quorum"));
							return ( ( msgData.getBallotNum().equals(dataStore.getData("BallotNum")) ) && ( msgData.getValue().equals( ((IPConDataStore)entity).getValue() ) ) && (voteCount < quorum) );
						}
						else return false;
					}
				}
			),
			State.OPEN_VOTE, State.CHOSEN,
			new Action() {

			@Override
			public void execute(Object event, Object entity,Transition transition) {
				if (entity instanceof IPConDataStore) {
					IPConDataStore store = ((IPConDataStore)entity);
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
						if ( (event instanceof Message<?>) && ( ((Message<?>)event).getData() instanceof IPConMsgData) && (entity instanceof IPConDataStore) ) {
							Message<?> m = (Message<?>) event;
							IPConMsgData msgData = (IPConMsgData) m.getData();
							IPConDataStore dataStore = (IPConDataStore)entity;
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
					if (		(message.getData() instanceof IPConMsgData)
							&&	(conv.getEntity() instanceof IPConDataStore) ) {
						IPConMsgData msgData = (IPConMsgData)message.getData();
						IPConDataStore store = ((IPConDataStore)conv.getEntity());
						Integer voteCount = (Integer)store.getData("VoteCount")+1;
						store.getDataMap().put("VoteCount", voteCount);
						conv.getNetwork().sendMessage(
								new BroadcastMessage<IPConMsgData>(
									Performative.INFORM, MessageType.CHOSEN.name(), SimTime.get(), conv.getNetwork().getAddress(),
									/* Send chosen message with correct ballot number. Value should also be correct, obviously */
									new IPConMsgData(msgData.getIssue(), store.getValue(), (Integer) store.getData("BallotNum"))
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
						IPConDataStore dataStore = (IPConDataStore)entity;
						return (	( (dataStore.getData("SyncCount").equals(null)) || (dataStore.getData("SyncCount").equals(0)) ) && 
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
						// check syncCount is not 0, check msg is right type, then get the data and make sure it's correct/valid
						// Should be the first agent being synched, should be the right msgtype, should be adding an agent to acceptor with the right ballotnum
						IPConDataStore dataStore = (IPConDataStore)entity;
						return  ( (!dataStore.getData("SyncCount").equals(0))  && 
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
				new ConversationCondition()
			),
			State.SYNC, State.SYNC,
			new Action() {

				@Override
				public void execute(Object event, Object entity,
						Transition transition) {
					// TODO check safety is ok, update own knowledge
					
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
				new IPConOwnRoleCondition(Role.LEADER),
				new IPConSenderRoleCondition(Role.ACCEPTOR),
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
				new IPConOwnRoleCondition(Role.LEADER),
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
	
	class IPConRoleChangeMessageData {
		private final NetworkAddress agentId;
		private final String issue;
		/**
		 * true if added, false if removed
		 */
		private final Boolean addRole;
		/**
		 * The agent's old role
		 */
		private final Role oldRole;
		/**
		 * The agent's new role
		 */
		private final Role newRole;
		
		private final Integer ballotNum;
		
		/**
		 * @param agentId
		 * @param issue
		 * @param addRole
		 * @param oldRole
		 * @param newRole
		 */
		public IPConRoleChangeMessageData(NetworkAddress agentId, String issue, Integer ballotNum,
				Boolean addRole, Role oldRole, Role newRole) {
			super();
			this.agentId = agentId;
			this.issue = issue;
			this.ballotNum = ballotNum;
			this.addRole = addRole;
			this.oldRole = oldRole;
			this.newRole = newRole;
		}

		/**
		 * @return the agentId
		 */
		public NetworkAddress getAgentId() {
			return agentId;
		}

		/**
		 * @return the issue
		 */
		public String getIssue() {
			return issue;
		}

		/**
		 * @return the addRole
		 */
		public Boolean getAddRole() {
			return addRole;
		}

		/**
		 * @return the oldRole
		 */
		public Role getOldRole() {
			return oldRole;
		}

		/**
		 * @return the newRole
		 */
		public Role getNewRole() {
			return newRole;
		}

		/**
		 * @return the ballotNum
		 */
		public Integer getBallotNum() {
			return ballotNum;
		}
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
					&& (conv.getEntity() instanceof IPConDataStore) ) {
				IPConRoleChangeMessageData msgData = (IPConRoleChangeMessageData)message.getData();
				IPConDataStore store = ((IPConDataStore)conv.getEntity());
				// Check it's not null since we're reusing this block of code...
				Integer syncCount = (Integer) store.getData("SyncCount");
				if (syncCount.equals(null)) { syncCount = 0; }
				store.getDataMap().put("SyncCount", syncCount+1);
				/* Send SyncReq message with correct ballot number. Value should also be correct, obviously */
				IPConMsgData newData = new IPConMsgData(msgData.getIssue(), store.getValue(), (Integer) store.getData("BallotNum"));
				conv.getNetwork().sendMessage(
						new BroadcastMessage<IPConMsgData>(
							Performative.INFORM, MessageType.SYNC_REQ.name(), SimTime.get(), conv.getNetwork().getAddress(),
							newData
						)
					);
				conv.getNetwork().sendMessage(
						new UnicastMessage<IPConMsgData>(
							Performative.INFORM, MessageType.SYNC_REQ.name(), SimTime.get(),
							conv.getNetwork().getAddress(), msgData.getAgentId(),
							newData
						)
					);
				logger.trace("Updated syncCount to " + syncCount + " on issue " + store.getIssue() + " and issued a SYNC_REQ message to the agent (Addr:"+ msgData.getAgentId() + "/UUID:"+msgData.getAgentId().getId() + ") and broadcast group with value " + store.getValue() + " and ballot " + store.getData("BallotNum"));
			}
		}
	}
	
	private class IPConMsgData {
		private final String issue;
		private final Object value;
		private Integer ballotNum;
		/**
		 * @param issue
		 * @param value
		 */
		public IPConMsgData(String issue, Object value) {
			super();
			this.issue = issue;
			this.value = value;
			this.setBallotNum(null);
		}
		/**
		 * @param issue
		 * @param value
		 */
		public IPConMsgData(String issue, Object value, Integer ballotNum) {
			super();
			this.issue = issue;
			this.value = value;
			this.setBallotNum(ballotNum);
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
		 * @param ballotNum the ballotNum to set
		 */
		public void setBallotNum(Integer ballotNum) {
			this.ballotNum = ballotNum;
		}
		/**
		 * @return the ballotNum
		 */
		public Integer getBallotNum() {
			return ballotNum;
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
			if (entity instanceof IPConDataStore) {
				return ((IPConDataStore)entity).getRole(myId).equals(role);
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
				if (entity instanceof IPConDataStore) {
					return ((IPConDataStore)entity).getRole(m.getFrom().getId()).equals(role);
				}
				else return false;
			}
			else return false;
		}
	}
	
	
}
