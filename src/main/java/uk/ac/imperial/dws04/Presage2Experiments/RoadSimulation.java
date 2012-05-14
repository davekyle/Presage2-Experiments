/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.participant.Participant;
import uk.ac.imperial.presage2.core.plugin.PluginModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.location.LocationService;
import uk.ac.imperial.presage2.util.location.LocationStoragePlugin;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
import uk.ac.imperial.presage2.util.location.area.Area.Edge;
import uk.ac.imperial.presage2.util.network.NetworkModule;

/**
 * run with uk.ac.imperial.dws04.Presage2Experiments.Presage2Experiments.RoadSimulation finishTime=10 length=10 lanes=4 initialAgents=2 maxSpeed=3 maxAccel=1 maxDecel=1 junctionCount=0
 * CLI add -classname uk.ac.imperial.dws04.Presage2Experiments.Presage2Experiments.RoadSimulation -finish 10 -name RoadSim  -P length=10 -P lanes=4 -P initialAgents=2 -P maxSpeed=3 -P maxAccel=1 -P maxDecel=1 -P junctionCount=0
 * 
 * @author dws04
 *
 */
@Singleton
public class RoadSimulation extends InjectedSimulation {

	@Parameter(name="length")
	public int length;
	
	/**
	 * We expect this to be 4(or more): lane0 is the "onramp" with lane1+ being the actual lanes.
	 */
	@Parameter(name="lanes")
	public int lanes;
	 
	@Parameter(name="initialAgents")
	public int initialAgents;
	 
	@Parameter(name="maxSpeed")
	public int maxSpeed;
	 
	@Parameter(name="maxAccel")
	public int maxAccel;
	 
	@Parameter(name="maxDecel")
	public int maxDecel;
	 
	@Parameter(name="junctionCount")
	public int junctionCount;
	
	HashMap<UUID, String> agentNames;
	HashMap<UUID,RoadLocation> agentLocations;

	EnvironmentServiceProvider serviceProvider;
	
	/**
	 * @param modules
	 */
	public RoadSimulation(Set<AbstractModule> modules) {
		super(modules);
		agentLocations = new HashMap<UUID, RoadLocation>();
		agentNames = new HashMap<UUID, String>();
	}
	
	@Inject
	void setEnvironmentServiceProvider(EnvironmentServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	
	RoadLocationService getLocationService() {
		try {
			return this.serviceProvider.getEnvironmentService(RoadLocationService.class);
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	SpeedService getSpeedService() {
		try {
			return this.serviceProvider.getEnvironmentService(SpeedService.class);
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	RoadEnvironmentService getEnvironmentService() {
		try {
			return this.serviceProvider.getEnvironmentService(RoadEnvironmentService.class);
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param serviceProvider
	 * @return
	 * @throws UnavailableServiceException 
	 */
	private EnvironmentMembersService getMembersService() {
		try {
			return this.serviceProvider.getEnvironmentService(EnvironmentMembersService.class);
		} catch (UnavailableServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.core.simulator.InjectedSimulation#addToScenario(uk.ac.imperial.presage2.core.simulator.Scenario)
	 */
	@Override
	protected void addToScenario(Scenario s) {
		for (int i = 0; i < initialAgents; i++) {
			int initialX = Random.randomInt(lanes);
			int initialY = Random.randomInt(length);
			UUID uuid = Random.randomUUID();
			String name = "agent"+ i;
			RoadLocation startLoc = new RoadLocation(initialX, initialY);
			while (agentLocations.containsValue(startLoc)) {
				// keep making random numbers until you have a free spot
				initialX = Random.randomInt(lanes);
				initialY = Random.randomInt(length);
				startLoc = new RoadLocation(initialX, initialY);
				logger.debug("Looping...");
			}
			// don't want speeds to be 0
			int startSpeed = Random.randomInt(maxSpeed)+1;
			RoadAgentGoals goals = new RoadAgentGoals((Random.randomInt(maxSpeed)+1), 0, 0);
			s.addParticipant(new RoadAgent(uuid, name, startLoc, startSpeed, goals));
			agentLocations.put(uuid, startLoc);
			agentNames.put(uuid, name);
			
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.core.simulator.InjectedSimulation#getModules()
	 */
	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
	 
		// 2D area that wraps at the top
		modules.add(Area.Bind.area2D(lanes, length).addEdgeHander(Edge.Y_MAX, WrapEdgeHandler.class));
		// Environment with MoveHandler and ParticipantLocationService
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(LaneMoveHandler.class)
			.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
			.addParticipantEnvironmentService(ParticipantSpeedService.class)
			//.addGlobalEnvironmentService(RoadLocationService.class)
			.addGlobalEnvironmentService(RoadEnvironmentService.class));
		// No network
		modules.add(NetworkModule.noNetworkModule());
		// Location plugin
		// TODO need to modify the plugin
		modules.add(new PluginModule().addPlugin(LocationStoragePlugin.class));
		return modules;
	}
	
	/**
	 * Allows new agents to be inserted at the onramps at the end of a timecycle based on a function
	 * @param e
	 * @return
	 */
	@EventListener
	public int makeNewAgent(EndOfTimeCycle e) {
		logger.debug("Detected an EndOfTimeCycle event so seeing if we should insert an agent");
		if (Random.randomInt()%2!=0) {
			Integer junctionOffset = this.getEnvironmentService().getNextInsertionJunction();
			if (junctionOffset!=null) {
				createNextAgent(0, junctionOffset);
			}
		}
		return 0;
	}

	/**
	 * Creates the next (in naming) agent at the location specified with speed 0 and random goals
	 * @param lane
	 * @param startOffset
	 */
	private void createNextAgent(int lane, int startOffset) {
		UUID uuid = Random.randomUUID();
		String name = "agent"+ agentNames.size();
		RoadLocation startLoc = new RoadLocation(lane, startOffset);
		int startSpeed = 0;
		RoadAgentGoals goals = new RoadAgentGoals((Random.randomInt(maxSpeed)+1), 0, 0);
		Participant p = new RoadAgent(uuid, name, startLoc, startSpeed, goals);
		this.scenario.addParticipant(p);
		p.initialise();
		logger.info("Inserting " + name + " [" + uuid + "] at " + startLoc);
		agentNames.put(uuid, name);
		this.agentLocations.put(uuid, startLoc);
	}
	
	@EventListener
	public void updateAgentLocations(EndOfTimeCycle e) {
		for (UUID a : getMembersService().getParticipants()) {
			this.agentLocations.remove(a);
			logger.trace("Updating location of agent " + a + " from " + this.agentLocations.get(a) + " to " + (RoadLocation) getLocationService().getAgentLocation(a));
			this.agentLocations.put(a, (RoadLocation) getLocationService().getAgentLocation(a));
		}
	}

}
