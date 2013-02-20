/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

/**
 * @author dws04
 *
 */
public class Chosen extends IPConFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1975980506802237667L;
	final Integer ballot;
	final Object value;
	/**
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @param ballot
	 * @param value
	 */
	public Chosen(Integer revision, Integer ballot, Object value, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.ballot = ballot;
		this.value = value;
	}
	/**
	 * @return the ballot
	 */
	public Integer getBallot() {
		return ballot;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ballot == null) ? 0 : ballot.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Chosen other = (Chosen) obj;
		if (ballot == null) {
			if (other.ballot != null)
				return false;
		} else if (!ballot.equals(other.ballot))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Chosen [ballot=" + ballot + ", value=" + value + ", revision="
				+ revision + ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
}
