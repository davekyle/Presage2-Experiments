/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import static org.junit.Assert.assertEquals;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent;
import uk.ac.imperial.dws04.Presage2Experiments.RoadAgentGoals;
import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
import uk.ac.imperial.dws04.Presage2Experiments.SpeedService;
import uk.ac.imperial.presage2.core.IntegerTime;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
import uk.ac.imperial.presage2.util.location.area.Area.Edge;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * @author dws04
 *
 */
public class RoadAgentTest {

	
	Injector injector;
	AbstractEnvironment env;
	//RoadEnvironmentService roadEnvironmentService;
	SpeedService globalSpeedService;
	
	private int lanes = 3;
	private int length = 10;
	private int maxSpeed = 10;
	private int maxAccel = 1;
	private int maxDecel = 1;
	private final int junctionCount = 0;
	IntegerTime time = new IntegerTime(0);
	SimTime sTime = new SimTime(time);

	@Before
	public void setUp() throws Exception {
		injector = Guice.createInjector(
				new AbstractEnvironmentModule()
					.addActionHandler(LaneMoveHandler.class)
					.addParticipantEnvironmentService(ParticipantLocationService.class)
					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
					.addParticipantEnvironmentService(ParticipantSpeedService.class)
					.addGlobalEnvironmentService(RoadEnvironmentService.class),
				Area.Bind.area2D(lanes, length).addEdgeHander(Edge.Y_MAX,
						WrapEdgeHandler.class), new EventBusModule(),
				new AbstractModule() {
					// add in params that are required
					@Override
					protected void configure() {
						bind(Integer.TYPE).annotatedWith(Names.named("params.maxSpeed")).toInstance(maxSpeed);
						bind(Integer.TYPE).annotatedWith(Names.named("params.maxAccel")).toInstance(maxAccel);
						bind(Integer.TYPE).annotatedWith(Names.named("params.maxDecel")).toInstance(maxDecel);
						bind(Integer.TYPE).annotatedWith(Names.named("params.junctionCount")).toInstance(junctionCount);
						bind(Integer.TYPE).annotatedWith(Names.named("params.lanes")).toInstance(lanes);
						bind(Integer.TYPE).annotatedWith(Names.named("params.length")).toInstance(length);
					}
				});

		env = injector.getInstance(AbstractEnvironment.class);
		globalSpeedService = injector.getInstance(SpeedService.class);
		
	}
	
	

	@After
	public void tearDown() throws Exception {
	}
	
	private RoadAgent createAgent(String name, RoadLocation startLoc, int startSpeed, RoadAgentGoals goals) {
		RoadAgent a = new RoadAgent(Random.randomUUID(), name, startLoc, startSpeed, goals);
		injector.injectMembers(a);
		a.initialise();
		return a;
	}
	
	public static void assertLocation(RoadAgent a, RoadLocation expected) {
		assertEquals(expected.getLane(), ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getLane());
		assertEquals(expected.getOffset(), ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getOffset());
		assertEquals(expected.getLane(), a.myLoc.getLane());
		assertEquals(expected.getOffset(), a.myLoc.getOffset());
		// the agent only updates at the start of an execute() cycle.
		//assertEquals(expected, ((RoadLocation) a.locationService.getAgentLocation(a.getID())) );
		//assertEquals(expected, a.myLoc);
	}
	
	public static void assertLocation(RoadAgent a, int expectedLane, int expectedOffset) {
		assertEquals(expectedLane, ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getLane());
		assertEquals(expectedOffset, ((RoadLocation) a.locationService.getAgentLocation(a.getID())).getOffset());
		//assertEquals(expectedLane, a.myLoc.getLane());
		//assertEquals(expectedOffset, a.myLoc.getOffset());
	}
	
	public static void assertSpeed(RoadAgent a, int expected){
		assertEquals(expected, a.speedService.getAgentSpeed(a.getID()));
		//assertEquals(expected, a.mySpeed);
	}
	
	public void incrementTime(){
		time.increment();
		env.incrementTime();
	}
	
