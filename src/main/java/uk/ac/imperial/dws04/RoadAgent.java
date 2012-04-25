/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.CellMove;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * @author dws04
 * 
 */
public class RoadAgent extends AbstractParticipant {

	/**
	 * Class to contain goals for the RoadAgent
	 * @author dws04
	 *
	 */
	protected class RoadAgentGoals {
		/**
		 *  Preferred speed
		 */
		private final int speed;
		/**
		 *  Goal destination - how many junctions to pass
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
		 * @return the goal dest
		 */
		public int getDest() {
			return dest;
		}

		/**
		 * @return the spacing
		 */
		public int getSpacing() {
			return spacing;
		}
	}

	protected RoadLocation myLoc;
	protected int mySpeed;
	protected final RoadAgentGoals goals;
	
	// Variable to store the location service.
	ParticipantRoadLocationService locationService;
	ParticipantSpeedService speedService;
	 
	RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals) {
		super(id, name);
		this.myLoc = myLoc;
		this.mySpeed = mySpeed;
		this.goals = goals;
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(ParticipantRoadLocationService.createSharedState(getID(), myLoc));
		ss.add(ParticipantSpeedService.createSharedState(getID(), mySpeed));
		return ss;
	}
	
	@Override
	public void initialise() {
		super.initialise();
		// get the ParticipantRoadLocationService.
		try {
			this.locationService = getEnvironmentService(ParticipantRoadLocationService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
		// get the ParticipantRoadSpeedService.
		try {
			this.speedService = getEnvironmentService(ParticipantSpeedService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
	}
	
	@Override
	public void execute() {
		myLoc = (RoadLocation) locationService.getAgentLocation(getID());
		mySpeed = speedService.getAgentSpeed(getID());
	 
		logger.info("My location is: "+ this.myLoc + " and my speed is " + this.mySpeed);
		logger.info("I can see the following agents:" + locationService.getNearbyAgents());
		saveDataToDB();
	 
		CellMove move = createMove();
	 
		submitMove(move);
	}

	/**
	 * @return
	 */
	private CellMove createMove() {
		// Create a random Move.
		int dx = Random.randomInt(2) - 1;
		int dy = Random.randomInt(2);
		CellMove move = new CellMove(dx, dy);
		return move;
	}

	/**
	 * @param move
	 */
	private void submitMove(CellMove move) {
		// submit move action to the environment.
		try {
			environment.act(move, getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Error trying to move", e);
		}
	}

	/**
	 * 
	 */
	private void saveDataToDB() {
		// get current simulation time
		int time = SimTime.get().intValue();
		// check db is available
		if (this.persist != null) {
			// save our location for this timestep
			this.persist.getState(time).setProperty("location", this.myLoc.toString());
			this.persist.getState(time).setProperty("speed", ((Integer)(this.mySpeed)).toString());
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.util.participant.AbstractParticipant#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 */
	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub
		logger.info("Agent " + this.getName() + " processing input: " + arg0.toString());
	}

}
