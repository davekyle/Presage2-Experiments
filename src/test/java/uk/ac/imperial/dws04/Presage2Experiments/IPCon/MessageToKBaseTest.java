/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.definition.type.FactType;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jmock.Expectations;
import org.jmock.Mockery;
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
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.IPConActionMsg;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.IPConMsgToRuleEngine;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.presage2.core.IntegerTime;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.TimeDriven;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.ConstrainedNetworkController;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.network.NetworkConstraint;
import uk.ac.imperial.presage2.core.network.NetworkController;
import uk.ac.imperial.presage2.core.simulator.Scenario;
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
import uk.ac.imperial.presage2.util.network.NetworkModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * @author dws04
 *
 */
public class MessageToKBaseTest {

	final private Logger logger = Logger.getLogger(this.getClass());

	Injector injector;
	RuleStorage rules;
	StatefulKnowledgeSession session;
	
	AbstractEnvironment env;
	//RoadEnvironmentService roadEnvironmentService;
	SpeedService globalSpeedService;
	RoadEnvironmentService globalRoadEnvironmentService;
	IPConService globalIPConService;
	
	private int lanes = 3;
	private int length = 10;
	private int maxSpeed = 10;
	private int maxAccel = 1;
	private int maxDecel = 1;
	private int junctionCount = 0;
	Time time = new IntegerTime(0);
	SimTime sTime = new SimTime(time);
	
