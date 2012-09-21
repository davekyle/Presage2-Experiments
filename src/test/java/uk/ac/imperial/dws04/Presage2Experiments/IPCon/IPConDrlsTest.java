/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.definition.type.FactType;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.AddRole;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPCNV;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Response1B;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Revise;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Submit2A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncAck;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.SyncReq;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Vote2B;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;


import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author dave
 *
 */
public class IPConDrlsTest {
	
	final private Logger logger = Logger.getLogger(IPConDrlsTest.class);

	Injector injector;
	RuleStorage rules;
	StatefulKnowledgeSession session;

	@Before
	public void setUp() throws Exception {
		injector = Guice.createInjector(new RuleModule()
				//.addClasspathDrlFile("test.drl")
				.addClasspathDrlFile("IPCon_Institutional_Facts.drl")
				.addClasspathDrlFile("IPConUtils.drl")
				.addClasspathDrlFile("IPConPowPer.drl")
				.addClasspathDrlFile("IPCon.drl")
				.addClasspathDrlFile("IPConOblSan.drl")
				);
		rules = injector.getInstance(RuleStorage.class);
		session = injector.getInstance(StatefulKnowledgeSession.class);
		initSession();
	}

	@After
	public void tearDown() throws Exception {
		session.dispose();
	}
	
	public void initSession() {
		session.setGlobal("logger", this.logger);
		session.setGlobal("IPCNV_val", IPCNV.val());
		//session.setGlobal("session", session);
		//session.setGlobal("storage", null);
		
		for (Role role : Role.values()) {
			session.insert(role);
		}
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
	
	public void initAgent(IPConAgent agent, Role role, Integer revision, String issue, UUID cluster) throws Exception {
		ArrayList<Role> roles = new ArrayList<Role>();
		roles.add(role);
		initAgent(agent, roles, revision, issue, cluster);
	}
	
	public void initAgent(IPConAgent agent, ArrayList<Role> roles, Integer revision, String issue, UUID cluster) throws Exception {
		//IPConAgent agent = new IPConAgent();
		//session.insert(agent);
		
		//Set roles
		final FactType hasRoleType = typeFromString("HasRole");
		for (Role role : roles) {
			Object hasRole = hasRoleType.newInstance();
			hasRoleType.set(hasRole, "role", role);
			hasRoleType.set(hasRole, "agent", agent);
			hasRoleType.set(hasRole, "revision", revision);
			hasRoleType.set(hasRole, "issue", issue);
			hasRoleType.set(hasRole, "cluster", cluster);
			session.insert(hasRole);
		}
		
		
		// Initially didn't vote
		final FactType votedType = typeFromString("Voted");
		Object v1Vote = votedType.newInstance();
		votedType.set(v1Vote, "agent", agent);
		votedType.set(v1Vote, "revision", revision);
		votedType.set(v1Vote, "ballot", 0);
		votedType.set(v1Vote, "value", IPCNV.val());
		votedType.set(v1Vote, "issue", issue);
		votedType.set(v1Vote, "cluster", cluster);
		session.insert(v1Vote);
		
		// And the reportedvote for the initially
		final FactType reportedVoteType = typeFromString("ReportedVote");
		Object v1RVote = reportedVoteType.newInstance();
		reportedVoteType.set(v1RVote, "agent", agent);
		reportedVoteType.set(v1RVote, "voteRevision", revision);
		reportedVoteType.set(v1RVote, "voteBallot", 0);
		reportedVoteType.set(v1RVote, "voteValue", IPCNV.val());
		reportedVoteType.set(v1RVote, "revision", revision);
		reportedVoteType.set(v1RVote, "ballot", 0);
		reportedVoteType.set(v1RVote, "issue", issue);
		reportedVoteType.set(v1RVote, "cluster", cluster);
		session.insert(v1RVote);
	}
	
	@Test
	public void arrogateLeadershipTest() throws Exception {
		logger.info("\nStarting arrogateLeadershipTest()");
		IPConAgent agent = new IPConAgent();
		Integer revision = 0;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		session.insert(agent);
		//rules.incrementTime();
		session.insert(new ArrogateLeadership(agent, revision, issue, cluster));
		rules.incrementTime();
		
		outputObjects();
		
		//String hasRoleTypeString = "HasRole";
		/*final FactType hasRoleType = typeFromString("HasRole");
		
		Collection<Object> hasRoles = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return assertFactType(object, hasRoleType);
			}
		});
		assertEquals(1, hasRoles.size());*/
		Collection<Object> hasRoles = assertFactCount("HasRole", 1);
		Object hasRole = Arrays.asList(hasRoles.toArray()).get(0);
		Role role = (Role) typeFromString("HasRole").get(hasRole, "role");
		assertEquals(role.toString(), "LEADER");
		logger.info("Finished arrogateLeadershipTest()\n");
	}
	
	@Test
	public void requestTest() throws Exception {
		logger.info("\nStarting requestTest()");
		
		Integer revision = 0;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		
		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2);
		ArrayList<Role> a1Roles = new ArrayList<Role>();
		ArrayList<Role> a2Roles = new ArrayList<Role>();
		a1Roles.add(Role.LEADER);
		a1Roles.add(Role.ACCEPTOR);
		a2Roles.add(Role.PROPOSER);
		a2Roles.add(Role.ACCEPTOR);
		initAgent(a1, a1Roles, revision, issue, cluster);
		initAgent(a2, a2Roles, revision, issue, cluster);
		
		rules.incrementTime();
		
		assertFactCount("HasRole", 4);
		assertFactCount("Proposed", 0);
		
		session.insert(new Request0A( a2, revision, "VALUE", issue, cluster ));
		
		rules.incrementTime();
		
		assertFactCount("Proposed", 1);
		
		logger.info("Finished requestTest()\n");
	}
	
