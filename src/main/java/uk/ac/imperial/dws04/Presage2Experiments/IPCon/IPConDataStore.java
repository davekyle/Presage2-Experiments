/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.HashMap;
import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.Role;

/**
 * Convenience object for storing data in an IPCon FSMConversation's entity object.
 * @author dws04
 *
 */
public class IPConDataStore {
	private final UUID myId;
	private final HashMap<UUID, IPConProtocol.Role> roleMap;
	private final String issue;
	/**
	 * Examples of things to store (and remember to update !)
	 *  - BallotNum = most recent ballot you voted in / know about
	 *  - ReceiveCount = number of receive messages you've heard this round
	 *  - Quorum = quorum on this issue
	 */
	private final HashMap<String, Object> dataMap;
	
	/**
	 * @param myId
	 * @param roleMap
	 * @param issue
	 */
	public IPConDataStore(UUID myId, String issue, HashMap<UUID, Role> roleMap) {
		super();
		this.myId = myId;
		this.roleMap = roleMap;
		this.issue = issue;
		this.dataMap = new HashMap<String,Object>();
	}
	
	/**
	 * @param myId
	 * @param myStartingRole
	 * @param issue
	 */
	public IPConDataStore(UUID myId, String issue, Role myStartingRole) {
		super();
		this.myId = myId;
		this.roleMap = new HashMap<UUID, Role>();
		this.roleMap.put(myId, myStartingRole);
		this.issue = issue;
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
	public HashMap<UUID, IPConProtocol.Role> getRoleMap() {
		return roleMap;
	}
	
	/**
	 * 
	 * @param agentId
	 * @return the role of the given agent in this conversation
	 */
	public IPConProtocol.Role getRole(UUID agentId) {
		return roleMap.get(agentId);
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
	

}
