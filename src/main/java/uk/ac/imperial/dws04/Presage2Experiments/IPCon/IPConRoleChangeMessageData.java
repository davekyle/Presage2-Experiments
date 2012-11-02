package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import uk.ac.imperial.presage2.core.network.NetworkAddress;

public class IPConRoleChangeMessageData {
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