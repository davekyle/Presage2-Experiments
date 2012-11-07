/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.lang.reflect.Field;

/**
 * @author dws04
 *
 */
public abstract class IPConAction {
	
	/**
	 * Indicates whether this action *could* fulfil an obligation to perform the given action (also requires other checks on the obl)
	 * @param obligation
	 * @return true if this action is "the same as" the argument with the exception of the agent
	 */
	public final boolean fulfils(IPConAction obligation) {
		// check for equality
		if (this.equals(obligation)) return true;
		// check for different class
		else if (!(this.getClass().isAssignableFrom(obligation.getClass()))) return false;
		else {
			boolean result = true;
			// check each field is either null or the same
			for (Field f : this.getClass().getFields()) {
				if (!result) return false;
				else {
					try {
						result = result && (
								(f.get(obligation).equals(null)) || 
								(f.get(this).equals(f.get(obligation)))
								);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			}
			return result;
		}
		
	}
	
	/**
	 * Copy constructor...
	 * @return new action with same values
	 */
	public abstract IPConAction copy();

}
