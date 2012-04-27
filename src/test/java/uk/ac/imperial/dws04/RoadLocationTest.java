/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math.geometry.Vector3D;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.Location;


/**
 * @author dws04
 *
 */
public class RoadLocationTest {
	
	private final Logger logger = Logger.getLogger(RoadLocationTest.class);

	@Before
	public void setUp() throws Exception {
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testConstructor() {
		int lane = Random.randomInt();
		int offset = Random.randomInt();
		logger.info("Checking standard constructor with lane=" + lane + ", offset=" + offset);
		RoadLocation current = new RoadLocation(lane, offset);
		assertEquals(lane, current.getLane());
		assertEquals(offset, current.getOffset());
	}
	
	@Test
	public void testCopyConstructor() {
		int x = Random.randomInt();
		int y = Random.randomInt();
		Location loc = new Location(x,y);
		logger.info("Checking copy constructor with lane=" + x + ", offset=" + y);
		RoadLocation current = new RoadLocation(loc);
		assertEquals(((Double)loc.getX()).intValue(), x);
		assertEquals(((Double)loc.getY()).intValue(), y);
		assertEquals(((Double)loc.getX()).intValue(), current.getLane());
		assertEquals(((Double)loc.getY()).intValue(), current.getOffset());
		assertEquals(x, current.getLane());
		assertEquals(y, current.getOffset());
	}
	
	@Test
	public void testVectorConstructor() {
		Double x = (double) Random.randomInt();
		Double y = (double) Random.randomInt();
		Vector3D vect = new Vector3D(x, y, 0);
		logger.info("Checking vector constructor with lane=" + x + ", offset=" + y);
		RoadLocation current = new RoadLocation(vect);
		assertEquals(((Double)vect.getX()).intValue(), x.intValue());
		assertEquals(((Double)vect.getY()).intValue(), y.intValue());
		assertEquals(((Double)vect.getX()).intValue(), current.getLane());
		assertEquals(((Double)vect.getY()).intValue(), current.getOffset());
		assertEquals(x.intValue(), current.getLane());
		assertEquals(y.intValue(), current.getOffset());
	}
	
}
