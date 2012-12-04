/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import uk.ac.imperial.presage2.core.Action;

public abstract class TimeStampedAction implements Action {

	int t;

	TimeStampedAction() {
		super();
	}

	TimeStampedAction(int t) {
		super();
		this.t = t;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

}
