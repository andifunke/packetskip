/*
 * Copyright (c) 2012-2013 Open Source Community - <http://www.peerfact.org>
 * Copyright (c) 2011-2012 University of Paderborn - UPB
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.peerfact.impl.service.skipgraph.analyzer.metrics;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.peerfact.Constants;
import org.peerfact.impl.analyzer.AbstractFileAnalyzer;
import org.peerfact.impl.analyzer.AbstractFileMetricAnalyzer;
import org.peerfact.impl.analyzer.metric.Metric;

/**
 * a test metric.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class AvgSGSuccessAndFailedMetric<Peer> implements Metric<Peer, Object>{

	/* **********************************************
	 ************* GnuPlot constants ****************
	 ********************************************** */
	
	private static final String FONT_SIZE = "medium";
	private static final String XSIZE = "1024";
	private static final String YSIZE = "768";
	private static final String FONT_PNG = "'Helvetica'";
	private static final String FONT_PDF = "'Helvetica,10'";
	private static final String XRANGE = "[:]";
	private static final String YRANGE = "[:]";
	private static final String LEGEND = "set key top center horizontal";
	private static final String XLABEL_TIME = "'Time [Minutes]'";
	private static final String XLABEL_PEERS = "'Peer [ID]'";
	

	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	/** Name (y-axis) of this metric */
	private String name;
	
	/** Unit (y-axis) of this metric. */
	private String unit;
	
	/** This is our current measures map where we save the {@link OutcomeAvg2} per peer. */
	private Map<Peer,OutcomeAvg2> peerMap;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	/**
	 * Constructor for this metric with the specified name and unit.
	 * @param name
	 * @param unit
	 */
	public AvgSGSuccessAndFailedMetric(String name, String unit) {
		super();
		this.name = name;
		this.unit = unit;
		this.peerMap = new LinkedHashMap<>();
	}

	

	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getUnit() {
		return this.unit;
	}

	@Override
	public List<String> getHeadlines() {
		/*
		 * Here, we specify the column names for the *.dat file of the analyzer.
		 * Each list element we return symbolize exactly one column in
		 * exactly the order we return it.
		 * IMPORTANT:
		 * Keep this in mind when you write out data using the two
		 * getTime/PeerMeasurementValues methods, as we always
		 * need to write out the same number of values as columns 
		 * or list elements mentioned here.
		 * Remember that all metrics share one *.dat file of an analyzer, thus,
		 * not returning the same number of elements (headlines vs. each analyzer step)
		 * following metrics get into trouble as they do not come upon their saved values!
		 */
		return Arrays.asList("AverageSuccessful", "AverageFailed");
	}

	@Override
	public List<String> getTimeMeasurementValues(long currentTime) {
		// write out data for this analyze step. Pay attention to the notes in the getHeadlines() method!
		// make use of java8 stuff to sum up all successful and all failed stuff
		int sumOfSuccess=0,sumOfFailed=0;
		sumOfSuccess = this.peerMap.values().stream().mapToInt(outcome -> outcome.getSuccessCounter()).sum();
		sumOfFailed = this.peerMap.values().stream().mapToInt(outcome -> outcome.getFailedCounter()).sum();
		return Arrays.asList(String.valueOf(sumOfSuccess),String.valueOf(sumOfFailed));
	}

	@Override
	public List<String> getPeerMeasurementValues(Object peer) {
		// get the current measurement for the specified peer object. Pay attention to the notes in the getHeadlines() method!
		OutcomeAvg2 peerOutcome = this.peerMap.getOrDefault(peer, new OutcomeAvg2());
		return Arrays.asList(String.valueOf(peerOutcome.successCounter),String.valueOf(peerOutcome.failedCounter));
	}


	
	/* ************************************************
	 **************** DATA SOURCE *********************
	 ************************************************ */

	/**
	 * This is our data source - it is called to push data to the metric.
	 * Here, we update the {@link #peerMap}.
	 * @param contact
	 * @param isSuccessful
	 */
	public void addSearchFinished(Peer contact,
			boolean isSuccessful) {
		// fill the peer map
		OutcomeAvg2 peerOutcome = this.peerMap.getOrDefault(contact, new OutcomeAvg2());
		if(isSuccessful) peerOutcome.incrementSuccessful();
		else peerOutcome.incrementFailed();
		// save back to map, which is necessary if we just created a new object
		this.peerMap.put(contact,peerOutcome);
	}



	/* ************************************************
	 ************** Metric INTERFACE ******************
	 ************************************************ */

	@Override
	public void resetTimeMeasurement() {
		this.peerMap.clear();
		//this.interval.entrySet().removeIf(entry -> entry.getValue().allContextsComplete());
	}
	
	@Override
	public void generateTimePlots(Writer script, String analyzer,
			int startIndex) throws IOException {
		/*
		 * generate the time plot for this metric. This means we generate a gnuplot plot script,
		 * which is written the the provided Writer.
		 * Here, just call generatePlot for a common plot generation.
		 */
		generatePlot(script,analyzer,startIndex,true);		
	}

	@Override
	public void resetPeerMeasurement() {
		this.peerMap.clear();
	}

	@Override
	public void generatePeerPlots(Writer script, String analyzer, int startIndex) 
			throws IOException {
		// just like generateTimePlots for peers! We have a common plot generator.
		generatePlot(script,analyzer,startIndex,false);
	}
	

	
	/* ************************************************
	 ****************** PLOTTER ***********************
	 ************************************************ */

	/**
	 * Helper method to generate a gnuplot script using the given parameters.
	 * @param script Write out the gnuplot script using this Writer.
	 * @param analyzer Analyzer of this metric
	 * @param startIndex Start index (column) for this metric.
	 * @param isTimePlot True if it is a time plot, false if a peer plot.
	 * @throws IOException
	 */
	private void generatePlot(Writer script, String analyzer,
			int startIndex, boolean isTimePlot) throws IOException{
		// just write out the gnuplot script! Do not forget to terminate each line with a Constants.LINE_END.
		
		// first: A png plot using the static gnuplot constants
		script.write("set term png " + FONT_SIZE + " size " + XSIZE
				+ "," + YSIZE + " font " + FONT_PNG
				+ Constants.LINE_END);
		
		// file name: It is always of the pattern: <analyzer>-<metricName>.<fileExtension>
        script.write("set output '" + analyzer + "-"+ name.replace(" ", "") 
        		+ AbstractFileMetricAnalyzer.FILE_PNG_EXTENSION + "'" + Constants.LINE_END);
        
        // some more stuff from our gnuplot constants to set labels and ranges accordingly 
		script.write(LEGEND + Constants.LINE_END);
		script.write("set xlabel " + (isTimePlot==true?XLABEL_TIME:XLABEL_PEERS) + Constants.LINE_END);
		script.write("set ylabel '" + this.unit + "'" + Constants.LINE_END);
		script.write("set xrange " + XRANGE + Constants.LINE_END);
		script.write("set yrange " + YRANGE + Constants.LINE_END);
		
		/*
		 * Here is the main point: the plot command.
		 * We always plot the first column vs our metrics column(s) starting at startIndex
		 */
		script.write("plot '" + analyzer
				+ AbstractFileAnalyzer.FILE_EXTENSION + "' using 1:"
				+ startIndex		// the first column is aggregated success!
				+ " ls 2 title '" + "Average Success" + "' with linespoints,"
				+ "'" + analyzer
				+ AbstractFileAnalyzer.FILE_EXTENSION + "' using 1:"
				+ (startIndex+1)	// the second column is aggregated failed!
				+ " ls 1 title '" + "Average Failed" + "' with linespoints"
                + Constants.LINE_END);	// do not forget this one - it is annoying :)
		
		// replot the above plot using pdf
		script.write("set term pdf monochrome dashed font " + FONT_PDF
				+ Constants.LINE_END);
		// adapt the file name (see above) - only change is the PDF extension
		script.write("set output '" + analyzer + "-"+ name.replace(" ", "") 
			+ AbstractFileMetricAnalyzer.FILE_PDF_EXTENSION + "'" + Constants.LINE_END);
		script.write("replot" + Constants.LINE_END);	
	}
	
	/**
	 * This is just a helper class in order to be able to
	 * save all stuff we need for this metric in just one object.
	 * This can be quite handy when using the stream-api to 
	 * filter/map/sum up stuff in the analyze steps :) 
	 * 
	 * @author 
	 * @version 
	 */
	private static class OutcomeAvg2 {
		
		int successCounter;
		int failedCounter;
		
		OutcomeAvg2(){
			this(0,0);
		}
	
		OutcomeAvg2(int successCounter, int failedCounter) {
			super();
			this.successCounter = successCounter;
			this.failedCounter = failedCounter;
		}
		
		public void incrementSuccessful(){
			this.successCounter++;
		}
		
		public void incrementFailed(){
			this.failedCounter++;
		}

		public int getSuccessCounter() {
			return successCounter;
		}

		public int getFailedCounter() {
			return failedCounter;
		}

		@Override
		public String toString() {
			return "Outcome [successCounter=" + successCounter
					+ ", failedCounter=" + failedCounter + "]";
		}
	}
}
