/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
import uk.ac.imperial.dws04.utils.convert.StringSerializer;
import uk.ac.imperial.presage2.core.db.DatabaseModule;
import uk.ac.imperial.presage2.core.db.DatabaseService;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.PersistentAgent;
import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;

/**
 * @author dws04
 *
 */
public class GraphBuilder {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	final DatabaseService db;
	final StorageService sto;

	PersistentSimulation sim;
	int t = 0;
	int windowSize = 50;
	int t0 = -1;

	boolean exportMode = true;

	final static String imagePath = "/Users/dave/Documents/workspace/ExperimentalData/";
	
	ArrayList<Chart> charts = new ArrayList<Chart>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DatabaseModule module = DatabaseModule.load();
		if (module != null) {
			Injector injector = Guice.createInjector(module);
			GraphBuilder gui = injector.getInstance(GraphBuilder.class);
			if (args.length > 1 && Boolean.parseBoolean(args[1]) == true)
				gui.exportMode = true;
			try {
				//gui.init(Integer.parseInt(args[0]));
				gui.init(3);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Inject
	public GraphBuilder(DatabaseService db, StorageService sto) {
		super();
		this.db = db;
		this.sto = sto;
	}
	
	void saveChart(JFreeChart chart, int simId, String description) {
		try {
			ChartUtilities
					.saveChartAsPNG(
							new File(imagePath + simId + "_" + description + ".png"),
							chart, 1280, 720);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init(int simId) throws IOException, ClassNotFoundException {
		try {
			db.start();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		sim = sto.getSimulationById(simId);
		if (exportMode) {
			File exportDir = new File(imagePath + sim.getName());
			if (!exportDir.exists())
				exportDir.mkdir();
			else if (!exportDir.isDirectory())
				System.exit(60);
		}
		
		// get the data you want...
		
		Set<PersistentAgent> pAgents = sim.getAgents();
		int endTime = sim.getCurrentTime();
		
		
		/*
		 * Agent Persistent:
		 * simId/aid/name/state
		 * state:
		 *  - type (String)
		 *  - goals (Serialised RoadAgentGoals)
		 *  - insertedAt (Serialised int)
		 *  - leftAt (Serialised int)
		 */
		/*
		 * Agent Transient:
		 * simId/time/aid/state
		 * state:
		 *  - x (double)
		 *  - y (double)
		 *  - z (double)
		 *  - location (Serialised RoadLocation)
		 *  - speed (Serialised int)
		 *  - dissatisfaction (Serialised double)
		 *  - move (Serialised Pair<CellMove,Integer>)
		 */
		/*
		 * Environment Transient:
		 * simId/state/time
		 * state:
		 *  - RIC_Count (Serialised int)
		 *  - [ric.toString()]_roles ( Serializable / Collection<HasRole> / HashSet )
		 *  - [ric.toString()]_chosen (Serialised Chosen)
		 */
		/*
		 * Environment Persistent:
		 * (nothing)
		 */
		/*
		 * Simulation:
		 * id/name/state/currentTime/finishTime/createdAt/classname/finishedAt/parent/parameters/startedAt
		 */
		
		// make list of charts you want
		
		// make frame & panel and add charts to them
		
		// draw "non-timeSeries" charts
		/*
		 * Agent join\leave - this will be time along the x, but you only iterate across agents and add in the time values, not across time
		 *  - This can be used for throughput ?
		 */
		
		// draw timeSeries charts iterating over time in sim
		/*
		 * Agent count - ref to congestion
		 * Agent location - can do density/congestion ?
		 *  - by lane and loc ?
		 * Agent speeds - avg/max/min
		 * Agent dissatisfaction - do line for each agent and a total/average ?
		 * Agent moveUtil - line for each and total/average ?
		 * IPCon cluster count
		 * IPCon max/avg cluster size percycle
		 *  - also do an overall value ? maybe a box for each sim ?
		 */
		
		/*
		 * Declare datasets
		 */
		XYDataset speedDataset = new XYSeriesCollection();
		XYSeriesCollection speedCollection = (XYSeriesCollection)speedDataset;
		
		XYDataset congestionDataset = new XYSeriesCollection();
		XYSeriesCollection congestionCollection = (XYSeriesCollection)congestionDataset;
		String congestionCountKey = simId+"_agentCount";
		congestionCollection.addSeries(new XYSeries(congestionCountKey, true, false));
		XYSeries congestionCount = congestionCollection.getSeries(congestionCountKey);
		String congestionChangeInKey = simId+"_changeIn";
		congestionCollection.addSeries(new XYSeries(congestionChangeInKey, true, false));
		XYSeries congestionChangeIn = congestionCollection.getSeries(congestionChangeInKey);
		String congestionChangeOutKey = simId+"_changeOut";
		congestionCollection.addSeries(new XYSeries(congestionChangeOutKey, true, false));
		XYSeries congestionChangeOut = congestionCollection.getSeries(congestionChangeOutKey);
		for (int t = 0; t<=endTime; t++) {
			congestionChangeIn.add(t, 0);
			congestionChangeOut.add(t, 0);
		}
		
		XYDataset ricCountDataset = new XYSeriesCollection();
		XYSeriesCollection ricCountCollection = (XYSeriesCollection)ricCountDataset;
		String ricCountKey = simId+"_ricCount";
		ricCountCollection.addSeries(new XYSeries(ricCountKey, true, false));
		String occupiedRICCountKey = simId+"_occupiedCount";
		ricCountCollection.addSeries(new XYSeries(occupiedRICCountKey, true, false));
		
		XYDataset ricMemberDataset = new XYSeriesCollection();
		XYSeriesCollection ricMemberCollection = (XYSeriesCollection)ricMemberDataset;
		/*
		 * Map of RIC(toString) to maps from timestamp to acceptorcount
		 */
		HashMap<String, HashMap<Integer, Integer>> acceptorTimeMap = new HashMap<String, HashMap<Integer, Integer>>();

		
		// declare charts
		DefaultTimeSeriesChart speedChart = new DefaultTimeSeriesChart(sim, speedDataset, "Agent Speed TimeSeries", "timestep", "speed");
		speedChart.hideLegend(true);
		charts.add(speedChart);
		DefaultTimeSeriesChart congestionChart = new DefaultTimeSeriesChart(sim, congestionDataset, "Congestion", "timestep", "AgentCount");
		charts.add(congestionChart);
		DefaultTimeSeriesChart ricCountChart = new DefaultTimeSeriesChart(sim, ricCountDataset, "RICs", "timestep", "Count");
		charts.add(ricCountChart);
		DefaultTimeSeriesChart ricAcceptorCountChart = new DefaultTimeSeriesChart(sim, ricMemberDataset, "RIC size", "timestep", "Number of Acceptors");
		ricAcceptorCountChart.hideLegend(true);
		charts.add(ricAcceptorCountChart);
		
		
		
		
		// get environment values
		PersistentEnvironment pEnv = sim.getEnvironment();
		
		// get persistent env data
		
		
		// transient env data
		for (int t = 0; t<=endTime; t++) {
			Integer ricCount = (Integer) StringSerializer.fromString(pEnv.getProperty("RIC_Count", t));
			ricCountCollection.getSeries(ricCountKey).add(t, ricCount);
			Integer occupiedRIC = (Integer) StringSerializer.fromString(pEnv.getProperty("OccupiedRIC_Count", t));
			ricCountCollection.getSeries(occupiedRICCountKey).add(t, occupiedRIC);
			
			Map<String,String> properties = pEnv.getProperties(t);
			for (Entry<String,String>property : properties.entrySet()) {
				if (property.getKey().endsWith("_roles")) {
					addAcceptorCountToMap(acceptorTimeMap, t, property.getValue());
				}
			}
			
			
			
		}
		
		
		populateRICAcceptorCountDatasetFromMap(ricMemberCollection, acceptorTimeMap);
		
		
		// get agent data
		for (PersistentAgent pAgent : pAgents) {
			String key = pAgent.getID().toString();
			logger.info("Processing agent " + key);
			
			// add series' in all the relevant collections
			speedCollection.addSeries(new XYSeries(key, true, false));		
			
			
			
			// get agent persistent values
			Integer insertedAt = (Integer) StringSerializer.fromString(pAgent.getProperty("insertedAt"));
			Integer leftAt = (Integer) StringSerializer.fromString(pAgent.getProperty("leftAt"));
			Double inValue = (Double) congestionChangeIn.getY(insertedAt);
			congestionChangeIn.update((Number)insertedAt, (Number)(inValue+1));
			if (leftAt!=null) {
				Double outValue = (Double) congestionChangeOut.getY(leftAt);
				congestionChangeOut.update((Number)leftAt, (Number)(outValue+1));
			}
			
			// get agent transient values
			for (int t=0; t<=endTime; t++) {
				logger.trace("Time " + t);
				// get transient values
				logger.debug("Processing speed for agent " + key);
				Integer speed = (Integer) StringSerializer.fromString(pAgent.getState(t).getProperty("speed"));
				speedCollection.getSeries(key).add(t, speed);
				
			}
		}
		
		Double agentCount = 0.0;
		for (int t=0; t<=endTime; t++) {
			agentCount = agentCount + (Double)congestionChangeIn.getY(t) - (Double)congestionChangeOut.getY(t);
			congestionCount.add(t, agentCount);
			annotateCongestionChart(congestionChart, t, simId);
		}
		

		// declare BAW chart because it's data doesn't update...
		DefaultBoxAndWhiskerChart speedBAW = new DefaultBoxAndWhiskerChart(sim, speedCollection, "Agent Speed BAW", "Agent", true);
		speedBAW.hideLegend(true);
		charts.add(speedBAW);
		
		
		Frame frame = new Frame("GRAPHS");
		Panel panel = new Panel(new GridLayout(0,2));
		frame.add(panel);
		for (Chart chart : charts) {
			chart.getChart().fireChartChanged();
			ChartUtils.tweak(chart.getChart(), false, false);
			//ChartUtils.removeLegendForBAWPlots(chart);
			saveChart(chart.getChart(), simId, chart.getChart().getTitle().getText());
			panel.add(chart.getPanel());
		}
		frame.pack();
		frame.setVisible(true);
		
		
		db.stop();
		//System.exit(0);
	}

	/**
	 * @param congestionChart
	 * @param t
	 * @param simId
	 */
	private static void annotateCongestionChart( DefaultTimeSeriesChart congestionChart, int t, Integer simId) {
		String congestionCountKey = simId+"_agentCount";
		String congestionChangeInKey = simId+"_changeIn";
		String congestionChangeOutKey = simId+"_changeOut";
		XYSeriesCollection collection = (XYSeriesCollection)congestionChart.getChart().getXYPlot().getDataset();
		XYSeries congestionCount = collection.getSeries(congestionCountKey);
		XYSeries congestionChangeIn = collection.getSeries(congestionChangeInKey);
		XYSeries congestionChangeOut = collection.getSeries(congestionChangeOutKey);
		double angle = 2*(Math.PI/16);
		if ((Double)congestionChangeIn.getY(t)!=0.0 && t!=0) {
			String inAnnotate = "+"+((Double)congestionChangeIn.getY(t)).intValue();
			XYTextAnnotation annotation = new XYTextAnnotation(inAnnotate, t-1, (Double)congestionCount.getY(t-1));
			annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
			TextAnchor anchor = TextAnchor.BOTTOM_RIGHT;
			annotation.setTextAnchor(anchor);
			annotation.setRotationAnchor(anchor);
		    annotation.setRotationAngle(angle);
			((XYPlot)congestionChart.getChart().getPlot()).addAnnotation(annotation);
		}
		if ((Double)congestionChangeOut.getY(t)!=0.0 && t!=0) {
			String outAnnotate = "-"+((Double)congestionChangeOut.getY(t)).intValue();
			XYTextAnnotation annotation = new XYTextAnnotation(outAnnotate, t-1, (Double)congestionCount.getY(t-1));
			annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
			TextAnchor anchor = TextAnchor.TOP_RIGHT;
			annotation.setTextAnchor(anchor);
			annotation.setRotationAnchor(anchor);
		    annotation.setRotationAngle(-angle);
			((XYPlot)congestionChart.getChart().getPlot()).addAnnotation(annotation);
		}
	}

	private static void populateRICAcceptorCountDatasetFromMap(XYSeriesCollection ricMemberCollection, HashMap<String, HashMap<Integer, Integer>> acceptorTimeMap) {
		for (Entry<String, HashMap<Integer,Integer>> topEntry : acceptorTimeMap.entrySet()) {
			String ricName = topEntry.getKey();
			HashMap<Integer,Integer> ricData = topEntry.getValue();
			XYSeries series = new XYSeries(ricName, true, false);
			
			for (Entry<Integer,Integer> ricEntry : ricData.entrySet()) {
				series.add(ricEntry.getKey(), ricEntry.getValue());
			}
			ricMemberCollection.addSeries(series);
		}
	}

	/**
	 * 
	 * @param map hashmap of RICname to map of time:count
	 * @param value String of serialised data (HashSet of HasRole)
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	private static void addAcceptorCountToMap(HashMap<String, HashMap<Integer,Integer>> map, Integer time, String value) throws IOException, ClassNotFoundException {
		HashSet<HasRole> roles = (HashSet<HasRole>) StringSerializer.fromString(value);
		Integer count = 0;
		String ricName = null;
		for (HasRole role : roles) {
			if (ricName==null) {
				ricName = role.getRIC().toString();
			}
			if (role.getRole().equals(Role.ACCEPTOR)) {
				count++;
			}
		}
		if (count!=0) {
			if (!map.containsKey(ricName)) {
				map.put(ricName, new HashMap<Integer,Integer>());
			}
			map.get(ricName).put(time, count);
		}
	}
	
}