	/**
	 * Test to check the power, permission, and obligations around Response1B action
	 * 
	 * Pre: Acceptor will be inserted with a previous vote and a pre_vote will be asserted
	 * Test: Check the agent has power to respond, permission to respond correctly, no permission to respond incorrectly 
	 * 
	 * @throws Exception
	 */
	@Test
	public void checkResponsePowPerObl() throws Exception {
		logger.info("\nStarting checkResponsePowPerObl()");
		IPConAgent a1 = new IPConAgent("a1");
		Integer revision = 1;
		Integer ballot = 10;
		Object v1 = "1";
		Object v2 = "v2";
		Object v3 = 3;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		session.insert(a1);
		// Insert intially facts:
		// Agent is acceptor
		FactType roleType = typeFromString("HasRole");
		Object role = roleType.newInstance();
		roleType.set(role, "role", Role.ACCEPTOR);
		roleType.set(role, "agent", a1);
		roleType.set(role, "revision", revision);
		roleType.set(role, "issue", issue);
		roleType.set(role, "cluster", cluster);
		session.insert(role);
		// PreVote exists
		FactType preVoteType = typeFromString("Pre_Vote");
		Object preVote = preVoteType.newInstance();
		preVoteType.set(preVote, "revision", revision);
		preVoteType.set(preVote, "ballot", ballot);
		preVoteType.set(preVote, "issue", issue);
		preVoteType.set(preVote, "cluster", cluster);
		session.insert(preVote);
		// Agent voted in 3 previous ballots
		final FactType votedType = typeFromString("Voted");
		Object v1Vote = votedType.newInstance();
		votedType.set(v1Vote, "agent", a1);
		votedType.set(v1Vote, "revision", revision);
		votedType.set(v1Vote, "ballot", ballot-5);
		votedType.set(v1Vote, "value", v1);
		votedType.set(v1Vote, "issue", issue);
		votedType.set(v1Vote, "cluster", cluster);
		session.insert(v1Vote);
		Object v2Vote = votedType.newInstance();
		votedType.set(v2Vote, "agent", a1);
		votedType.set(v2Vote, "revision", revision);
		votedType.set(v2Vote, "ballot", ballot-3);
		votedType.set(v2Vote, "value", v2);
		votedType.set(v2Vote, "issue", issue);
		votedType.set(v2Vote, "cluster", cluster);
		session.insert(v2Vote);
		Object v3Vote = votedType.newInstance();
		votedType.set(v3Vote, "agent", a1);
		votedType.set(v3Vote, "revision", revision);
		votedType.set(v3Vote, "ballot", ballot-2);
		votedType.set(v3Vote, "value", v3);
		votedType.set(v3Vote, "issue", issue);
		votedType.set(v3Vote, "cluster", cluster);
		session.insert(v3Vote);
		
		rules.incrementTime();
		
		/*
		 * Make sure initially facts hold
		 */
		// agent is inserted
		assertFactCount("IPConAgent", 1);
		assertFactCount("HasRole", 1);
		assertFactCount("Pre_Vote", 1);
		assertFactCount("Voted", 3);
		

		session.insert(new Response1B( a1, 1, 0, IPCNV.val(), 1, 10, issue, cluster));
		
	}
	
	@Test
	public void narrative1Consensus() throws Exception {
		narrative1(true);
	}
	
