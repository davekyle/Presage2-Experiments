/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * @author dws04
 *
 */
public class IPConActionMsg extends BroadcastMessage<IPConAction> {

	public IPConActionMsg(Performative performative, Time timestamp, NetworkAddress from, IPConAction data) {
		super(performative, "IPConActionMsg[" + data.getClass().getSimpleName() + "]", timestamp, from, data);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type + "[time=" + timestamp + ", from=" + from + ", IPConAction=" + data + "]";
	}

}
