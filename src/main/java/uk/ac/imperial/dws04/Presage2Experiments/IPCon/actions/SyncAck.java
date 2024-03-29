/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;

/**
 * If value is the string "PLACEHOLDER" this is from an obligation and either the correct value or a null (no) should be chosen
 * 
 * @author dws04
 *
 */
public class SyncAck extends IPConAction {
	public IPConAgent agent;
	public Object value;
	public Integer revision;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param value
	 * @param revision
	 * @param issue
	 * @param cluster
	 */
	public SyncAck(IPConAgent agent, Object value, Integer revision,
			String issue, UUID cluster) {
		super();
		this.agent = agent;
		this.value = value;
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
		SyncAck other = (SyncAck) obj;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SyncAck [agent=" + agent + ", value=" + value + ", revision="
				+ revision + ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
	/*@Override
	public boolean fulfils(IPConAction obligation) {
		return ( (this.equals(obligation)) || (
				(this.getClass().isAssignableFrom(obligation.getClass())) &&
				( (((SyncAck)obligation).getAgent()==null) || (this.getAgent().equals(((SyncAck)obligation).getAgent())) ) &&
				( (((SyncAck)obligation).getValue()==null) || (this.getValue().equals(((SyncAck)obligation).getValue())) ) &&
				( (((SyncAck)obligation).getRevision()==null) || (this.getRevision().equals(((SyncAck)obligation).getRevision())) ) &&
				( (((SyncAck)obligation).getIssue()==null) || (this.getIssue().equals(((SyncAck)obligation).getIssue())) ) &&
				( (((SyncAck)obligation).getCluster()==null) || (this.getCluster().equals(((SyncAck)obligation).getCluster())) )
				) );
	}*/
	
	@Override
	public SyncAck copy() {
		return new SyncAck(agent, value, revision, issue, cluster);
	}
}
