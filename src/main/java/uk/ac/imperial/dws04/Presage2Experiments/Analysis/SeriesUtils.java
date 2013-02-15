/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.util.ArrayList;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.xy.XYSeries;

/**
 * @author dws04
 *
 */
public abstract class SeriesUtils {
	
	public final static DescriptiveStatistics xySeriesToApacheStats(XYSeries series) {
		DescriptiveStatistics result = new DescriptiveStatistics();
		for (int x = 0; x<series.getItemCount(); x++) {
			result.addValue((Double) series.getY(x));
		}
		return result;
	}
	
	public final static ArrayList<Number> xySeriesToArrayList(XYSeries series){
		ArrayList<Number> result = new ArrayList<Number>();
		for (int x=0; x<series.getItemCount(); x++) {
			result.add(series.getY(x));
		}
		return result;
	}
}
