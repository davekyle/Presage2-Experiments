/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * @author dws04
 *
 */
interface TimeSeriesChart {

		ChartPanel getPanel();

		JFreeChart getChart();

		void redraw(int t);

}
