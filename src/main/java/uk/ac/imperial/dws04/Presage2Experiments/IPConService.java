/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments;


import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.drools.runtime.rule.Variable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

/**
 * @author dws04
 *
 */
@Singleton
public class IPConService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(this.getClass());
	final StatefulKnowledgeSession session;

	@Inject
	public IPConService(EnvironmentSharedStateAccess sharedState,
			StatefulKnowledgeSession session) {
		super(sharedState);
		this.session = session;
	}
	
	/**
	 * 
	 * @param queryName should be "getPowers" "getPermissions" or "getObligations"
	 * @param actionType type of action to filter by, or null to get all
	 * @param agent agent to get filter by, or null to get all
	 * @return Collection of IPConActions
	 */
	private final Collection<IPConAction> getActionQueryResults(final String queryName, final String actionType, final IPConAgent agent) {
		HashSet<IPConAction> set = new HashSet<IPConAction>();
		QueryResults results = null;
		// Set agent to look up
		Object lookup = null;
		if (agent==null) {
			lookup = Variable.v;
		}
		else {
			lookup = agent;
		}
		// Add "Actions" to the string to get the other query
		if (actionType!=null) {
			results = session.getQueryResults(queryName+"Actions", new Object[]{ lookup, actionType });
		}
		else {
			results = session.getQueryResults(queryName, new Object[]{ lookup });
		}
		for ( QueryResultsRow row : results ) {
			set.add((IPConAction)row.get("$action"));
		}
		return set;
	}
	
	private final Collection<IPConAction> getActionQueryResultsForRIC(	final String queryName,
			final String actionType,
			final IPConAgent agent,
			final Integer revision,
			final String issue,
			final UUID cluster) {
		HashSet<IPConAction> set = new HashSet<IPConAction>();
		for (IPConAction action : getActionQueryResults(queryName, actionType, agent)) {
			//logger.trace("Checking: " + action);
			if (matchesRIC(action, revision, issue, cluster)) {
				set.add((IPConAction)action);
			}
		}
		return set;
	}
	
	/**
	 * Utility function to check if an object either has-equal, or does not have, the given RIC.
	 * If any arg is null, the check is ignored.
	 * @param object
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @return
	 */
	private final boolean matchesRIC(Object object, Integer revision, String issue, UUID cluster) {
		Integer actionRev = null;
		if (revision!=null) {
			try {
				actionRev = (Integer) object.getClass().getMethod("getRevision").invoke(object, (Object[])null);
			} catch (Exception e) {
				// do nothing - if it doesn't have such a method then stay null
				//e.printStackTrace();
			}
		}
		
		String actionIssue = null;
		if (issue!=null) {
			try {
				actionIssue = (String) object.getClass().getMethod("getIssue").invoke(object, (Object[])null);
			} catch (Exception e) {
				// do nothing
			}
		}
		
		UUID actionCluster = null;
		if (cluster!=null) {
			try {
				actionCluster = (UUID) object.getClass().getMethod("getCluster").invoke(object, (Object[])null);
			} catch (Exception e) {
				// do nothing
			}
		}
		
		//logger.trace("Matching " + object + " against " + revision + ", " + issue + ", " + cluster + 
		//				" and got r:" + actionRev + ", i:" + actionIssue + ", c:" + actionCluster + ".");
		
		return	( ( actionRev==null || actionRev.equals(revision) ) &&
				( actionIssue==null || actionIssue.equals(issue)) &&
				( actionCluster==null || actionCluster.equals(cluster)) );
	}
	
}
