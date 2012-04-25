package uk.ac.imperial.dws04;

import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.util.location.CellMove;
import uk.ac.imperial.presage2.util.location.LocationService;

public class Driver {

	final private LocationService locationService;
	final private SpeedService speedService;
	final private UUID myId;

	public Driver(UUID myId, LocationService locationService, SpeedService speedService) {
		super();
		this.myId = myId;
		this.locationService = locationService;
		this.speedService = speedService;
	}

	public Driver(UUID myId, EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		super();
		this.myId = myId;
		this.locationService = serviceProvider
				.getEnvironmentService(LocationService.class);
		this.speedService = serviceProvider.getEnvironmentService(SpeedService.class);
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

}
