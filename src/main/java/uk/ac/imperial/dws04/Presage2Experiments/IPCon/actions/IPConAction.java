/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

/**
 * @author dave
 *
 */
public abstract class IPConAction {
	
	/**
	 * Indicates whether this action *could* fulfil an obligation to perform the given action (also requires other checks on the obl)
	 * @param action
	 * @return true if this action is "the same as" the argument with the exception of the agent
	 */
	public abstract boolean fulfils(IPConAction action);

}
