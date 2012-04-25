package uk.ac.imperial.dws04;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.CellMove;

/**
 * 
 * @author Sam MacBeth, dws04
 *
 */
public class Driver {

	private ParticipantRoadLocationService locationService;
	final private ParticipantSpeedService speedService;
	//final private RoadEnvironmentService environmentService;
	final private UUID myId;
	private EnvironmentServiceProvider serviceProvider;
	private final Logger logger = Logger.getLogger(Driver.class);

	public Driver(UUID myId, ParticipantRoadLocationService locationService, ParticipantSpeedService speedService/*, RoadEnvironmentService environmentService*/) {
		super();
		this.myId = myId;
		this.locationService = locationService;
		this.speedService = speedService;
		/*this.environmentService = environmentService;*/
	}

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
	
	private ParticipantRoadLocationService getLocationService() {
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

	public CellMove accelerate(int n) {
		return new CellMove(0, getSpeed()+n);
	}
	
	public CellMove accelerateMax() {
		return new CellMove(0, getSpeed()+getMaxAccel());
	}
	
	public CellMove accelerate() {
		return new CellMove(0, getSpeed()+1);
	}

	public CellMove decelerate(int n) {
		return new CellMove(0, getSpeed()-n);
	}
	
	public CellMove decelerateMax() {
		return new CellMove(0, getSpeed()-getMaxDecel());
	}

	public CellMove decelerate() {
		return new CellMove(0, getSpeed()-1);
	}
	
	public CellMove changeLaneLeft() {
		return new CellMove(-1, getSpeed());
	}

	public CellMove changeLaneRight() {
		return new CellMove(1, getSpeed());
	}

	public CellMove constantSpeed() {
		return new CellMove(0, getSpeed());
	}
	
	public CellMove random() {
		return new CellMove(Random.randomInt(2), Random.randomInt(2));
	}
	
	/**
	 * TODO doesn't actually do anything different.
	 * @return
	 */
	public CellMove randomValid() {
		int lane = Random.randomInt(2);
		int speed = Random.randomInt(2);
		return new CellMove(lane, speed);
	}

}
