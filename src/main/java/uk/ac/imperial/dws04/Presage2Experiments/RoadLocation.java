package uk.ac.imperial.dws04.Presage2Experiments;

import org.apache.commons.math.geometry.Vector3D;

import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.Location;

/**
 * A {@link Cell} whose X coordinate is a lane, and Y coordinate is the distance
 * along the road.
 * 
 * @author Sam Macbeth
 * 
 */
public class RoadLocation extends Cell {

	private static final long serialVersionUID = 1L;

	public RoadLocation(int lane, int offset) {
		super(lane, offset);
	}
	
	public RoadLocation(Location loc) {
		super(((Double)(loc.getX())).intValue(), ((Double)(loc.getY())).intValue());
	}

	/**
	 * NOTE: Use the 3-argument constructor for Vector3D, or you might not be making what you expect...
	 * @param v
	 */
	public RoadLocation(Vector3D v) {
		this((int)v.getX(), (int)v.getY());
	}
	
	/*// Removing the explicit rounding to ensure that attempts to move to -1 don't round up to 0...
	public RoadLocation(Vector3D v) {
		this((int) (v.getX() + 0.5), (int) (v.getY() + 0.5));
	}*/

	public int getLane() {
		return (int) this.getX();
	}

	public int getOffset() {
		return (int) this.getY();
	}

	/**
	 * Don't use this as it doesn't take wrapping into account, use ParticipantRoadLocationService.getOffsetDistanceBetween instead
	 */
	@Override
	@Deprecated
	public double distanceTo(Location l) {
		return distanceTo((RoadLocation)l);
	}


}
