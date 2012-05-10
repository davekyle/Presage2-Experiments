/**
 * 
 */
package uk.ac.imperial.dws04.utils.record;

import java.util.Map.Entry;

/**
 * Because I want a tuple class damnit.
 * 
 * @author dws04
 *
 */
public class Pair<A,B> {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 8428350406115588233L;
	
	private A a;
	private B b;
	
	public Pair(){
		super();
		this.a = null;
		this.b = null;
	}
	
	public Pair(A a, B b){
		super();
		this.a = a;
		this.b = b;
	}

	/**
	 * @return the a
	 */
	public A getA() {
		return a;
	}
	
	/**
	 * @return the b
	 */
	public B getB() {
		return b;
	}
	
	/**
	 * @param a the a to set
	 */
	public void setA(A a) {
		this.a = a;
	}
	
	/**
	 * @param b the b to set
	 */
	public void setB(B b) {
		this.b = b;
	}
	
	/**
	 * simpler setter
	 */
	public void set(A a, B b){
		this.a = a;
		this.b = b;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Pair entryToPair(Entry entry){
		return new Pair(entry.getKey(), entry.getValue());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pair{@" + Integer.toHexString(this.hashCode()) + "} [" + a + "," + b + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		return true;
	}

/*	@Override
	public int compareTo(Pair<A, B> o) {
		int result = (this.getA()).compareTo(o.getA());
		if (result==0){
			result = this.getB().compareTo(o.getB());
		}
		return result;
	}*/
	
}

