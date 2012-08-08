/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;


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
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
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
	RoadEnvironmentService globalRoadEnvironmentService;
	
	private int lanes = 3;
	private int length = 10;
	private int maxSpeed = 10;
	private int maxAccel = 1;
	private int maxDecel = 1;
	private int junctionCount = 0;
	IntegerTime time = new IntegerTime(0);
	SimTime sTime = new SimTime(time);

	@Before
	public void setUp() throws Exception {
		injector = Guice.createInjector(
				// rule module
				new RuleModule(),
				new AbstractEnvironmentModule()
					.addActionHandler(LaneMoveHandler.class)
					.addParticipantEnvironmentService(ParticipantLocationService.class)
					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
					.addParticipantEnvironmentService(ParticipantSpeedService.class)
					.addGlobalEnvironmentService(RoadEnvironmentService.class)
					.setStorage(RuleStorage.class),
				Area.Bind.area2D(lanes, length).addEdgeHandler(Edge.Y_MAX,
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
		globalRoadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
		
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
	public void testSingleVehicle() throws Exception {
		setUp();
		int startSpeed = Random.randomInt(maxSpeed)+1;
		int startLane = Random.randomInt(lanes);
		int spacing = 0;
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
	public void testSlowing() throws Exception {
		setUp();
		int startSpeed = 3;
		int startLane = 1;
		int spacing = 0;
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
	public void testLaneChangeUp() throws Exception {
		setUp();
		int startSpeed = 3;
		int startLane = 1;
		int spacing = 0;
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
	public void testLaneChangeDown() throws Exception {
		setUp();
		int startSpeed = 3;
		int startLane = 1;
		int spacing = 0;
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
	public void testSlowingWrap() throws Exception {
		setUp();
		int startSpeed = 3;
		int startLane = 1;
		int spacing = 0;
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
	public void testLaneChangeUpWrap() throws Exception {
		setUp();
		int startSpeed = 3;
		int startLane = 1;
		int spacing = 0;
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
	public void testLaneChangeDownWrap() throws Exception {
		setUp();
		int startSpeed = 3;
		int startLane = 1;
		int spacing = 0;
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
	public void testLaneChangeChoice() throws Exception {
		setUp();
		int aSpeed = 4; int aStart = 0;
		int bSpeed = 3; int bStart = 2;
		int cSpeed = 3; int cStart = 3;
		int aLane = 0;
		int spacing = 0;
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
	
	@Test
	public void testJunctions() throws Exception {
		maxDecel = 2;
		length = 20;
		junctionCount = 3;
		setUp();
		ArrayList<Integer> locations = globalRoadEnvironmentService.getJunctionLocations();
		assertEquals(locations.size(), 3);
		assertTrue(locations.contains(0));
		assertTrue(locations.contains(6));
		assertTrue(locations.contains(13));
		
		int aLane = 0; int aOffset = 0; int aSpeed = 5;
		int bLane = 0; int bOffset = 14; int bSpeed = 5;
		int cLane = 2; int cOffset = 5; int cSpeed = 5;
		
		RoadAgent a = createAgent("a", new RoadLocation(aLane, aOffset), aSpeed, new RoadAgentGoals(aSpeed, 3, 0));
		RoadAgent b = createAgent("b", new RoadLocation(bLane, bOffset), bSpeed, new RoadAgentGoals(bSpeed, null, 0));
		RoadAgent c = createAgent("c", new RoadLocation(cLane, cOffset), cSpeed, new RoadAgentGoals(cSpeed, 1, 0));
		
		incrementTime();

		assertLocation(a, aLane, aOffset); // a has 2 left to PASS, wants to leave at the 3rd
		assertSpeed(a, aSpeed);
		assertLocation(b, bLane, bOffset);
		assertSpeed(b, bSpeed);
		assertLocation(c, cLane, cOffset); // c has 0 left to pass, wants to turn off immediately (but can't)
		assertSpeed(c, cSpeed);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		assertLocation(a, aLane, 5); // a has 2 left to PASS
		assertSpeed(a, aSpeed);
		assertLocation(b, bLane, 19);
		assertSpeed(b, bSpeed);
		assertLocation(c, 1, 8); // c wants to turn off, so changed lanes and slowed
		assertSpeed(c, 3);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		assertLocation(a, aLane, 10); // a has 1 left to pass
		assertSpeed(a, aSpeed);
		assertLocation(b, bLane, 4);
		assertSpeed(b, bSpeed);
		assertLocation(c, 0, 9); // c wants to turn off, so changed lanes and slowed
		assertSpeed(c, 1);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		assertLocation(a, aLane, 15); // a has 0 left to pass, wants to turn off next 
		assertSpeed(a, aSpeed);
		assertLocation(b, bLane, 7); // b saw c moving slowly infront of it, so slowed down
		assertSpeed(b, 3);
		assertLocation(c, 0, 11); // c wants to turn off at 13 but can speed up a bit
		assertSpeed(c, 2);
		
		a.execute();
		b.execute();
		c.execute();
		incrementTime();
		
		assertLocation(a, 0, 15); // a has turned off, so it's location remains, because the simulation is the thing that removes the state
		assertSpeed(a, aSpeed);
		assertLocation(b, bLane, 9); // c is seen at 11 still, so is slowing
		assertSpeed(b, 2);
		assertLocation(c, 0, 11); // c has turned off, but it's location remains here because the removal of state isnt done
		assertSpeed(c, 2);
	}
	
}