	@Test
	public void narrative1NoConsensus() throws Exception {
		narrative1(false);
	}
	
	
	public void narrative1(Boolean pass) throws Exception {
		if (pass) {
			logger.info("\nStarting narrative1 with successful consensus");
		} else {
			logger.info("\nStarting narrative1 intending a sanction against leader");
		}
		// create agents
		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2);
		IPConAgent a3 = new IPConAgent("a3"); session.insert(a3);
		IPConAgent a4 = new IPConAgent("a4"); session.insert(a4);
		IPConAgent a5 = new IPConAgent("a5"); session.insert(a5);
		//specify revision/issue/cluster
		Integer revision = 1;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		//set initially roles
		session.insert(new ArrogateLeadership(a1, revision, issue, cluster));
		session.insert(new AddRole(a1, a1, Role.PROPOSER, revision, issue, cluster));
		session.insert(new AddRole(a1, a1, Role.ACCEPTOR, revision, issue, cluster));
		session.insert(new AddRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
		session.insert(new AddRole(a1, a3, Role.ACCEPTOR, revision, issue, cluster));
		session.insert(new AddRole(a1, a4, Role.ACCEPTOR, revision, issue, cluster));
		session.insert(new AddRole(a1, a5, Role.ACCEPTOR, revision, issue, cluster));
		//set initially voted
		final FactType votedType = typeFromString("Voted");
		Object a2Vote = votedType.newInstance();
		votedType.set(a2Vote, "agent", a2);
		votedType.set(a2Vote, "revision", revision);
		votedType.set(a2Vote, "ballot", 1);
		votedType.set(a2Vote, "value", 4);
		votedType.set(a2Vote, "issue", issue);
		votedType.set(a2Vote, "cluster", cluster);
		session.insert(a2Vote);
		Object a3Vote = votedType.newInstance();
		votedType.set(a3Vote, "agent", a3);
		votedType.set(a3Vote, "revision", revision);
		votedType.set(a3Vote, "ballot", 1);
		votedType.set(a3Vote, "value", 4);
		votedType.set(a3Vote, "issue", issue);
		votedType.set(a3Vote, "cluster", cluster);
		session.insert(a3Vote);
		if (!pass) {
			Object a4Vote = votedType.newInstance();
			votedType.set(a4Vote, "agent", a4);
			votedType.set(a4Vote, "revision", revision);
			votedType.set(a4Vote, "ballot", 2);
			votedType.set(a4Vote, "value", 5);
			votedType.set(a4Vote, "issue", issue);
			votedType.set(a4Vote, "cluster", cluster);
			session.insert(a4Vote);
		}
		rules.incrementTime();
		
		// check there are the right number of roles
		// 5 acceptors, one leader, one proposer
		assertFactCount("IPConAgent", 5);
		assertFactCount("HasRole", 7);
		
		// check theres only one agent can request (the proposer)
		// one proposer has permission
		//assertFactCount("Request", 1);
		assertCount("getPowers", "Request0A", 1);
		
		
		/*
		 *  check some arbitrary fact counts
		 */
		// everyone can arrogate (leader could arrogate something else for example)
		//assertFactCount("Arrogate", 5);
		assertCount("getPowers", "ArrogateLeadership", 5);
		// only leader can resign
		//assertFactCount("Resign", 1);
		assertCount("getPowers", "ResignLeadership", 1);
		// all can leave
		// TODO need to fix this holdsAt so each role doesn't count
		// inadvertantly fixed this by putting into a HashSet to remove dups :P
		//assertFactCount("Leave", 7);
		assertCount("getPowers", "LeaveCluster",5);
		
		/*
		 * 5 roles in total
		 * leader can add roles:
		 * 2 for himself
		 * (4 for others)*4 = 16
		 * Total: 18
		 */
		assertCount("getPowers", "AddRole", 18);
		assertCount("getPermissions", "AddRole", 18);
		/*
		 * 5 roles in total
		 * leader can rem roles:
		 * 3 for himself
		 * (1 for others)*4 = 4
		 * Total: 7
		 */
		assertCount("getPowers", "RemRole", 7);
		assertCount("getPermissions", "RemRole", 7);
		// leader can revise
		assertCount("getPowers", "Revise", 1);
		assertCount("getPermissions", "Revise", 1);
		// leader has pow to syncreq all acceptors (incl himself)
		assertCount("getPowers", "SyncReq", 5);
		// but not the permission
		assertCount("getPermissions", "SyncReq", 0);
		//no one can syncack
		assertCount("getPowers", "SyncAck", 0);
		assertCount("getPermissions", "SyncAck", 0);
		
		/*
		 * Begin the time steps.
		 */
		
		/*
		 * Time step 1 :
		 * leader/proposer requests 3, check no one can respond yet
		 */
		session.insert(new Request0A(a1, revision, 3, issue, cluster));
		rules.incrementTime();
		
		// check no one can response
		assertCount("getPermissions", "Response1B", 0);
		
		
		/*
		 * Time step 2 :
		 * leader issues prepare, check 5 acceptors can respond
		 */
		session.insert(new Prepare1A(a1, revision, 10, issue, cluster));
		rules.incrementTime();
		
		// check all acceptors can response
		// 5 acceptors have permission
		assertCount("getPermissions", "Response1B", 5);
		if (pass) {
			// 2 acceptors have to respond
			assertCount("getObligations", "Response1B", 2);
		}else {
			// 3 have to
			assertCount("getObligations", "Response1B", 3);
		}
		
		
		/*
		 * Time step 3 :
		 * All agents send a response, check the responsecount and per/obl to submit
		 */
		session.insert(new Response1B( a1, 1, 0, IPCNV.val(), 1, 10, issue, cluster));
		session.insert(new Response1B( a2, 1, 1, 4, 1, 10, issue, cluster));
		session.insert(new Response1B( a3, 1, 1, 4, 1, 10, issue, cluster));
		if (pass) {
			session.insert(new Response1B( a4, 1, 0, IPCNV.val(), 1, 10, issue, cluster));
		} else {
			session.insert(new Response1B( a4, 1, 2, 5, 1, 10, issue, cluster));
		}
		session.insert(new Response1B( a5, 1, 0, IPCNV.val(), 1, 10, issue, cluster));
		rules.incrementTime();
		if (pass) {
			assertCount("getPowers", "Submit2A", 1);
			assertCount("getPermissions", "Submit2A", 1);
			assertCount("getObligations", "Submit2A", 1);
		} else {
			assertCount("getPowers", "Submit2A", 1);
			assertCount("getPermissions", "Submit2A", 0);
			assertCount("getObligations", "Submit2A", 0);
		}
		
		/*
		 * Time step 3
		 * Leader submits. Check permission to vote for all agents.
		 */
		session.insert( new Submit2A(a1, revision, 10, 3, issue, cluster));
		rules.incrementTime();
		
		// all acceptors can vote either way because leader had power
		assertCount("getPermissions", "Vote2B", 5);
		
		/*
		 * Time step 4
		 * First 2 agents vote correctly, 3rd agent does not.
		 * Check value is not chosen, and voted is not set for 3rd agent
		 */
		session.insert(new Vote2B(a1, 1, 10, 3, issue, cluster));
		session.insert(new Vote2B(a2, 1, 10, 3, issue, cluster));
		session.insert(new Vote2B(a3, 1, 10, 4, issue, cluster));
		rules.incrementTime();
		
		if (pass) {
			//voted count is 4 (2 from before, 2 now)
			assertFactCount("Voted", 4);
		} else {
			// count ag4 for voted, but not reportedVote (since they had a nullvote before)
			assertFactCount("Voted", 5);
		}
		//reportedVote count is 7 (5 from before due to nullvotes, 2 from now)
		assertFactCount("ReportedVote", 7);
		// value was not chosen
		assertFactCount("Chosen", 0);
		
		
		/*
		 * Time step 5
		 * Agent 3 votes correctly, 3 agents have now voted, so chosen
		 * Check correct value has been chosen
		 */
		session.insert( new Vote2B(a3, 1, 10, 3, issue, cluster));
		rules.incrementTime();
		// value was chosen
		Collection<Object> chosens = assertFactCount("Chosen", 1);
		Object chosen = Arrays.asList(chosens.toArray()).get(0);
		Object value = typeFromString("Chosen").get(chosen, "value");
		// correct value was chosen
		assertEquals(value, 3);
		
		
		/*
		 * Time step 6
		 * Remaining 2 agents vote correctly
		 * Check vote counts and correct chosen
		 */
		session.insert( new Vote2B(a4, 1, 10, 3, issue, cluster));
		session.insert( new Vote2B(a5, 1, 10, 3, issue, cluster));
		rules.incrementTime();
		
		if (pass) {
			//voted count is 7 (4 from before, 3 now)
			assertFactCount("Voted", 7);
		} else {
			// count ag4's additional previous vote
			assertFactCount("Voted", 8);
		}
		//reportedVote count is 10 (7 from before, 3 from now)
		assertFactCount("ReportedVote", 10);
		// value was chosen
		assertFactCount("Chosen", 1);
		
		// no risk from adding or removing agents
		assertFactCount("PossibleAddRevision", 0);
		assertFactCount("PossibleRemRevision", 0);
		
		
		outputObjects();
		if (pass) {
			logger.info("Finished narrative1 with successful consensus\n");
		}
		else {
			logger.info("Finished narrative1 with sanction against leader\n");
		}
	}
	
