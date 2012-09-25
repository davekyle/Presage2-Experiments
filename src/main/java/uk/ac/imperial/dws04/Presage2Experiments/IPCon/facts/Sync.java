/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

/**
 * @author dws04
 *
 */
public class Sync extends IPConFact {
	
	final IPConAgent agent;
	final Object value;
	/**
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @param agent
	 * @param value
	 */
	public Sync(IPConAgent agent, Object value, Integer revision, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.agent = agent;
		this.value = value;
	}
	/**
	 * @return the agent
	 */
	public IPConAgent getAgent() {
		return agent;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sync other = (Sync) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Sync [agent=" + agent + ", value=" + value + ", revision="
				+ revision + ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
}
