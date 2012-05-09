/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;

import uk.ac.imperial.dws04.SpeedServiceTest.TestAgent;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.CannotSeeAgent;
import uk.ac.imperial.presage2.util.location.Location;
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
public class ParticipantRoadLocationServiceTest {
	Injector injector;
	AbstractEnvironment env;
	//RoadEnvironmentService roadEnvironmentService;
	
	private int lanes = 3;
	private int length = 10;
	private int maxSpeed = 10;
	private int maxAccel = 1;
	private int maxDecel = 1;
	private final int junctionCount = 0;

	// can't use this globally anymore since we want to be able to alter the params
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
	}

	@After
	public void tearDown() throws Exception {
	}
	
	class TestAgent extends AbstractParticipant {

		RoadLocation startLoc;
		int startSpeed;
		ParticipantRoadLocationService locationService;
		Driver driver;

		public TestAgent(UUID id, String name, RoadLocation startLoc, int startSpeed) {
			super(id, name);
			this.startLoc = startLoc;
			this.startSpeed = startSpeed;
		}

		@Override
		protected Set<ParticipantSharedState> getSharedState() {
			Set<ParticipantSharedState> ss = super.getSharedState();
			ss.add(ParticipantRoadLocationService.createSharedState(getID(),startLoc));
			ss.add(ParticipantSpeedService.createSharedState(getID(), startSpeed));
			return ss;
		}

		@Override
		public void initialise() {
			super.initialise();
			try {
				this.locationService = getEnvironmentService(ParticipantRoadLocationService.class);
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
		
		
	}

	private TestAgent createTestAgent(String name, RoadLocation startLoc, int startSpeed) {
		TestAgent a = new TestAgent(Random.randomUUID(), name, startLoc, startSpeed);
		injector.injectMembers(a);
		a.initialise();
		return a;
	}
	
	private void assertLocEq(RoadLocation a, int lane, int offset) {
		assertTrue(a.getLane() == lane);
		assertTrue(a.getOffset() == offset);
	}
	
	@Test
	public void testGetters() throws Exception {
		lanes = 3; // redefine this, just to be safe
		setUp();
		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
		assertEquals(a.locationService.getLanes(),2);
		assertTrue(a.locationService.isValidLane(0));
		assertTrue(a.locationService.isValidLane(1));
		assertTrue(a.locationService.isValidLane(2));
		assertTrue(!a.locationService.isValidLane(3));
		assertTrue(!a.locationService.isValidLane(-1));
		assertTrue(!a.locationService.isValidLane(49));
	}
	
	@Test
	public void testGetLocation() throws Exception {
		length = 50;
		maxSpeed = 5;
		maxDecel = 1;
		// perception range is 15
		setUp();
		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
		TestAgent b = createTestAgent("b", new RoadLocation(1, 0), 2);
		TestAgent c = createTestAgent("c", new RoadLocation(2, 14), 3);
		TestAgent d = createTestAgent("d", new RoadLocation(0, 20), 4);
		TestAgent e = createTestAgent("e", new RoadLocation(0, 49), 5);
		
		env.incrementTime();
		// check it's all correct
		a.assertLocation(0, 0);
		b.assertLocation(1, 0);
		c.assertLocation(2, 14);
		d.assertLocation(0, 20);
		e.assertLocation(0, 49);
		
		// check they can see people they're supposed to
		assertLocEq((RoadLocation)a.locationService.getAgentLocation(b.getID()), 1, 0);
		assertLocEq((RoadLocation)a.locationService.getAgentLocation(c.getID()), 2, 14);
		// (it should wrap)
		assertLocEq((RoadLocation)a.locationService.getAgentLocation(e.getID()), 0, 49);
		assertLocEq((RoadLocation)b.locationService.getAgentLocation(a.getID()), 0, 0);
		assertLocEq((RoadLocation)b.locationService.getAgentLocation(c.getID()), 2, 14);
		assertLocEq((RoadLocation)c.locationService.getAgentLocation(a.getID()), 0, 0);
		assertLocEq((RoadLocation)c.locationService.getAgentLocation(b.getID()), 1, 0);
		assertLocEq((RoadLocation)c.locationService.getAgentLocation(d.getID()), 0, 20);
		assertLocEq((RoadLocation)d.locationService.getAgentLocation(c.getID()), 2, 14);
		assertLocEq((RoadLocation)e.locationService.getAgentLocation(a.getID()), 0, 0);
		
		try {
			// try to get someone you can't see
			a.locationService.getAgentLocation(d.getID());
			fail();
		} catch (CannotSeeAgent ex) {
		}
		try {
			// should be reflexive
			d.locationService.getAgentLocation(a.getID());
			fail();
		} catch (CannotSeeAgent ex) {
		}
		
		assertLocEq((RoadLocation)b.locationService.getAgentLocation(a.getID()), 0, 0);
		try {
			// can't see
			a.locationService.getAgentLocation(d.getID());
			fail();
		} catch (CannotSeeAgent ex) {
		}
		
	}
	
	@Test
	public void testGetNearbyAgents() throws Exception {
		length = 50;
		maxSpeed = 5;
		maxDecel = 1;
		// perception range is 15
		setUp();
		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
		TestAgent b = createTestAgent("b", new RoadLocation(1, 0), 2);
		TestAgent c = createTestAgent("c", new RoadLocation(2, 14), 3);
		TestAgent d = createTestAgent("d", new RoadLocation(0, 20), 4);
		TestAgent e = createTestAgent("e", new RoadLocation(0, 49), 5);
		
		env.incrementTime();
		// check it's all correct
		a.assertLocation(0, 0);
		b.assertLocation(1, 0);
		c.assertLocation(2, 14);
		d.assertLocation(0, 20);
		e.assertLocation(0, 49);
		Map<UUID, Location> aMap = a.locationService.getNearbyAgents();
		assertLocEq((RoadLocation)aMap.get(b.getID()), 1, 0);
		assertLocEq((RoadLocation)aMap.get(c.getID()), 2, 14);
		// wraps
		assertLocEq((RoadLocation)aMap.get(e.getID()), 0, 49);
		// can't see too far
		assertTrue((!aMap.containsKey(d.getID())));
	}
	
	@Test
	public void testGetAgentToFrontLimitedPerception() throws Exception {
		lanes = 4;
		length = 50;
		maxSpeed = 5;
		maxDecel = 1;
		// perception range is 15 (28 with speed 7)
		setUp();
		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
		TestAgent b = createTestAgent("b", new RoadLocation(1, 0), 2);
		TestAgent c = createTestAgent("c", new RoadLocation(2, 14), 3);
		TestAgent d = createTestAgent("d", new RoadLocation(0, 20), 4);
		TestAgent e = createTestAgent("e", new RoadLocation(0, 49), 5);
		
		env.incrementTime();
		// check it's all correct
		a.assertLocation(0, 0);
		b.assertLocation(1, 0);
		c.assertLocation(2, 14);
		d.assertLocation(0, 20);
		e.assertLocation(0, 49);
		
		assertTrue(a.locationService.getAgentToFront(0)==null);
		assertTrue(a.locationService.getAgentToFront(1)==b.getID());	// b is alongside
		assertTrue(a.locationService.getAgentToFront(2)==c.getID());
		assertTrue(a.locationService.getAgentToFront(3)==null);
		
		assertTrue(b.locationService.getAgentToFront(0)==a.getID());	// a is alongside
		assertTrue(b.locationService.getAgentToFront(1)==null);
		assertTrue(b.locationService.getAgentToFront(2)==c.getID());
		assertTrue(b.locationService.getAgentToFront(3)==null);
		
		assertTrue(c.locationService.getAgentToFront(0)==d.getID());
		assertTrue(c.locationService.getAgentToFront(1)==null);
		assertTrue(c.locationService.getAgentToFront(2)==null);
		assertTrue(c.locationService.getAgentToFront(3)==null);
		
		assertTrue(d.locationService.getAgentToFront(0)==null);
		assertTrue(d.locationService.getAgentToFront(1)==null);
		assertTrue(d.locationService.getAgentToFront(2)==null);
		assertTrue(d.locationService.getAgentToFront(3)==null);
		
		assertTrue(e.locationService.getAgentToFront(0)==a.getID());
		assertTrue(e.locationService.getAgentToFront(1)==b.getID());
		assertTrue(e.locationService.getAgentToFront(2)==c.getID());
		assertTrue(e.locationService.getAgentToFront(3)==null);
	}
	
	@Test
	public void testGetAgentToFrontUnlimitedPerception() throws Exception {
		lanes = 4;
		length = 50;
		maxSpeed = 500;
		maxDecel = 1;
		// perception range is big
		setUp();
		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
		TestAgent b = createTestAgent("b", new RoadLocation(1, 0), 2);
		TestAgent c = createTestAgent("c", new RoadLocation(2, 14), 3);
		TestAgent d = createTestAgent("d", new RoadLocation(0, 20), 4);
		TestAgent e = createTestAgent("e", new RoadLocation(0, 49), 5);
		
		env.incrementTime();
		// check it's all correct
		a.assertLocation(0, 0);
		b.assertLocation(1, 0);
		c.assertLocation(2, 14);
		d.assertLocation(0, 20);
		e.assertLocation(0, 49);
		
		assertTrue(a.locationService.getAgentToFront(0)==d.getID());
		assertTrue(a.locationService.getAgentToFront(1)==b.getID());	// b is alongside
		assertTrue(a.locationService.getAgentToFront(2)==c.getID());
		assertTrue(a.locationService.getAgentToFront(3)==null);
		
		assertTrue(b.locationService.getAgentToFront(0)==a.getID());	// a is alongside
		assertTrue(b.locationService.getAgentToFront(1)==null);
		assertTrue(b.locationService.getAgentToFront(2)==c.getID());
		assertTrue(b.locationService.getAgentToFront(3)==null);
		
		assertTrue(c.locationService.getAgentToFront(0)==d.getID());
		assertTrue(c.locationService.getAgentToFront(1)==b.getID());
		assertTrue(c.locationService.getAgentToFront(2)==null);
		assertTrue(c.locationService.getAgentToFront(3)==null);
		
		assertTrue(d.locationService.getAgentToFront(0)==e.getID());
		assertTrue(d.locationService.getAgentToFront(1)==b.getID());
		assertTrue(d.locationService.getAgentToFront(2)==c.getID());
		assertTrue(d.locationService.getAgentToFront(3)==null);
		
		assertTrue(e.locationService.getAgentToFront(0)==a.getID());
		assertTrue(e.locationService.getAgentToFront(1)==b.getID());
		assertTrue(e.locationService.getAgentToFront(2)==c.getID());
		assertTrue(e.locationService.getAgentToFront(3)==null);
	}
	
	@Test
	public void testGetOffsetDistanceBetween() throws Exception {
		length = 50;
		// perception range is 15 (28 with speed 7)
		setUp();
		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
		RoadLocation aLoc = new RoadLocation(0, 0);
		RoadLocation bLoc = new RoadLocation(1, 0);
		RoadLocation cLoc = new RoadLocation(2, 14);
		RoadLocation dLoc = new RoadLocation(0, 20);
		RoadLocation eLoc = new RoadLocation(0, 49);

		
		assertEquals(a.locationService.getOffsetDistanceBetween(aLoc, bLoc), 0);
		assertEquals(a.locationService.getOffsetDistanceBetween(aLoc, cLoc), 14);
		assertEquals(a.locationService.getOffsetDistanceBetween(aLoc, dLoc), 20);
		assertEquals(a.locationService.getOffsetDistanceBetween(aLoc, eLoc), 49);
		// it's not reflexive
		assertEquals(a.locationService.getOffsetDistanceBetween(eLoc, aLoc), 1);
		assertEquals(a.locationService.getOffsetDistanceBetween(cLoc, aLoc), 36);
		
	}
}