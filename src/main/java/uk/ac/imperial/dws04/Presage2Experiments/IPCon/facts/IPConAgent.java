/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * @author dave
 *
 */
public class IPConAgent {
	
	NetworkAddress address;

	/**
	 * 
	 */
	public IPConAgent() {
		super();
		this.address = new NetworkAddress(Random.randomUUID());
	}
	
	/**
	 * @param address
	 */
	public IPConAgent(NetworkAddress address) {
		super();
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
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
		IPConAgent other = (IPConAgent) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IPConAgent [address=" + address + "]";
	}
	
}
