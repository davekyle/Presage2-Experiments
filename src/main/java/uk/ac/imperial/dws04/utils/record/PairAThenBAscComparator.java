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
public class PairAThenBAscComparator<T extends Comparable<T>,T1 extends Comparable<T1>> implements Comparator<Pair<T,T1>> {

	/**
	 * Sorts the Pairs by A then B in ascending order
	 */
	@Override
	public int compare(Pair<T,T1> o1, Pair<T,T1> o2) {
		if (o1.getA().compareTo(o2.getA())==0) {
			return (o1.getB()).compareTo(o2.getB());
		}
		else
			return o1.getA().compareTo(o2.getA());
	}

}