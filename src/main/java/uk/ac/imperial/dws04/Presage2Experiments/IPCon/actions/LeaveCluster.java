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
public class LeaveCluster extends IPConAction {
	public IPConAgent agent;
	public UUID cluster;
	
	/**
	 * @param agent
	 * @param cluster
	 */
	public LeaveCluster(IPConAgent agent, UUID cluster) {
		super();
		this.agent = agent;
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
		LeaveCluster other = (LeaveCluster) obj;
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
		return "LeaveCluster [agent=" + agent + ", cluster=" + cluster + "]";
	}
	
	/*@Override
	public boolean fulfils(IPConAction obligation) {
		return ( (this.equals(obligation)) || (
				(this.getClass().isAssignableFrom(obligation.getClass())) &&
				(((LeaveCluster)obligation).getAgent()==null) &&
				(this.getCluster().equals(((LeaveCluster)obligation).getCluster()))
				) );
	}*/
	
	@Override
	public LeaveCluster copy() {
		return new LeaveCluster(agent, cluster);
	}
	
	
}
