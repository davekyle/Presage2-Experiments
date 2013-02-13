/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.event.Event;

/**
 * Event to signal the simulation should end early
 * 
 * @author dws04
 *
 */
public class FinishEarlyEvent implements Event {

	final Time t;

	FinishEarlyEvent(Time t) {
		super();
		this.t = t;
	}

	public Time getTime() {
		return t;
	}
	
}