	// jmock stuff for scenario/time
	Mockery context = new Mockery();
	final Scenario scenario = context.mock(Scenario.class);
	NetworkController networkController;
	
	
	@Before
	public void setUp() throws Exception {
		Set<Class<? extends NetworkConstraint>> constraints = new HashSet<Class<? extends NetworkConstraint>>();
		constraints.add(IPConMsgToRuleEngine.class);
		injector = Guice.createInjector(
				// rule module
				new RuleModule()
					.addClasspathDrlFile("IPConUtils.drl")
					.addClasspathDrlFile("IPConPowPer.drl")
					.addClasspathDrlFile("IPCon.drl")
					.addClasspathDrlFile("IPConOblSan.drl"),
				new AbstractEnvironmentModule()
					.addActionHandler(LaneMoveHandler.class)
					.addParticipantEnvironmentService(ParticipantLocationService.class)
					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
					.addParticipantEnvironmentService(ParticipantSpeedService.class)
					.addParticipantEnvironmentService(ParticipantIPConService.class)
					.addGlobalEnvironmentService(RoadEnvironmentService.class)
					.addGlobalEnvironmentService(IPConService.class)
					.addParticipantGlobalEnvironmentService(IPConBallotService.class)
					.setStorage(RuleStorage.class),
				Area.Bind.area2D(lanes, length).addEdgeHandler(Edge.Y_MAX,
						WrapEdgeHandler.class), new EventBusModule(),
				NetworkModule.constrainedNetworkModule(constraints).withNodeDiscovery(),
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
						// need to bind a time and a scenario
						bind(Time.class).to(IntegerTime.class);
						bind(Scenario.class).toInstance(scenario);
						
						// temporary hack while Sam writes a real fix
						bind(NetworkController.class).in(Singleton.class);
						bind(ConstrainedNetworkController.class).in(Singleton.class);
					}
				});
		
		// allow the fake scenario to use any mock timedriven and environment classes
		context.checking(new Expectations() {
			{
				allowing(scenario).addTimeDriven(with(any(NetworkController.class)));
				allowing(scenario).addEnvironment(with(any(TimeDriven.class)));
			}
		});
		
		networkController = injector.getInstance(ConstrainedNetworkController.class);
		env = injector.getInstance(AbstractEnvironment.class);
		globalSpeedService = injector.getInstance(SpeedService.class);
		globalRoadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
		globalIPConService = injector.getInstance(IPConService.class);
		
		rules = injector.getInstance(RuleStorage.class);
		session = injector.getInstance(StatefulKnowledgeSession.class);
	}
	
	@After
	public void tearDown() throws Exception {
		session.dispose();
	}
	
	public void outputObjects() {
		Collection<Object> objects = session.getObjects();
		logger.info("\nObjects: " + objects.toString() + " are :");
		for (Object object : objects) {
			logger.info(object);
		}
		logger.info("/objects");
		analyseDroolsUsage();
		logger.info("/usage");
	}
	
	private void analyseDroolsUsage() {
		Map<Class<?>, Integer> typeCards = new HashMap<Class<?>, Integer>();
		for (Object o : session.getObjects()) {
				if (!typeCards.containsKey(o.getClass())) {
					typeCards.put(o.getClass(), 0);
				}
				typeCards.put(o.getClass(), typeCards.get(o.getClass()) + 1);
		}
		logger.info("Drools memory usage:");
		for (Map.Entry<Class<?>, Integer> entry : typeCards.entrySet()) {
			logger.info(entry.getKey().getSimpleName() + " - " + entry.getValue());
		}
	}
	
	class TestAgent extends RoadAgent {

		public TestAgent(UUID id, String name, RoadLocation myLoc, int mySpeed,
				RoadAgentGoals goals) {
			super(id, name, myLoc, mySpeed, goals);
		}
		
		public NetworkAdaptor getNetwork() {
			return this.network;
		}
		
	}
	
	private TestAgent createAgent(String name, RoadLocation startLoc, int startSpeed) {
		TestAgent a = new TestAgent(Random.randomUUID(), name, startLoc, startSpeed, new RoadAgentGoals((Random.randomInt(maxSpeed)+1), Random.randomInt(length), 0));
		// FIXME TODO Not sure if this is needed...?
		injector.injectMembers(a);
		a.initialise();
		//Call this if needed
		//initAgent(a.getIPConHandle(), roles, 0, issue, cluster);
		return a;
	}
	
	public void incrementTime(){
		time.increment();
		networkController.incrementTime();
		env.incrementTime();
		
	}
	
	/**
	 * Artificially inserting facts without the service for sake of testing
	 */
	private void addRoles(IPConAgent agent, Role[] roles, Integer revision, String issue, UUID cluster) {
		//Set roles
		for (Role role : roles) {
			session.insert(new HasRole(role, agent, revision, issue, cluster));
		}
		// Initially didn't vote
		//session.insert(new Voted(agent, revision, 0, IPCNV.val(), issue, cluster));
		// And the reportedvote for the initially
		//session.insert(new ReportedVote(agent, revision, 0, IPCNV.val(), revision, 0, issue, cluster));
	}
	
	@Test
	public void msgToKbaseTest() {
		// make vars
		Integer revision = 1;
		String issue = "ISSUE STRING";
		UUID cluster = Random.randomUUID();
		
		// make agents
		TestAgent a1 = createAgent("a1", new RoadLocation(0, 0), 1);
		TestAgent a2 = createAgent("a2", new RoadLocation(1, 0), 1);
		
		// new cycle
		incrementTime();
		
		// make ipcon msg
		IPConAction act = new ArrogateLeadership(a1.getIPConHandle(), revision, issue, cluster);
		IPConActionMsg msg = new IPConActionMsg(Performative.INFORM, time, a1.getNetwork().getAddress(), act);
		
		// make nonipcon msg
		BroadcastMessage<Integer> msg2 = new BroadcastMessage<Integer>(Performative.INFORM, a2.getNetwork().getAddress(), time, new Integer(5));
		
		// agents send msg
		a1.getNetwork().sendMessage(msg);
		a2.getNetwork().sendMessage(msg2);
		
		// new cycle
		incrementTime();
		
		// action is in kbase
		//assertThat( (globalIPConService.getFactQueryResults("ArrogateLeadership", revision, issue, cluster)).size(), is( 1 ) );
		
		// ugly ugly hackery
		Collection<Object> coll = session.getObjects(new ObjectFilter() {
			
			@Override
			public boolean accept(Object object) {
				//logger.trace("Filtering (" + object.getClass() + ") " + object);
				return ( object instanceof ArrogateLeadership );
			}
		});
		logger.trace("Got " + Arrays.asList(coll.toArray()));
		assertThat( coll.size(), is( 1 ) );
		logger.info("***Arrogate correctly inserted to kbase***");
		
		// kbase rules run correctly
		assertThat( (globalIPConService.getAgentRoles(a1.getIPConHandle(), revision, issue, cluster)).size(), is( 1 ) );
		logger.info("***Rules fired correctly to make agent leader***");
		
		// non-ipcon msgs not added to kbase
		assertThat( (globalIPConService.getFactQueryResults("BroadcastMessage", revision, issue, cluster)).size(), is( 0 ) );
		logger.info("***Generic broadcast message correctly NOT inserted to kbase***");
		
		// manual check
		outputObjects();
		
		logger.info("Test complete.");
	}
	
}
