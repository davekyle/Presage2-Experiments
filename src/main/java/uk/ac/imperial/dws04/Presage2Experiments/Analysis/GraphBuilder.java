/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
import uk.ac.imperial.dws04.utils.convert.StringSerializer;
import uk.ac.imperial.presage2.core.db.DatabaseModule;
import uk.ac.imperial.presage2.core.db.DatabaseService;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.PersistentAgent;
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
				gui.init(1);
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
		XYDataset speedDataset = new XYSeriesCollection();
		XYSeriesCollection speedCollection = (XYSeriesCollection)speedDataset;
		for (PersistentAgent pAgent : pAgents) {
			String key = pAgent.getID().toString();
			logger.info("Processing agent " + key);
			logger.debug("Processing speed for agent " + key);
			speedCollection.addSeries(new XYSeries(key, true, false));
			for (int t=0; t<=endTime; t++) {
				logger.trace("Time " + t);
				Integer speed = (Integer) StringSerializer.fromString(pAgent.getState(t).getProperty("speed"));
				speedCollection.getSeries(key).add(t, speed);
			}
		}
		
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
		DefaultTimeSeriesChart speedChart = new DefaultTimeSeriesChart(sim, speedDataset, "Agent Speed", "timestep", "speed");
		saveChart(speedChart.getChart(), 1, "speed");
		
		
		// save to file if required
		
		
		
		db.stop();
		System.exit(0);
	}

}
