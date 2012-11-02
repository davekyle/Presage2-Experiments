/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;

/**
 * @author dws04
 *
 */
public class Obligation {
	final IPConAgent agent;
	final IPConAction action;
	final boolean agentNeutral;
	final Role role;
	
	/**
	 * @param agent
	 * @param action
	 * @param agentNeutral
	 * @param role
	 */
	public Obligation(IPConAgent agent, IPConAction action,
			boolean agentNeutral, Role role) {
		super();
		this.agent = agent;
		this.action = action;
		this.agentNeutral = agentNeutral;
		this.role = role;
	}

	/**
	 * @return the agent
	 */
	public IPConAgent getAgent() {
		return agent;
	}

	/**
	 * @return the action
	 */
	public IPConAction getAction() {
		return action;
	}

	/**
	 * @return the agentNeutral
	 */
	public boolean isAgentNeutral() {
		return agentNeutral;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + (agentNeutral ? 1231 : 1237);
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
		Obligation other = (Obligation) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (agentNeutral != other.agentNeutral)
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
		return "Obligation [agent=" + agent + ", action=" + action
				+ ", agentNeutral=" + agentNeutral + ", role=" + role + "]";
	}
}
