/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.awt.Color;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent.OwnChoiceMethod;

/**
 * @author dws04
 *
 */
public class ChoiceMethodTimeSeriesChart implements TimeSeriesChart, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -518980725070751920L;

	private static final Logger logger = Logger.getLogger(ChoiceMethodTimeSeriesChart.class);
	
	
	final XYDataset data;
	final JFreeChart chart;
	final ChartPanel panel;
	final OwnChoiceMethod choiceMethod;

	public ChoiceMethodTimeSeriesChart(OwnChoiceMethod choiceMethod, XYDataset data, String chartType, Integer endTime) {
		Boolean includeLastCycle = false;
		if ((chartType.equalsIgnoreCase(GraphBuilder.ricSizeTitle)) || (chartType.equalsIgnoreCase(GraphBuilder.occupiedRICTitle))) {
			includeLastCycle = true;
		}
		this.data = data;
		this.choiceMethod = choiceMethod;
		String xLabel = "Timestep";
		String yLabel = generateYLabel(chartType);
		String title = generateTitle(choiceMethod, chartType);
		chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, data, PlotOrientation.VERTICAL, true, false, false);
		panel = new ChartPanel(chart);

		chart.getXYPlot().setBackgroundPaint(Color.WHITE);
		chart.getXYPlot().getDomainAxis().setAutoRange(true);
		
		// add the avg line
		ChartUtils.makeAvgLineOnChart(this, endTime, "", includeLastCycle);
	}
	
	private static final String generateYLabel(final String chartType) {
		String result = null;
		if (chartType.equalsIgnoreCase(GraphBuilder.speedTitle)) {
			result = "Speed";
		}
		else if (	(chartType.equalsIgnoreCase(GraphBuilder.speedUtilTitle)) ||
				(chartType.equalsIgnoreCase(GraphBuilder.privacyUtilTitle)) ){
			result = "Utility";
		}
		else if (chartType.equalsIgnoreCase(GraphBuilder.utilTitle)) {
			result = "Move utility (/100)";
		}
		else if (chartType.equalsIgnoreCase(GraphBuilder.congestionTitle)) {
			result = "Agent density";
		}
		else if (chartType.equalsIgnoreCase(GraphBuilder.ricCountTitle)) {
			result = "Number of RICs";
		}
		else if (chartType.equalsIgnoreCase(GraphBuilder.ricSizeTitle)) {
			result = "Number of agents per RIC";
		}
		else if (chartType.equalsIgnoreCase(GraphBuilder.occupiedRICTitle)) {
			result = "Number of RICs";
		}
		else {
			logger.warn("Did not recognise chartType " + chartType);
			result = "";
		}
		return result;
	}

	private static final String generateTitle(OwnChoiceMethod choiceMethod, String chartType) {
		return chartType + " (" + choiceMethod.toString() + ")";
	}

	@Override
	public Long getSimId() {
		return null;
	}

	@Override
	public OwnChoiceMethod getChoiceMethod() {
		return this.choiceMethod;
	}

	@Override
	public ChartPanel getPanel() {
		return this.panel;
	}

	@Override
	public JFreeChart getChart() {
		return this.chart;
	}

	@Override
	public void hideLegend(boolean hide) {
		this.chart.getLegend().setVisible(!hide);
	}

	@Override
	public void redraw(int t) {
		
	}

	@Override
	public XYPlot getXYPlot() {
		return this.chart.getXYPlot();
	}

}
