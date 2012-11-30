package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.HashMap;

import uk.ac.imperial.dws04.utils.record.Pair;

/**
 * Class to contain goals for the RoadAgent
 * @author dws04
 *
 */
public class RoadAgentGoals {
	private final HashMap<String,Pair<Integer, Integer>> map = new HashMap<String,Pair<Integer, Integer>>();
	
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
		if (speed-2<0) {
			speedTolerance = speed;
		}
		else {
			this.speedTolerance = 2;
		}
		if (spacing-2<0) {
			spacingTolerance = spacing;
		}
		else {
			this.spacingTolerance = 2;
		}
		map.put("speed", new Pair<Integer, Integer>(getSpeed(),getSpeedTolerance()));
		map.put("spacing", new Pair<Integer, Integer>(getSpacing(),getSpacingTolerance()));
	}
	
	public RoadAgentGoals(Integer speed, Integer speedTolerance, Integer dest, Integer spacing, Integer spacingTolerance) {
		this.speed = speed;
		this.dest = dest;
		this.spacing = spacing;
		this.speedTolerance = speedTolerance;
		this.spacingTolerance = spacingTolerance;
		if (speed-speedTolerance<0) {
			speedTolerance = speed;
		}
		if (spacing-spacingTolerance<0) {
			spacingTolerance = spacing;
		}
		
		map.put("speed", new Pair<Integer, Integer>(getSpeed(),getSpeedTolerance()));
		map.put("spacing", new Pair<Integer, Integer>(getSpacing(),getSpacingTolerance()));
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
	
	/**
	 * @return map of goals <value,tolerance>
	 */
	public final HashMap<String,Pair<Integer, Integer>> getMap() {
		return map;
	}
}