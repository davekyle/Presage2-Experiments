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
public class AddRole extends IPConAction {
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
	public AddRole(IPConAgent leader, IPConAgent agent, Role role,
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
		AddRole other = (AddRole) obj;
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
	public uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role getRole() {
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
		return "AddRole [leader=" + leader + ", agent=" + agent + ", role="
				+ role + ", revision=" + revision + ", issue=" + issue
				+ ", cluster=" + cluster + "]";
	}
	
	@Override
	public boolean fulfils(IPConAction action) {
		return ( (this.equals(action)) || (
				(this.getClass().isAssignableFrom(action.getClass())) &&
				(((AddRole)action).getAgent()==null) &&
				(this.getAgent().equals(((AddRole)action).getAgent())) &&
				(this.getRole().equals(((AddRole)action).getRole())) &&
				(this.getRevision().equals(((AddRole)action).getRevision())) &&
				(this.getIssue().equals(((AddRole)action).getIssue())) &&
				(this.getCluster().equals(((AddRole)action).getCluster()))
				) );
	}
	
	@Override
	public AddRole copy() {
		return new AddRole(this.leader, this.agent, this.role, this.revision, this.issue, this.cluster);
	}
}
