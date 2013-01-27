package uk.ac.imperial.dws04.utils.misc;
/**
 * 
 */


import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.presage2.util.location.CellMove;

/**
 * @author dws04
 *
 */
public class WeightedMoveComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<CellMove, V>> {

	/** 
	 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 * 
	 * Should order by Value in Ascending order. When values are equal, will place keys of 0 first.
	 * This prevents the agents changing lanes all the time.
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Entry<CellMove, V> arg0, Entry<CellMove, V> arg1) {
		int compare = arg0.getValue().compareTo(arg1.getValue());
		if (compare!=0) {
			return compare;
		}
		else {
			// values are equal - doing ascending, so want 0keys to be *last* (better)
			return compareCellMove(arg0.getKey(), arg1.getKey());
		}
	}
	
	/*
	 * Sorts by speed (highest better) then lane (closest to no-change better)
	 */
	private int compareCellMove(CellMove a, CellMove b) {
		int compare = ((Integer)a.getYInt()).compareTo( b.getYInt() );
		if (compare!=0) {
			return compare;
		}
		else {
			return Math.abs( ((Integer)a.getXInt()).compareTo( b.getXInt() ) );
		}
	}
	

}
