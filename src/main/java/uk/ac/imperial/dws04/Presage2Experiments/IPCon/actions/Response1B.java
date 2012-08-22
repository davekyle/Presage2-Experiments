/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

import java.util.UUID;

import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * @author dave
 *
 */
public class Response1B extends IPConAction {
	public NetworkAddress agent;
	public Integer voteRevision;
	public Integer voteBallot;
	public Object voteValue;
	public Integer revision;
	public Integer ballot;
	public String issue;
	public UUID cluster;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((ballot == null) ? 0 : ballot.hashCode());
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		result = prime * result + ((issue == null) ? 0 : issue.hashCode());
		result = prime * result
				+ ((revision == null) ? 0 : revision.hashCode());
		result = prime * result
				+ ((voteBallot == null) ? 0 : voteBallot.hashCode());
		result = prime * result
				+ ((voteRevision == null) ? 0 : voteRevision.hashCode());
		result = prime * result
				+ ((voteValue == null) ? 0 : voteValue.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response1B other = (Response1B) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (ballot == null) {
			if (other.ballot != null)
				return false;
		} else if (!ballot.equals(other.ballot))
			return false;
		if (cluster == null) {
			if (other.cluster != null)
				return false;
		} else if (!cluster.equals(other.cluster))
			return false;
		if (issue == null) {
			if (other.issue != null)
				return false;
		} else if (!issue.equals(other.issue))
			return false;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		if (voteBallot == null) {
			if (other.voteBallot != null)
				return false;
		} else if (!voteBallot.equals(other.voteBallot))
			return false;
		if (voteRevision == null) {
			if (other.voteRevision != null)
				return false;
		} else if (!voteRevision.equals(other.voteRevision))
			return false;
		if (voteValue == null) {
			if (other.voteValue != null)
				return false;
		} else if (!voteValue.equals(other.voteValue))
			return false;
		return true;
	}
}
