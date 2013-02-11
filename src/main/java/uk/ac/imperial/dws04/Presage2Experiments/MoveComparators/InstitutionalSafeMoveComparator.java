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
public class InstitutionalSafeMoveComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<CellMove, V>> {
	
	private final Integer comparisonSpeed;
	private final Integer comparisonLane;
	
	public InstitutionalSafeMoveComparator(Integer currentSpeed, Integer comparisonLane) {
		this.comparisonSpeed = currentSpeed;
		this.comparisonLane = comparisonLane;
	}

	/** 
	 * When sorted with this, agents will move at speed and lane closest to given, using safety to tiebreak...
	 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 *
	 * Note: this operator imposes an ordering that is inconsistent with equals.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Entry<CellMove, V> arg0, Entry<CellMove, V> arg1) {
		int compareCellMove = compareCellMove(arg0.getKey(), arg1.getKey());
		if (compareCellMove!=0) {
			return compareCellMove;
		}
		else {
			int compareSafety = arg0.getValue().compareTo(arg1.getValue()); 
			if (compareSafety!=0) {
				return compareSafety;
			}
			else {
				// prefer overtake to undertake
				return ((Integer)arg0.getKey().getXInt()).compareTo(((Integer)arg1.getKey().getXInt()));
			}
		}
	}
	
	/*
	 * Sorts by speed (closest to desired better) then lane (closest to desired better)
	 */
	private int compareCellMove(CellMove arg0, CellMove arg1) {
		Integer arg0SpeedDelta = Math.abs(comparisonSpeed-arg0.getYInt());
		Integer arg1SpeedDelta = Math.abs(comparisonSpeed-arg1.getYInt());
		int compareSpeed = arg1SpeedDelta.compareTo( arg0SpeedDelta );
		//System.out.println("Comparing curr:" + currentSpeed + ", arg0:" + arg0 + ", arg0D:" + arg0Delta + ", arg1:" + arg1 + ", arg1D:" + arg1Delta + ", compare:" + compare);
		if (compareSpeed!=0) {
			return compareSpeed;
		}
		else {
			Integer arg0LaneDelta = Math.abs(comparisonLane - arg0.getXInt());
			Integer arg1LaneDelta = Math.abs(comparisonLane - arg1.getXInt());
			int compareLane = arg1LaneDelta.compareTo(arg0LaneDelta);
			return compareLane;
			// let equally close values be sorted out in main compare()
		}
	}
	

}
