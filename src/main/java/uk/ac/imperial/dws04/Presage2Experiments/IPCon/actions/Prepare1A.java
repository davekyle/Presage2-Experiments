/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;

/**
 * If ballot is null, this is from an obligation to prepare and any sensible value may be chosen
 * 
 * @author dws04
 *
 */
public class Prepare1A extends IPConAction {
	public IPConAgent agent;
	public Integer revision;
	public Integer ballot;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param revision
	 * @param ballot
	 * @param issue
	 * @param cluster
	 */
	public Prepare1A(IPConAgent agent, Integer revision, Integer ballot,
			String issue, UUID cluster) {
		super();
		this.agent = agent;
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
		Prepare1A other = (Prepare1A) obj;
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
		return true;
	}
	/**
	 * @return the agent
	 */
	public IPConAgent getAgent() {
		return agent;
	}
	/**
	 * @param agent the agent to set
	 */
	public void setAgent(IPConAgent agent) {
		this.agent = agent;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Prepare1A [agent=" + agent + ", revision=" + revision
				+ ", ballot=" + ballot + ", issue=" + issue + ", cluster="
				+ cluster + "]";
	}
	
	@Override
	public boolean fulfils(IPConAction action) {
		return ( (this.equals(action)) || (
				(this.getClass().isAssignableFrom(action.getClass())) &&
				(((Prepare1A)action).getAgent()==null) &&
				(this.getBallot().equals(((Prepare1A)action).getBallot())) &&
				(this.getRevision().equals(((Prepare1A)action).getRevision())) &&
				(this.getIssue().equals(((Prepare1A)action).getIssue())) &&
				(this.getCluster().equals(((Prepare1A)action).getCluster()))
				) );
	}
	@Override
	public Prepare1A copy() {
		return new Prepare1A(agent, revision, ballot, issue, cluster);
	}
}
