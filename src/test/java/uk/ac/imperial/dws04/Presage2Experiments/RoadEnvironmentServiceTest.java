package uk.ac.imperial.dws04.Presage2Experiments;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Test;

import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.location.area.Area.Edge;
import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class RoadEnvironmentServiceTest {

	Injector injector;
	AbstractEnvironment env;
	RoadEnvironmentService roadEnvironmentService;
	
	private int lanes = 3;
	private int length = 10;
	private int maxSpeed = 10;
	private int maxAccel = 1;
	private int maxDecel = 1;
	private int junctionCount = 0;
	
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
						bind(Integer.TYPE).annotatedWith(Names.named("params.nonStopMode")).toInstance(0);
					}
				});

		env = injector.getInstance(AbstractEnvironment.class);
		roadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
	}

	@After
	public void tearDown() throws Exception {
	}
	


	@Test
	public void testGetters() throws Exception {
		junctionCount = 2;
		setUp();
		assertEquals(roadEnvironmentService.getLanes(),lanes);
		assertEquals(roadEnvironmentService.getLength(),length);
		assertEquals(roadEnvironmentService.getMaxAccel(), maxAccel);
		assertEquals(roadEnvironmentService.getMaxDecel(), maxDecel);
		assertEquals(roadEnvironmentService.getMaxSpeed(), maxSpeed);
		ArrayList<Integer> locations = roadEnvironmentService.getJunctionLocations();
		assertEquals(locations.size(), 2);
		assertTrue(locations.contains(0));
		assertTrue(locations.contains(5));
	}
	
	@Test
	public void testJunctions() throws Exception {
		junctionCount = 4;
		setUp();
		ArrayList<Integer> locations = roadEnvironmentService.getJunctionLocations();
		assertEquals(locations.size(), 4);
		assertTrue(locations.contains(0));
		assertTrue(locations.contains(2));
		assertTrue(locations.contains(5));
		assertTrue(locations.contains(7));
	}
}