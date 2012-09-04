/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.util.UUID;

import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * @author dave
 *
 */
public class Response1B extends IPConAction {
	public NetworkAddress agent;
	public Integer voteRevision;
	public Integer voteBallot;
	public Object voteValue;
	public Integer revision;
	public Integer ballot;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param voteRevision
	 * @param voteBallot
	 * @param voteValue
	 * @param revision
	 * @param ballot
	 * @param issue
	 * @param cluster
	 */
	public Response1B(NetworkAddress agent, Integer voteRevision,
			Integer voteBallot, Object voteValue, Integer revision,
			Integer ballot, String issue, UUID cluster) {
		super();
		this.agent = agent;
		this.voteRevision = voteRevision;
		this.voteBallot = voteBallot;
		this.voteValue = voteValue;
		this.revision = revision;
		this.ballot = ballot;
		this.issue = issue;
		this.cluster = cluster;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((ballot == null) ? 0 : ballot.hashCode());
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		result = prime * result + ((issue == null) ? 0 : issue.hashCode());
		result = prime * result
				+ ((revision == null) ? 0 : revision.hashCode());
		result = prime * result
				+ ((voteBallot == null) ? 0 : voteBallot.hashCode());
		result = prime * result
				+ ((voteRevision == null) ? 0 : voteRevision.hashCode());
		result = prime * result
				+ ((voteValue == null) ? 0 : voteValue.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response1B other = (Response1B) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (ballot == null) {
			if (other.ballot != null)
				return false;
		} else if (!ballot.equals(other.ballot))
			return false;
		if (cluster == null) {
			if (other.cluster != null)
				return false;
		} else if (!cluster.equals(other.cluster))
			return false;
		if (issue == null) {
			if (other.issue != null)
				return false;
		} else if (!issue.equals(other.issue))
			return false;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		if (voteBallot == null) {
			if (other.voteBallot != null)
				return false;
		} else if (!voteBallot.equals(other.voteBallot))
			return false;
		if (voteRevision == null) {
			if (other.voteRevision != null)
				return false;
		} else if (!voteRevision.equals(other.voteRevision))
			return false;
		if (voteValue == null) {
			if (other.voteValue != null)
				return false;
		} else if (!voteValue.equals(other.voteValue))
			return false;
		return true;
	}
	/**
	 * @return the agent
	 */
	public NetworkAddress getAgent() {
		return agent;
	}
	/**
	 * @param agent the agent to set
	 */
	public void setAgent(NetworkAddress agent) {
		this.agent = agent;
	}
	/**
	 * @return the voteRevision
	 */
	public Integer getVoteRevision() {
		return voteRevision;
	}
	/**
	 * @param voteRevision the voteRevision to set
	 */
	public void setVoteRevision(Integer voteRevision) {
		this.voteRevision = voteRevision;
	}
	/**
	 * @return the voteBallot
	 */
	public Integer getVoteBallot() {
		return voteBallot;
	}
	/**
	 * @param voteBallot the voteBallot to set
	 */
	public void setVoteBallot(Integer voteBallot) {
		this.voteBallot = voteBallot;
	}
	/**
	 * @return the voteValue
	 */
	public Object getVoteValue() {
		return voteValue;
	}
	/**
	 * @param voteValue the voteValue to set
	 */
	public void setVoteValue(Object voteValue) {
		this.voteValue = voteValue;
	}
	/**
	 * @return the revision
	 */
	public Integer getRevision() {
		return revision;
	}
	/**
	 * @param revision the revision to set
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	/**
	 * @return the ballot
	 */
	public Integer getBallot() {
		return ballot;
	}
	/**
	 * @param ballot the ballot to set
	 */
	public void setBallot(Integer ballot) {
		this.ballot = ballot;
	}
	/**
	 * @return the issue
	 */
	public String getIssue() {
		return issue;
	}
	/**
	 * @param issue the issue to set
	 */
	public void setIssue(String issue) {
		this.issue = issue;
	}
	/**
	 * @return the cluster
	 */
	public UUID getCluster() {
		return cluster;
	}
	/**
	 * @param cluster the cluster to set
	 */
	public void setCluster(UUID cluster) {
		this.cluster = cluster;
	}
}
