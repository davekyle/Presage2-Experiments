/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

/**
 * Class to hold global shared state variables for the RoadSimulation.
 * Currently creates empty junctionLocations list if no junctions. 
 * 
 * @author dws04
 *
 */
@Singleton
public class RoadEnvironmentService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(RoadEnvironmentService.class);
	private final ArrayList<Integer> junctionLocations;
	
	@Inject
	protected RoadEnvironmentService(EnvironmentSharedStateAccess sharedState,
			@Named("params.length") int length, @Named("params.junctionCount") int junctionCount,
			@Named("params.lanes") int lanes, @Named("params.maxSpeed") int maxSpeed,
			@Named("params.maxAccel") int maxAccel, @Named("params.maxDeccel") int maxDeccel){
		super(sharedState);
		junctionLocations = createJunctionList(length, junctionCount);
		sharedState.createGlobal("junctionLocations", junctionLocations);
		sharedState.createGlobal("maxSpeed", maxSpeed);
		sharedState.createGlobal("maxAccel", maxAccel);
		sharedState.createGlobal("maxDeccel", maxDeccel);
		sharedState.createGlobal("length", length);
		sharedState.createGlobal("lanes", lanes);
	}
	
	/**
	 * Creates list of locations at which to place evenly-spaced junctions. (length*(0->jcCount/jCount+1) 
	 * @param length
	 * @param junctionCount
	 * @return
	 */
	private final ArrayList<Integer> createJunctionList(int length, int junctionCount) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		if (junctionCount!=0){
			// length*(i/(junctionCount+1) -> (length/(junctionCount+1))*i to avoid Integer rounding
			for (int i = 1; i<=junctionCount; i++) {
				//Integer temp = length*(i/(junctionCount+1));
				result.add((length/(junctionCount+1))*i);
				logger.trace("Added a junction at " + (length/(junctionCount+1))*i);
			}
		}
		return result;
	}

	public int getMaxSpeed() {
		return (Integer) this.sharedState.getGlobal("maxSpeed");
	}

	public int getLength() {
		return (Integer) this.sharedState.getGlobal("length");
	}

}
