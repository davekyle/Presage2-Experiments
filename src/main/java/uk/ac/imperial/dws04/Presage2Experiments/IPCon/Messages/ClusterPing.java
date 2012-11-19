/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
import uk.ac.imperial.dws04.utils.record.Pair;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * Class for broadcast message indicating the RIC an agent is in and the currently chosen value, if one exists
 * @author dws04
 *
 */
public class ClusterPing extends BroadcastMessage<Pair<IPConRIC,Object>> {

	/**
	 * Broadcast message indicating the RIC an agent is in and the currently chosen value, if one exists
	 * @param performative
	 * @param type
	 * @param timestamp
	 * @param from
	 * @param data the IPConRIC in question, and the chosen value if one exists, or null otherwise
	 */
	public ClusterPing(Performative performative, Time timestamp,
			NetworkAddress from, final Pair<IPConRIC, Object> data) {
		super(performative, "ClusterPing", timestamp, from, data);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @return the RIC the agent is in
	 */
	public IPConRIC getRIC() {
		return getData().getA();
	}
	
	/**
	 * 
	 * @return The currently chosen value in the RIC, or null if nothing is chosen
	 */
	public Object getValue() {
		return getData().getB();
	}

}
