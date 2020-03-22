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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.peerfact.api.analyzer.Analyzer;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;
import org.peerfact.impl.util.communicationmanager.DefaultCommunicationManagerSupportMonitor;

/**
 * implements the DefaultSkipgraphMonitor.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class DefaultSkipgraphMonitor extends DefaultCommunicationManagerSupportMonitor 
		implements SkipgraphMonitor {

	/** Registered SkipgraphAnalyzers for this monitor */
	private final List<SkipgraphAnalyzer> skipgraphAnalyzers;
	
	/** Registered SkipgraphNodeAnalyzers for this monitor */
	private final List<SkipgraphNodeAnalyzer> skipgraphNodeAnalyzers;
	
	
	public DefaultSkipgraphMonitor() {
		this.skipgraphAnalyzers = new LinkedList<>();
		this.skipgraphNodeAnalyzers = new LinkedList<>();
	}
	

	@Override
	public void setAnalyzer(Analyzer analyzer) {
		/*
		 *  if we know this analyzer: register it! 
		 *  It is necessary to call the super.setAnalyzer() method, 
		 *  otherwise other analyzer will be not registered!
		 */
		super.setAnalyzer(analyzer);
		if(analyzer instanceof SkipgraphAnalyzer){
			this.skipgraphAnalyzers.add((SkipgraphAnalyzer)analyzer);
		}
		if(analyzer instanceof SkipgraphNodeAnalyzer){
			this.skipgraphNodeAnalyzers.add((SkipgraphNodeAnalyzer)analyzer);
		}
	}

	
	/* ************************************************
	 *************** SkipgraphAnalyzer ****************
	 ************************************************ */

	@Override
	public void addNewPeer(ApplicationContact contact) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addNewPeer(contact);
			}
		}
	}

	
	@Override
	public void searchFinished(ApplicationContact contact, boolean isSuccessful) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.searchFinished(contact, isSuccessful);
			}
		}
	}

	
	@Override
	public void addData(ApplicationContact contact, List<SkipgraphElement> elements) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addData(contact, elements);
			}
		}
	}
	
	
	@Override
	public void removeData(ApplicationContact contact, List<SkipgraphElement> elements) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.removeData(contact, elements);
			}
		}
	}
	

	@Override
	public void nodeAdded(ApplicationContact contact, SkipgraphNode node) {
		// if we are monitoring: just delegate the monitor method to the appropriate analyzer
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.nodeAdded(contact, node);
			}
		}
	}

	
	@Override
	public void nodeRemoved(ApplicationContact contact, SkipgraphNode node) {
		// if we are monitoring: just delegate the monitor method to the appropriate analyzer
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.nodeRemoved(contact, node);
			}
		}
	}

	
	@Override
	public void addUpdateDuration(ApplicationContact contact, long duration) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addUpdateDuration(contact, duration);
			}
		}
	}


	@Override
	public void addUpdateDurationFirst(ApplicationContact contact, long duration) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addUpdateDurationFirst(contact, duration);
			}
		}
	}


	@Override
	public void addUpdateDurationTimespan(ApplicationContact contact, long duration) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addUpdateDurationTimespan(contact, duration);
			}
		}
	}


	@Override
	public void addUpdateInputHopCount(ApplicationContact contact, int hops,
			int distinctHops) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addUpdateInputHopCount(contact, hops, distinctHops);
			}
		}
	}


	@Override
	public void addUpdateDeleteHopCount(ApplicationContact contact, int hops,
			int distinctHops) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addUpdateDeleteHopCount(contact, hops, distinctHops);
			}
		}
	}


	@Override
	public void addUpdateTimeout(ApplicationContact contact) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addUpdateTimeout(contact);
			}
		}
	}


	@Override
	public void addSearchQueryDuration(ApplicationContact contact,
			long duration) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addSearchQueryDuration(contact, duration);
			}
		}
	}

	
	@Override
	public void addSearchQueryMessageCount(ApplicationContact contact,
			int bootstrap, int nack, int forwarding, int reply) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addSearchQueryMessageCount(contact, bootstrap, nack, forwarding, reply);
			}
		}
	}


	@Override
	public void addSearchQueryHopCount(ApplicationContact contact, int hops,
			int distinctHops) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addSearchQueryHopCount(contact, hops, distinctHops);
			}
		}
	}


	@Override
	public void addSearchQueryResults(ApplicationContact contact, List<SearchQuery> queries, 
			int k, Set<ApplicationContact> resultContacts) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.addSearchQueryResults(contact, queries, k, resultContacts);
			}
		}
	}


	@Override
	public void bootstrapAttempts(ApplicationContact contact, int number) {
		if(isMonitoring){
			for(SkipgraphAnalyzer analyzer : skipgraphAnalyzers){
				analyzer.bootstrapAttempts(contact, number);
			}
		}
	}


	
	/* ************************************************
	 ************* SkipgraphNodeAnalyzer **************
	 ************************************************ */

	@Override
	public void addNewNode(SkipgraphNode node) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addNewNode(node);
			}
		}
	}


	@Override
	public void addElementTableSize(SkipgraphNode node, int size) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addElementTableSize(node, size);
			}
		}
	}


	@Override
	public void addContactTableSize(SkipgraphNode node, int size) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addContactTableSize(node, size);
			}
		}
	}


	@Override
	public void addContactTableEffectiveSize(SkipgraphNode node, int size) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addContactTableEffectiveSize(node, size);
			}
		}
	}


	@Override
	public void addNumberOfDistinctContacts(SkipgraphNode node, int size) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addNumberOfDistinctContacts(node, size);
			}
		}
	}


	@Override
	public void addNumberOfIncomingMessages(SkipgraphNode node, int number) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addNumberOfIncomingMessages(node, number);
			}
		}
	}


	@Override
	public void addNumberOfOutgoingMessages(SkipgraphNode node, int number) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.addNumberOfOutgoingMessages(node, number);
			}
		}
	}


	@Override
	public void currentNumberOfNodes(SkipgraphNode node, int number) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.currentNumberOfNodes(node, number);
			}
		}
	}


	@Override
	public void nodeLifespan(SkipgraphNode node, long lifespan) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.nodeLifespan(node, lifespan);
			}
		}
	}


	@Override
	public void loadBalancingSuccessAndFailur(SkipgraphNode node, boolean success) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.loadBalancingSuccessAndFailur(node, success);
			}
		}
	}


	@Override
	public void nodeAdded(SkipgraphNode node) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.nodeAdded(node);
			}
		}
	}


	@Override
	public void nodeRemoved(SkipgraphNode node) {
		if(isMonitoring){
			for(SkipgraphNodeAnalyzer analyzer : skipgraphNodeAnalyzers){
				analyzer.nodeRemoved(node);
			}
		}
	}


}
