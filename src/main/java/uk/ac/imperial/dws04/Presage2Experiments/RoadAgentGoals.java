package uk.ac.imperial.dws04.Presage2Experiments;

/**
 * Class to contain goals for the RoadAgent
 * @author dws04
 *
 */
public class RoadAgentGoals {
	/**
	 *  Preferred speed
	 */
	private final Integer speed;
	/**
	 *  Goal destination - how many junctions to pass. Null if the agent should never leave
	 */
	private final Integer dest;
	/**
	 * The preferred space left between this agent and the ones around it
	 * when not in a cluster
	 */
	private final Integer spacing;
	
	private final Integer speedTolerance;
	private final Integer spacingTolerance;
	
	public RoadAgentGoals(Integer speed, Integer dest, Integer spacing) {
		this.speed = speed;
		this.dest = dest;
		this.spacing = spacing;
		this.speedTolerance = 2;
		this.spacingTolerance = 2;
	}
	
	public RoadAgentGoals(Integer speed, Integer speedTolerance, Integer dest, Integer spacing, Integer spacingTolerance) {
		this.speed = speed;
		this.dest = dest;
		this.spacing = spacing;
		this.speedTolerance = speedTolerance;
		this.spacingTolerance = spacingTolerance;
	}

	/**
	 * @return the speed
	 */
	public Integer getSpeed() {
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
	public Integer getSpacing() {
		return spacing;
	}

	/**
	 * @return the speedTolerance
	 */
	public Integer getSpeedTolerance() {
		return speedTolerance;
	}

	/**
	 * @return the spacingTolerance
	 */
	public Integer getSpacingTolerance() {
		return spacingTolerance;
	}
}