/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.dws04.Presage2Experiments.IPConService;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.SharedStateAccessException;
import uk.ac.imperial.presage2.core.participant.Participant;

/**
 * @author dws04
 *
 */
public class ParticipantIPConService extends IPConService {

	private final EnvironmentServiceProvider serviceProvider;
	protected final IPConAgent handle;
	private final Logger logger = Logger.getLogger(ParticipantIPConService.class);
	
	ParticipantIPConService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider, HasIPConHandle p, StatefulKnowledgeSession session) {
		super(sharedState, session);
		this.serviceProvider = serviceProvider;
		this.handle = p.getIPConHandle();
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
	
}
