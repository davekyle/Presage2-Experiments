/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.LocationService;
import uk.ac.imperial.presage2.util.location.area.AreaService;

/**
 * @author dws04
 *
 */
@ServiceDependencies({ AreaService.class })
public class RoadLocationService extends LocationService {

	protected double perceptionRange;
	
	public RoadLocationService(EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider) {
		super(sharedState, serviceProvider);
		this.perceptionRange = calculatePerceptionRange(sharedState);
	}
	
	/**
	 * Perception range should be stopping distance at maxSpeed.
	 * @param sharedState
	 * @return
	 */
	private double calculatePerceptionRange(EnvironmentSharedStateAccess sharedState) {
		double mD = ((Integer)sharedState.getGlobal("maxDecel"));
		double mS = ((Integer)sharedState.getGlobal("maxSpeed"));
		double a = mS % mD;
		double n = ((mS-a) / mD);
		
		return (int)(((n/2)*( 2*a + ((n+1)*mD) )) + a);
	}
	
	@Override
	public RoadLocation getAgentLocation(UUID participantID){
		return (RoadLocation)super.getAgentLocation(participantID);
	}
	
	
	public void setAgentLocation(final UUID participantID, final RoadLocation l){
		super.setAgentLocation(participantID, l);
	}

	/**
	 * 
	 * @param l
	 * @return the agent at that location, or null if no one is there. If there has been a collision and mutliple agents are at the location, it returns one of them
	 */
	public UUID getLocationContents(final RoadLocation l) {
		Set<UUID> set = this.getAreaService().getCell(l.getLane(), l.getOffset(), 0);
		if (set.isEmpty()) {
			return null;
		}
		else {
			return set.iterator().next();
		}
	}
	
	/**
	 * 
	 * @param locA
	 * @param locB
	 * @return the distance that must be travelled between A.offset and B.offset (ie, it ignores difference in lanes)
	 */
	public int getOffsetDistanceBetween(RoadLocation locA, RoadLocation locB) {
		// check locations aren't the same
		if (locA.getOffset() == locB.getOffset()) {
			return 0;
		}
		// check for simple case (no wrap)
		else if (locB.getOffset() > locA.getOffset()) {
			return (locB.getOffset() - locA.getOffset());
		}
		// check for wrap
		else {
			int distanceToEnd = (this.getAreaService().getSizeY()) - locA.getOffset();
			return distanceToEnd + locB.getOffset();
		}
	}

	public int getLanes() {
		return this.getAreaService().getSizeX()-1;
	}

	public boolean isValidLane(int lane) {
		return (lane>=0 && lane<=getLanes());
	}
	
	

}
