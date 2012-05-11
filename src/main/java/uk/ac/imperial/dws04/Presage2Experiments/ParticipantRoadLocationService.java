/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;


import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.participant.Participant;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.location.CannotSeeAgent;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.HasPerceptionRange;

/**
 * @author dws04
 *
 */
public class ParticipantRoadLocationService extends RoadLocationService {

	private final Logger logger = Logger.getLogger(ParticipantRoadLocationService.class);

	protected final UUID myID;

	protected final EnvironmentMembersService membersService;
	
	public ParticipantRoadLocationService(
			Participant p,
			EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider) {
		super(sharedState, serviceProvider);
		this.myID = p.getID();
		this.membersService = getMembersService(serviceProvider);
	}
	
	/**
	 * @param serviceProvider
	 * @return
	 */
	private EnvironmentMembersService getMembersService(
			EnvironmentServiceProvider serviceProvider) {
		try {
			return serviceProvider
					.getEnvironmentService(EnvironmentMembersService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Could not retrieve EnvironmentMembersService; functionality limited.");
			return null;
		}
	}
	
	/**
	 * Overriding this because we don't have a range provider...
	 * ASSUMES that you can see all lanes at a given distance
	 */
	@Override
	public RoadLocation getAgentLocation(UUID participantID) {
		final RoadLocation theirLoc = super.getAgentLocation(participantID);
		final RoadLocation myLoc = super.getAgentLocation(myID);

		if ( (getOffsetDistanceBetween((RoadLocation)myLoc, (RoadLocation)theirLoc) <= this.perceptionRange) ||
			 (getOffsetDistanceBetween((RoadLocation)theirLoc, (RoadLocation)myLoc) <= this.perceptionRange) )	{
			return theirLoc;
		} else {
			throw new CannotSeeAgent(this.myID, participantID);
		}
	}

	/**
	 * Get the agents who are visible to me at this time and their
	 * {@link Location}s.
	 * 
	 * FIXME make this more efficient - do it with mod ?
	 * 
	 * @return {@link HashMap} of agent's {@link UUID} to {@link Location}
	 */
	public Map<UUID, RoadLocation> getNearbyAgents() {
		if (this.membersService == null) {
			throw new UnsupportedOperationException();
		} else if (this.getAreaService() != null && this.getAreaService().isCellArea())  {
			final HashMap<UUID, RoadLocation> agents = new HashMap<UUID, RoadLocation>();
			RoadLocation myLoc = (RoadLocation) super.getAgentLocation(myID);
			double range = this.perceptionRange;

			for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
					.getSizeX(), (int) (myLoc.getX() + range)); x++) {
				for (int y = Math.max(0, (int) (myLoc.getY() - range)); y < Math.min(
						getAreaService().getSizeY(), (int) (myLoc.getY() + range)); y++) {
					RoadLocation c = new RoadLocation(x, y);
					//System.err.println(c);
					for (UUID a : getAreaService().getCell(x, y, 0)) {
						agents.put(a, c);
					}
				}
			}
			/*
			 *  Need to wrap perception.
			 *   check for wrapped behind you first, then wrapped infront of you
			 */
			//System.err.println("-");
			int diffBack = (int) (range - myLoc.getY());
			//System.out.println(diffBack);
			if (diffBack > 0) {
				for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
						.getSizeX(), (int) (myLoc.getX() + range)); x++) {
					for (int y = Math.max(0, getAreaService().getSizeY() - diffBack); y < getAreaService().getSizeY(); y++) {
						RoadLocation c = new RoadLocation(x, y);
						//System.err.println(c);
						for (UUID a : getAreaService().getCell(x, y, 0)) {
							agents.put(a, c);
						}
					}
				}
			}
			//System.err.println("-");
			int diffFront = (int) (range - getAreaService().getSizeY() - myLoc.getY());
			//System.out.println(diffFront);
			if (diffFront > 0) {
				for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
						.getSizeX(), (int) (myLoc.getX() + range)); x++) {
					for (int y = 0; y < Math.min(getAreaService().getSizeY(), diffFront); y++) {
						RoadLocation c = new RoadLocation(x, y);
						//System.err.println(c);
						for (UUID a : getAreaService().getCell(x, y, 0)) {
							agents.put(a, c);
						}
					}
				}
			}
			
			agents.remove(this.myID);
			return agents;
		} else {
			throw new UnsupportedOperationException("Not a cell area !");
		}
	}
	
	/** 
	 * @param lane to check 
	 * @return UUID of closest agent to front (or agent alongside), or null if there wasn't one
	 */
	public UUID getAgentToFront(int lane){
		UUID result = null;
		int startLoc = ((RoadLocation) super.getAgentLocation(myID)).getOffset();
		for (int i = 0; i <= this.perceptionRange; i++) {
			if ( !getAreaService().getCell(lane, ((startLoc+i)%this.getAreaService().getSizeY()), 0).isEmpty() ) {
				for (UUID a : getAreaService().getCell(lane, ((startLoc+i)%this.getAreaService().getSizeY()), 0)) {
					if (a!=myID) {
						result = a; // should only be one
					}
				}
				if (result!=null) {
					break;
				}
			}
		}
		return result;
		
	}

	public static ParticipantSharedState createSharedState(UUID id, RoadLocation myLoc) {
		return new ParticipantSharedState("util.location", myLoc, id);
	}
	
	@Override
	public void setAgentLocation(final UUID participantID, final RoadLocation l){
		throw new SharedStateAccessException("setAgentLocation not accessible to agents !");
	}
	
	@Override
	public void setAgentLocation(final UUID participantID, final Location l){
		throw new SharedStateAccessException("setAgentLocation not accessible to agents !");
	}
	
	@Override
	public UUID getLocationContents(final RoadLocation l) {
		throw new SharedStateAccessException("getLocationContents not accessible to agents !");
	}
	
	@Override
	public UUID getLocationContents(final int lane, final int offset) {
		throw new SharedStateAccessException("getLocationContents not accessible to agents !");
	}

}