	public void testPossibleRevisionDetectionSetup(HashMap<String,Object> map) throws Exception {
		//specify revision/issue/cluster
		Integer revision = 1;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		// create agents
		IPConAgent a1 = new IPConAgent("a1"); session.insert(a1);
		IPConAgent a2 = new IPConAgent("a2"); session.insert(a2);
		IPConAgent a3 = new IPConAgent("a3"); session.insert(a3);
		ArrayList<Role> leaderRoles = new ArrayList<Role>();
		leaderRoles.add(Role.LEADER);
		leaderRoles.add(Role.PROPOSER);
		leaderRoles.add(Role.ACCEPTOR);
		initAgent(a1, leaderRoles, revision, issue, cluster);
		initAgent(a2, Role.ACCEPTOR, revision, issue, cluster);
		initAgent(a3, Role.ACCEPTOR, revision, issue, cluster);
		map.put("issue", issue);
		map.put("revision", revision);
		map.put("cluster", cluster);
		map.put("a1", a1);
		map.put("a2", a2);
		map.put("a3", a3);
		
		/**/
		//set initially roles
		/*session.insert(new ArrogateLeadership(a1, revision, issue, cluster));
		session.insert(new AddRole(a1, a1, Role.PROPOSER, revision, issue, cluster));
		session.insert(new AddRole(a1, a1, Role.ACCEPTOR, revision, issue, cluster));
		session.insert(new AddRole(a1, a2, Role.ACCEPTOR, revision, issue, cluster));
		session.insert(new AddRole(a1, a3, Role.ACCEPTOR, revision, issue, cluster));*/
		
		// Increment now to stop all agents requiring Sync (because theyre being added at the same time)
		rules.incrementTime();
		
		/*
		 * Set Time step 1 (initially)
		 */
		// Agent voted in previous ballot
		final FactType votedType = typeFromString("Voted");
		Object v1Vote = votedType.newInstance();
		votedType.set(v1Vote, "agent", a1);
		votedType.set(v1Vote, "revision", revision);
		votedType.set(v1Vote, "ballot", 1);
		votedType.set(v1Vote, "value", "A");
		votedType.set(v1Vote, "issue", issue);
		votedType.set(v1Vote, "cluster", cluster);
		session.insert(v1Vote);
		Object v2Vote = votedType.newInstance();
		votedType.set(v2Vote, "agent", a2);
		votedType.set(v2Vote, "revision", revision);
		votedType.set(v2Vote, "ballot", 1);
		votedType.set(v2Vote, "value", "A");
		votedType.set(v2Vote, "issue", issue);
		votedType.set(v2Vote, "cluster", cluster);
		session.insert(v2Vote);
		
		// Agent voted in previous ballot
		final FactType reportedVoteType = typeFromString("ReportedVote");
		Object v1RVote = reportedVoteType.newInstance();
		reportedVoteType.set(v1RVote, "agent", a1);
		reportedVoteType.set(v1RVote, "voteRevision", revision);
		reportedVoteType.set(v1RVote, "voteBallot", 1);
		reportedVoteType.set(v1RVote, "voteValue", "A");
		reportedVoteType.set(v1RVote, "revision", revision);
		reportedVoteType.set(v1RVote, "ballot", 1);
		reportedVoteType.set(v1RVote, "issue", issue);
		reportedVoteType.set(v1RVote, "cluster", cluster);
		session.insert(v1RVote);
		Object v2RVote = reportedVoteType.newInstance();
		reportedVoteType.set(v2RVote, "agent", a2);
		reportedVoteType.set(v2RVote, "voteRevision", revision);
		reportedVoteType.set(v2RVote, "voteBallot", 1);
		reportedVoteType.set(v2RVote, "voteValue", "A");
		reportedVoteType.set(v2RVote, "revision", revision);
		reportedVoteType.set(v2RVote, "ballot", 1);
		reportedVoteType.set(v2RVote, "issue", issue);
		reportedVoteType.set(v2RVote, "cluster", cluster);
		session.insert(v2RVote);
		
		//insert Open_Vote to allow Chosen to kick in
		final FactType openVoteType = typeFromString("Open_Vote");
		Object openVote = openVoteType.newInstance();
		openVoteType.set(openVote, "revision", revision);
		openVoteType.set(openVote, "ballot", 1);
		openVoteType.set(openVote, "value", "A");
		openVoteType.set(openVote, "issue", issue);
		openVoteType.set(openVote, "cluster", cluster);
		session.insert(openVote);
		
		rules.incrementTime();
		
		assertFactCount("IPConAgent", 3);
		// Remember the initial "didnt vote" facts
		assertFactCount("Voted", 5);
		assertFactCount("ReportedVote", 5);
		assertFactCount("HasRole", 5);
		assertFactFieldValue("QuorumSize", "quorumSize", 2);
		
		outputObjects();
		
		// value was chosen
		assertFactCount("Chosen", 1);
		
		// no risk from adding agents
		assertFactCount("PossibleAddRevision", 0);
		// no risk from removing (status quo holds)
		assertFactCount("PossibleRemRevision", 0);

		/*
		 * Time step 2
		 * Insert new agent
		 * Check that there is an obligation to syncreq
		 */
		IPConAgent a4 = new IPConAgent("a4"); session.insert(a4); map.put("a4", a4);
		session.insert(new AddRole(a1, a4, Role.ACCEPTOR, revision, issue, cluster));
		rules.incrementTime();
		
		// check obligation to sync the new agent
		Object obl = Arrays.asList(assertFactCount("Obligation", 1).toArray()).get(0);
		SyncReq fact = null;
		if (typeFromString("Obligation").get(obl, "action") instanceof SyncReq ) {
			fact = (SyncReq)typeFromString("Obligation").get(obl, "action");
		}
		else {
			fail();
		}
		assertEquals(fact.getAgent(), a4);

		assertFactCount("HasRole", 6);
		assertFactCount("Sync", 0);
		assertFactCount("NeedToSync", 1);
		
		//outputObjects();
		
		assertFactFieldValue("QuorumSize", "quorumSize", 2);
		
		// value was chosen
		assertFactCount("Chosen", 1);
		
		// no risk from adding or removing agent because status quo holds
		assertFactCount("PossibleAddRevision", 0);
		assertFactCount("PossibleRemRevision", 0);
		
		/*
		 * Time step 3
		 * Sync the new agent, check new risks
		 */
		session.insert(new SyncReq( a1, a4, "A", revision, issue, cluster));
		rules.incrementTime();
		
		assertFactCount("HasRole", 6);
		assertFactCount("Sync", 1);
		assertFactCount("NeedToSync", 0);
		
		// quorumsize should still be 2 because the agent didn't complete the sync
		assertFactFieldValue("QuorumSize", "quorumSize", 2);
		
		// value was chosen
		assertFactCount("Chosen", 1);
		
		// still no risk because status quo holds
		assertFactCount("PossibleAddRevision", 0);
		assertFactCount("PossibleRemRevision", 0);
		
		/*
		 * Time step 4
		 * Agent syncs no
		 */
		session.insert(new SyncAck( a4, IPCNV.val(), revision, issue, cluster));
		rules.incrementTime();
		
		assertCount("getObligations", "SyncAck", 0);
		
		assertFactCount("HasRole", 6);
		assertFactCount("Sync", 0);
		assertFactCount("NeedToSync", 0);
		
		// quorumsize should be 3 because the agent completed the sync
		assertFactFieldValue("QuorumSize", "quorumSize", 3);
		
		// 5 votes but 6 reportedVotes because syncAck no doesn't count as a vote
		assertFactCount("Voted", 5);
		assertFactCount("ReportedVote", 6);
		
		// value chosen
		assertFactCount("Chosen", 1);
		
		
		// still no risk because status quo holds
		assertFactCount("PossibleAddRevision", 0);
		assertFactCount("PossibleRemRevision", 1);
		
		/*
		 * Time step 5
		 * Ag5 joins
		 */
		
		IPConAgent a5 = new IPConAgent("a5"); session.insert(a5); map.put("a5", a5);
		session.insert(new AddRole(a1, a5, Role.ACCEPTOR, revision, issue, cluster));
		rules.incrementTime();
		
		assertFactCount("HasRole", 7);
		assertFactCount("Sync", 0);
		assertFactCount("NeedToSync", 1);
		assertFactFieldValue("QuorumSize", "quorumSize", 3);
		assertFactCount("Chosen", 1);
		assertFactCount("PossibleAddRevision", 0); // they're not synching yet
		assertFactCount("PossibleRemRevision", 1);
		assertFactCount("Voted", 5);
		assertFactCount("ReportedVote", 6);
		
		/*
		 * Time step 6
		 * Ag5 starts to sync
		 */
		
		session.insert(new SyncReq( a1, a5, "A", revision, issue, cluster));
		rules.incrementTime();
		
		assertFactCount("HasRole", 7);
		assertFactCount("Sync", 1);
		assertFactCount("NeedToSync", 0);
		assertFactFieldValue("QuorumSize", "quorumSize", 3);
		assertFactCount("Chosen", 1);
		assertFactCount("Voted", 5);
		assertFactCount("ReportedVote", 6);
		assertFactCount("PossibleAddRevision", 1); // if ag5 says no, safety is violated, if they say yes then PRR goes away
		assertFactCount("PossibleRemRevision", 1); // if ag1 or 2 leave, safety is violated
		
	}
	
