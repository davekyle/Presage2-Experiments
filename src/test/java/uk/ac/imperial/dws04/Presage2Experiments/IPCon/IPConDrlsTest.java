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
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.ArrogateLeadership;
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
		session.setGlobal("logger", this.logger);
		//session.setGlobal("session", session);
		//session.setGlobal("storage", null);
		
		for (Role role : Role.values()) {
			session.insert(role);
		}
	}

	@After
	public void tearDown() throws Exception {
		session.dispose();
	}
	
	@Test
	public void basicTest() throws Exception {
		NetworkAddress agent = new NetworkAddress(Random.randomUUID());
		Integer revision = 0;
		String issue = "IssueString";
		UUID cluster = Random.randomUUID();
		session.insert(agent);
		session.insert(new ArrogateLeadership(agent, revision, issue, cluster));
		rules.incrementTime();
		
		Collection<Object> objects = session.getObjects();
		logger.info("Objects: " + objects.toString() + " are :");
		for (Object object : objects) {
			logger.info(object);
		}
		logger.info("/objects");
		
		//String hasRoleTypeString = "HasRole";
		final FactType hasRoleType = typeFromString("HasRole");
		
		Collection<Object> hasRoles = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return assertFactType(object, hasRoleType);
			}
		});
		assertEquals(1, hasRoles.size());
		Object hasRole = Arrays.asList(hasRoles.toArray()).get(0);
		Role role = (Role) hasRoleType.get(hasRole, "role");
		assertEquals(role.toString(), "LEADER");
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
