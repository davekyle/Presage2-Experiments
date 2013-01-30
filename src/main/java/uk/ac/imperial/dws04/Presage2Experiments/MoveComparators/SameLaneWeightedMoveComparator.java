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
public class SameLaneWeightedMoveComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<CellMove, V>> {
	
	private final Integer currentSpeed; 
	
	public SameLaneWeightedMoveComparator(Integer currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	/** 
	 * When sorted with this, agents will move in same lame if possible, as safely as possible (2nd arg is safety weight) at constant speed if possible
	 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 *
	 * Note: this operator imposes an ordering that is inconsistent with equals.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Entry<CellMove, V> arg0, Entry<CellMove, V> arg1) {
		// sort by staying in lane
		Integer arg0LaneDelta = Math.abs(arg0.getKey().getXInt());
		Integer arg1LaneDelta = Math.abs(arg1.getKey().getXInt());
		int compareLane = arg1LaneDelta.compareTo(arg0LaneDelta);
		if (compareLane!=0) {
			return compareLane;
		}
		else {
			// sort by safety (safest)
			int compareSafety = arg0.getValue().compareTo(arg1.getValue());
			if (compareSafety!=0) {
				return compareSafety;
			}
			else {
				// sort by speed (closest to current)
				Integer arg0SpeedDelta = Math.abs(currentSpeed-arg0.getKey().getYInt());
				Integer arg1SpeedDelta = Math.abs(currentSpeed-arg1.getKey().getYInt());
				int compareSpeed = arg1SpeedDelta.compareTo( arg0SpeedDelta );
				// if all three are the same, then we don't care which you do :P
				return compareSpeed;
			}
			
		}
	}
}