	@Test
	public void testSyncAckNoSafetyViolation() throws Exception {
		HashMap<String, Object> map = new HashMap<String,Object>();
		testPossibleRevisionDetectionSetup(map);
		
		//get data out of map
		IPConAgent a1 = (IPConAgent) map.get("a1");
		IPConAgent a2 = (IPConAgent) map.get("a2");
		IPConAgent a3 = (IPConAgent) map.get("a3");
		IPConAgent a4 = (IPConAgent) map.get("a4");
		IPConAgent a5 = (IPConAgent) map.get("a5");
		Integer revision = (Integer) map.get("revision");
		String issue = (String) map.get("issue");
		UUID cluster = (UUID) map.get("cluster");
		
		
		// Check things are right after Time step 6
		assertFactCount("HasRole", 7);
		assertFactCount("Sync", 1);
		assertFactCount("NeedToSync", 0);
		assertFactFieldValue("QuorumSize", "quorumSize", 3);
		assertFactCount("Chosen", 1);
		assertFactCount("Voted", 5);
		assertFactCount("ReportedVote", 6);
		assertFactCount("PossibleAddRevision", 1); // if ag5 says no, safety is violated, if they say yes then PRR goes away
		assertFactCount("PossibleRemRevision", 1);
		//assertFactCount("Obligation", 2); // check no obligations
		
		outputObjects();
		
		/*
		 * Time step 7
		 * Ag5 says no
		 * Safety should be violated
		 */
		session.insert(new SyncAck(a5, IPCNV.val(), revision, issue, cluster));
		outputObjects();
		rules.incrementTime();
		assertFactCount("HasRole", 7);
		assertFactCount("Sync", 0);
		assertFactCount("NeedToSync", 0);
		assertFactFieldValue("QuorumSize", "quorumSize", 3);
		assertFactCount("Chosen", 1);
		assertFactCount("Voted", 5); // null votes don't get created because it doesnt make any sense
		assertFactCount("ReportedVote", 7); // add a null reportedvote
		
		outputObjects();
		
		assertCount("getObligations", "Revise", 1);
		
		assertFactCount("PossibleAddRevision", 1); // if ag5 says no, safety is violated, if they say yes then PRR goes away
		assertFactCount("PossibleRemRevision", 1);
		
	}
	
