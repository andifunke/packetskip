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

package org.peerfact.impl.application.capacitymanager;

import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind1;
import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.logging;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.random.RandomGenerator;
import org.peerfact.api.common.AvailableCapacities;
import org.peerfact.api.common.Host;
import org.peerfact.api.common.Metric;
import org.peerfact.api.overlay.OverlayContact;
import org.peerfact.api.overlay.OverlayID;
import org.peerfact.api.overlay.dht.DHTKey;
import org.peerfact.api.overlay.dht.DHTNode;
import org.peerfact.impl.application.AbstractApplication;
import org.peerfact.impl.common.SingleCapacity;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceInterface;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceInterface.UpdateCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceInterface.SearchQueryCallback;
import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;
/**
 * Represents an application for searching and indexing capacities in an DHT-based overlay.
 * uses the skipgraph service.
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class CapacityManagerApplication extends AbstractApplication {

	private static int stdDevDivisor = CapacityManagerApplicationConstants.getMaxCapacity()/50;
	
	/* *******************************************
	 ****************** FIELDS *******************
	 *********************************************/

	/** Reference to the service. */
	private SkipgraphServiceInterface skipgraphService;
	
	/** backup from last capacity announcement. helps to announce only changes in capacities */
	private Map<String, SkipgraphElement> lastAnnouncedCapacities = new LinkedHashMap<>();

	/** Reference for local node (DHTNode). */
	private DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> localNode;

	/** Reference for local contact (overlay layer (PeerID) + transport layer information (IP+Port). */
	private ApplicationContact localContact;
	
	private StoreCapacitiesPeriodicOperation storeOp;
	
	private SearchCapacitiesPeriodicOperation searchOp;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	/**
	 * Constructor with a {@link SkipgraphServiceInterface} instance.
	 * @param skipgraphService
	 */
	@SuppressWarnings("unchecked")
	public CapacityManagerApplication(Host host, SkipgraphServiceInterface skipgraphService) {
		// set host of this application
		setHost(host);

		// set reference to the skipgraph service
		this.skipgraphService = skipgraphService;
		
		// set dht node reference
		this.localNode = (DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>>)
				host.getOverlay(DHTNode.class);
		
		// create local contact (PeerID + IPAddress/PORT)
		this.localContact = new ApplicationContact(
				(BigInteger) this.localNode.getOverlayID().getUniqueValue(),
				this.localNode.getLocalOverlayContact());
	}


	/**
	 * Called from the configuration file.
	 * Set initial capacities of the local node and
	 * call the service's initService method to give the
	 * service the opportunity to init some stuff.
	 * schedules the capacity store periodic operation
	 * schedules the capacity search periodic operation
	 */
	public void startApplication() {
		if (!skipgraphService.isRunning()) {
			this.skipgraphService.init();
		}
		// generate some dummy capacities for the local node
		setNewCapacityValues();

		// schedule periodic operations
		storeCapacitiesPeriodically(0l);
		searchCapacitiesPeriodically(10*Simulator.MINUTE_UNIT + 30*Simulator.SECOND_UNIT);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public ApplicationContact getLocalContact() {
		return this.localContact;
	}

	public DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> getLocalNode() {
		return this.localNode;
	}


	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */
	
	void log(String str) {
		log(str, false);
	}
	
	void log(String str, boolean forceLogging) {
		if (logging || forceLogging)
			System.out.println(String.format("\n• %s: %s (%d)", this, str, Simulator.getSeed()));
	}
	
	@Override 
	public String toString() {
		return String.format("Peer=%s %s <CapacityManager>",
				SGUtil.formatContact(skipgraphService.getLocalContact()),
				localNode.isPresent() ? "PRESENT" : "NOT PRESENT");
	}


	
	/* *************************************************
	 ************ CHANGE CAPACITY VALUES ***************
	 ***************************************************/

	/**
	 * Called from the configuration file.
	 * changes capacities
	 */
	public void setNewCapacityValues() {
		// generate some dummy capacities for the local node
		for (Entry<AvailableCapacities, SingleCapacity<Integer>> singleCap : generateRandomCapacities().entrySet()) {
			getHost().getProperties().addMetric(singleCap.getValue());
		}
	}



	/**
	 * update capacities smoothly by normal distribution
	 */
	public void changeCapacityValues() {
		Set<Metric<? extends Enum<?>, ? extends Number>> hostCapacities	= 
				getHost().getProperties().getAllMetrics();
		List<SingleCapacity<Integer>> updatedCapacities = new LinkedList<>();

		// get capacities and change values with gaussian distribution
		for (Metric<? extends Enum<?>, ? extends Number> metric : hostCapacities) {
			if (metric instanceof SingleCapacity) {
				@SuppressWarnings("unchecked")
				SingleCapacity<Integer> capacity = (SingleCapacity<Integer>)metric;
				log("before: "+capacity);
				// change with a probabillity of 0.5
				if (Simulator.getRandom().nextInt(2) > 0) {
					int value = capacity.getValue();
					int summand = (int)(Simulator.getRandom().nextGaussian() * (value/stdDevDivisor));
					value += summand;
					// avoid values <= 0
					if (value < 0) {
						value *= -1;
					}
					else if (value == 0) {
						value = 1;
					}
					SingleCapacity<Integer> updatedCapacity = new SingleCapacity<>(capacity.getMetric(), value);
					updatedCapacities.add(updatedCapacity);
					log("changed to: "+updatedCapacity);
				}
			}
		}
		// update host properties
		for (SingleCapacity<Integer> updatedCapacity : updatedCapacities) {
			getHost().getProperties().addMetric(updatedCapacity);
		}
	}
	

	
	/* *************************************************
	 ***************** STORE METHODS *******************
	 ***************************************************/

	/**
	 * Called from the configuration file.
	 * Set initial capacities of the local node and
	 * call the service's initService method to give the
	 * service the opportunity to init some stuff.
	 * starts the capacity store periodic operation
	 */
	public void startStore() {
		if (!skipgraphService.isRunning()) {
			this.skipgraphService.init();
		}
		// generate some dummy capacities for the local node
		setNewCapacityValues();
		storeCapacitiesPeriodically(0);
	}

	
	/**
	 * Called from the configuration file.
	 * stops the capacity store periodic operation
	 */
	public void stopStore() {
		if (storeOp != null) {
			storeOp.stop();
		}
		pretendChurn();
	}
	
	
	public void pretendChurn() {
		// inform the monitor
		List<SkipgraphElement> oldCapacitiesList = new LinkedList<>();
		for (Entry<String, SkipgraphElement> entry : lastAnnouncedCapacities.entrySet()) {
			oldCapacitiesList.add(entry.getValue());
		}
		pushDeletionsToMonitor(oldCapacitiesList);
		lastAnnouncedCapacities.clear();
	}

	
	public void storeCapacitiesPeriodically(long delay) {
		if (storeOp == null || storeOp.isFinished()) {
			storeOp = new StoreCapacitiesPeriodicOperation(this);
			storeOp.scheduleWithDelay(delay + 11);
		}
		else if (storeOp.isStopped()) {
			storeOp.start();
		}
	}
	
	
	
	/** Called from the configuration file or periodically
	 * reads current capacities from host and announces them to the skip graph service
	 */
	public void storeCapacities() {
		Map<String, SkipgraphElement> newCapacities = new LinkedHashMap<>();
		Map<String, SkipgraphElement> oldCapacities = new LinkedHashMap<>(lastAnnouncedCapacities);
		
		log("old capacities="+oldCapacities, false);
		log("storing:", false);
		for (AvailableCapacities cap : AvailableCapacities.values()) {
			Metric<?, ?> capacity = getHost().getProperties().getLocalMetricFor(cap);
			String dimension = capacity.getName();
			BigDecimal value = new BigDecimal(capacity.getValue().toString());
			SkipgraphElement element = new SkipgraphElement(dimension, value, localContact);
			if (logging || false)
				System.out.println("  - "+element);
			newCapacities.put(dimension, element);
		}
		
		log("updating:\ninput capacities="+newCapacities+"\ndelete capacities="+oldCapacities, false);
		// calling service only if at least one capacity has changed
		if (!newCapacities.isEmpty() || !oldCapacities.isEmpty()) {
			
			List<SkipgraphElement> newCapacitiesList = new LinkedList<>();
			List<SkipgraphElement> oldCapacitiesList = new LinkedList<>();
			for (Entry<String, SkipgraphElement> entry : newCapacities.entrySet()) {
				newCapacitiesList.add(entry.getValue());
			}
			for (Entry<String, SkipgraphElement> entry : oldCapacities.entrySet()) {
				oldCapacitiesList.add(entry.getValue());
			}

			// inform monitor about updates
			pushDeletionsToMonitor(oldCapacitiesList);
			pushInputsToMonitor(newCapacitiesList);

			// call the skipgraph service
			skipgraphService.update(
					newCapacitiesList,
					oldCapacitiesList, 
					new UpdateCallback() {

						@Override
						public void gotAnswer(boolean successful) {
							log(successful ? "capacities successfully stored in skip graph."
									: "capacity store failed.", false);
						}

					});
			this.lastAnnouncedCapacities.putAll(newCapacities);
			log("lastAnnouncedCapacities="+lastAnnouncedCapacities, false);
		}
		else {
			log("capacities haven't changed. abort update.", false);
		}
	}


	private void pushInputsToMonitor(List<SkipgraphElement> elements) {
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			SkipgraphMonitor monitor = (SkipgraphMonitor)Simulator.getMonitor();
			if (elements != null) {
				monitor.addData(localContact, new LinkedList<>(elements));
			}
		}
	}
	
	
	private void pushDeletionsToMonitor(List<SkipgraphElement> elements) {
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			SkipgraphMonitor monitor = (SkipgraphMonitor)Simulator.getMonitor();
			if (elements != null) {
				monitor.removeData(localContact, new LinkedList<>(elements));
			}
		}
	}
	
	


	/* *************************************************
	 **************** SEARCH METHODS *******************
	 ***************************************************/

	/**
	 * Called from the configuration file.
	 * Set initial capacities of the local node and
	 * call the service's initService method to give the
	 * service the opportunity to init some stuff.
	 * starts the capacity search periodic operation
	 */
	public void startSearch() {
		if (!skipgraphService.isRunning()) {
			this.skipgraphService.init();
		}
		searchCapacitiesPeriodically(0l);
	}

	
	/**
	 * Called from the configuration file.
	 * stops the capacity search periodic operation
	 */
	public void stopSearch() {
		if (searchOp != null) {
			searchOp.stop();
		}
	}

	
	public void searchCapacitiesPeriodically(long delay) {
		if (searchOp == null || searchOp.isFinished()) {
			searchOp = new SearchCapacitiesPeriodicOperation(this);
			searchOp.scheduleWithDelay(delay + 22);
		}
		else if (searchOp.isStopped()) {
			searchOp.scheduleWithDelay(delay + 22);
		}
	}
	
	
	/**
	 * 
	 */
	public void searchCapacities() {
		List<SearchQuery> queries = new LinkedList<>();
		int k = CapacityManagerApplicationConstants.kSearch;
		if (k < 0) {
			k = Simulator.getRandom().nextInt(8);
		}
		String kStr = k == 0 ? "full" : Integer.toString(k);
		Set<String> capacities = new LinkedHashSet<>();
		
		//int dimensionallity = AvailableCapacities.values().length;
		int numberOfDimensions = Simulator.getRandom().nextInt(100);
		//System.out.println(numberOfDimensions);
		if (numberOfDimensions < 15) {
			numberOfDimensions = 1;
		}
		else if (numberOfDimensions < 50) {
			numberOfDimensions = 2;
		}
		else if (numberOfDimensions < 85) {
			numberOfDimensions = 3;
		}
		else {
			numberOfDimensions = 4;
		}

		for (int i=1; i<=numberOfDimensions; i++) {
			SearchQuery query = generateRandomSearchQuery();
			// generate a query for a different capacity
			while (capacities.contains(query.getDimension())) {
				query = generateRandomSearchQuery();
			}
			capacities.add(query.getDimension());
			queries.add(query);
		}
		log(queries.toString());
		
		skipgraphService.search(
				queries,
				k, 
				new SearchQueryCallback() {

					@Override
					public void gotAnswer(boolean successful, Set<ApplicationContact> contacts) 
					{
						StringBuilder logSB = new StringBuilder();
				
						if (successful) {
							logSB.append("\nsearch results for " +queries +" k="+kStr+"\n");

							if (contacts.isEmpty()) {
								logSB.append("EMPTY!");
							}
							else {
								for (ApplicationContact contact : contacts) {
									logSB.append(ind1+contact);
								}
							}
						}
					
						else {
							log("\ncapacity search failed.");
						}
						log(logSB.toString());
					}
				});
	}
	
	
	
	
	
	/* ********************************************************************
	 * ******************************************************************** 
	 * STATIC FUNCTIONS TO GENERATE RANDOM CAPACITIES FOR DIFFERENT NEEDS *
	 ********************************************************************** 
	 ******************************************************************** */
	
	/**
	 * Helper method to generate some random capacities using the
	 * maxCapacity from the application constants.
	 * @return
	 */
	private static Map<AvailableCapacities, SingleCapacity<Integer>> generateRandomCapacities(){
		Map<AvailableCapacities, SingleCapacity<Integer>> ret = new LinkedHashMap<>();
		for (AvailableCapacities cap : AvailableCapacities.values()){
			ret.put(cap, new SingleCapacity<>(cap, 
					Simulator.getRandom().nextInt(CapacityManagerApplicationConstants.getMaxCapacity())));
		}
		return ret;
	}

	
	private static SearchQuery generateRandomSearchQuery() {
		RandomGenerator rand = Simulator.getRandom();
		int dimensionality = AvailableCapacities.values().length;
		int dimension = rand.nextInt(dimensionality);
		String cap = AvailableCapacities.values()[dimension].toString();
		BigDecimal rangeStart 
			= new BigDecimal(rand.nextInt(CapacityManagerApplicationConstants.getMaxCapacity()));
		BigDecimal rangeEnd
			= new BigDecimal(rand.nextInt(CapacityManagerApplicationConstants.getMaxCapacity()));
		if (rangeEnd.compareTo(rangeStart) < 0) {
			BigDecimal tmp = rangeEnd;
			rangeEnd = rangeStart;
			rangeStart = tmp;
		}
		// simulating an open rangeEnd
		else if (rand.nextInt(20) == 0) {
			rangeEnd = null;
		}
		int maxNumberOfValues = 0;

		return new SearchQuery(cap, rangeStart, rangeEnd, maxNumberOfValues);
	}
}
