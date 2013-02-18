/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import org.jfree.chart.plot.XYPlot;


/**
 * @author dws04
 *
 */
interface TimeSeriesChart extends Chart {

		void redraw(int t);
		
		XYPlot getXYPlot();

}
