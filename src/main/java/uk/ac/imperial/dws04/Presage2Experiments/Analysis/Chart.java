/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author dws04
 *
 */
public interface Chart {

	ChartPanel getPanel();

	JFreeChart getChart();

}
