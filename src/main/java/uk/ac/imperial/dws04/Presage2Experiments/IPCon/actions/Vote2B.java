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
public class Vote2B extends IPConAction {
	public IPConAgent agent;
	public Integer revision;
	public Integer ballot;
	public Object value;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param revision
	 * @param ballot
	 * @param value
	 * @param issue
	 * @param cluster
	 */
	public Vote2B(IPConAgent agent, Integer revision, Integer ballot,
			Object value, String issue, UUID cluster) {
		super();
		this.agent = agent;
		this.revision = revision;
		this.ballot = ballot;
		this.value = value;
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
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Vote2B other = (Vote2B) obj;
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
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
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
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
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
		return "Vote2B [agent=" + agent + ", revision=" + revision
				+ ", ballot=" + ballot + ", value=" + value + ", issue="
				+ issue + ", cluster=" + cluster + "]";
	}
	
	/*@Override
	public boolean fulfils(IPConAction obligation) {
		return ( (this.equals(obligation)) || (
				(this.getClass().isAssignableFrom(obligation.getClass())) &&
				( (((Vote2B)obligation).getAgent()==null) || (this.getAgent().equals(((Vote2B)obligation).getAgent())) ) &&
				( (((Vote2B)obligation).getValue()==null) || (this.getValue().equals(((Vote2B)obligation).getValue())) ) &&
				( (((Vote2B)obligation).getBallot()==null) || (this.getBallot().equals(((Vote2B)obligation).getBallot())) ) &&
				( (((Vote2B)obligation).getRevision()==null) || (this.getRevision().equals(((Vote2B)obligation).getRevision())) ) &&
				( (((Vote2B)obligation).getIssue()==null) || (this.getIssue().equals(((Vote2B)obligation).getIssue())) ) &&
				( (((Vote2B)obligation).getCluster()==null) || (this.getCluster().equals(((Vote2B)obligation).getCluster())) ) 
				) );
	}*/
	
	@Override
	public Vote2B copy() {
		return new Vote2B(agent, revision, ballot, value, issue, cluster);
	}
}
