package uk.ac.imperial.dws04.Presage2Experiments;

/**
 * Class to contain goals for the RoadAgent
 * @author dws04
 *
 */
class RoadAgentGoals {
	/**
	 *  Preferred speed
	 */
	private final int speed;
	/**
	 *  Goal destination - how many junctions to pass. Null if the agent should never leave
	 */
	private final int dest;
	/**
	 * The preferred space left between this agent and the ones around it
	 * when not in a cluster
	 */
	private final int spacing;
	
	RoadAgentGoals(int speed, int dest, int spacing) {
		this.speed = speed;
		this.dest = dest;
		this.spacing = spacing;
	}

	/**
	 * @return the speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * @return the goal dest. Is null if the agent will stay on the road forever
	 */
	public Integer getDest() {
		return dest;
	}

	/**
	 * @return the spacing
	 */
	public int getSpacing() {
		return spacing;
	}
}