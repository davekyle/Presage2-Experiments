package uk.ac.imperial.dws04.Presage2Experiments.MoveComparators;
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
public class ExitMoveComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<CellMove, V>> {

	/** 
	 * When sorted with this, agents will move as slow as safely possible (2nd arg is safety weight) and prefer to move to lane0
	 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 *
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
	 * Sorts by speed (lower better) then lane (lowest better)
	 */
	private int compareCellMove(CellMove arg0, CellMove arg1) {
		int compareSpeed = ((Integer)arg1.getYInt()).compareTo( arg0.getYInt() );
		if (compareSpeed!=0) {
			return compareSpeed;
		}
		else {
			int compareLane = ((Integer)arg1.getXInt()).compareTo(arg0.getXInt());
			if (compareLane!=0) {
				return compareLane;
			}
			else {
				// prefer undertake to overtake
				return ((Integer)arg1.getXInt()).compareTo(((Integer)arg0.getXInt()));
			}
		}
	}
	

}
