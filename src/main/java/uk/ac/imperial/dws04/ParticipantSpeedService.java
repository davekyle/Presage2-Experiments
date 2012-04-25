/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.participant.Participant;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.location.CannotSeeAgent;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.HasPerceptionRange;

/**
 * Modified from {@link ParticipantLocationService} by Sam MacBeth
 * 
 * @author dws04
 *
 */
@ServiceDependencies({ EnvironmentMembersService.class, ParticipantRoadLocationService.class })
public class ParticipantSpeedService extends SpeedService {
	
	protected final UUID myID;
	private final Logger logger = Logger.getLogger(ParticipantSpeedService.class);
	protected final HasPerceptionRange rangeProvider;
	protected final EnvironmentMembersService membersService;
	protected final ParticipantRoadLocationService locationService;

	/*
	 * This has to only have the 3 args; you can pull everything else from the agent through the serviceProvider
	 */
	public ParticipantSpeedService(Participant p, EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider) {
		super(sharedState, serviceProvider);
		this.myID = p.getID();
		if (p instanceof HasPerceptionRange) {
			this.rangeProvider = (HasPerceptionRange) p;
		} else {
			this.rangeProvider = null;
			if (this.logger.isDebugEnabled()) {
				this.logger
						.debug("ParticipantSpeedService created with no perception range. This agent is all seeing!");
			}
		}
		this.membersService = getMembersService(serviceProvider);
		this.locationService = getLocationService(serviceProvider);
	}
	
	/**
	 * @param serviceProvider
	 * @return
	 */
	private EnvironmentMembersService getMembersService(EnvironmentServiceProvider serviceProvider) {
		try {
			return serviceProvider.getEnvironmentService(EnvironmentMembersService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Could not retrieve EnvironmentMembersService; functionality limited.");
			return null;
		}
	}
	
	/**
	 * @param serviceProvider
	 * @return
	 */
	private ParticipantRoadLocationService getLocationService(EnvironmentServiceProvider serviceProvider) {
		try {
			return serviceProvider.getEnvironmentService(ParticipantRoadLocationService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Could not retrieve ParticipantRoadLocationService; functionality limited.");
			return null;
		}
	}
	
	/**
	 * Gets the speed of the agent.
	 * FIXME: checking this way is a bit of a hack
	 */
	@Override
	public int getAgentSpeed(UUID participantID) {
		if (this.rangeProvider == null) {
			return super.getAgentSpeed(participantID);
		} else {
			// locationService wont have been loaded when class is created, because services are still being generated
			if (this.locationService == null) { 
				this.getLocationService(serviceProvider);
			}
			try { // try to observe them; if you can then they're close enough.
				final Location theirLoc = locationService.getAgentLocation(participantID);
			} catch (CannotSeeAgent e) {
				throw e;
			}
			return super.getAgentSpeed(participantID);
		}
	}

	/**
	 * Not available for Participant use!
	 */
	@Override
	public void setAgentSpeed(UUID participantID, int s) {
		throw new SharedStateAccessException(
				"A participant may not modify other participant's speeds!");
	}

	/**
	 * Get the agents who are visible to me at this time and their
	 * {@link RoadSpeed}s.
	 * 
	 * @return {@link HashMap} of agent's {@link UUID} to {@link RoadSpeed}
	 */
	public Map<UUID, Integer> getNearbyAgents() {
		// locationService wont have been loaded when class is created, because services are still being generated
		if (this.locationService == null) { 
			this.getLocationService(serviceProvider);
		}
		if ((this.membersService == null)||(this.locationService == null)) {
			throw new UnsupportedOperationException();
		} else {
			final Map<UUID, Integer> agents = new HashMap<UUID, Integer>();
			for (UUID id : locationService.getNearbyAgents().keySet()) {
				agents.put(id, super.getAgentSpeed(id));
			}
			agents.remove(this.myID);
			return agents;
		}
	}

	/**
	 * Create the {@link ParticipantSharedState} required for this service.
	 * 
	 * @param pid
	 *            {@link UUID} of the participant to create sharedstate object
	 *            for.
	 * @param s
	 *            {@link RoadSpeed} initial speed for this participant.
	 * @return {@link ParticipantSharedState} on the type that this service
	 *         uses.
	 */
	public static ParticipantSharedState createSharedState(UUID pid, int s) {
		return new ParticipantSharedState("util.speed", s, pid);
	}

}
