/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;

/**
 * @author dws04
 *
 */
public class DefaultBoxAndWhiskerChart implements Chart {
	final PersistentSimulation sim;
	final BoxAndWhiskerCategoryDataset data;
	final JFreeChart chart;
	final ChartPanel panel;

	public DefaultBoxAndWhiskerChart(PersistentSimulation sim, BoxAndWhiskerCategoryDataset data, String title, String categoryLabel) {
		super();
		this.sim = sim;
		this.data = data;
		this.chart = ChartFactory.createBoxAndWhiskerChart(title, categoryLabel, null, data, true);
		panel = new ChartPanel(chart);
	}
	
	public DefaultBoxAndWhiskerChart(PersistentSimulation sim, XYSeriesCollection data, String title, String categoryLabel, Boolean stripNullAndNaNItems) {
		super();
		this.sim = sim;
		this.data = BAWDatasetFromXYCollection(data, stripNullAndNaNItems);
		this.chart = ChartFactory.createBoxAndWhiskerChart(title, categoryLabel, null, this.data, true);
		panel = new ChartPanel(chart);
	}

	private DefaultBoxAndWhiskerCategoryDataset BAWDatasetFromXYCollection(XYSeriesCollection collection, Boolean stripNullAndNaNItems) {
		DefaultBoxAndWhiskerCategoryDataset result = new DefaultBoxAndWhiskerCategoryDataset();
		for (Object seriesObj : collection.getSeries()) {
			XYSeries series = ((XYSeries)seriesObj);
			result.add(xySeriesToBAW(series, stripNullAndNaNItems), "RowKey", series.getKey());
		}
		return result;
	}
	
	public final static BoxAndWhiskerItem xySeriesToBAW(XYSeries series, Boolean stripNullAndNaNItems) {
		ArrayList<Number> list = SeriesUtils.xySeriesToArrayList(series);
		return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(list, stripNullAndNaNItems);
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.Chart#getPanel()
	 */
	@Override
	public ChartPanel getPanel() {
		return this.panel;
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.Chart#getChart()
	 */
	@Override
	public JFreeChart getChart() {
		return this.chart;
	}

}
