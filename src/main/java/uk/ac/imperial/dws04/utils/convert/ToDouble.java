/**
 * 
 */
package uk.ac.imperial.dws04.utils.convert;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @author dws04
 *
 */
public class ToDouble {
	/**
	 * Could use overloading here, but meh. Generic fn for conversion to Double.
	 * @param <K>
	 * @param value
	 * @return
	 */
	public static <K> Double toDouble(K value){
		Double result = null;
		Class c = value.getClass(); 
		
		if (c.getSimpleName().equals("Double")) {
			result = (Double)value;
		}
		else if (c.getSimpleName().equals("double")) {
			result = (Double)value;
		}
		else if (c.getSimpleName().equals("Integer")) {
			result = (Double)((Integer)value).doubleValue();
		}
		else if (c.getSimpleName().equals("int")) {
			result = (Double)((Integer)value).doubleValue();
		}
		else {
			result = Double.NaN;
			throw new NumberFormatException("ToDouble.toDouble(" + value + ") encountered an error: " + value + " cannot be converted to a Double."); 
		}
		return result;
	}

	/**
	 * Converts all the entries in a LinkedHashMap to a Double
	 * @param <K> Type of Keys in map
	 * @param <V> Type of Values in map
	 * @param map map to be converted
	 * @return LinkedHashMap of <Double,Double>
	 */
	public static <K,V> LinkedHashMap<Double,Double> toDouble(LinkedHashMap<K,V> map){
		LinkedHashMap<Double,Double> result = new LinkedHashMap<Double,Double>();
		for (Entry<K,V> entry : map.entrySet()) {
			result.put(toDouble(entry.getKey()),toDouble(entry.getValue()));
		}
		return result;
	}
}
