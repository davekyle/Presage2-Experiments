package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.CellMove;

/**
 * 
 * @author Sam MacBeth, dws04
 *
 */
@ServiceDependencies( {ParticipantRoadLocationService.class, ParticipantSpeedService.class} )
public class Driver {

	private ParticipantRoadLocationService locationService;
	final private ParticipantSpeedService speedService;
	//final private RoadEnvironmentService environmentService;
	final private UUID myId;
	private EnvironmentServiceProvider serviceProvider;
	private final Logger logger = Logger.getLogger(Driver.class);

	/*public Driver(UUID myId, ParticipantRoadLocationService locationService, ParticipantSpeedService speedService, RoadEnvironmentService environmentService) {
		super();
		this.myId = myId;
		this.locationService = locationService;
		this.speedService = speedService;
		this.environmentService = environmentService;
	}*/

	public Driver(UUID myId, EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		super();
		this.myId = myId;
		this.serviceProvider = serviceProvider;
		this.locationService = serviceProvider
				.getEnvironmentService(ParticipantRoadLocationService.class);
		this.speedService = serviceProvider.getEnvironmentService(ParticipantSpeedService.class);
		/*this.environmentService = serviceProvider.getEnvironmentService(RoadEnvironmentService.class);*/
	}
	
	private RoadLocationService getLocationService() {
		if (this.locationService == null) {
			try {
				locationService = serviceProvider.getEnvironmentService(ParticipantRoadLocationService.class);
			} catch (UnavailableServiceException e) {
				logger.warn(e);
			}
		}
		return locationService;
	}
	
	RoadLocation getLocation() {
		return (RoadLocation) this.getLocationService().getAgentLocation(myId);
	}
	
	int getSpeed() {
		return this.speedService.getAgentSpeed(myId);
	}
	
	int getMaxAccel() {
		return this.speedService.getMaxAccel();
	}
	
	int getMaxDecel() {
		return this.speedService.getMaxDecel();
	}

	/**
	 * Accelerate by n, maintaining current lane
	 * @param n amount to accelerate by
	 * @return
	 */
	public CellMove accelerate(int n) {
		return new CellMove(0, getSpeed()+n);
	}
	
	/**
	 * Accelerate as much as you physically can, maintaining current lane
	 * @return
	 */
	public CellMove accelerateMax() {
		return new CellMove(0, getSpeed()+getMaxAccel());
	}
	
	/**
	 * Accelerate by one, maintaining current lane
	 * @return
	 */
	public CellMove accelerate() {
		return new CellMove(0, getSpeed()+1);
	}

	/**
	 * Decelerate by n, maintaining current lane
	 * @param n the amount to decelerate by
	 * @return
	 */
	public CellMove decelerate(int n) {
		return new CellMove(0, getSpeed()-n);
	}
	
	/**
	 * Decelerate as much as you physically can, maintaining current lane
	 * @return
	 */
	public CellMove decelerateMax() {
		return new CellMove(0, getSpeed()-getMaxDecel());
	}

	/**
	 * Decelerate by one, maintaining current lane
	 * @return
	 */
	public CellMove decelerate() {
		return new CellMove(0, getSpeed()-1);
	}
	
	/**
	 * Move one lane to the left, maintaining current speed
	 * @return
	 */
	public CellMove changeLaneLeft() {
		return new CellMove(-1, getSpeed());
	}
	
	/**
	 * Move one lane to the right, maintaining current speed
	 * @return
	 */
	public CellMove changeLaneRight() {
		return new CellMove(1, getSpeed());
	}

	/**
	 * Maintain current lane and speed
	 * @return
	 */
	public CellMove constantSpeed() {
		return new CellMove(0, getSpeed());
	}
	
	/**
	 * This is probably a risky one to use, although convenient.
	 * @param speed
	 * @return
	 */
	public CellMove moveAt(int speed){
		return new CellMove(0, speed);
	}
	
	public CellMove random() {
		return new CellMove(Random.randomInt(2), Random.randomInt(2));
	}
	
	/**
	 * TODO doesn't actually do anything different.
	 * @return
	 */
	public CellMove randomValid() {
		logger.warn("CellMove.randomValid() not implemented yet; returning (possibly invalid) random move");
		int lane = Random.randomInt(2);
		int speed = Random.randomInt(2);
		return new CellMove(lane, speed);
	}
	
	public CellMove turnOff() {
		Integer junctionDistance = this.locationService.getDistanceToNextJunction();
		if (junctionDistance!= null){
			logger.trace("Agent " + myId + " attempting to turn off");
			return new CellMove((0-(this.getLocation().getLane()+1)), junctionDistance);
		}
		else {
			logger.warn("Agent " + myId + " tried to turn off, but there aren't any junctions !");
			return this.decelerateMax();
		}
	}

	public CellMove moveIntoLaneAtSpeed(int lane, int speed) {
		//System.err.println("Agent is trying to move to lane " + lane + " from " + this.getLocation().getLane() + " so returning " + (lane-this.getLocation().getLane()));
		return new CellMove((lane-this.getLocation().getLane()), speed);
	}
	
	/**
	 * Decelerate as much as you physically can, to a limit of moving at 1, maintaining current lane
	 * @return
	 */
	public CellMove decelerateToCrawl() {
		if (getSpeed()-getMaxDecel()<=1) {
			return new CellMove(0, 1);
		}
		else {
			return new CellMove(0, getSpeed()-getMaxDecel());
		}
	}

}
