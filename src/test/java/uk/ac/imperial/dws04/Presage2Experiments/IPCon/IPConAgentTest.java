/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
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
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.AddRole;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncAck;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConFact;
import uk.ac.imperial.presage2.core.IntegerTime;
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
public class IPConAgentTest {

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
	IntegerTime time = new IntegerTime(0);
	SimTime sTime = new SimTime(time);
	
	
	@Before
	public void setUp() throws Exception {
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
	
	private RoadAgent createAgent(String name, RoadLocation startLoc, int startSpeed) {
		RoadAgent a = new RoadAgent(Random.randomUUID(), name, startLoc, startSpeed, new RoadAgentGoals((Random.randomInt(maxSpeed)+1), Random.randomInt(length), 0));
		// FIXME TODO Not sure if this is needed...?
		injector.injectMembers(a);
		a.initialise();
		//Call this if needed
		//initAgent(a.getIPConHandle(), roles, 0, issue, cluster);
		return a;
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
	public void testObligationInstantiation() throws Exception {
		logger.info("\nBeginning test of obligation instantiation...");
		/**
		 * This requires access to a fn which should be private, so calling public wrapper fn
		 */
		final Integer revision = 1;
		final String issue = "IssueString";
		final UUID cluster = Random.randomUUID();
		Object value = "VALUE";
		/*
		 * Create agents
		 */
		RoadAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
		addRoles(a1.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
		RoadAgent a2 = createAgent("a2", new RoadLocation(1,0), 1);
		addRoles(a2.getIPConHandle(), new Role[]{Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
		RoadAgent a3 = createAgent("a3", new RoadLocation(2,0), 1);
		addRoles(a3.getIPConHandle(), new Role[]{Role.ACCEPTOR}, revision, issue, cluster);
		
		/*
		 * Set up cluster to ensure at least one agent-relative obligation and one agent-neutral obligation
		 * where the agents are all permitted. Preferably the permissions should include examples of multiple
		 * options, no options, single options, and unconstrained(-ish) options. 
		 */
		/*
		 * A2 requests.
		 * A1 now should be obligated to prepare.
		 */
		session.insert(new Request0A(a2.getIPConHandle(), revision, value, issue, cluster));
		rules.incrementTime();
		Collection<IPConAction> obl1 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 obligated to :" + obl1);
		assertEquals(1,obl1.size());
		Collection<IPConAction> obl1a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		assertEquals(1,obl1a.size());
		Collection<IPConAction> per1 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 permitted to :" + per1);
		assertEquals(1,per1.size());
		
		ArrayList<IPConAction> a1Obl1 = a1.TESTgetInstantiatedObligatedActionQueue();
		assertEquals(1, a1Obl1.size());
		logger.info("A1 obligated to: " + obl1.toArray()[0]);
		logger.info("A1 instantiated: " + a1Obl1.get(0));
		assertTrue(a1Obl1.get(0).fulfils((IPConAction)obl1.toArray()[0]));
		logger.info("** Unconstrained instantiation (Prepare1A) test passed **");
		
		/*
		 * A1 sends instantiated prepare message.
		 * A2 and A3 not obligated to respond, but should respond with IPCNV.
		 */
		session.insert(a1Obl1.get(0));
		rules.incrementTime();
		Collection<IPConAction> obl2 = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		logger.info("Agents not obligated :" + obl2);
		assertEquals(0,obl2.size());
		Collection<IPConAction> per2 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Response1B", null, revision, issue, cluster);
		logger.info("Agents permitted to :" + per2);
		assertEquals(3,per2.size());
		
		/*
		 * Check that agents with no obligations don't try instantiating anything
		 */
		for (RoadAgent ag : new RoadAgent[]{a1, a2, a3}) {
			assertEquals(0, ag.TESTgetInstantiatedObligatedActionQueue().size());
		}
		logger.info("** No obligations to instantiate test passed **");
		
		/*
		 * Cheat a bit and just insert the permitted response actions for A1-3.
		 * A1 should be obligated to submit the proposed value.
		 */
		for (IPConAction act : per2) {
			session.insert(act);
		}
		rules.incrementTime();
		Collection<IPConAction> obl3 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Submit2A", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 obligated to :" + obl3);
		assertEquals(1,obl3.size());
		Collection<IPConAction> obl3a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		assertEquals(1,obl3a.size());
		
		ArrayList<IPConAction> a1Obl3 = a1.TESTgetInstantiatedObligatedActionQueue();
		assertEquals(1, a1Obl3.size());
		logger.info("A1 obligated to: " + obl3.toArray()[0]);
		logger.info("A1 instantiated: " + a1Obl3.get(0));
		assertTrue(a1Obl3.get(0).fulfils((IPConAction)obl3.toArray()[0]));
		logger.info("** One-option-constrained instantiation (Submit2A) test passed **");
		
		/*
		 * Insert A1's instantiated submit action.
		 * A1-3 not obligated to vote, but permitted to.
		 */
		session.insert(a1Obl3.get(0));
		rules.incrementTime();
		Collection<IPConAction> per3 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Vote2B", null, revision, issue, cluster);
		logger.info("Agents permitted to :" + per3);
		assertEquals(3,per3.size());
		
		/*
		 * Cheat a bit again and send the permitted vote actions for A1-3.
		 * Agents should not be obligated to do anything.
		 * VALUE should be chosen.
		 */
		for (IPConAction act : per3) {
			session.insert(act);
		}
		rules.incrementTime();
		Collection<IPConAction> obl4 = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		logger.info("Agents not obligated :" + obl4);
		assertEquals(0,obl4.size());
		Collection<IPConFact> fact4 = globalIPConService.getFactQueryResults("Chosen", revision, issue, cluster);
		assertEquals(1,fact4.size());
		Chosen chosen = globalIPConService.getChosen(revision, issue, cluster);
		assertEquals(value, chosen.getValue());
		
		/*
		 * Add a new agent to check syncreq obligation.
		 * A1 should be obligated to SyncReq the new agent.
		 */
		RoadAgent a4 = createAgent("a4", new RoadLocation(0,2), 1);
		session.insert(new AddRole(a1.getIPConHandle(), a4.getIPConHandle(), Role.ACCEPTOR, revision, issue, cluster));
		rules.incrementTime();
		
		Collection<IPConAction> obl5 = globalIPConService.getActionQueryResultsForRIC("getObligations", "SyncReq", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 obligated to :" + obl5);
		assertEquals(1,obl5.size());
		Collection<IPConAction> obl5a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		assertEquals(1,obl5a.size());
		
		ArrayList<IPConAction> a1Obl5 = a1.TESTgetInstantiatedObligatedActionQueue();
		assertEquals(1, a1Obl5.size());
		logger.info("A1 obligated to: " + obl5.toArray()[0]);
		logger.info("A1 instantiated: " + a1Obl5.get(0));
		assertTrue(a1Obl5.get(0).fulfils((IPConAction)obl5.toArray()[0]));
		logger.info("** One-option-constrained (self-)instantiation (SyncReq) test passed **");
		
		/*
		 * A1 sends instantiated SyncReq.
		 * A4 should be obligated to reply (multiple choice).
		 * A1-3 should not be obligated.
		 */
		session.insert(a1Obl5.get(0));
		rules.incrementTime();
		Collection<IPConAction> obl6 = globalIPConService.getActionQueryResultsForRIC("getObligations", "SyncAck", a4.getIPConHandle(), revision, issue, cluster);
		logger.info("A4 obligated to :" + obl6);
		assertEquals(1,obl6.size());
		Collection<IPConAction> obl6a = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		assertEquals(1,obl6a.size());
		
		ArrayList<IPConAction> a4Obl6 = a4.TESTgetInstantiatedObligatedActionQueue();
		assertEquals(1, a4Obl6.size());
		logger.info("A4 obligated to: " + obl6.toArray()[0]);
		logger.info("A4 instantiated: " + a4Obl6.get(0));
		assertTrue(a4Obl6.get(0).fulfils((IPConAction)obl6.toArray()[0]));
		assertEquals( value, ((SyncAck)a4Obl6.get(0)).getValue() );
		logger.info("** Multi-option-constrained instantiation (SyncAck to unknown issue) test passed **");
		
		logger.info("Finished test of obligation instantiation.\n");
	}
	
	@Test
	public void multiLeaderObligationSelfInstantiationTest() throws Exception {
		logger.info("\nBeginning test of obligation self-instantiation in the face of multiple leaders...");
		/**
		 * This requires access to a fn which should be private, so calling public wrapper fn
		 */
		final Integer revision = 1;
		final String issue = "IssueString";
		final UUID cluster = Random.randomUUID();
		Object value = "VALUE";
		/*
		 * Create agents
		 */
		RoadAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
		addRoles(a1.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
		RoadAgent a2 = createAgent("a2", new RoadLocation(1,0), 1);
		addRoles(a2.getIPConHandle(), new Role[]{Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
		RoadAgent a3 = createAgent("a3", new RoadLocation(2,0), 1);
		addRoles(a3.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
		
		/*
		 * A2 requests.
		 * A1 and A3 now should be obligated to prepare.
		 */
		session.insert(new Request0A(a2.getIPConHandle(), revision, value, issue, cluster));
		rules.incrementTime();
		Collection<IPConAction> obl1 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 obligated to :" + obl1);
		assertEquals(1,obl1.size());
		Collection<IPConAction> obl3 = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a3.getIPConHandle(), revision, issue, cluster);
		logger.info("A3 obligated to :" + obl1);
		assertEquals(1,obl3.size());
		Collection<IPConAction> obla = globalIPConService.getActionQueryResultsForRIC("getObligations", null, null, revision, issue, cluster);
		assertEquals(1,obla.size()); // NB. there is only one actual obligation, but 2 agents consider it "theirs"
		Collection<IPConAction> per1 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 permitted to :" + per1);
		assertEquals(1,per1.size());
		Collection<IPConAction> per3 = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a3.getIPConHandle(), revision, issue, cluster);
		logger.info("A3 permitted to :" + per3);
		assertEquals(1,per3.size());
		
		ArrayList<IPConAction> a1Obl1 = a1.TESTgetInstantiatedObligatedActionQueue();
		assertEquals(1, a1Obl1.size());
		logger.info("A1 obligated to: " + obl1.toArray()[0]);
		logger.info("A1 instantiated: " + a1Obl1.get(0));
		assertTrue(a1Obl1.get(0).fulfils((IPConAction)obl1.toArray()[0]));
		
		ArrayList<IPConAction> a3Obl1 = a3.TESTgetInstantiatedObligatedActionQueue();
		assertEquals(1, a3Obl1.size());
		logger.info("A3 obligated to: " + obl3.toArray()[0]);
		logger.info("A3 instantiated: " + a3Obl1.get(0));
		assertTrue(a3Obl1.get(0).fulfils((IPConAction)obl3.toArray()[0]));
		
		Prepare1A a1Prep = ((Prepare1A)a1Obl1.get(0));
		Prepare1A a3Prep = ((Prepare1A)a3Obl1.get(0));

		// FIXME TODO this needs doing...
		// assertThat(a1Prep.getBallot(), is ( not( a3Prep.getBallot() ) ) );
		assertEquals(a1Prep.getRevision(), a3Prep.getRevision());
		assertEquals(a1Prep.getIssue(), a3Prep.getIssue());
		assertEquals(a1Prep.getCluster(), a3Prep.getCluster());
		
		logger.info("** Multiple leader self-instantiation test passed **");
		

		logger.info("Finished test of obligation self-instantiation in the face of multiple leaders.\n");
	}
	
	@Test
	public void test() throws Exception {
		/*
		 * Create agents
		 */
		
		/*
		 * One of them arrogates
		 */
		
		/*
		 * Agents join
		 */
		
		/*
		 * Proposal for issues
		 */
		
		/*
		 * Prepare
		 */
		
		/*
		 * Response
		 */
		
		/*
		 * Submit
		 */
		
		/*
		 * Vote
		 */
	}
}
