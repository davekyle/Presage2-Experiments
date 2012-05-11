/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Level;

import uk.ac.imperial.dws04.utils.record.Pair;
import uk.ac.imperial.dws04.utils.record.PairBDescComparator;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.location.CellMove;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * @author dws04
 * 
 */
public class RoadAgent extends AbstractParticipant {

	/**
	 * @author dws04
	 *
	 */
	protected enum MoveType { CONSTANT, LEFT, RIGHT, ACCEL, ACCELMAX, DECEL, DECELMAX, RANDOM	}

	protected Driver driver;
	protected RoadLocation myLoc;
	protected int mySpeed;
	protected final RoadAgentGoals goals;
	
	// Variable to store the location service.
	ParticipantRoadLocationService locationService;
	ParticipantSpeedService speedService;
	/*RoadEnvironmentService environmentService;*/
	 
	RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals) {
		super(id, name);
		this.myLoc = myLoc;
		this.mySpeed = mySpeed;
		this.goals = goals;
		//this.driver = new Driver(id, locationService, speedService/*, environmentService*/);
		
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
		try {
			this.driver = new Driver(getID(), this);
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
		}
		/*// get the RoadEnvironmentService.
		try {
			this.environmentService = getEnvironmentService(RoadEnvironmentService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}*/
	}
	
	@Override
	public void execute() {
		if (locationService == null){
			logger.debug("bla");
		}
		myLoc = (RoadLocation) locationService.getAgentLocation(getID());
		mySpeed = speedService.getAgentSpeed(getID());
	 
		logger.info("My location is: "+ this.myLoc + ", my speed is " + this.mySpeed + ", and my goalSpeed is " + this.goals.getSpeed());
		logger.info("I can see the following agents:" + locationService.getNearbyAgents());
		saveDataToDB();
	 
		CellMove move = createMove();
	 
		submitMove(move);
	}
	
	@SuppressWarnings("unused")
	private CellMove createMove(){
		Pair<CellMove, Integer> result = null;
		Pair<CellMove, Integer> temp = null;
		ArrayList<Integer> availableLanes = new ArrayList<Integer>(3);
		LinkedList<Pair<CellMove,Integer>> actions = new LinkedList<Pair<CellMove,Integer>>();
		availableLanes.add(myLoc.getLane());
		availableLanes.add(myLoc.getLane()+1);
		availableLanes.add(myLoc.getLane()-1);
		Level lvl = logger.getLevel();
		//logger.setLevel(Level.TRACE);
		logger.trace("list of lanes is: " + availableLanes);
		//logger.setLevel(lvl);
		
		for (int i = 0; i <=availableLanes.size()-1; i++) {
			if (locationService.isValidLane(availableLanes.get(i))) {
				temp = createMove(availableLanes.get(i));
				if (temp.getB().equals(Integer.MAX_VALUE)) {
					logger.debug("Agent " + getName() + " found a safe move in lane " + availableLanes.get(i)); 
				}
				else {
					logger.debug("Agent " + getName() + " couldn't find a safe move in lane " + availableLanes.get(i));
				}
				actions.add(new Pair<CellMove,Integer>(new CellMove((availableLanes.get(i)-myLoc.getLane()), (int)temp.getA().getY()), temp.getB()));
			}
			else {
				// skip, not a valid lane
			}
		}
//		if (temp==null) {
//			logger.warn("Agent " + getName() + " doesn't think there is a safe move to make ! Decelerating as much as possible...");
//			return driver.decelerateMax();
//		}
//		logger.error("You should never get here (deadcode), but Eclipse is insisting on a return here, so let's pass out a null to throw some exceptions later :D");
		Collections.sort(actions, new PairBDescComparator<Integer>());
		result = actions.getFirst();
		if (result.getB().equals(Integer.MAX_VALUE)) {
			logger.debug("Agent " + getName() + " attempting safe move: " + result.getA());
			if (result.getA().getX()!=myLoc.getLane()) {
				logger.debug("Agent is going to change lanes.");
			}
		}
		else {
			logger.warn("Agent " + getName() + " doesn't think there is a safe move to make ! Decelerating as much as possible...");
			if (result.getA().getX()!=myLoc.getLane()) {
				logger.debug("Agent is going to change lanes.");
			}
		}
		return result.getA();
	}
	
	/**
	 * @return a reasoned move/viability pair. Viability is Integer.Max_VALUE if the move is safe, or not if otherwise.
	 */
	private Pair<CellMove,Integer> createMove(int lane) {
		int newSpeed;
		// this doesn't really show if something isn't safe, but it does show if something is safe
		// (ie, only true if you know you can go at your preferred speed instead of one for safety's sake.
		boolean safe = false;
		Integer reqStopDist = null; // init to null incase we get to needing it and it hasnt been assigned..
		
		logger.debug("Agent " + getName() + " is checking lane " + lane + " for a valid move");
		// get agent in front
		UUID target = this.locationService.getAgentToFront(lane);
		// if there is someone there
		if (target!=null) {
			// get agent in front's stopping distance
			logger.debug("Agent " + getName() + " saw agent " + target + " at " + (RoadLocation)locationService.getAgentLocation(target));
			int targetStopDist = speedService.getStoppingDistance(target);
			logger.debug("Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
			// add the distance between you and their current location (then minus 1 to make sure you can stop BEFORE them)
			reqStopDist = targetStopDist + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(target)))-1;
			logger.debug("Agent " + getName() + " got a reqStopDist of " + reqStopDist
					+ " ( distanceBetween(" + myLoc + "," + (RoadLocation)locationService.getAgentLocation(target) +")= " + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(target))) + ") ");
			// work out what speed you can be at to stop in time
			int stoppingSpeed = speedService.getSpeedToStopInDistance(reqStopDist);
			logger.debug("Agent " + getName() + " thinks they need to travel at " + stoppingSpeed + " to stop in " + reqStopDist);
			if ( (stoppingSpeed <0) || ( (mySpeed>stoppingSpeed) && (mySpeed-speedService.getMaxDecel() > stoppingSpeed) ) ) {
				logger.debug("Agent " + getName() + " doesn't think they can stop in time in lane " + lane);
				return new Pair<CellMove,Integer>(driver.decelerateMax(), reqStopDist);
			}
			// if this is more than your preferred speed, aim to go at your preferred speed instead
			if (stoppingSpeed > goals.getSpeed()) {
				newSpeed = goals.getSpeed();
				logger.debug("Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
				safe = true;
			}// otherwise, aim to go at that speed
			else {
				newSpeed = stoppingSpeed; 
				logger.debug("Agent " + getName() + " deciding to move at safe speed of " + newSpeed);
			}
		}
		// if there isn't anyone there, aim to go at your preferred speed
		else {
			newSpeed = goals.getSpeed();
			logger.debug("Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
			safe = true;
		}
		// get the difference between it and your current speed
		int speedDelta = mySpeed-newSpeed;
		// if there isn't a difference, chill
		if (speedDelta == 0) {
			logger.debug("Agent " + getName() + " attempting move at constant speed of " + newSpeed);
			return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
		}
		// if it's greater than your current speed, accelerate
		else if (speedDelta < 0) {
			// you know which you're in, so now abs() it...
			speedDelta = Math.abs(speedDelta);
			// if you're at maxSpeed, don't try and speed up...
			if (mySpeed == speedService.getMaxSpeed()) {
				return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
			}
			else {
				// work out if you can change to that speed now
				if (speedDelta <= speedService.getMaxAccel()) {
					// if you can, do so
					if (mySpeed+speedDelta > speedService.getMaxSpeed()) {
						logger.debug("Agent " + getName() + " adjusted acceleration from " + speedDelta + " to move at maxSpeed.");
						return new Pair<CellMove,Integer>(driver.moveAt(speedService.getMaxSpeed()), Integer.MAX_VALUE);
					}
					else {
						logger.debug("Agent " + getName() + " attempting to accelerate by " + speedDelta);
						return new Pair<CellMove,Integer>(driver.accelerate(speedDelta), Integer.MAX_VALUE);
					}
				}
				else {
					// if not, just accel as much as you can, and you'll make it up
					if (mySpeed+speedService.getMaxAccel() > speedService.getMaxSpeed()) {
						logger.debug("Agent " + getName() + " adjusted acceleration from maxAccel to move at maxSpeed.");
						return new Pair<CellMove,Integer>(driver.moveAt(speedService.getMaxSpeed()), Integer.MAX_VALUE);
					}
					else {
						logger.debug("Agent " + getName() + " attempting to accelerate as much as possible to meet speedDelta of " + speedDelta);
						return new Pair<CellMove,Integer>(driver.accelerateMax(), Integer.MAX_VALUE);
					}
				}
			}
		}
		// if it's less than your current speed, decelerate
		else {
			// you know which you're in, so now abs() it...
			speedDelta = Math.abs(speedDelta);
			// if your current speed is 0, then don't even try attempting to decelerate...
			if (mySpeed == 0) {
				logger.debug("Agent " + getName() + " is at zero already...");
				return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
			}
			else {
				// work out if you can change to that speed now
				if (speedDelta <= speedService.getMaxDecel()) {
					// if you can, do so (checking to make sure it won't take you below 0)
					int temp = mySpeed-speedDelta;
					if (temp<=0) {
						// if you're going to go below 0, then set your decel to hit 0
						logger.debug("Agent " + getName() + " adjusting decel from " + speedDelta + " to move at zero.");
						return new Pair<CellMove,Integer>(driver.moveAt(0), Integer.MAX_VALUE);
					}
					else {
						logger.debug("Agent " + getName() + " attempting to decelerate by " + speedDelta);
						return new Pair<CellMove,Integer>(driver.decelerate(speedDelta), Integer.MAX_VALUE);
					}
				}
				else {
					// if not, PANIC ! (just decel max and hope for the best ? maybe change lanes...)
					if (mySpeed-speedService.getMaxDecel() < 0){
						logger.debug("Agent " + getName() + " would decelMAx but adjusted to move at zero.");
						return new Pair<CellMove,Integer>(driver.moveAt(0), Integer.MAX_VALUE);
					}
					else {
						// If you *know* you're safe, then chill.
						if (!safe) {
							logger.debug("Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta);
							// not convince we'll get here...
							return new Pair<CellMove,Integer>(driver.decelerateMax(), reqStopDist);
						}
						else {
							logger.debug("Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta + " so decellerating as much as they can");
							return new Pair<CellMove,Integer>(driver.decelerateMax(), Integer.MAX_VALUE);
						}
					}
				}
			}
		}
		
		//return this.driver.randomValid();
	}

	/**
	 * @return a random move
	 */
	private CellMove randomMove() {
		return this.driver.random();
	}
	
	/**
	 * @return a move
	 */
	private CellMove explicitMove(MoveType type, int n) {
		switch (type) {
		case CONSTANT : return driver.constantSpeed();
		case ACCEL : return driver.accelerate(n);
		case ACCELMAX : return driver.accelerateMax();
		case DECEL : return driver.decelerate(n);
		case DECELMAX : return driver.decelerateMax();
		case LEFT : return driver.changeLaneLeft();
		case RIGHT : return driver.changeLaneRight();
		case RANDOM : return driver.randomValid();
		default : return driver.random();
		}
	}

	/**
	 * @param move
	 */
	private void submitMove(CellMove move) {
		// submit move action to the environment.
		try {
			logger.debug("Agent " + getName() + " attempting move: " + move);
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
		logger.info("Agent " + this.getName() + " not processing input: " + arg0.toString());
	}

}
