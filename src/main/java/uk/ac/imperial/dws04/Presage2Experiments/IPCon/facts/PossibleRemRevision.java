/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

/**
 * @author dws04
 *
 */
public class PossibleRemRevision extends IPConFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4611015044360357227L;
	final Object value;

	/**
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @param value
	 */
	public PossibleRemRevision(Object value, Integer revision, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.value = value;
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
		PossibleRemRevision other = (PossibleRemRevision) obj;
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
		return "PossibleRemRevision [value=" + value + ", revision=" + revision
				+ ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	

}