	private final FactType typeFromString(String factTypeString) {
		return rules.getKbase().getFactType("uk.ac.imperial.dws04.Presage2Experiments.IPCon", factTypeString);
	}
	
	/**
	 * Wrapper function to check an object from the kbase is a drls FactType. Replaces instanceof functionality.
	 * @param obj
	 * @param factTypeName
	 * @return
	 */
	private final boolean assertFactType(Object obj, String factTypeName) {
		try {
			return ( ( typeFromString(factTypeName).newInstance().getClass() ).equals( ( obj.getClass() ) ) );
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Wrapper function to check an object from the kbase is a drls FactType. Replaces instanceof functionality.
	 * @param obj
	 * @param factTypeName
	 * @return
	 */
	private final boolean assertFactType(Object obj, FactType factType) {
		try {
			return ( ( factType.newInstance().getClass() ).equals( ( obj.getClass() ) ) );
		} catch (NullPointerException e) {
			//e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * @param factTypeString
	 * @param count
	 * @return the collection of facts that matched the query
	 */
	private final Collection<Object> assertFactCount(final String factTypeString, int count) {
		final FactType factType = typeFromString(factTypeString);
		
		// try drls fact types
		Collection<Object> facts = new HashSet<Object>();
		facts.addAll(session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return assertFactType(object, factType);
			}
		}));
		if (facts.size()==0) {
			// try java classes
			facts.addAll(session.getObjects(new ObjectFilter() {
	
				@Override
				public boolean accept(Object object) {
					Class<?> c;
					try {
						c = Class.forName("uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts." + factTypeString);
						//logger.trace("Testing " + object + " which is a " + object.getClass());
						return c.isInstance(object);
					} catch (NullPointerException e) {
						//e.printStackTrace();
						return false;
					} catch (ClassNotFoundException e) {
						//e.printStackTrace();
						return false;
					}
				}
				
			}));
		}
		assertEquals(count, facts.size());
		return facts;
	}
	
	private final void assertCount( final String queryName, final String actionType, final int count) {
		assertEquals(count, filterQueryByAction(queryName, actionType).size());
	}
	
	private final Collection<IPConAction> filterQueryByAction( final String queryName, final String actionType) {	
		HashSet<IPConAction> set = new HashSet<IPConAction>();
		QueryResults results = session.getQueryResults(queryName);
		for ( QueryResultsRow row : results ) {
			set.add((IPConAction)row.get("$action"));
		}
		
		HashSet<IPConAction> result = new HashSet<IPConAction>();
		for (IPConAction action : set) {
			if (action.getClass().getSimpleName().equals(actionType)) {
				result.add(action);
			}
		}
		return result;
		
	}
	
	private final Collection<IPConAction> assertPowerCount( final String actionType, int count) {
		Collection<IPConAction> actions = matchPowers(actionType);
		assertEquals(count, actions.size());
		return actions;
	}
	
	/**
	 * Allows testing the field of the first AND ONLY fact of a specified type
	 * Returns the field you tested, for convenience (?)
	 * @param factTypeString
	 * @param fieldString
	 * @param value
	 * @return field
	 */
	private final Object assertFactFieldValue(String factTypeString, String fieldString, Object value) {
		Collection<Object> facts = assertFactCount(factTypeString, 1);
		Object fact = Arrays.asList(facts.toArray()).get(0);
		Object field = typeFromString(factTypeString).get(fact, fieldString);
		// correct value was chosen
		assertEquals(value, field);
		return field;
	}
	
	private final ArrayList<IPConAction> getAgentPowers(IPConAgent agent) {
		ArrayList<IPConAction> actions = new ArrayList<IPConAction>();
		QueryResults results = session.getQueryResults("agentPowers", new Object[] { agent });
		for ( QueryResultsRow row : results ) {
			actions.add((IPConAction)row.get("$action"));
		}
		logger.info("Agent " + agent.getName() + " has the following powers: " + actions);
		return actions;
	}
	
	private final ArrayList<IPConAction> getAgentPermissions(IPConAgent agent) {
		ArrayList<IPConAction> actions = new ArrayList<IPConAction>();
		QueryResults results = session.getQueryResults("agentPermissions", new Object[] { agent });
		for ( QueryResultsRow row : results ) {
			actions.add((IPConAction)row.get("$action"));
		}
		logger.info("Agent " + agent.getName() + " has the following permissions: " + actions);
		return actions;
	}
	
	private final ArrayList<IPConAction> getAgentObligations(IPConAgent agent) {
		ArrayList<IPConAction> actions = new ArrayList<IPConAction>();
		QueryResults results = session.getQueryResults("agentObligations", new Object[] { agent });
		for ( QueryResultsRow row : results ) {
			actions.add((IPConAction)row.get("$action"));
		}
		logger.info("Agent " + agent.getName() + " has the following obligations: " + actions);
		return actions;
	}
	
	private final HashSet<IPConAction> getPowers() {
		HashSet<IPConAction> set = new HashSet<IPConAction>();
		QueryResults results = session.getQueryResults("getPowers");
		for ( QueryResultsRow row : results ) {
			set.add((IPConAction)row.get("$action"));
		}
		return set;
	}

	private final HashSet<IPConAction> matchPowers(String actionType) {
		HashSet<IPConAction> result = new HashSet<IPConAction>();
		for (IPConAction action : getPowers()) {
			if (action.getClass().getSimpleName().equals(actionType)) {
				result.add(action);
			}
		}
		return result;
	}
	
	private final HashMap<IPConAgent, ArrayList<IPConAction>> getPowersByAgent() {
		HashMap<IPConAgent, ArrayList<IPConAction>> map = new HashMap<IPConAgent, ArrayList<IPConAction>>();
		QueryResults results = session.getQueryResults("getPowers");
		for ( QueryResultsRow row : results ) {
			IPConAgent agent = (IPConAgent)row.get("$agent");
			if (!map.containsKey(agent)) {
				map.put(agent, new ArrayList<IPConAction>() );
			}
			map.get(agent).add((IPConAction)row.get("$action"));
		}
		return map;
	}
	
	private final HashMap<IPConAgent, ArrayList<IPConAction>> matchPowersByAgent(String actionType) {
		HashMap<IPConAgent, ArrayList<IPConAction>> result = new HashMap<IPConAgent, ArrayList<IPConAction>>();
		for (Entry<IPConAgent, ArrayList<IPConAction>> entry : getPowersByAgent().entrySet()) {
			logger.debug("Entry is : " + entry);
			for (IPConAction action : entry.getValue()) {
				logger.trace("Matching against class " + action.getClass().getSimpleName() + " from " + action);
				if (action.getClass().getSimpleName().equals(actionType)) {
					IPConAgent agent = entry.getKey();
					if (!result.containsKey(agent)) {
						result.put(agent, new ArrayList<IPConAction>() );
					}
					result.get(agent).add(action);
				}
			}
		}
		logger.info("Matched " + result.size() + " against " + actionType + " : " + result);
		return result;
	}
	
	

}
