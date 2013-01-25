/**
 * 
 */
package uk.ac.imperial.dws04.utils.misc;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;



/**
 * @author dws04
 *
 */
public class MapValueAscComparatorTest {
	
	@Test
	public void test() {
		HashMap<String, Integer> unsortedMap = new HashMap<String,Integer>();
		unsortedMap.put("A", 99);
		unsortedMap.put("B", 63);
		unsortedMap.put("C", 68);
		unsortedMap.put("D", -64);
		unsortedMap.put("E", 99);
		
		LinkedList<Map.Entry<String,Integer>> list = new LinkedList<Entry<String,Integer>>();
		list.addAll(unsortedMap.entrySet());
		Collections.sort(list, new MapValueAscComparator());

		
		/*
		 * Sorted list should be:
		 * B,63
		 * D,-64
		 * C,68
		 * A,99 (or E,99)
		 * E,99 (or A,99)
		 */
		
		assertThat(list.containsAll(unsortedMap.entrySet()), is(true));
		assertThat(list.getLast().getKey(), anyOf( is("A"), is("E")));
		assertThat(list.getLast().getValue(), is(99));
		assertThat(list.getFirst().getKey(), is("D"));
		assertThat(list.getFirst().getValue(), is(-64));

	}

}
