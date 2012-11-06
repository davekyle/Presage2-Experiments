/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;

/**
 * @author dws04
 *
 */
public class Revise extends IPConAction {
	public IPConAgent agent;
	public Integer revision;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param oldRevision
	 * @param issue
	 * @param cluster
	 */
	public Revise(IPConAgent agent, Integer revision, String issue,
			UUID cluster) {
		super();
		this.agent = agent;
		this.revision = revision;
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
	 * @return the oldRevision
	 */
	public Integer getRevision() {
		return revision;
	}
	/**
	 * @param oldRevision the oldRevision to set
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
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
		return "Revise [agent=" + agent + ", revision=" + revision
				+ ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
	@Override
	public boolean fulfils(IPConAction action) {
		return ( (this.equals(action)) || (
				(this.getClass().isAssignableFrom(action.getClass())) &&
				(((Revise)action).getAgent()==null) &&
				(this.getRevision().equals(((Revise)action).getRevision())) &&
				(this.getIssue().equals(((Revise)action).getIssue())) &&
				(this.getCluster().equals(((Revise)action).getCluster()))
				) );
	}
	@Override
	public Revise copy() {
		return new Revise(agent, revision, issue, cluster);
	}
}