	@Test
	public void testSingleVehicle() throws ActionHandlingException {
		int startSpeed = Random.randomInt(maxSpeed)+1;
		int startLane = Random.randomInt(lanes);
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 0), startSpeed, goals);

		incrementTime();

		assertLocation(a, startLane, 0);
		assertSpeed(a, startSpeed);
		
		a.execute();
		incrementTime();
		
		assertLocation(a,startLane, startSpeed%length);
		assertSpeed(a, startSpeed);
		
		a.execute();
		incrementTime();
		
		assertLocation(a,startLane, (startSpeed*2)%length);
		assertSpeed(a, startSpeed);
		
	}
	
	@Test
	public void testSlowing() throws ActionHandlingException {
		int startSpeed = 3;
		int startLane = 1;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 0), startSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(startLane, 5), 0, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, startLane, 0);
		assertSpeed(a, startSpeed);
		assertLocation(b, startLane, 5);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 2);
		assertSpeed(a, 2);
		assertLocation(b, startLane, 5);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 3);
		assertSpeed(a, 1);
		assertLocation(b, startLane, 5);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 4);
		assertSpeed(a, 1);
		assertLocation(b, startLane, 5);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 4);
		assertSpeed(a, 0);
		assertLocation(b, startLane, 5);
		assertSpeed(b, 0);
		
	}
	
	@Test
	public void testLaneChangeUp() throws ActionHandlingException {
		int startSpeed = 3;
		int startLane = 1;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 0), startSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(startLane, 2), 0, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, startLane, 0);
		assertSpeed(a, startSpeed);
		assertLocation(b, startLane, 2);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		// a can't slow in time so will "overtake"
		assertLocation(a, 2, 3);
		assertSpeed(a, 3);
		assertLocation(b, startLane, 2);
		assertSpeed(b, 0);
	}
	
	@Test
	public void testLaneChangeDown() throws ActionHandlingException {
		int startSpeed = 3;
		int startLane = 1;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 0), startSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(startLane, 2), 0, new RoadAgentGoals(0, 0, 0));
		RoadAgent c = createAgent("c", new RoadLocation(startLane+1, 2), 0, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, startLane, 0);
		assertSpeed(a, startSpeed);
		assertLocation(b, startLane, 2);
		assertSpeed(b, 0);
		assertLocation(c, startLane+1, 2);
		assertSpeed(c, 0);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		// a can't slow in time and c prevents "overtaking" so will "undertake"
		assertLocation(a, 0, 3);
		assertSpeed(a, 3);
		assertLocation(b, startLane, 2);
		assertSpeed(b, 0);
		assertLocation(c, startLane+1, 2);
		assertSpeed(c, 0);
	}
	
	@Test
	public void testSlowingWrap() throws ActionHandlingException {
		int startSpeed = 3;
		int startLane = 1;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 5), startSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(startLane, 0), 0, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, startLane, 5);
		assertSpeed(a, startSpeed);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 7);
		assertSpeed(a, 2);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 8);
		assertSpeed(a, 1);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 9);
		assertSpeed(a, 1);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		assertLocation(a,startLane, 9);
		assertSpeed(a, 0);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		
	}
	
	@Test
	public void testLaneChangeUpWrap() throws ActionHandlingException {
		int startSpeed = 3;
		int startLane = 1;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 9), startSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(startLane, 0), 0, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, startLane, 9);
		assertSpeed(a, startSpeed);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		
		a.execute();
		b.execute();
		incrementTime();
		
		// a can't slow in time so will "overtake"
		assertLocation(a, 2, 2);
		assertSpeed(a, 3);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
	}
	
	@Test
	public void testLaneChangeDownWrap() throws ActionHandlingException {
		int startSpeed = 3;
		int startLane = 1;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(startSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(startLane, 9), startSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(startLane, 0), 0, new RoadAgentGoals(0, 0, 0));
		RoadAgent c = createAgent("c", new RoadLocation(startLane+1, 0), 0, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, startLane, 9);
		assertSpeed(a, startSpeed);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		assertLocation(c, startLane+1, 0);
		assertSpeed(c, 0);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		// a can't slow in time and cannot "overtake" so will "undertake"
		assertLocation(a, startLane-1, 2);
		assertSpeed(a, 3);
		assertLocation(b, startLane, 0);
		assertSpeed(b, 0);
		assertLocation(c, startLane+1, 0);
		assertSpeed(c, 0);
	}
	
	@Test
	public void testLaneChangeChoice() throws ActionHandlingException {
		int aSpeed = 4; int aStart = 0;
		int bSpeed = 3; int bStart = 2;
		int cSpeed = 3; int cStart = 3;
		int aLane = 0;
		int spacing = Random.randomInt(maxDecel);
		RoadAgentGoals goals = new RoadAgentGoals(aSpeed, 0, spacing);
		RoadAgent a = createAgent("a", new RoadLocation(aLane, aStart), aSpeed, goals);
		RoadAgent b = createAgent("b", new RoadLocation(aLane, bStart), bSpeed, new RoadAgentGoals(0, 0, 0));
		RoadAgent c = createAgent("c", new RoadLocation(aLane+1, cStart), cSpeed, new RoadAgentGoals(0, 0, 0));

		incrementTime();

		assertLocation(a, aLane, aStart);
		assertSpeed(a, aSpeed);
		assertLocation(b, aLane, bStart);
		assertSpeed(b, bSpeed);
		assertLocation(c, aLane+1, cStart);
		assertSpeed(c, cSpeed);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		//assertLocation(a, aLane, (aStart+aSpeed-1)%length);
		//assertSpeed(a, aSpeed-1);
		assertLocation(b, aLane, (bStart+bSpeed-1)%length);
		assertSpeed(b, bSpeed-1);
		assertLocation(c, aLane+1, (cStart+cSpeed-1)%length);
		assertSpeed(c, cSpeed-1);
		// a can't slow in time, can't undertake, but c is better than b so will "overtake" with hope of something better in future
		assertLocation(a, aLane+1, (aStart+aSpeed-1)%length);
		assertSpeed(a, aSpeed-1);
	}
	
}
