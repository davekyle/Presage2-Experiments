/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

/**
 * @author dws04
 *
 */
public class ReportedVote extends IPConFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5336534921341514835L;
	final IPConAgent agent;
	final Integer voteRevision;
	final Integer voteBallot;
	final Object voteValue;
	final Integer ballot;
	
	/**
	 * @param agent
	 * @param voteRevision
	 * @param voteBallot
	 * @param revision
	 * @param ballot
	 * @param issue
	 * @param cluster
	 */
	public ReportedVote(IPConAgent agent, Integer voteRevision, Integer voteBallot, Object voteValue, 
			Integer revision, Integer ballot, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.agent = agent;
		this.voteRevision = voteRevision;
		this.voteBallot = voteBallot;
		this.voteValue = voteValue;
		this.ballot = ballot;
	}

	/**
	 * @return the agent
	 */
	public IPConAgent getAgent() {
		return agent;
	}

	/**
	 * @return the voteRevision
	 */
	public Integer getVoteRevision() {
		return voteRevision;
	}

	/**
	 * @return the voteBallot
	 */
	public Integer getVoteBallot() {
		return voteBallot;
	}

	/**
	 * @return the voteValue
	 */
	public Object getVoteValue() {
		return voteValue;
	}

	/**
	 * @return the ballot
	 */
	public Integer getBallot() {
		return ballot;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((ballot == null) ? 0 : ballot.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportedVote other = (ReportedVote) obj;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportedVote [agent=" + agent + ", voteRevision="
				+ voteRevision + ", voteBallot=" + voteBallot + ", voteValue="
				+ voteValue + ", ballot=" + ballot + ", revision=" + revision
				+ ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
	
}
