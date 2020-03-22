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

package org.peerfact.impl.service.skipgraph.analyzer;

/**
 * Analyses the service component of the skip graph. 
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.peerfact.impl.analyzer.AbstractFileMetricAnalyzer;
import org.peerfact.impl.analyzer.metric.CounterMetric;
import org.peerfact.impl.analyzer.metric.StatisticMetric;
import org.peerfact.impl.analyzer.metric.StatisticMetricNoReset;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.analyzer.metrics.AvgSGSuccessAndFailedMetric;
import org.peerfact.impl.service.skipgraph.analyzer.metrics.MultiSumMetricByString;
import org.peerfact.impl.service.skipgraph.analyzer.metrics.PrecisionMetric;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

public class DefaultSkipgraphAnalyzer extends AbstractFileMetricAnalyzer<ApplicationContact> 
		implements SkipgraphAnalyzer {
	
	private final static String BOOTSTRAP = "bootstrap"; 
	private final static String FORWARDING = "forwarding"; 
	private final static String ACK = "(n)ack"; 
	private final static String RESULTS = "results"; 

	private final PrecisionMetric<ApplicationContact> precision;
	
	private final AvgSGSuccessAndFailedMetric<ApplicationContact> searchAvgSuccessesAndFails;
	
	private final StatisticMetric<ApplicationContact, Long> updateDuration;
	private final StatisticMetricNoReset<ApplicationContact, Long> updateDurationNoReset;
	private final StatisticMetric<ApplicationContact, Long> updateDurationFirst;
	private final StatisticMetricNoReset<ApplicationContact, Long> updateDurationFirstNoReset;
	private final StatisticMetric<ApplicationContact, Long> updateDurationTimespan;
	private final StatisticMetricNoReset<ApplicationContact, Long> updateDurationTimespanNoReset;
	private final CounterMetric<ApplicationContact> updateTimeouts;
	private final StatisticMetric<ApplicationContact, Double>        updateInputHops;
	private final StatisticMetricNoReset<ApplicationContact, Double> updateInputHopsNoReset;
	private final StatisticMetric<ApplicationContact, Double>        updateInputHopsDistinct;
	private final StatisticMetricNoReset<ApplicationContact, Double> updateInputHopsDistinctNoReset;
	private final StatisticMetric<ApplicationContact, Double>        updateDeleteHops;
	private final StatisticMetricNoReset<ApplicationContact, Double> updateDeleteHopsNoReset;
	private final StatisticMetric<ApplicationContact, Double>        updateDeleteHopsDistinct;
	private final StatisticMetricNoReset<ApplicationContact, Double> updateDeleteHopsDistinctNoReset;

	private final StatisticMetric<ApplicationContact, Long> searchDuration;
	private final StatisticMetricNoReset<ApplicationContact, Long> searchDurationNoReset;
	private final StatisticMetric<ApplicationContact, Integer> searchMessageCount;
	private final StatisticMetricNoReset<ApplicationContact, Integer> searchMessageCountNoReset;
	private final StatisticMetric<ApplicationContact, Double>        searchHops;
	private final StatisticMetricNoReset<ApplicationContact, Double> searchHopsNoReset;
	private final StatisticMetric<ApplicationContact, Double>        searchHopsDistinct;
	private final StatisticMetricNoReset<ApplicationContact, Double> searchHopsDistinctNoReset;
	
	private final List<String> searchMessageCountSelection;
	private final MultiSumMetricByString<ApplicationContact, Integer> searchMessageCountDetails;
	
	private final StatisticMetric<ApplicationContact, Double> bootstrapAttempts;
	
	/** This predefined metric is used to keep track of the number of node's added per minute. */
	private final CounterMetric<ApplicationContact> nodesAddedPerMinute;
	
	/** This predefined metric is used to keep track of the number of node's removed per minute. */
	private final CounterMetric<ApplicationContact> nodesRemovedPerMinute;
	
	private final StatisticMetric<ApplicationContact, Double> nodeFluctuation;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public DefaultSkipgraphAnalyzer(){
		// set an output file name - the associated file name is <yourParameter>.dat -> here: Demo.dat
		setOutputFileName("AAF_SKIPGRAPH");
		// flushes all information written in the above mentioned file
		setFlushEveryLine(true);
		
		/*
		 * set the time between two analyze steps - by changing this, the rate
		 * at which your metrics are called to write their information to the file
		 * is changed. Keep in mind that the AbstractFileMetricAnalyzer is minute-based,
		 * thus, changing this value to for example 30 seconds results into two lines of
		 * the same minute in the above mentioned *.dat file.
		 * This is may be important when plotting the *.dat files using gnuplot. 
		 */
		setTimeBetweenAnalyzeSteps(60*Simulator.SECOND_UNIT);
		
		this.precision = new PrecisionMetric<>("Precision", "Rate");
		
		this.searchAvgSuccessesAndFails      = new AvgSGSuccessAndFailedMetric<>("AverageSearchQueries", "Number");
		
		this.updateDuration                  = new StatisticMetric<>       ("Update Duration", "Milliseconds");
		this.updateDurationNoReset           = new StatisticMetricNoReset<>("Update Duration (smoothed)", "Milliseconds");
		this.updateDurationFirst             = new StatisticMetric<>       ("Update Duration first element", "Milliseconds");
		this.updateDurationFirstNoReset      = new StatisticMetricNoReset<>("Update Duration first element (smoothed)", "Milliseconds");
		this.updateDurationTimespan          = new StatisticMetric<>       ("Update Duration timespan", "Milliseconds");
		this.updateDurationTimespanNoReset   = new StatisticMetricNoReset<>("Update Duration timespan (smoothed)", "Milliseconds");
		this.updateTimeouts                  = new CounterMetric<>         ("Update Timeouts", "Number");
		this.updateInputHops                 = new StatisticMetric<>       ("Update Input Hops", "Number");
		this.updateInputHopsNoReset          = new StatisticMetricNoReset<>("Update Input Hops (smoothed)", "Number");
		this.updateInputHopsDistinct         = new StatisticMetric<>       ("Update Input distinct Hops", "Number");
		this.updateInputHopsDistinctNoReset  = new StatisticMetricNoReset<>("Update Input distinct Hops (smoothed)", "Number");
		this.updateDeleteHops                = new StatisticMetric<>       ("Update Delete Hops", "Number");
		this.updateDeleteHopsNoReset         = new StatisticMetricNoReset<>("Update Delete Hops (smoothed)", "Number");
		this.updateDeleteHopsDistinct        = new StatisticMetric<>       ("Update Delete distinct Hops", "Number");
		this.updateDeleteHopsDistinctNoReset = new StatisticMetricNoReset<>("Update Delete distinct Hops (smoothed)", "Number");
		
		this.searchDuration                  = new StatisticMetric<>       ("Search Duration", "Milliseconds");
		this.searchDurationNoReset           = new StatisticMetricNoReset<>("Search Duration (smoothed)", "Milliseconds");
                                             
		this.searchMessageCount              = new StatisticMetric<>       ("Search Messages", "Number");
		this.searchMessageCountNoReset       = new StatisticMetricNoReset<>("Search Messages (smoothed)", "Number");
		this.searchMessageCountSelection     = new LinkedList<>();
		searchMessageCountSelection.add(BOOTSTRAP);
		searchMessageCountSelection.add(FORWARDING);
		searchMessageCountSelection.add(ACK);
		searchMessageCountSelection.add(RESULTS);
		this.searchMessageCountDetails       = new MultiSumMetricByString<>("Search Message Top", "Number", searchMessageCountSelection, 4, false);
		this.searchHops                      = new StatisticMetric<>       ("Search Hops", "Number");
		this.searchHopsNoReset               = new StatisticMetricNoReset<>("Search Hops (smoothed)", "Number");
		this.searchHopsDistinct              = new StatisticMetric<>       ("Search distinct Hops", "Number");
		this.searchHopsDistinctNoReset       = new StatisticMetricNoReset<>("Search distinct Hops (smoothed)", "Number");
		
		this.bootstrapAttempts               = new StatisticMetric<>       ("bootstrap attempts", "Number");
		                                     
		this.nodesAddedPerMinute             = new CounterMetric<>         ("SkipgraphNodes added", "Number");
		this.nodesRemovedPerMinute           = new CounterMetric<>         ("SkipgraphNodes removed", "Number");
		this.nodeFluctuation                 = new StatisticMetric<>       ("node fluctuation", "Changes");
	}


	
	/* ************************************************
	 ********** AbstractFileMetricAnalyzer ************
	 ************************************************ */

	@Override
	protected void initializeMetrics() {
		/*
		 * here you have to initialize all your metrics by
		 * simply calling the addMetric(Metric<ApplicationContact, ?>) function 
		 */
		addMetric(this.precision);
		addMetric(this.searchAvgSuccessesAndFails);
		addMetric(this.nodesAddedPerMinute);
		addMetric(this.nodesRemovedPerMinute);
		addMetric(this.updateDuration);
		addMetric(this.updateDurationNoReset);
		addMetric(this.updateDurationFirst);
		addMetric(this.updateDurationFirstNoReset);
		addMetric(this.updateDurationTimespan);
		addMetric(this.updateDurationTimespanNoReset);
		addMetric(this.updateTimeouts);
		addMetric(this.searchDuration);
		addMetric(this.searchDurationNoReset);
		addMetric(this.searchMessageCount);
		addMetric(this.searchMessageCountNoReset);
		addMetric(this.searchMessageCountDetails);
		addMetric(this.bootstrapAttempts);
		addMetric(this.nodeFluctuation);
		addMetric(this.updateInputHops                );
		addMetric(this.updateInputHopsNoReset         );
		addMetric(this.updateInputHopsDistinct        );
		addMetric(this.updateInputHopsDistinctNoReset );
		addMetric(this.updateDeleteHops               );
		addMetric(this.updateDeleteHopsNoReset        );
		addMetric(this.updateDeleteHopsDistinct       );
		addMetric(this.updateDeleteHopsDistinctNoReset);
		addMetric(this.searchHops               );
		addMetric(this.searchHopsNoReset        );
		addMetric(this.searchHopsDistinct       );
		addMetric(this.searchHopsDistinctNoReset);
	}
	


	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public void addNewPeer(ApplicationContact peer) {
		addPeer(peer);
	}


	@Override
	public void searchFinished(ApplicationContact peer, boolean isSuccessful) {
		searchAvgSuccessesAndFails.addSearchFinished(peer, isSuccessful);
	}

	
	@Override
	public void nodeAdded(ApplicationContact peer, SkipgraphNode node) {
		nodesAddedPerMinute.increment(peer);
		nodeFluctuation.addValue(peer, 1.0);
	}


	@Override
	public void nodeRemoved(ApplicationContact peer, SkipgraphNode node) {
		nodesRemovedPerMinute.increment(peer);
		nodeFluctuation.addValue(peer, 1.0);
	}


	@Override
	public void addData(ApplicationContact peer, List<SkipgraphElement> elements) {
		precision.addData(elements);
	}


	@Override
	public void removeData(ApplicationContact peer, List<SkipgraphElement> elements) {
		precision.removeData(elements);
	}


	@Override
	public void addSearchQueryResults(ApplicationContact peer, List<SearchQuery> queries, 
			int k, Set<ApplicationContact> resultContacts) {
		precision.addSearchQueryResults(peer, queries, k, resultContacts);
	}


	@Override
	public void addSearchQueryDuration(ApplicationContact peer, long duration) {
		searchDuration.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
		searchDurationNoReset.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
	}


	@Override
	public void addSearchQueryMessageCount(ApplicationContact peer,
			int bootstrap, int nack, int forwarding, int reply) {
		int sum = bootstrap + nack + forwarding + reply;
		searchMessageCount.addValue(peer, sum);
		searchMessageCountNoReset.addValue(peer, sum);
		searchMessageCountDetails.addValue(peer, BOOTSTRAP, bootstrap);
		searchMessageCountDetails.addValue(peer, ACK, nack);
		searchMessageCountDetails.addValue(peer, FORWARDING, forwarding);
		searchMessageCountDetails.addValue(peer, RESULTS, reply);
	}


	@Override
	public void addSearchQueryHopCount(ApplicationContact peer, int hops,
			int distinctHops) {
		searchHops               .addValue(peer, (double)hops);
		searchHopsNoReset        .addValue(peer, (double)hops);
		searchHopsDistinct       .addValue(peer, (double)distinctHops);
		searchHopsDistinctNoReset.addValue(peer, (double)distinctHops);
		
	}
	

	@Override
	public void bootstrapAttempts(ApplicationContact peer, int number) {
		bootstrapAttempts.addValue(peer, (double)number);
	}


	@Override
	public void addUpdateDuration(ApplicationContact peer, long duration) {
		updateDuration.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
		updateDurationNoReset.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
	}


	@Override
	public void addUpdateDurationFirst(ApplicationContact peer, long duration) {
		updateDurationFirst.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
		updateDurationFirstNoReset.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
	}


	@Override
	public void addUpdateDurationTimespan(ApplicationContact peer, long duration) {
		updateDurationTimespan.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
		updateDurationTimespanNoReset.addValue(peer, duration/Simulator.MILLISECOND_UNIT);
	}


	@Override
	public void addUpdateTimeout(ApplicationContact peer) {
		updateTimeouts.increment(peer);
	}



	@Override
	public void addUpdateInputHopCount(ApplicationContact peer, int hops,
			int distinctHops) {
		updateInputHops                .addValue(peer, (double)hops);        
		updateInputHopsNoReset         .addValue(peer, (double)hops);        
		updateInputHopsDistinct        .addValue(peer, (double)distinctHops);
		updateInputHopsDistinctNoReset .addValue(peer, (double)distinctHops);
	}



	@Override
	public void addUpdateDeleteHopCount(ApplicationContact peer, int hops,
			int distinctHops) {
		updateDeleteHops               .addValue(peer, (double)hops);        
		updateDeleteHopsNoReset        .addValue(peer, (double)hops);        
		updateDeleteHopsDistinct       .addValue(peer, (double)distinctHops);
		updateDeleteHopsDistinctNoReset.addValue(peer, (double)distinctHops);
	}


}
