/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;

/**
 * @author dws04
 *
 */
public class RemRole extends IPConAction {
	public IPConAgent leader;
	public IPConAgent agent;
	public Role role;
	public Integer revision;
	public String issue;
	public UUID cluster;
	
	/**
	 * @param leader
	 * @param agent
	 * @param role
	 * @param revision
	 * @param issue
	 * @param cluster
	 */
	public RemRole(IPConAgent leader, IPConAgent agent, Role role,
			Integer revision, String issue, UUID cluster) {
		super();
		this.leader = leader;
		this.agent = agent;
		this.role = role;
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
		result = prime * result + ((leader == null) ? 0 : leader.hashCode());
		result = prime * result
				+ ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
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
		RemRole other = (RemRole) obj;
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
		if (leader == null) {
			if (other.leader != null)
				return false;
		} else if (!leader.equals(other.leader))
			return false;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		if (role != other.role)
			return false;
		return true;
	}
	/**
	 * @return the leader
	 */
	public IPConAgent getLeader() {
		return leader;
	}
	/**
	 * @param leader the leader to set
	 */
	public void setLeader(IPConAgent leader) {
		this.leader = leader;
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
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}
	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
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
		return "RemRole [leader=" + leader + ", agent=" + agent + ", role="
				+ role + ", revision=" + revision + ", issue=" + issue
				+ ", cluster=" + cluster + "]";
	}
	
	@Override
	public boolean fulfils(IPConAction obligation) {
		return ( (this.equals(obligation)) || (
				(this.getClass().isAssignableFrom(obligation.getClass())) &&
				(((RemRole)obligation).getAgent()==null) &&
				(this.getAgent().equals(((RemRole)obligation).getAgent())) &&
				(this.getRole().equals(((RemRole)obligation).getRole())) &&
				(this.getRevision().equals(((RemRole)obligation).getRevision())) &&
				(this.getIssue().equals(((RemRole)obligation).getIssue())) &&
				(this.getCluster().equals(((RemRole)obligation).getCluster()))
				) );
	}
	@Override
	public RemRole copy() {
		return new RemRole(leader, agent, role, revision, issue, cluster);
	}
}
