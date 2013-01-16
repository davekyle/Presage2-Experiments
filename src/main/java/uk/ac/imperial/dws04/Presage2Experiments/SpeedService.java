/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import java.util.UUID;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
//import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;

/**
 * An {@link EnvironmentService} to provide information on the speeds of
 * agents.
 * 
 * <h3>Usage</h3>
 * 
 * <p>
 * Add as a global environment service in the environment
 * <p>
 * 
 * The ServiceDependency on the RoadEnvironmentService ensures that the global shared states have been added
 * 
 * @author dws04
 * 
 */
@ServiceDependencies({ RoadEnvironmentService.class })
public class SpeedService extends EnvironmentService {
	
	EnvironmentServiceProvider serviceProvider;
	//private RoadEnvironmentService roadEnvironmentService;

	@Inject
	public SpeedService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider) {
		super(sharedState);
		this.serviceProvider = serviceProvider;
	}
	
	/*protected RoadEnvironmentService getRoadEnvironmentService(){
		if (roadEnvironmentService == null) {
			try {
				roadEnvironmentService = serviceProvider.getEnvironmentService(RoadEnvironmentService.class);
			} catch (UnavailableServiceException e) {
				throw new RuntimeException(e);
			}
		}
		return roadEnvironmentService;
	}*/
	
	/*public int getMaxSpeed() {
		return (Integer) this.getRoadEnvironmentService().getMaxSpeed();
	}
	
	public int getMaxDecel() {
		return (Integer) this.getRoadEnvironmentService().getMaxDecel();
	}
	
	public int getMaxAccel() {
		return (Integer) this.getRoadEnvironmentService().getMaxAccel();
	}*/
	
	public int getMaxSpeed() {
		return (Integer) this.sharedState.getGlobal("maxSpeed");
	}
	
	public int getMaxDecel() {
		return (Integer) this.sharedState.getGlobal("maxDecel");
	}
	
	public int getMaxAccel() {
		return (Integer) this.sharedState.getGlobal("maxAccel");
	}

	/**
	 * Get the speed of a given agent specified by it's participant UUID.
	 * 
	 * @param participantID
	 *            {@link UUID} of participant to look up
	 * @return {@link RoadSpeed} of participants
	 */
	public int getAgentSpeed(UUID participantID) {
		return (Integer) this.sharedState.get("util.speed", participantID);
	}

	/**
	 * Update this agent's speed to s for the next cycle.
	 * 
	 * 
	 * @param participantID
	 * @param s
	 */
	public void setAgentSpeed(final UUID participantID, final int s) {
		this.sharedState.change("util.speed", participantID, s);
	}
	
	/**
	 * @param speed
	 * @return the distance required to stop at the given speed. Allows one extra cycle of movement at current speed
	 */
	public int getConservativeStoppingDistance(int speed) {
/*		double mD = (Integer)getMaxDecel();
		double n = (((double)speed) / mD);
		return (int) (((((n+1)*n)/2)*mD) + ((double)speed));
	}*/
		double mD = (Integer)getMaxDecel();
		// a is what is left over if speed-nmD is not 0
		double a = ((double)speed) % mD;
		double n = ((((double)speed)-a) / mD);
		return (int)(((n/2)*( 2*a + ((n+1)*mD) )) + a + ((double)speed));
	}
	
	/**
	 * @param speed
	 * @return the distance required for the given agent to stop.
	 *//*
	public int getStoppingDistanceOld(UUID agent) {
		double speed = getAgentSpeed(agent);
		double mD = (Integer)getMaxDecel();
		double n = (speed / mD);
		return (int) (((((n+1)*n)/2)*mD));
	}*/
	
	/**
	 * @param speed
	 * @return the distance required for the given agent to stop.
	 */
	public int getStoppingDistance(UUID agent) {
		double speed = getAgentSpeed(agent);
		double mD = (Integer)getMaxDecel();
		// a is what is left over if speed-nmD is not 0
		double a = speed % mD;
		double n = ((speed-a) / mD);
		/*if (a!=0) {
			n = Math.floor(n);
		}*/
		//double temp;
		// need to add a if a is not 0 to account for the last cycle
		//System.out.println("speed=" + speed + " mD=" + mD + " a=" + a + " n=" + n);
		//System.out.println("Old=" + getStoppingDistanceOld(agent));
		/*temp = ((n/2)*( 2*a + ((n+1)*mD) ));
		if (a!=0) {
			temp = temp+a;
		}*/
		return (int)(((n/2)*( 2*a + ((n+1)*mD) )) + a);
	}
	
	/**
	 * @param speed
	 * @param checkingFront true if you're checking agents infront of you (so they could decel); false if checking behind you (so could accel)
	 * @return the distance required for the given agent to stop TAKING INTO ACCOUNT THEY CAN {AC/DE}CEL BEFORE DOING WHAT YOU CALCULATE.
	 */
	public int getAdjustedStoppingDistance(UUID agent, boolean checkingFront) {
		double mD = (Integer)getMaxDecel();
		double mA = (Integer)getMaxAccel();
		double speed;
		if (checkingFront) {
			speed = getAgentSpeed(agent)-mD;
		}
		else {
			speed = getAgentSpeed(agent)+mA;
		}
		// stop them thinking cars can move backwards
		if (speed<0) speed = 0;
		// a is what is left over if speed-nmD is not 0
		double a = speed % mD;
		double n = ((speed-a) / mD);
		/*if (a!=0) {
			n = Math.floor(n);
		}*/
		//double temp;
		// need to add a if a is not 0 to account for the last cycle
		//System.out.println("speed=" + speed + " mD=" + mD + " a=" + a + " n=" + n);
		//System.out.println("Old=" + getStoppingDistanceOld(agent));
		/*temp = ((n/2)*( 2*a + ((n+1)*mD) ));
		if (a!=0) {
			temp = temp+a;
		}*/
		return (int)(((n/2)*( 2*a + ((n+1)*mD) )) + a);
	}
	
	/**
	 * 
	 * @param dist distance in which you want to stop
	 * @return the speed you need to be travelling at, or any negative if it is not possible
	 */
	public int getSpeedToStopInDistance(int dist) {
		double mD = (Integer)getMaxDecel();
		if (dist<=mD) {
			return dist;
		}
		else {
			double n;
			/*
			 * equation is :
			 *  sum-to-n * mD = dist
			 *  (((n+1)n)/2)*mD = dist
			 *  n^2+n = (2*(dist/mD))
			 *  n^2 + n - (2*(dist/mD)) = 0
			 *  use x = ( -b +/- sqrt(b^2-4ac)) / 2a
			 *  n = ( -1 +/- sqrt(1+(8*(dist/mD))) ) / 2
			 *  we want the -'ve because we use a +ve mD not a -ve one...
			 */
			n = ( -1 - Math.sqrt(1+(8*(((double)dist)/mD))) ) / 2;
			// We need to invert the value because we're taking the -ve root above... :P
			// -mD for correct result - you have to be travelling LESS THAN the result...
			return (0-((Double)(n*mD)).intValue())-((int)Math.floor(mD));
		}
	}

	public void removeAgent(UUID uuid) {
		sharedState.delete("util.speed", uuid);
	}
}
