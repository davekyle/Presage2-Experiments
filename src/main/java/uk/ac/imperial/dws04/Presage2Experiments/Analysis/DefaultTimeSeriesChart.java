/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.awt.Color;
import java.io.Serializable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;

/**
 * @author dws04
 *
 */
public class DefaultTimeSeriesChart implements TimeSeriesChart, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -736475791627637895L;
	
	final int simId;
	final XYDataset data;
	final JFreeChart chart;
	final ChartPanel panel;

	public DefaultTimeSeriesChart(int simId, XYDataset data, String title, String xLabel, String yLabel) {
		super();
		this.simId = simId;

		this.data = data;
		chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, data, PlotOrientation.VERTICAL, true, false, false);
		panel = new ChartPanel(chart);

		chart.getXYPlot().setBackgroundPaint(Color.WHITE);
		//chart.getXYPlot().getDomainAxis().setRange(0, 30);
		chart.getXYPlot().getDomainAxis().setAutoRange(true);
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.TimeSeriesChart#getPanel()
	 */
	@Override
	public ChartPanel getPanel() {
		// TODO Auto-generated method stub
		return this.panel;
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.TimeSeriesChart#getChart()
	 */
	@Override
	public JFreeChart getChart() {
		// TODO Auto-generated method stub
		return this.chart;
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.TimeSeriesChart#redraw(int)
	 */
	@Override
	public void redraw(int t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hideLegend(boolean hide) {
		this.chart.getLegend().setVisible(!hide);
	}

	@Override
	public XYPlot getXYPlot() {
		return chart.getXYPlot();
	}

}
