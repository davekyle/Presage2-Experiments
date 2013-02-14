/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;

/**
 * @author dws04
 *
 */
public class DefaultTimeSeriesChart implements TimeSeriesChart {
	
	final PersistentSimulation sim;
	final DefaultXYDataset data;
	final JFreeChart chart;
	final ChartPanel panel;

	public DefaultTimeSeriesChart(PersistentSimulation sim) {
		super();
		this.sim = sim;

		data = new DefaultXYDataset();
		chart = ChartFactory.createXYLineChart("title", "", "timestep", data, PlotOrientation.HORIZONTAL, true, false, false);
		panel = new ChartPanel(chart);

		chart.getXYPlot().setBackgroundPaint(Color.WHITE);
		chart.getXYPlot().getDomainAxis().setRange(0, 30);
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

}
