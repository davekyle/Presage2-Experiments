/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

/**
 * @author dws04
 *
 */
public class Pre_Vote extends IPConFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5843776959610250625L;
	final Integer ballot;

	/**
	 * @param revision
	 * @param issue
	 * @param cluster
	 */
	public Pre_Vote(Integer revision, Integer ballot, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.ballot = ballot;
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
		result = prime * result + ((ballot == null) ? 0 : ballot.hashCode());
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
		Pre_Vote other = (Pre_Vote) obj;
		if (ballot == null) {
			if (other.ballot != null)
				return false;
		} else if (!ballot.equals(other.ballot))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pre_Vote [ballot=" + ballot + ", revision=" + revision
				+ ", issue=" + issue + ", cluster=" + cluster + "]";
	}

}
