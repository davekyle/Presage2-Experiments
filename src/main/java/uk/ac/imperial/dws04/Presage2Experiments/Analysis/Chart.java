/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent.OwnChoiceMethod;

/**
 * @author dws04
 *
 */
public interface Chart {
	
	Long getSimId();
	
	OwnChoiceMethod getChoiceMethod();

	ChartPanel getPanel();

	JFreeChart getChart();
	
	void hideLegend(boolean hide);

}
