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
public class Revise extends IPConAction {
	public NetworkAddress agent;
	public Integer oldRevision;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param oldRevision
	 * @param issue
	 * @param cluster
	 */
	public Revise(NetworkAddress agent, Integer oldRevision, String issue,
			UUID cluster) {
		super();
		this.agent = agent;
		this.oldRevision = oldRevision;
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
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		result = prime * result + ((issue == null) ? 0 : issue.hashCode());
		result = prime * result
				+ ((oldRevision == null) ? 0 : oldRevision.hashCode());
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
		Revise other = (Revise) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
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
		if (oldRevision == null) {
			if (other.oldRevision != null)
				return false;
		} else if (!oldRevision.equals(other.oldRevision))
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
	 * @return the oldRevision
	 */
	public Integer getOldRevision() {
		return oldRevision;
	}
	/**
	 * @param oldRevision the oldRevision to set
	 */
	public void setOldRevision(Integer oldRevision) {
		this.oldRevision = oldRevision;
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
