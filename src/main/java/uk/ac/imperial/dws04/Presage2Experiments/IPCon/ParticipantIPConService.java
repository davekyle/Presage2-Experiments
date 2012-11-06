/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import com.google.inject.Inject;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConFact;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.participant.Participant;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;

/**
 * @author dws04
 *
 */
public class ParticipantIPConService extends IPConService {

	protected final IPConAgent handle;
	private final Logger logger = Logger.getLogger(ParticipantIPConService.class);
	
	public ParticipantIPConService(Participant p, EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider) {
		// construct from the protected one, then hope it injects correctly !
		super(sharedState, serviceProvider);
		if (p instanceof HasIPConHandle) {
			this.handle = ((HasIPConHandle)p).getIPConHandle();
		}
		else {
			this.handle = null;
			logger.error(p.getID() + " does not have an IPCon handle...");
		}
    }
	
	private StatefulKnowledgeSession getSession() {
		if (this.session==null) {
			setSession(session);
		}
		return this.session;
	}
	
	@Override
	/**
	 * Returns the RICs that the agent is a member of
	 */
	public Collection<IPConRIC> getCurrentRICs(){
		return super.getCurrentRICs(this.handle);
	}
	
	@Override
	/**
	 * Returns the RICs that the agent is a member of
	 */
	public Collection<IPConRIC> getCurrentRICs(IPConAgent handle) {
		if (handle.equals(this.handle)) {
			return super.getCurrentRICs(handle);
		}
		else {
			throw new SharedStateAccessException("A participant may not view another agent's RICs!");
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getObligations(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Collection<IPConAction> getObligations(IPConAgent agent,
			Integer revision, String issue, UUID cluster) {
		if (agent.equals(this.handle)) {
			return super.getObligations(agent, revision, issue, cluster);
		}
		else {
			throw new SharedStateAccessException("A participant (" + this.handle + ") may not view another agent's (" + agent + ") obligations!");
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getObligations(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Collection<IPConAction> getPermissions(IPConAgent agent,
			Integer revision, String issue, UUID cluster) {
		if (agent.equals(this.handle)) {
			return super.getPermissions(agent, revision, issue, cluster);
		}
		else {
			throw new SharedStateAccessException("A participant may not view another agent's permissions!");
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getObligations(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Collection<IPConAction> getPowers(IPConAgent agent,
			Integer revision, String issue, UUID cluster) {
		if (agent.equals(this.handle)) {
			return super.getPowers(agent, revision, issue, cluster);
		}
		else {
			throw new SharedStateAccessException("A participant may not view another agent's powers!");
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getFactQueryResults(java.lang.String, java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Collection<IPConFact> getFactQueryResults(String factType,
			Integer revision, String issue, UUID cluster) {
		throw new SharedStateAccessException("getFactQueryResults not available to agents!");
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getActionQueryResultsForRIC(java.lang.String, java.lang.String, uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Collection<IPConAction> getActionQueryResultsForRIC(
			String queryName, String actionType, IPConAgent agent,
			Integer revision, String issue, UUID cluster) {
		throw new SharedStateAccessException("getActionQueryResultsForRIC not available to agents!");
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getQuorumSize(java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Integer getQuorumSize(Integer revision, String issue, UUID cluster) {
		if ( revision!=null && issue!=null && cluster!=null && (!super.getAgentRoles(this.handle, revision, issue, cluster).isEmpty()) ) {
			return super.getQuorumSize(revision, issue, cluster);
		}
		else {
			throw new SharedStateAccessException("A participant may not view info about a RIC they are not in!");
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConService#getAgentRoles(uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent, java.lang.Integer, java.lang.String, java.util.UUID)
	 */
	@Override
	public Collection<HasRole> getAgentRoles(IPConAgent agent,
			Integer revision, String issue, UUID cluster) {
		if ( ( revision!=null && issue!=null && cluster!=null && (!super.getAgentRoles(this.handle, revision, issue, cluster).isEmpty()) ) ||
				(agent.equals(this.handle)) ) {
			return super.getAgentRoles(agent, revision, issue, cluster);
		}
		else {
			throw new SharedStateAccessException("A participant may not view another agent's roles unless they are in the same RIC!");
		}
	}

	
	
	
	
	
}
