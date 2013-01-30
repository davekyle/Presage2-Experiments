/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.StateTransformer;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.LocationService;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.AreaService;

/**
 * @author dws04
 *
 */
@Singleton
@ServiceDependencies({ AreaService.class, RoadEnvironmentService.class, LocationService.class })
public class RoadLocationService extends LocationService {

	private final Logger logger = Logger.getLogger(RoadLocationService.class);
	private Double perceptionRange;
	private final EnvironmentServiceProvider serviceProvider;
	
	@Inject
	public RoadLocationService(EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider) {
		super(sharedState, serviceProvider);
		this.perceptionRange = null;
		this.serviceProvider = serviceProvider;
	}
	
	/** Overriding to make insertion immediate instead of waiting until next state update
	 * @see uk.ac.imperial.presage2.util.location.LocationService#registerParticipant(uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest)
	 */
	@Override
	public void registerParticipant(final EnvironmentRegistrationRequest req) {
		super.registerParticipant(req);
		// do the super but also create the shared state directly
		for (ParticipantSharedState s : req.getSharedState()) {
			if (s.getName().equals("util.location") && s.getValue() instanceof Cell) {
				Cell c = (Cell) s.getValue();
				int x = (int) c.getX();
				int y = (int) c.getY();
				int z = (int) c.getZ();
				String cellKey = "area.cell." + x + "." + y + "." + z;
				Set<UUID> cell = getAreaService().getCell(x, y, z);
				cell = (HashSet<UUID>) sharedState.getGlobal(cellKey);
				if (cell == null)
					cell = new HashSet<UUID>();
				cell.add(req.getParticipantID());
				//cell = Collections.unmodifiableSet(cell);
				sharedState.createGlobal(cellKey, (HashSet<UUID>)cell);
			}
		}
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
	
	/**
	 * @param serviceProvider
	 * @return
	 */
	protected RoadEnvironmentService getRoadEnvironmentService() {
		try {
			return serviceProvider.getEnvironmentService(RoadEnvironmentService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Could not retrieve RoadEnvironmentService; functionality limited.");
			return null;
		}
	}
	
	/**
	 * @return the perceptionRange
	 */
	protected double getPerceptionRange() {
		if (perceptionRange==null) {
			perceptionRange = calculatePerceptionRange(sharedState);
		}
		return perceptionRange;
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
		Set<UUID> set;
		try {
			set = this.getAreaService().getCell(l.getLane(), l.getOffset(), 0);
			if (set.isEmpty()) {
				return null;
			}
			else {
				return set.iterator().next();
			}
		} catch (RuntimeException e){
			logger.warn(e);
			return null;
		}
	}
	/**
	 * 
	 * @param lane
	 * @param offset
	 * @return the agent at that location, or null if no one is there. If there has been a collision and mutliple agents are at the location, it returns one of them
	 */
	public UUID getLocationContents(final int lane, final int offset) {
		Set<UUID> set;
		try {
			set = this.getAreaService().getCell(lane, offset, 0);
			if (set.isEmpty()) {
				return null;
			}
			else {
				return set.iterator().next();
			}
		} catch (RuntimeException e){
			logger.warn(e);
			return null;
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

	public void removeAgent(UUID uuid) {
		RoadLocation loc = getAgentLocation(uuid);
		this.getAreaService().removeFromCell((int)loc.getX(), (int)loc.getY(), (int)loc.getZ(), uuid);
		sharedState.delete("util.location", uuid);
	}
	
	public int getWrapPoint() {
		return this.getAreaService().getSizeY();
	}
	
	

}
