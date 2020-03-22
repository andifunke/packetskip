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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.peerfact.Constants;
import org.peerfact.api.common.AvailableCapacities;
import org.peerfact.impl.analyzer.AbstractFileAnalyzer;
import org.peerfact.impl.analyzer.AbstractFileMetricAnalyzer;
import org.peerfact.impl.analyzer.metric.Metric;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * Calculates the precision and false positives rate of search queries.
 * Holds a global view on capacities in the overlay.
 * Peer capacities have to be pushed during updates. 
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class PrecisionMetric<Peer> implements Metric<Peer, Double>{

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
	
	/** This is our current measures map where we save the {@link OutcomePrecision2} per peer. */
	private Map<Peer, OutcomePrecision2> peerMap;
	private Map<Peer, OutcomePrecision2> timeMap;
	
	private Map<String, Set<SkipgraphElement>> actualCapacities;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	/**
	 * Constructor for this metric with the specified name and unit.
	 * @param name
	 * @param unit
	 */
	public PrecisionMetric(String name, String unit) {
		super();
		this.name = name;
		this.unit = unit;
		this.peerMap = new LinkedHashMap<>();
		this.timeMap = new LinkedHashMap<>();
		this.actualCapacities = new LinkedHashMap<>();
		for (AvailableCapacities cap : AvailableCapacities.values()){
			actualCapacities.put(cap.toString(), new LinkedHashSet<>());
		}
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
		return Arrays.asList("Precision", "False Positives");
	}

	@Override
	public List<String> getTimeMeasurementValues(long currentTime) {
		// write out data for this analyze step. Pay attention to the notes in the getHeadlines() method!
		// make use of java8 stuff to sum up all successful and all failed stuff
		int sumOfCount=0;
		double sumOfPrecision=0, sumOfFalsePositives=0;
		sumOfCount = this.peerMap.values().stream().mapToInt(outcome -> outcome.getCounter()).sum();
		sumOfPrecision = this.peerMap.values().stream().mapToDouble(outcome -> outcome.getAccumulatedPrecisionRate()).sum();
		sumOfFalsePositives = this.peerMap.values().stream().mapToDouble(outcome -> outcome.getAccumulatedFalsePositivesRate()).sum();
		double avgPrecision = sumOfPrecision/sumOfCount;
		double avgFalsePositives = sumOfFalsePositives/sumOfCount;
		return Arrays.asList(String.valueOf(avgPrecision),String.valueOf(avgFalsePositives));
	}

	@Override
	public List<String> getPeerMeasurementValues(Object peer) {
		// get the current measurement for the specified peer object. Pay attention to the notes in the getHeadlines() method!
		OutcomePrecision2 peerOutcome = this.peerMap.getOrDefault(peer, new OutcomePrecision2());
		double avgPrecision = peerOutcome.getAccumulatedPrecisionRate()/peerOutcome.getCounter();
		double avgFalsePositives = peerOutcome.getAccumulatedFalsePositivesRate()/peerOutcome.getCounter();
		return Arrays.asList(String.valueOf(avgPrecision),String.valueOf(avgFalsePositives));
	}


	
	/* ************************************************
	 **************** DATA SOURCE *********************
	 ************************************************ */

	/**
	 * This is our data source - it is called to push data to the metric.
	 */
	public void addData(List<SkipgraphElement> elements) {
//		System.out.println("adding to actualData "+elements);
		for (SkipgraphElement element : elements) {
			actualCapacities.get(element.getDimension()).add(element);
		}
	}
	
	public void removeData(List<SkipgraphElement> elements) {
//		System.out.println("removing from actualData "+elements);
		for (SkipgraphElement element : elements) {
			actualCapacities.get(element.getDimension()).remove(element);
		}
	}
	

	public void addSearchQueryResults(Peer contact, List<SearchQuery> queries, 
			int k, Set<ApplicationContact> actualSearchResults) {
		
		/** the final result set: these are the application contacts that are actually 
		 * in the overlay with the desired capacity values */ 
		Set<ApplicationContact> targetSearchResults = null;
		
		// make sure k != 0 (k > 0 for k-search || k < 0 for full search)
		// int l = k == 0 ? -1 : k;
		
		// for each 1-dimensional query we make a temporary set with results from the actual capacity data.
		for (SearchQuery query : queries) {
			/* the search query fields */
			String dimension = query.getDimension();
			BigDecimal rangeStart = query.getRangeStart();
			BigDecimal rangeEnd = query.getRangeEnd();

			/** temporary result set for each dimension */
			Set<ApplicationContact> resultSetPerDimension = new LinkedHashSet<>();

			for (SkipgraphElement element : actualCapacities.get(dimension)) {
				if ((rangeStart == null || rangeStart.compareTo(element.getValue()) <= 0) &&
					(rangeEnd == null || rangeEnd.compareTo(element.getValue()) >= 0)) {
						resultSetPerDimension.add(element.getContact());
					}
			}
			
			if (targetSearchResults == null) {
				// the first dimension is reference (no intersection possible)
				targetSearchResults = resultSetPerDimension;
			}
			else {
				// otherwise we create an intersection with results from the higher dimensions
				targetSearchResults.retainAll(resultSetPerDimension);
			}
		}
		
		// compare target and actual search results and apply k search
		int targetSize = 0;
		int actualSize = actualSearchResults.size();
		int falsePositives = 0;
		int precisionSize = 0;

		if (targetSearchResults != null) {
			if (k>0) {
				// k search
				targetSize = Math.min(k, targetSearchResults.size());
			}
			else {
				// full search
				targetSize = targetSearchResults.size();
			}
			// create intersection
			actualSearchResults.retainAll(targetSearchResults);
			precisionSize = actualSearchResults.size();
			falsePositives = actualSize-precisionSize;
		}
		
		double precisionRate = targetSize == 0 ? (precisionSize == 0 ? 1 : 0) : (double)precisionSize/targetSize;
		double falsePositivesRate = targetSize == 0 ? (falsePositives == 0 ? 0 : 1) : (double)falsePositives/targetSize;

		// put outcome in peer map
		OutcomePrecision2 peerOutcome = this.peerMap.getOrDefault(contact, new OutcomePrecision2());
		peerOutcome.addData(precisionRate, falsePositivesRate);
		// save back to map, which is necessary if we just created a new object
		this.peerMap.put(contact, peerOutcome);
		this.timeMap.put(contact, peerOutcome);
		
//		System.out.println("\n-----");
//		System.out.println("queries="+queries + " k="+(k<1?"inf":k));
//		//System.out.println("actualCapacities="+actualCapacities);
//		System.out.println("targetSearchResults="+targetSearchResults);
//		System.out.println("actualSearchResults="+actualSearchResults);
//		System.out.println("targetSize="+targetSize);
//		System.out.println("actualSize="+actualSize);
//		System.out.println("precisionSize="+precisionSize);
//		System.out.println("falsePositives="+falsePositives);
//		System.out.println("precisionRate="+precisionRate);
//		System.out.println("falsePositivesRate="+falsePositivesRate);
	}



	/* ************************************************
	 ************** Metric INTERFACE ******************
	 ************************************************ */

	@Override
	public void resetTimeMeasurement() {
		this.timeMap.clear();
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
	public void generatePeerPlots(Writer script, String analyzer,
			int startIndex) throws IOException {
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
				+ " ls 2 title '" + "Precision" + "' with linespoints,"
				+ "'" + analyzer
				+ AbstractFileAnalyzer.FILE_EXTENSION + "' using 1:"
				+ (startIndex+1)	// the second column is aggregated failed!
				+ " ls 1 title '" + "False Positives" + "' with linespoints"
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
	private static class OutcomePrecision2 {
		
		int counter;
		double accumulatedPrecisionRate;
		double accumulatedFalsePositivesRate;
		
		OutcomePrecision2(){
			this(0,0,0);
		}
	
		OutcomePrecision2(int counter, double accumulatedPrecisionRate, 
				double accumulatedFalsePositivesRate) {
			this.counter = counter;
			this.accumulatedFalsePositivesRate=accumulatedFalsePositivesRate;
			this.accumulatedPrecisionRate=accumulatedPrecisionRate;
		}
		
		/**
		 * add single event precision rate and false positives rate to the accumulated data
		 * @param precisionRate
		 * @param falsePositivesRate
		 */
		public void addData(double precisionRate, double falsePositivesRate){
			this.counter++;
			this.accumulatedPrecisionRate += precisionRate;
			this.accumulatedFalsePositivesRate += falsePositivesRate;
		}
		
		public int getCounter() {
			return counter;
		}

		public double getAccumulatedPrecisionRate() {
			return accumulatedPrecisionRate;
		}

		public double getAccumulatedFalsePositivesRate() {
			return accumulatedFalsePositivesRate;
		}


		@Override
		public String toString() {
			return "Outcome [counter=" + counter
					+ ", accumulatedPrecisionRate=" + accumulatedPrecisionRate
					+ ", accumulatedFalsePositivesRate=" + accumulatedFalsePositivesRate
					+ "]";
		}
	}
}
