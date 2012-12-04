/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions;

/**
 * @author dws04
 *
 */
public class IPConTime {

	Integer time;

	public IPConTime(Integer time) {
		this.time = time;
	}

	/**
	 * @return the time
	 */
	public Integer getTime() {
		return time;
	}


	/**
	 * @param time the time to set
	 */
	public void setTime(Integer time) {
		this.time = time;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IPConTime [time=" + time + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		IPConTime other = (IPConTime) obj;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}
}
