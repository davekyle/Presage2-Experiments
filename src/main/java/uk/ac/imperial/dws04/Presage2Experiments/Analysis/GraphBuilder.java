/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.Analysis;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import uk.ac.imperial.presage2.core.db.DatabaseModule;
import uk.ac.imperial.presage2.core.db.DatabaseService;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;

/**
 * @author dws04
 *
 */
public class GraphBuilder {

	final DatabaseService db;
	final StorageService sto;

	PersistentSimulation sim;
	int t = 0;
	int windowSize = 50;
	int t0 = -1;

	boolean exportMode = false;

	final static String imagePath = "/Users/dave/Documents/workspace/ExperimentalData";
	
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
			gui.init(Integer.parseInt(args[0]));
		}
	}

	@Inject
	public GraphBuilder(DatabaseService db, StorageService sto) {
		super();
		this.db = db;
		this.sto = sto;
	}
	
	void saveChart(JFreeChart chart, String base, int i) {
		if (t0 == -1) {
			t0 = i;
		}
		try {
			ChartUtilities
					.saveChartAsPNG(
							new File(imagePath + base + ""
									+ String.format("%04d", i - t0) + ".png"),
							chart, 1280, 720);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init(int simId) {
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
		
		// save to file if required
		
		
		
		
	}

}
