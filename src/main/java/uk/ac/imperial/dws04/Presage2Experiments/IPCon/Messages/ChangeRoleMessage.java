/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.MessageType;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConRoleChangeMessageData;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.BroadcastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.protocols.FSMConversation;

/**
 * @author dws04
 *
 */
public class ChangeRoleMessage extends BroadcastMessage<IPConRoleChangeMessageData> {
	
	ChangeRoleMessage(FSMConversation conv, NetworkAddress agentId, String issue, Integer ballotNum, Boolean addOrRem, Role oldRole, Role newRole) {
		super(Performative.INFORM, MessageType.INTERNAL_ROLE_CHANGE.name(), SimTime.get(), conv.getNetwork().getAddress(), new IPConRoleChangeMessageData(agentId, issue, ballotNum, addOrRem, oldRole, newRole));
	}
	
	/**
	 * @return the agentId
	 */
	public NetworkAddress getAgentId() {
		return data.getAgentId();
	}

	/**
	 * @return the issue
	 */
	public String getIssue() {
		return data.getIssue();
	}

	/**
	 * @return the addRole
	 */
	public Boolean getAddRole() {
		return data.getAddRole();
	}

	/**
	 * @return the oldRole
	 */
	public Role getOldRole() {
		return data.getOldRole();
	}

	/**
	 * @return the newRole
	 */
	public Role getNewRole() {
		return data.getNewRole();
	}

	/**
	 * Don't think this is relevant
	 * @return the ballotNum
	 */
	@Deprecated
	public Integer getBallotNum() {
		return data.getBallotNum();
	}
}
