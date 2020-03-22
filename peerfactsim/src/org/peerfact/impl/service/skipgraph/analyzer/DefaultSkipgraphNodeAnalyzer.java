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

import org.peerfact.impl.analyzer.AbstractFileMetricAnalyzer;
import org.peerfact.impl.analyzer.metric.StatisticMetric;
import org.peerfact.impl.analyzer.metric.StatisticMetricNoReset;
import org.peerfact.impl.service.skipgraph.analyzer.metrics.AvgSGSuccessAndFailedMetric;
import org.peerfact.impl.service.skipgraph.analyzer.metrics.SumMetricNoReset;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.simengine.Simulator;

/**
 * Analyses the node and controller component of the skip graph. 
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class DefaultSkipgraphNodeAnalyzer extends AbstractFileMetricAnalyzer<SkipgraphNode> 
		implements SkipgraphNodeAnalyzer {
	

	private final SumMetricNoReset<SkipgraphNode, Integer> sumOfNodesSum;
	private final StatisticMetric<SkipgraphNode, Double> sumOfNodes;
//	private final StatisticMetricNoReset<SkipgraphNode, Double> sumOfNodesNoReset;

	private final StatisticMetric<SkipgraphNode, Double> contactStatistics;
	private final StatisticMetric<SkipgraphNode, Double> elementStatistics;
	private final StatisticMetricNoReset<SkipgraphNode, Double> contactStatisticsNoReset;
	private final StatisticMetricNoReset<SkipgraphNode, Double> elementStatisticsNoReset;

	private final StatisticMetric<SkipgraphNode, Double> contactEffectiveSize;
	private final StatisticMetricNoReset<SkipgraphNode, Double> contactEffectiveSizeNoReset;
	private final StatisticMetric<SkipgraphNode, Double> contactNumberOf;
	private final StatisticMetricNoReset<SkipgraphNode, Double> contactNumberOfNoReset;

	private final StatisticMetric<SkipgraphNode, Double> 		incomingMessages;
	private final StatisticMetric<SkipgraphNode, Double> 		outgoingMessages;
	private final StatisticMetricNoReset<SkipgraphNode, Double> incomingMessagesNoReset;
	private final StatisticMetricNoReset<SkipgraphNode, Double> outgoingMessagesNoReset;

	private final StatisticMetric<SkipgraphNode, Long> lifespanStatistics;
//	private final StatisticMetricNoReset<SkipgraphNode, Long> lifespanStatisticsNoReset;

	private final AvgSGSuccessAndFailedMetric<SkipgraphNode> loadbalancingAvgSuccessesAndFails;

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public DefaultSkipgraphNodeAnalyzer(){
		// set an output file name - the associated file name is <yourParameter>.dat -> here: Demo.dat
		setOutputFileName("AAF_SKIPGRAPHNODE");
		// flushes all information written in the above mentioned file
		setFlushEveryLine(true);

		setTimeBetweenAnalyzeSteps(60*Simulator.SECOND_UNIT);
		
		// instantiate our metrics 
		this.sumOfNodesSum = new SumMetricNoReset<>("Sum of skipgraph nodes", "Number");
		this.sumOfNodes = new StatisticMetric<>("Sum of skipgraph nodes avg", "Number");
//		this.sumOfNodesNoReset = new StatisticMetricNoReset<>("Sum of skipgraph nodes (smoothed)", "Number");
		this.contactStatistics = new StatisticMetric<>("Contact Table", "Size");
		this.elementStatistics = new StatisticMetric<>("Element Table", "Size");
		this.contactStatisticsNoReset = new StatisticMetricNoReset<>("Contact Table (smoothed)", "Size");
		this.elementStatisticsNoReset = new StatisticMetricNoReset<>("Element Table (smoothed)", "Size");
		this.contactEffectiveSize = new StatisticMetric<>("Contact Table effective size", "Size");
		this.contactEffectiveSizeNoReset = new StatisticMetricNoReset<>("Contact Table effective size (smoothed)", "Size");
		this.contactNumberOf = new StatisticMetric<>("Distinct Contacts", "Number");
		this.contactNumberOfNoReset = new StatisticMetricNoReset<>("Distinct Contacts (smoothed)", "Number");
		this.incomingMessages      = new StatisticMetric<>("incoming messages", "Number");
		this.outgoingMessages      = new StatisticMetric<>("outgoing messages", "Number");
		this.incomingMessagesNoReset = new StatisticMetricNoReset<>("incoming messages (smoothed)", "Number");
		this.outgoingMessagesNoReset = new StatisticMetricNoReset<>("outgoing messages (smoothed)", "Number");
		this.lifespanStatistics        = new StatisticMetric<>("node lifespan", "Minutes");
//		this.lifespanStatisticsNoReset = new StatisticMetricNoReset<>("node lifespan (smoothed)", "Minutes");
		this.loadbalancingAvgSuccessesAndFails = new AvgSGSuccessAndFailedMetric<>("Loadbalancing Success and Failure", "Number");
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
		addMetric(this.sumOfNodesSum);
		addMetric(this.sumOfNodes);
//		addMetric(this.sumOfNodesNoReset);
		addMetric(this.contactStatistics);
		addMetric(this.elementStatistics);
		addMetric(this.contactStatisticsNoReset);
		addMetric(this.elementStatisticsNoReset);
		addMetric(this.contactEffectiveSize);
		addMetric(this.contactEffectiveSizeNoReset);
		addMetric(this.contactNumberOf);
		addMetric(this.contactNumberOfNoReset);
		addMetric(this.incomingMessages     );
		addMetric(this.outgoingMessages     );
		addMetric(this.incomingMessagesNoReset);
		addMetric(this.outgoingMessagesNoReset);
		addMetric(this.lifespanStatistics     );
//		addMetric(this.lifespanStatisticsNoReset);
		addMetric(this.loadbalancingAvgSuccessesAndFails);
	}
	


	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public void addNewNode(SkipgraphNode node) {
		addPeer(node);
	}


	@Override
	public void addElementTableSize(SkipgraphNode node, int size) {
		elementStatistics       .addValue(node, (double)size);
		elementStatisticsNoReset.addValue(node, (double)size);
	}


	@Override
	public void addContactTableSize(SkipgraphNode node, int size) {
		contactStatistics		.addValue(node, (double)size);
		contactStatisticsNoReset.addValue(node, (double)size);
	}


	@Override
	public void addContactTableEffectiveSize(SkipgraphNode node, int size) {
		contactEffectiveSize       .addValue(node, (double)size);
		contactEffectiveSizeNoReset.addValue(node, (double)size);
	}



	@Override
	public void addNumberOfDistinctContacts(SkipgraphNode node, int size) {
		contactNumberOf       .addValue(node, (double)size);
		contactNumberOfNoReset.addValue(node, (double)size);
	}


	@Override
	public void addNumberOfIncomingMessages(SkipgraphNode node, int number) {
		incomingMessages       .addValue(node, (double)number);
		incomingMessagesNoReset.addValue(node, (double)number);
	}


	@Override
	public void addNumberOfOutgoingMessages(SkipgraphNode node, int number) {
		outgoingMessages       .addValue(node, (double)number);
		outgoingMessagesNoReset.addValue(node, (double)number);
	}


	@Override
	public void currentNumberOfNodes(SkipgraphNode node, int number) {
		sumOfNodes       .addValue(node, (double)number);
//		sumOfNodesNoReset.addValue(node, (double)number);
	}

	
	@Override
	public void nodeLifespan(SkipgraphNode node, long lifespan) {
		lifespanStatistics		 .addValue(node, lifespan/Simulator.MINUTE_UNIT);
//		lifespanStatisticsNoReset.addValue(node, lifespan/Simulator.MINUTE_UNIT);
	}


	@Override
	public void loadBalancingSuccessAndFailur(SkipgraphNode node, boolean success) {
		loadbalancingAvgSuccessesAndFails.addSearchFinished(node, success);
	}



	@Override
	public void nodeAdded(SkipgraphNode node) {
		sumOfNodesSum.addValue(node, 1);
	}



	@Override
	public void nodeRemoved(SkipgraphNode node) {
		sumOfNodesSum.addValue(node, -1);
	}


}
