/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.dws04.Presage2Experiments.Driver;
import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
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
public class DriverTest {
	
	Injector injector;
	AbstractEnvironment env;
	RoadEnvironmentService roadEnvironmentService;
	
	private final int lanes = 3;
	private final int length = 10;
	private final int maxSpeed = 10;
	private final int maxAccel = 1;
	private final int maxDecel = 1;
	private final int junctionCount = 2;

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
		roadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
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
	
	public static void assertMoveEquals(CellMove expected, CellMove actual) {
		assertEquals(expected.getXInt(), actual.getXInt());
		assertEquals(expected.getYInt(), actual.getYInt());
		assertEquals(expected.getZInt(), actual.getZInt());
	}
	
	public static void assertMoveEquals(int x, int y, CellMove actual) {
		assertEquals(x, actual.getXInt());
		assertEquals(y, actual.getYInt());
		assertEquals(0, actual.getZInt());
	}
	
	@Test
	public void testAccelerate() throws ActionHandlingException {
		int startSpeed = Random.randomInt();
		int startLane = Random.randomInt(lanes);
		int n = Random.randomInt();
		CellMove move;
		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);

		env.incrementTime();

		// make some moves - doesn't matter if theyre valid or not because we're not going to execute
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		
		// acceleration moves
		move = a.driver.accelerate();
		assertMoveEquals(0, startSpeed+1, move);
		
		move = a.driver.accelerate(n);
		assertMoveEquals(0, startSpeed+n, move);
		
		move = a.driver.accelerateMax();
		assertMoveEquals(0, startSpeed+maxAccel, move);
	}
	
	@Test
	public void testDecelerate() throws ActionHandlingException {
		int startSpeed = Random.randomInt();
		int startLane = Random.randomInt(lanes);
		int n = Random.randomInt();
		CellMove move;
		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);

		env.incrementTime();

		// make some moves - doesn't matter if theyre valid or not because we're not going to execute
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		
		
		// deceleration moves
		move = a.driver.decelerate();
		assertMoveEquals(0, startSpeed-1, move);
		
		move = a.driver.decelerate(n);
		assertMoveEquals(0, startSpeed-n, move);
		
		move = a.driver.decelerateMax();
		assertMoveEquals(0, startSpeed-maxDecel, move);
	}
	
	@Test
	public void testLaneChange() throws ActionHandlingException {
		int startSpeed = Random.randomInt();
		int startLane = Random.randomInt(lanes);
		int n = Random.randomInt();
		CellMove move;
		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);

		env.incrementTime();

		// make some moves - doesn't matter if theyre valid or not because we're not going to execute
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		
		
		// lane change moves
		move = a.driver.changeLaneLeft();
		assertMoveEquals(-1, startSpeed, move);
		
		move = a.driver.changeLaneRight();
		assertMoveEquals(1, startSpeed, move);
	}
	
	@Test
	public void testOtherMoves() throws ActionHandlingException {
		int startSpeed = Random.randomInt();
		int startLane = Random.randomInt(lanes);
		int n = Random.randomInt();
		CellMove move;
		TestAgent a = createTestAgent("a", new RoadLocation(startLane, 0), startSpeed);

		env.incrementTime();

		// make some moves - doesn't matter if theyre valid or not because we're not going to execute
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		
		
		// other moves
		move = a.driver.constantSpeed();
		assertMoveEquals(0, startSpeed, move);
		
		move = a.driver.moveAt(n);
		assertMoveEquals(0, n, move);
		
		move = a.driver.random();
		assertTrue(move instanceof CellMove);
		
		// TODO Not written yet
		move = a.driver.randomValid();
		assertTrue(move instanceof CellMove);
	}
	
	@Test
	public void testGetters() throws ActionHandlingException {
		int startSpeed = Random.randomInt();
		int startLane = Random.randomInt(lanes);
		int n = Random.randomInt();
		CellMove move;
		RoadLocation startLocation = new RoadLocation(startLane, 0);
		TestAgent a = createTestAgent("a", startLocation, startSpeed);

		env.incrementTime();

		// Check that everything returns the same results
		a.assertLocation(startLane, 0);
		a.assertSpeed(startSpeed);
		
		assertTrue(startLocation.equals(a.driver.getLocation()));
		assertEquals(startSpeed, a.driver.getSpeed());
		assertEquals(maxAccel, a.driver.getMaxAccel());
		assertEquals(maxDecel, a.driver.getMaxDecel());
	}
	
	@Test
	public void testTurnOff() throws ActionHandlingException {
		int startSpeed = Random.randomInt();
		int startLane = Random.randomInt(lanes);
		int startLoc = 4;
		CellMove move;
		RoadLocation startLocation = new RoadLocation(startLane, startLoc);
		TestAgent a = createTestAgent("a", startLocation, startSpeed);
		TestAgent b = createTestAgent("b", new RoadLocation(0,6), startSpeed);
		TestAgent c = createTestAgent("c", new RoadLocation(0,0), startSpeed);
		

		env.incrementTime();

		// Check that everything returns the same results
		a.assertLocation(startLane, startLoc);
		a.assertSpeed(startSpeed);
		//check that junctions are where we expect
		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(0));
		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(5));
		
		// turn off moves
		move = a.driver.turnOff();
		assertMoveEquals((0-(startLane+1)), 1, move);
		move = b.driver.turnOff();
		// wraps correctly and doesn't look backwards
		assertMoveEquals(-1, 4, move);
		// won't try to turn off with no forward motion
		move = c.driver.turnOff();
		assertMoveEquals(-1, 5, move);
	}
}
