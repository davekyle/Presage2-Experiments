/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.CellMove;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
import uk.ac.imperial.presage2.util.location.area.Area.Edge;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * @author dws04
 *
 */
public class SpeedServiceTest {
	
	Injector injector;
	AbstractEnvironment env;
	//RoadEnvironmentService roadEnvironmentService;
	SpeedService globalSpeedService;
	
	private final int lanes = 3;
	private final int length = 10;
	private final int maxSpeed = 10;
	private final int maxAccel = 1;
	private final int maxDecel = 1;
	private final int junctionCount = 0;

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
	
	class TestAgent extends AbstractParticipant {

		RoadLocation startLoc;
		int startSpeed;
		ParticipantLocationService locationService;
		ParticipantSpeedService speedService;
		Driver driver;

		public TestAgent(UUID id, String name, RoadLocation startLoc, int startSpeed) {
			super(id, name);
			this.startLoc = startLoc;
			this.startSpeed = startSpeed;
		}

		@Override
		protected Set<ParticipantSharedState> getSharedState() {
			Set<ParticipantSharedState> ss = super.getSharedState();
			ss.add(ParticipantLocationService.createSharedState(getID(),startLoc));
			ss.add(ParticipantSpeedService.createSharedState(getID(), startSpeed));
			return ss;
		}

		@Override
		public void initialise() {
			super.initialise();
			try {
				this.locationService = getEnvironmentService(ParticipantLocationService.class);
			} catch (UnavailableServiceException e) {
				logger.warn(e);
			}
			try {
				this.speedService = getEnvironmentService(ParticipantSpeedService.class);
			} catch (UnavailableServiceException e) {
				logger.warn(e);
			}
			try {
				this.driver = new Driver(getID(), this);
			} catch (UnavailableServiceException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void processInput(Input in) {
		}

		public void performAction(Action a) throws ActionHandlingException {
			environment.act(a, getID(), authkey);
		}

		public void assertLocation(int lane, int offset) {
			RoadLocation current = (RoadLocation) this.locationService
					.getAgentLocation(getID());
			assertEquals(lane, current.getLane());
			assertEquals(offset, current.getOffset());
		}
		
		public void assertSpeed(int speed) {
			int current = this.speedService.getAgentSpeed(getID());
			assertEquals(speed, current);
		}
		
		
	}

	private TestAgent createTestAgent(String name, RoadLocation startLoc, int startSpeed) {
		TestAgent a = new TestAgent(Random.randomUUID(), name, startLoc, startSpeed);
		injector.injectMembers(a);
		a.initialise();
		return a;
	}
	
	public void assertGlobalSpeed(int expected, UUID agentID) {
		assertEquals(expected, globalSpeedService.getAgentSpeed(agentID));
	}
	
	@Test
	public void testSpeedCheck() throws ActionHandlingException {
		int startSpeed = Random.randomInt(maxSpeed-1)+1;
		int startLane = 2;
		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);

		env.incrementTime();

		// make some valid moves
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		assertGlobalSpeed(startSpeed, a.getID());
		
		System.out.println("Speed:" + startSpeed + " / move:" + a.driver.accelerate());
		a.performAction(a.driver.accelerate());
		env.incrementTime();
		a.assertSpeed(startSpeed+1);
		assertGlobalSpeed(startSpeed+1, a.getID());
		
		a.performAction(a.driver.decelerate());
		env.incrementTime();
		a.assertSpeed(startSpeed);
		assertGlobalSpeed(startSpeed, a.getID());
		
		a.performAction(a.driver.changeLaneLeft());
		env.incrementTime();
		a.assertSpeed(startSpeed);
		assertGlobalSpeed(startSpeed, a.getID());
		
		a.performAction(a.driver.constantSpeed());
		env.incrementTime();
		a.assertSpeed(startSpeed);
		assertGlobalSpeed(startSpeed, a.getID());

		globalSpeedService.setAgentSpeed(a.getID(), maxSpeed);
		// check it doesn't update immediately
		assertTrue(maxSpeed!=globalSpeedService.getAgentSpeed(a.getID()));
		assertTrue(maxSpeed!=a.speedService.getAgentSpeed(a.getID()));
		// check it does update in the next cycle
		env.incrementTime();
		assertGlobalSpeed(maxSpeed, a.getID());	
		a.assertSpeed(maxSpeed);
		
		// Check the participant can't change it
		try {
			a.speedService.setAgentSpeed(a.getID(), maxSpeed-1);
			fail();
		} catch (SharedStateAccessException e) {
			
		}
	}
	
	@Test
	public void testStoppingDistance() throws ActionHandlingException {
		int startSpeed = 5;
		int startLane = Random.randomInt(lanes);
		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);

		env.incrementTime();
		a.assertLocation(startLane, 0);
		a.assertSpeed(5);
		assertGlobalSpeed(startSpeed, a.getID());
		assertTrue(this.maxDecel == 1);
		
		// check stopping distance is correct.
		assertEquals(15, globalSpeedService.getStoppingDistance(a.getID()));
		assertEquals(20, globalSpeedService.getConservativeStoppingDistance(5));
		
		// slow down so we can check it with another value
		a.performAction(a.driver.decelerate());
		env.incrementTime();
		a.performAction(a.driver.decelerate());
		env.incrementTime();
		a.assertSpeed(3);
		assertGlobalSpeed(3, a.getID());
		assertEquals(6, globalSpeedService.getStoppingDistance(a.getID()));
		assertEquals(9, globalSpeedService.getConservativeStoppingDistance(3));
	}
	
	@Test
	public void testSpeedToStopIn() {
		/*
		 * Distance:	|0|9|8|7|6|5|4|3|2|1|0|
		 * Markers:		  4         3    2  1 0
		 * Speed:		|4|3|3|3|3|2|2|2|1|1|0|
		 */
		assertEquals(0,globalSpeedService.getSpeedToStopInDistance(0));
		assertEquals(1,globalSpeedService.getSpeedToStopInDistance(1));
		assertEquals(1,globalSpeedService.getSpeedToStopInDistance(2));
		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(3));
		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(4));
		assertEquals(2,globalSpeedService.getSpeedToStopInDistance(5));
		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(6));
		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(7));
		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(8));
		assertEquals(3,globalSpeedService.getSpeedToStopInDistance(9));
		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(10));
		assertEquals(4,globalSpeedService.getSpeedToStopInDistance(11));
	}
	
	@Test
	public void testGetters() throws ActionHandlingException {
		int startSpeed = Random.randomInt(maxSpeed);
		int startLane = Random.randomInt(lanes);
		RoadLocation startLocation = new RoadLocation(startLane, 0);
		TestAgent a = createTestAgent("a", startLocation, startSpeed);

		env.incrementTime();

		// Check that everything returns the same results
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		
		assertEquals(maxAccel, a.driver.getMaxAccel());
		assertEquals(maxDecel, a.driver.getMaxDecel());
		assertEquals(maxSpeed, globalSpeedService.getMaxSpeed());
		assertEquals(maxAccel, globalSpeedService.getMaxAccel());
		assertEquals(maxDecel, globalSpeedService.getMaxDecel());
		assertEquals(maxSpeed, a.speedService.getMaxSpeed());
		assertEquals(maxAccel, a.speedService.getMaxAccel());
		assertEquals(maxDecel, a.speedService.getMaxDecel());
		
	}
}
