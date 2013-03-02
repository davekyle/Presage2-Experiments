/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.dws04.utils.convert.ToDouble;
import uk.ac.imperial.dws04.utils.misc.ScreenImage;

/**
 * @author dws04
 *
 */
public abstract class ChartUtils {
	
	public static void tweak(JFreeChart chart, Boolean toggleLegend, Boolean resize){
		Class<? extends Plot> plotClass = chart.getPlot().getClass();
		final Plot plot = chart.getPlot();
	    plot.setBackgroundPaint(Color.white);
		if (plotClass.isAssignableFrom(XYPlot.class)) {
			final XYPlot xyPlot = (XYPlot)plot;
		    xyPlot.setDomainGridlinePaint(Color.lightGray);
		    xyPlot.setRangeGridlinePaint(Color.lightGray);
			if (resize) {
				Range range = ((XYSeriesCollection)(xyPlot.getDataset())).getRangeBounds(false);
				xyPlot.getRangeAxis().setRange(range);
			}
			
		}
		else if (plotClass.isAssignableFrom(CategoryPlot.class)) {
			final CategoryPlot cPlot = (CategoryPlot)plot;
			cPlot.setDomainGridlinePaint(Color.lightGray);
			cPlot.setRangeGridlinePaint(Color.lightGray);
		}
		if (chart.getLegend()!=null && toggleLegend){
			boolean visible = chart.getLegend().isVisible();
			chart.getLegend().setVisible(!visible);
		}
	}

	public static void removeLegendForBAWPlots(Chart chart) {
		if (chart.getClass().isAssignableFrom(DefaultBoxAndWhiskerChart.class) && chart.getChart().getLegend()!=null) {
			chart.getChart().getLegend().setVisible(false);
		}	
	}
	
	public static void makeLogRange(TimeSeriesChart chart) {
		XYPlot plot = chart.getXYPlot();
		LogarithmicAxis logAxis = new LogarithmicAxis(plot.getRangeAxis().getLabel());
		logAxis.setAutoRange(true);
		logAxis.setAllowNegativesFlag(true);
		plot.setRangeAxis(logAxis);
	}

	/**
	 * @param collection
	 * @param avgSeriesKey
	 * @param endTime
	 * @param includeLastCycle
	 * @return
	 */
	public static XYSeries createAvgSeries(final XYSeriesCollection collection, final String avgSeriesKey, final int endTime, final boolean includeLastCycle) {
		XYSeries avgSeries = new XYSeries(avgSeriesKey, true, false);
		int end = endTime;
		if (!includeLastCycle) {
			end--;
		}
		for (int t = 0; t<=end; t++) {
			double total = 0.0;
			double count = 0.0;
			for (Object seriesObj : collection.getSeries()) {
				XYSeries series = (XYSeries)seriesObj;
				Double val = null;
				Number tempVal = null;
				try {
					tempVal = series.getY(t);
					if (tempVal!=null) {
						val = ToDouble.toDouble(tempVal);
						if ( (!val.isInfinite()) && (!val.isNaN()) ) {
							count++;
							total = total+val;
						}
					}
				}
				catch (Exception e) {
					// ditch it
					//e.printStackTrace();
				}
			}
			avgSeries.add(t, (total/count));
		}
		return avgSeries;
	}

	/**
	 * 
	 * @param chart
	 * @param endTime
	 * @param keyStub end of key - will have "mean" prepended to it
	 * @param includeLastCycle
	 */
	public static void makeAvgLineOnChart(TimeSeriesChart chart, final int endTime, final String keyStub, final boolean includeLastCycle) {
		XYSeriesCollection collection = (XYSeriesCollection)chart.getXYPlot().getDataset();
		String key = "mean" + keyStub;
		XYDataset avgData = new XYSeriesCollection(createAvgSeries(collection, key, endTime, includeLastCycle));
		int avgIndex = chart.getXYPlot().getDatasetCount();
		chart.getXYPlot().setDataset(avgIndex, avgData);
		ChartUtils.renderSeriesAsAvg(chart, avgIndex);
	}

	/**
	 * @param chart
	 * @param avgIndex
	 */
	public static void renderSeriesAsAvg(TimeSeriesChart chart, int avgIndex) {
		XYLineAndShapeRenderer avgRenderer = new XYLineAndShapeRenderer(true, false);
		chart.getXYPlot().setRenderer(avgIndex, avgRenderer);
		chart.getXYPlot().getRenderer(avgIndex).setSeriesStroke(
			    0, 
			    new BasicStroke(
			        1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
			        1.0f, new float[] {3.0f}/*new float[] {6.0f, 6.0f}*/, 0.0f
			    ));
		chart.getXYPlot().getRenderer(avgIndex).setSeriesPaint(0, Color.RED);
		//chart.getXYPlot().setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);
		chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
	}

	/**
	 * Filename will be {imagePath}/preDesc + "_" + description + ".png"
	 * @param chart
	 * @param imagePath 
	 * @param preDesc
	 * @param description
	 */
	public static void saveChart(JFreeChart chart, String imagePath, String preDesc, String description) {
		try {
			ChartUtilities
					.saveChartAsPNG(
							new File(imagePath + preDesc + "_" + description + ".png"),
							chart, 1280, 720);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Filename will be {imagePath}/preDesc + "_" + description + ".png"
	 * @param panel
	 * @param imagePath
	 * @param preDesc
	 * @param description 
	 */
	public static void savePanel(Panel panel, String imagePath, String preDesc, String description) {
		try {
			Thread.sleep(1000);
			BufferedImage img = ScreenImage.createImage(panel);
			ScreenImage.writeImage(img, imagePath + preDesc + "_" + description + ".png");
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

}
