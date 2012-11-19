/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.*;

/**
 * @author dws04
 *
 */
public class IPConActionMsg extends BroadcastMessage<IPConAction> {

	public IPConActionMsg(Performative performative, Time timestamp, NetworkAddress from, IPConAction data) {
		super(performative, "IPConActionMsg[" + data.getClass().getSimpleName() + "]", timestamp, from, data);
		// TODO Auto-generated constructor stub
	}

}
