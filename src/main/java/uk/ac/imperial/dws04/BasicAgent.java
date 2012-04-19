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
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.Move;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * @author dws04
 * 
 */
public class BasicAgent extends AbstractParticipant {

	Location myLoc;
	
	// Variable to store the location service.
	ParticipantLocationService locationService;
	 
	BasicAgent(UUID id, String name, Location myLoc) {
		super(id, name);
		this.myLoc = myLoc;
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(ParticipantLocationService.createSharedState(getID(), myLoc));
		return ss;
	}
	
	@Override
	public void initialise() {
		super.initialise();
		// get the ParticipantLocationService.
		try {
			this.locationService = getEnvironmentService(ParticipantLocationService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
	}
	
	@Override
	public void execute() {
		myLoc = locationService.getAgentLocation(getID());
	 
		logger.info("My location is: "+ this.myLoc);
		// get current simulation time
		int time = SimTime.get().intValue();
		// check db is available
		if (this.persist != null) {
			// save our location for this timestep
			this.persist.getState(time).setProperty("location", this.myLoc.toString());
		}
	 
		// Create a random Move.
		int dx = Random.randomInt(2) - 1;
		int dy = Random.randomInt(2) - 1;
		Move move = new Move(dx, dy);
	 
		// submit move action to the environment.
		try {
			environment.act(move, getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Error trying to move", e);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.util.participant.AbstractParticipant#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 */
	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub

	}

}
