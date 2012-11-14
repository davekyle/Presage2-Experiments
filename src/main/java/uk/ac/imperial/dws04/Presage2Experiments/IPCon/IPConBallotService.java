/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

/**
 * @author dws04
 *
 */
@Singleton
public class IPConBallotService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(this.getClass());
	final private EnvironmentServiceProvider serviceProvider;
	private Map<IPConRIC, Integer> intMap; 
	
	@Inject
	public IPConBallotService(EnvironmentSharedStateAccess sharedState, 
            EnvironmentServiceProvider serviceProvider) {
        super(sharedState);
        this.serviceProvider = serviceProvider;
        this.intMap = Collections.synchronizedMap(new HashMap<IPConRIC,Integer>());
    }
	
	/**
	 * 
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @return the next unused ballot number for the given RIC
	 */
	public Integer getNext(Integer revision, String issue, UUID cluster) {
		IPConRIC ric = new IPConRIC(revision, issue, cluster);
		if (!intMap.containsKey(ric)) {
			intMap.put(ric, Integer.MIN_VALUE);
		}
		return intMap.get(ric)+1;
	}
	
	/**
	 * 
	 * @param ric
	 * @return the next unused ballot number for the given RIC
	 */
	public Integer getNext(IPConRIC ric) {
		if (!intMap.containsKey(ric)) {
			intMap.put(ric, Integer.MIN_VALUE);
		}
		return intMap.get(ric)+1;
	}

}
