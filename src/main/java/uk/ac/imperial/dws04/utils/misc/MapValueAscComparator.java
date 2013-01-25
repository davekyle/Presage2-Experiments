package uk.ac.imperial.dws04.utils.misc;
/**
 * 
 */


import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author dws04
 *
 */
public class MapValueAscComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<?, V>> {

	/** 
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Entry<?, V> arg0, Entry<?, V> arg1) {
		int compare = arg0.getValue().compareTo(arg1.getValue());
		if (compare<0) {
			return compare;
		}
		else {
			return 1;
		}
	}
	

}
