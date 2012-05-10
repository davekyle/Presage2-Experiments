/**
 * 
 */
package uk.ac.imperial.dws04.utils.record;

import java.util.Comparator;

/**
 * @author dws04
 * @param <T>
 *
 */
public class PairBDescComparator<T extends Comparable<T>> implements Comparator<Pair<?,T>> {

	/**
	 * Will sort in descending order
	 */
	@Override
	public int compare(Pair<?,T> o1, Pair<?,T> o2) {
		return (o2.getB()).compareTo(o1.getB());
	}

}