/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.Role;

/**
 * Convenience object for storing data in an IPCon FSMConversation's entity object.
 * @author dws04
 *
 */
public class IPConConvDataStore {
	private final UUID myId;
	private final HashMap<UUID, ArrayList<Role>> roleMap;
	private final String issue;
	private Object value = null;
	/**
	 * Revision of the issue
	 */
	private final Integer revision;
	/**
	 * Ballot that the conversation is for
	 */
	private final Integer ballotNum;
	/**
	 * Examples of things to store (and remember to update !)
	 *  - ReceiveCount = number of receive messages you've heard this round
	 *  - Quorum = quorum on this issue
	 *  - VoteCount = number of votes this round
	 *  - SyncSet = set of agents being synched
	 */
	private final HashMap<String, Object> dataMap;
	
	/**
	 * @param myId
	 * @param roleMap
	 * @param issue
	 */
	public IPConConvDataStore(UUID myId, String issue, Integer ballotNum, Integer revision, HashMap<UUID, ArrayList<Role>> roleMap) {
		super();
		this.myId = myId;
		this.roleMap = roleMap;
		this.issue = issue;
		this.ballotNum = ballotNum;
		this.revision = revision;
		this.dataMap = new HashMap<String,Object>();
	}
	
	/**
	 * @param myId
	 * @param myStartingRole
	 * @param issue
	 */
	public IPConConvDataStore(UUID myId, String issue, Integer ballotNum, Integer revision, Role myStartingRole) {
		super();
		this.myId = myId;
		this.roleMap = new HashMap<UUID, ArrayList<Role>>();
		this.addRole(myId, myStartingRole);
		this.issue = issue;
		this.ballotNum = ballotNum;
		this.revision = revision;
		this.dataMap = new HashMap<String,Object>();
	}

	/**
	 * @return the myId
	 */
	public UUID getMyId() {
		return myId;
	}

	/**
	 * @return the roleMap
	 */
	public HashMap<UUID, ArrayList<Role>> getRoleMap() {
		return roleMap;
	}
	
	/**
	 * 
	 * @param agentId
	 * @return the role of the given agent in this conversation
	 */
	public ArrayList<Role> getRoles(UUID agentId) {
		return roleMap.get(agentId);
	}
	
	public void addRole(UUID agentId, Role role) {
		if (!this.roleMap.get(agentId).contains(role)) {
			this.roleMap.get(agentId).add(role);
		}
	}
	
	/**
	 *
	 * @param agentId
	 * @param role
	 * @return true if the agent has the role, false otherwise
	 */
	public boolean agentHasRole(UUID agentId, Role role) {
		return roleMap.get(agentId).contains(role);
	}

	/**
	 * @return the issue
	 */
	public String getIssue() {
		return issue;
	}

	/**
	 * @return the dataMap
	 */
	public HashMap<String, Object> getDataMap() {
		return dataMap;
	}
	
	/**
	 * @return the value of the given object in memory, or null if it doesn't exist
	 */
	public Object getData(String key) {
		return dataMap.get(key);
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the revision
	 */
	public Integer getRevision() {
		return revision;
	}

	/**
	 * @return the ballotNum
	 */
	public Integer getBallotNum() {
		return ballotNum;
	}
	

}
