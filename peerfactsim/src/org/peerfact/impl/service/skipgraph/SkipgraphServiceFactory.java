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

package org.peerfact.impl.service.skipgraph;

import org.peerfact.api.common.ComponentFactory;
import org.peerfact.api.common.Host;
import org.peerfact.api.overlay.OverlayContact;
import org.peerfact.api.overlay.OverlayID;
import org.peerfact.api.overlay.dht.DHTKey;
import org.peerfact.api.overlay.dht.DHTNode;
import org.peerfact.api.scenario.ConfigurationException;
import org.peerfact.impl.service.dhtstorage.idealreplication.IdealReplicationDHTService;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.CommunicationManager;

/**
 * The factory to create the skip graph service and all necessary
 * components.
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphServiceFactory implements ComponentFactory {

	/**
	 * Constructs a new SkipgraphService for the given host.
	 * @param host
	 */
	@Override
	public SkipgraphService createComponent(Host host) {

		// check whether a replication Service is running
		if(host.getService(IdealReplicationDHTService.class) == null)
			throw new ConfigurationException("The host " + host	+ " is not supported by "
					+ "the SkipgraphService because the IdealReplicationDHTService has not been started");

		// Instantiate the service and a communication manager, which is used for message exchange.
		SkipgraphService service = null;
		CommunicationManager comManager = null;
		@SuppressWarnings("unchecked")
		DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> node =
				(DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>>) host.getOverlay(DHTNode.class);
		comManager = new CommunicationManager(node, 
				CommunicationManager.DEFAULT_LOOKUP_TIMEOUT, 
				CommunicationManager.DEFAULT_MESSAGE_TIMEOUT);
		service = new SkipgraphService(host, comManager);

		return service;
	}

	
	
	/* **************************************************
	 ***************** Config Setters ******************* 
	 ************************************************** */

	/**
	 * Called from the config: set the message timeout (used by the communication manager).
	 * @param timeout
	 */
	public static void setMessageTimeout(long timeout){
		System.out.println("INFO: setting message timeout to: "+timeout/Simulator.SECOND_UNIT+"s");
		SkipgraphServiceConstants.setMessageTimeout(timeout);
	}

	public static void setPurgeTolerance(long arg){
		System.out.println("INFO: setting purge tolerance to: "+arg/Simulator.MILLISECOND_UNIT+"ms");
		SkipgraphServiceConstants.setPurgeTolerance(arg);
	}

	
	/**
	 * Called from the config: set the minimum size/threshold  of the skip graph's element table.
	 * @param minSize
	 */
	public static void setElementTableMinSize(int minSize){
		System.out.println("INFO: setting minimum size of element table to: "+minSize);
		SkipgraphServiceConstants.setElementTableMinSize(minSize);
	}


	/**
	 * Called from the config: set the maximum size/threshold  of the skip graph's element table.
	 * @param maxSize
	 */
	public static void setElementTableMaxSize(int maxSize){
		System.out.println("INFO: setting maximum size of element table to: "+maxSize);
		SkipgraphServiceConstants.setElementTableMaxSize(maxSize);
	}

	
	public static void setParallelSearch(boolean arg){
		System.out.println("INFO: parallel search enabled? "+arg);
		SkipgraphServiceConstants.setParallelSearch(arg);
	}
	
	public static void setLoadBalancing(boolean loadBalancing){
		System.out.println("INFO: load balancing enabled? "+loadBalancing);
		SkipgraphServiceConstants.setLoadBalancing(loadBalancing);
	}
	
	public static void setChurnScenario(boolean arg){
		System.out.println("INFO: is churn scenario? "+arg);
		SkipgraphServiceConstants.setChurnScenario(arg);
	}
	
	public static void setIgnoreExpiredResults(boolean arg){
		System.out.println("INFO: ignore expired results? "+arg);
		SkipgraphServiceConstants.setIgnoreExpiredResults(arg);
	}
	
	public static void setOptimizeLevelPrefix(boolean arg){
		System.out.println("INFO: optimize level prefix? "+arg);
		SkipgraphServiceConstants.setOptimizeLevelPrefix(arg);
	}
	
	public static void setHeadroom(double arg) {
		System.out.println("INFO: setting loadbalancing headroom to "+(arg*100)+"%");
		SkipgraphServiceConstants.setHeadroom(arg);
	}

	public static void setLogSearch(boolean arg) {
		System.out.println("INFO: log search operations and messages? "+arg);
		SkipgraphServiceConstants.setLogSearch(arg);
	}

	public static void setLogUpdate(boolean arg) {
		System.out.println("INFO: log search input, delete and update operations and messages? "+arg);
		SkipgraphServiceConstants.setLogUpdate(arg);
	}

	public static void setLogJoin(boolean arg) {
		System.out.println("INFO: log join operations and messages? "+arg);
		SkipgraphServiceConstants.setLogJoin(arg);
	}

	public static void setLogLeave(boolean arg) {
		System.out.println("INFO: log leave operations and messages? "+arg);
		SkipgraphServiceConstants.setLogLeave(arg);
	}

	public static void setLogMaintenance(boolean arg) {
		System.out.println("INFO: log skip graph maintenance operations and messages? "+arg);
		SkipgraphServiceConstants.setLogMaintenance(arg);
	}

	public static void setPrintNodesWhileLogging(boolean arg) {
		System.out.println("INFO: print nodes to console while logging? "+arg);
		SkipgraphServiceConstants.setPrintNodesWhileLogging(arg);
	}

	public static void setPlotDotFiles(boolean arg) {
		System.out.println("INFO: plot .dot files? "+arg);
		SkipgraphServiceConstants.setPlotDotFiles(arg);
	}

}
