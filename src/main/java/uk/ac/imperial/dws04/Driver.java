package uk.ac.imperial.dws04;

import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.CellMove;
import uk.ac.imperial.presage2.util.location.LocationService;

public class Driver {

	final private LocationService locationService;
	final private SpeedService speedService;
	final private RoadEnvironmentService environmentService;
	final private UUID myId;

	public Driver(UUID myId, LocationService locationService, SpeedService speedService, RoadEnvironmentService environmentService) {
		super();
		this.myId = myId;
		this.locationService = locationService;
		this.speedService = speedService;
		this.environmentService = environmentService;
	}

	public Driver(UUID myId, EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		super();
		this.myId = myId;
		this.locationService = serviceProvider
				.getEnvironmentService(LocationService.class);
		this.speedService = serviceProvider.getEnvironmentService(SpeedService.class);
		this.environmentService = serviceProvider.getEnvironmentService(RoadEnvironmentService.class);
	}
	
	RoadLocation getLocation() {
		return (RoadLocation) this.locationService.getAgentLocation(myId);
	}
	
	int getSpeed() {
		return this.speedService.getAgentSpeed(myId);
	}

	public CellMove accelerate(int n) {
		return new CellMove(getLocation().getLane(), getSpeed()+n);
	}
	
	public CellMove accelerate() {
		return new CellMove(getLocation().getLane(), getSpeed()+1);
	}

	public CellMove decelerate(int n) {
		return new CellMove(getLocation().getLane(), getSpeed()+n);
	}

	public CellMove decelerate() {
		return new CellMove(getLocation().getLane(), getSpeed()+1);
	}
	
	public CellMove changeLaneLeft() {
		return new CellMove(getLocation().getLane()-1, getSpeed());
	}

	public CellMove changeLaneRight() {
		return new CellMove(getLocation().getLane()+1, getSpeed());
	}

	public CellMove constantSpeed() {
		return new CellMove(getLocation().getLane(), getSpeed());
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
