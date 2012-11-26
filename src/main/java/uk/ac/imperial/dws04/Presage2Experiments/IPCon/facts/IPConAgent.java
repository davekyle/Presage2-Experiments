/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts;

import java.util.UUID;

import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * Extends IPConFact despite not having a RIC...
 * 
 * @author dws04
 *
 */
public class IPConAgent extends IPConFact {
	
	String name;
	UUID uuid;

	/**
	 * 
	 */
	public IPConAgent() {
		super(null, null, null);
		this.uuid = Random.randomUUID();
		this.name = uuid.toString();
	}
	
	/**
	 * @param name
	 */
	public IPConAgent(String name) {
		super(null, null, null);
		this.uuid = Random.randomUUID();
		this.name = name;
	}
	
	/**
	 * @param address
	 */
	public IPConAgent(UUID uuid) {
		super(null, null, null);
		this.uuid = uuid;
		this.name = uuid.toString();
	}

	/**
	 * @param uuid
	 * @param name
	 */
	public IPConAgent(UUID uuid, String name) {
		super(null, null, null);
		this.uuid = uuid;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		IPConAgent other = (IPConAgent) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IPConAgent [name=" + name + ", uuid=" + uuid + "]";
	}

	public String getName() {
		return this.name;
	}
	
	public UUID getIPConID() {
		return this.uuid;
	}
	
}
