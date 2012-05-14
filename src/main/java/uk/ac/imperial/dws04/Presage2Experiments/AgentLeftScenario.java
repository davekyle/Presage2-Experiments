/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.UUID;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.event.Event;

/**
 * @author dws04
 *
 */
public class AgentLeftScenario implements Event {

	/**
	 * The agent's UUID
	 */
	final UUID agentID;
	/**
	 * The junction the agent left from
	 */
	final int junctionOffset;
	/**
	 * The time the agent left at
	 */
	final Time time;
	
	public AgentLeftScenario(UUID agentID, int junctionOffset, Time time) {
		this.agentID = agentID;
		this.junctionOffset = junctionOffset;
		this.time = time;
	}

	/**
	 * @return the UUID of the Agent
	 */
	public UUID getAgentID() {
		return agentID;
	}

	/**
	 * @return the junction location the agent left from
	 */
	public int getJunctionOffset() {
		return junctionOffset;
	}
	
	/**
	 * @return the time the agent left at 
	 */
	public Time getTime() {
		return time;
	}
	
	

}
