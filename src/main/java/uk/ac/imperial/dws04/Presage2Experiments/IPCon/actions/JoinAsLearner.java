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
public class JoinAsLearner extends IPConAction {
	
	public IPConAgent agent;
	public Integer revision;
	public String issue;
	public UUID cluster;

	public JoinAsLearner(IPConAgent agent, Integer revision, String issue,
			UUID cluster) {
		super();
		this.agent = agent;
		this.revision = revision;
		this.issue = issue;
		this.cluster = cluster;
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



	@Override
	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction#fulfils(uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction)
	 */
	public boolean fulfils(IPConAction action) {
		return ( (this.equals(action)) || (
				(this.getClass().isAssignableFrom(action.getClass())) &&
				(((JoinAsLearner)action).getAgent()==null) &&
				(this.getRevision().equals(((JoinAsLearner)action).getRevision())) &&
				(this.getIssue().equals(((JoinAsLearner)action).getIssue())) &&
				(this.getCluster().equals(((JoinAsLearner)action).getCluster()))
				) );

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
		JoinAsLearner other = (JoinAsLearner) obj;
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



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JoinAsLearner [agent=" + agent + ", revision=" + revision
				+ ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
}
