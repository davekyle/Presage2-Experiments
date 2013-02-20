/**
 * See http://www.presage2.info/ for more details on Presage2
 */
package uk.ac.imperial.dws04.Presage2Experiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * @author dws04
 *
 */
public class RoadAgentGoalsTest {

	@Test
	public void test() {
		Integer speed = Random.randomInt();
		Integer dest = Random.randomInt();
		Integer spacing = Random.randomInt();
		RoadAgentGoals test = new RoadAgentGoals(speed, dest, spacing);
		assertTrue(test instanceof RoadAgentGoals);
		assertEquals(test.getSpeed(), speed);
		assertEquals(test.getDest(), dest);
		assertEquals(test.getSpacing(), spacing);
	}
	
}
