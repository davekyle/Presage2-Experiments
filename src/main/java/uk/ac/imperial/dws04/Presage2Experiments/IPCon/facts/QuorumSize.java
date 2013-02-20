/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

/**
 * @author dws04
 *
 */
public class QuorumSize extends IPConFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3143585996481465946L;
	Integer quorumSize;

	/**
	 * @param revision
	 * @param issue
	 * @param cluster
	 * @param quorumSize
	 */
	public QuorumSize(Integer quorumSize, Integer revision, String issue, UUID cluster) {
		super(revision, issue, cluster);
		this.quorumSize = quorumSize;
	}

	/**
	 * @return the quorumSize
	 */
	public Integer getQuorumSize() {
		return quorumSize;
	}

	/**
	 * @param quorumSize the quorumSize to set
	 */
	public void setQuorumSize(Integer quorumSize) {
		this.quorumSize = quorumSize;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((quorumSize == null) ? 0 : quorumSize.hashCode());
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
		QuorumSize other = (QuorumSize) obj;
		if (quorumSize == null) {
			if (other.quorumSize != null)
				return false;
		} else if (!quorumSize.equals(other.quorumSize))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QuorumSize [quorumSize=" + quorumSize + ", revision="
				+ revision + ", issue=" + issue + ", cluster=" + cluster + "]";
	}
	
}
