/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.ArrayList;
import java.util.Arrays;
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
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConFact;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.QuorumSize;
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
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @return the quorum size for the RIC specified
	 */
	public Integer getQuorumSize(Integer revision, String issue, UUID cluster) {
		ArrayList<IPConFact> obj = new ArrayList<IPConFact>();
		obj.addAll(getFactQueryResults("QuorumSize", revision, issue, cluster));
		if (obj.size()==1) {
			return ((QuorumSize)obj.get(0)).getQuorumSize();
		}
		else {
			logger.warn("Got multiple values for getQuorumSize(" + revision + "," + issue + "," + cluster  + ") : " + obj);
			return 0;
		}
	}
	
	/**
	 * 
	 * @param factType typename of facts to match, or null to match all
	 * @param revision revision to match, or null to match all
	 * @param issue issue to match, or null to match all
	 * @param cluster cluster to match, or null to match all
	 * @return facts matching the query
	 */
	public Collection<IPConFact> getFactQueryResults(
			final String factType,
			final Integer revision,
			final String issue,
			final UUID cluster) {
		String queryString = "getFacts";
		HashSet<IPConFact> set = new HashSet<IPConFact>();
		ArrayList<Object> lookup = new ArrayList<Object>();
		lookup.addAll(Arrays.asList(new Object[]{revision, issue, cluster}));
		if (revision==null) {
			lookup.set(0, Variable.v);
		}
		if (issue==null) {
			lookup.set(1, Variable.v);
		}
		if (cluster==null) {
			lookup.set(2, Variable.v);
		}
		if (factType!=null) {
			queryString = queryString+"Named";
			lookup.add(factType);
		}
		QueryResults facts = session.getQueryResults(queryString, lookup.toArray());
		for (QueryResultsRow row : facts) {
			set.add((IPConFact)row.get("$fact"));
		}
		return set;
	}
	
	/**
	 * 
	 * @param queryName should be "getPowers" "getPermissions" or "getObligations"
	 * @param actionType
	 * @param agent
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @return matching actions; use null for any argument except queryName to get all matching
	 */
	public Collection<IPConAction> getActionQueryResultsForRIC(
			final String queryName,
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
	
	/**
	 * Utility function to check if an object either has-equal, or does not have, the given RIC.
	 * If any arg is null, the check for that arg is ignored.
	 * @param object
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @return true if object's RIC match the given arguments. Any null value (on either side of the check) acts as if that value matches.
	 */
	private final boolean matchesRIC(Object object, Integer revision, String issue, UUID cluster) {
		Integer actionRev = null;
		if (revision!=null) {
			try {
				// Check to see if object's class has a getRevision method, and invoke if it does
				actionRev = (Integer) object.getClass().getMethod("getRevision").invoke(object, (Object[])null);
			} catch (Exception e) {
				// do nothing - if it doesn't have such a method then stay null
				//e.printStackTrace();
			}
		}
		// If rev was null, then aR will be the initialised null, so do nothing and it will pass.
		
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
		
		// Check to see if the values match.
		return	( ( actionRev==null || actionRev.equals(revision) ) &&
				( actionIssue==null || actionIssue.equals(issue)) &&
				( actionCluster==null || actionCluster.equals(cluster)) );
	}
	
}
