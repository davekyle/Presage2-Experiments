/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;

/**
 * @author dws04
 *
 */
public class HasRole extends IPConFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7408397579826924177L;
	final Role role;
	final IPConAgent agent;
	/**
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @param role
	 * @param agent
	 */
	public HasRole(Role role, IPConAgent agent, Integer revision, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.role = role;
		this.agent = agent;
	}
	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}
	/**
	 * @return the agent
	 */
	public IPConAgent getAgent() {
		return agent;
	}
	/**
	 * @return the RIC as an object
	 */
	public IPConRIC getRIC() {
		return new IPConRIC(revision, issue, cluster);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		HasRole other = (HasRole) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (role != other.role)
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HasRole [role=" + role + ", agent=" + agent + ", revision="
				+ revision + ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
}
