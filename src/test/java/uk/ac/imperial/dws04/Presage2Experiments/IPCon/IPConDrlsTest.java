/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.definition.type.FactType;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.AddRole;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Prepare1A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Response1B;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Submit2A;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Vote2B;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
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
				.addClasspathDrlFile("IPCon_Institutional_Facts.drl")
				.addClasspathDrlFile("IPConUtils.drl")
				.addClasspathDrlFile("IPConPowPer.drl")
				.addClasspathDrlFile("IPConOblSan.drl")
				.addClasspathDrlFile("IPCon.drl"));
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
		logger.info("/objects\n");
	}
	
	public final Collection<Object> assertFactCount(final String factTypeString, int count) {
		final FactType factType = typeFromString(factTypeString);
		
		Collection<Object> facts = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return assertFactType(object, factType);
			}
		});
		assertEquals(count, facts.size());
		return facts;
	}
	
	@Test
	public void basicTest() throws Exception {
		logger.info("\nStarting basicTest()");
		IPConAgent agent = new IPConAgent();
		Integer revision = 0;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		session.insert(agent);
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
		logger.info("Finished basicTest()\n");
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
		assertFactCount("HasRole", 7);
		
		// check theres only one agent can request (the proposer)
		// one proposer has permission
		assertFactCount("RequestPer", 1);
		
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
		assertFactCount("ResponsePer", 0);
		
		/*
		 * Time step 2 :
		 * leader issues prepare, check 5 acceptors can respond
		 */
		session.insert(new Prepare1A(a1, revision, 10, issue, cluster));
		rules.incrementTime();
		
		// check all acceptors can response
		// 5 acceptors have permission
		assertFactCount("ResponsePer", 5);
		/*
		 * Time step 3 :
		 * All agents send a response, check the responsecount and per/obl to submit
		 */
		session.insert(new Response1B( a1, 1, 0, null, 1, 10, issue, cluster));
		session.insert(new Response1B( a2, 1, 1, 4, 1, 10, issue, cluster));
		session.insert(new Response1B( a3, 1, 1, 4, 1, 10, issue, cluster));
		if (pass) {
			session.insert(new Response1B( a4, 1, 0, null, 1, 10, issue, cluster));
		} else {
			session.insert(new Response1B( a4, 1, 2, 5, 1, 10, issue, cluster));
		}
		session.insert(new Response1B( a5, 1, 0, null, 1, 10, issue, cluster));
		rules.incrementTime();
		if (pass) {
			assertFactCount("SubmitPow", 1);
			assertFactCount("SubmitPer", 1);
			assertFactCount("Obligation", 1);
		} else {
			assertFactCount("SubmitPow", 1);
			assertFactCount("SubmitPer", 0);
			assertFactCount("Obligation", 0);
		}
		
		/*
		 * Time step 3
		 * Leader submits. Check permission to vote for all agents.
		 */
		session.insert( new Submit2A(a1, revision, 10, 3, issue, cluster));
		rules.incrementTime();
		
		// all acceptors can vote either way because leader had power
		assertFactCount("VotePer", 5);
		
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
		 * Remaining 3 agents vote correctly
		 * Check vote counts and correct chosen
		 */
		session.insert( new Vote2B(a3, 1, 10, 3, issue, cluster));
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
		
		
		
		outputObjects();
		if (pass) {
			logger.info("Finished narrative1 with successful consensus\n");
		}
		else {
			logger.info("Finished narrative1 with sanction against leader\n");
		}
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
	private boolean assertFactType(Object obj, String factTypeName) {
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
	private boolean assertFactType(Object obj, FactType factType) {
		try {
			return ( ( factType.newInstance().getClass() ).equals( ( obj.getClass() ) ) );
		} catch (NullPointerException e) {
			e.printStackTrace();
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

}
