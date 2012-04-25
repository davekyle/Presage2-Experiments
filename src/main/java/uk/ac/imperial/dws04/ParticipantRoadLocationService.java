/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;


import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.participant.Participant;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;

/**
 * @author dws04
 *
 */
public class ParticipantRoadLocationService extends ParticipantLocationService {

	private final double perceptionRange;
	
	public ParticipantRoadLocationService(
			Participant p,
			EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider) {
		super(p, sharedState, serviceProvider);
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
		double n = (mS / mD);
		return ((((n+1)*n)/2)*mD) + mS;
	}

	/**
	 * Get the agents who are visible to me at this time and their
	 * {@link Location}s.
	 * 
	 * TODO distance you can see infront of you is likely to be >> than width of road. Want to be able to see all lanes for the distance you can see.
	 * TODO Does this need to check that the Area has a wrap edge on the MAX_Y or can we assume ?
	 * FIXME make this more efficient - do it with mod ?
	 * 
	 * @return {@link HashMap} of agent's {@link UUID} to {@link Location}
	 */
	@Override
	public Map<UUID, Location> getNearbyAgents() {
		if (this.membersService == null) {
			throw new UnsupportedOperationException();
		} else if (this.getAreaService() != null && this.getAreaService().isCellArea())  {
			final Map<UUID, Location> agents = new HashMap<UUID, Location>();
			Cell myLoc = (Cell) super.getAgentLocation(myID);
			double range = this.perceptionRange;

			for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
					.getSizeX(), (int) (myLoc.getX() + range)); x++) {
				for (int y = Math.max(0, (int) (myLoc.getY() - range)); y < Math.min(
						getAreaService().getSizeY(), (int) (myLoc.getY() + range)); y++) {
					Cell c = new Cell(x, y, 0);
					for (UUID a : getAreaService().getCell(x, y, 0)) {
						agents.put(a, c);
					}
				}
			}
			/*
			 *  Need to wrap perception.
			 *   myLoc.getY()+range > getAreaService().getSizeY()
			 *   getAreaService().getMaxY() - myLoc.getY() > range
			 *   { diff = getAreaService().getSizeY() - myLoc.getY(); if (diff>range) then also check...
			 *   for (int y = 0; y < diff; y++) 
			 */
			int diff = (int) (getAreaService().getSizeY() - myLoc.getY());
			if (diff > range) {
				for (int x = Math.max(0, (int) (myLoc.getX() - range)); x < Math.min(getAreaService()
						.getSizeX(), (int) (myLoc.getX() + range)); x++) {
					for (int y = 0; y < diff; y++) {
							Cell c = new Cell(x, y, 0);
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
	 * @return Entry containing UUID and Location of closest agent to front
	 */
	public Entry<UUID,Location> getAgentToFront(int lane){
		Entry <UUID,Location> result = null;
		for (Entry<UUID, Location> entry : getNearbyAgents().entrySet()) {
			if (((RoadLocation)(entry.getValue())).getLane() == lane) {
				if ((result==null) || ((((RoadLocation)(entry.getValue())).getOffset()) < (((RoadLocation)(result.getValue())).getOffset())) ) {
					result = entry;
				}
			}
		}
		return result;
	}

}
