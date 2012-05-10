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
public class PairBAscComparator<T extends Comparable<T>> implements Comparator<Pair<?,T>> {

	/**
	 * Will sort in ascending order
	 */
	@Override
	public int compare(Pair<?,T> o1, Pair<?,T> o2) {
		return (o1.getB()).compareTo(o2.getB());
	}

}