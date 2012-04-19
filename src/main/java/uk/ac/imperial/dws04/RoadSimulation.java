/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.AbstractModule;

import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.MoveHandler;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.network.NetworkModule;

/**
 * @author dws04
 *
 */
public class RoadSimulation extends InjectedSimulation {

	@Parameter(name="length")
	public int length;
	
	@Parameter(name="lanes")
	public int lanes;
	 
	@Parameter(name="agents")
	public int agents;
	 
	@Parameter(name="maxSpeed")
	public int maxSpeed;
	 
	@Parameter(name="maxAccel")
	public int maxAccel;
	 
	@Parameter(name="maxDeccel")
	public int maxDeccel;
	 
	@Parameter(name="junctionCount")
	public int junctionCount;
	
	/**
	 * @param modules
	 */
	public RoadSimulation(Set<AbstractModule> modules) {
		super(modules);
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.core.simulator.InjectedSimulation#addToScenario(uk.ac.imperial.presage2.core.simulator.Scenario)
	 */
	@Override
	protected void addToScenario(Scenario s) {
		for (int i = 0; i < agents; i++) {
			int initialX = Random.randomInt(lanes);
			int initialY = Random.randomInt(length);
			Location startLoc = new Location(initialX, initialY);
			s.addParticipant(new BasicAgent(Random.randomUUID(), "agent"+ i, startLoc));
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.core.simulator.InjectedSimulation#getModules()
	 */
	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
	 
		// 2D area
		modules.add(Area.Bind.area2D(lanes, length));
		// Environment with MoveHandler and ParticipantLocationService
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(MoveHandler.class)
			.addParticipantEnvironmentService(ParticipantLocationService.class));
		// No network
		modules.add(NetworkModule.noNetworkModule());
	 
		return modules;
	}

}
