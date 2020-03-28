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

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.peerfact.api.common.Host;
import org.peerfact.api.common.Message;
import org.peerfact.api.common.NetConnectivityEvent;
import org.peerfact.api.common.NetConnectivityListener;
import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.common.OverlayConnectivityEvent;
import org.peerfact.api.common.OverlayConnectivityListener;
import org.peerfact.api.overlay.OverlayContact;
import org.peerfact.api.overlay.OverlayID;
import org.peerfact.api.overlay.dht.DHTKey;
import org.peerfact.api.overlay.dht.DHTNode;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.api.overlay.kbr.KBRForwardInformation;
import org.peerfact.api.overlay.kbr.KBRListener;
import org.peerfact.impl.service.AbstractService;
import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.AbstractSkipgraphNodeRequestMessage;
import org.peerfact.impl.service.skipgraph.util.DotFileBuilder;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;
import org.peerfact.impl.util.communicationmanager.CommunicationManager;

/**
 * Represents a service to provide and maintain a distributed skip graph.
 * The graph provides a sorted data-structure which can be used for indexing host-capacities.
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphService extends AbstractService implements SkipgraphServiceInterface,
		NetConnectivityListener,
		OverlayConnectivityListener,
		KBRListener<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> {


	/* *******************************************
	 ****************** FIELDS *******************
	 *********************************************/

	/** Communication manager. Often used from operations to send messages. */
	private final CommunicationManager comManager;

	/** Reference for local node (DHTNode). */
	private DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> localNode;

	/** Reference for local contact (overlay layer (PeerID) + transport layer information (IP+Port). */
	private ApplicationContact localContact;

	private final Map<String, SkipgraphNodeController> skipgraphNodeControllers = new LinkedHashMap<>();
	
	private final MessageDistributor messageDistributor;
	
	private final ServiceOperationManager opManager;
	
	private boolean running = false;
	


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	@SuppressWarnings("unchecked")
	public SkipgraphService(Host host, CommunicationManager comManager) {

		// set host of this service
		setHost(host);

		//  set the reference to the communication manager and
		//  register this service to get messages. See method processMsg.
		this.opManager = new ServiceOperationManager(this);
		this.messageDistributor = new MessageDistributor(this);
		this.comManager = comManager;
		this.comManager.setMessageHandler(messageDistributor);

		// set dht node reference
		this.localNode = (DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>>)
				host.getOverlay(DHTNode.class);

		// create local contact (PeerID + IPAddress/PORT)
		this.localContact = new ApplicationContact(
				(BigInteger) getLocalNode().getOverlayID().getUniqueValue(),
				getLocalNode().getLocalOverlayContact());

		// receive incoming network and overlay layer (churn) events
		getLocalNode().getHost().getProperties().addNetConnectivityListener(this);
		getLocalNode().getHost().getProperties().addOverlayConnectivityListener(this);

		// receive KBR (=Key-based-Routing) events
		// The KBR is a common interface for structured p2p systems.
		// For more information refer to the class {@link KBRNode}.
		getLocalNode().setKBRListener(this);
	}


	@Override
	public void init() {
		if (isRunning()) {
			return;
		}
		
		if (SkipgraphServiceConstants.LOG_OVERLAY_ACTIVITY)
			log("initializes SkipgraphService @ "+ Simulator.getFormattedTime(Simulator.getCurrentTime()));
		
		// checks if responsible for Overlay-ID '0' -> first Skipgraph Node
		DHTKey<?> defaultKey = getLocalNode().getNewOverlayKey(SkipgraphServiceConstants.defaultID);
		
		if (getLocalNode().isPresent()) {
			if (getLocalNode().isRootOf(defaultKey)) {
				if (SkipgraphServiceConstants.LOG_OVERLAY_ACTIVITY)
					log("is skip graph ENTRY POINT");
				addSkipgraphNode(new SkipgraphNode(SkipgraphServiceConstants.defaultID));
			}
		}
		
		// call the monitor and push once to inform the monitor of peer
		if(Simulator.getMonitor() instanceof SkipgraphMonitor){
			((SkipgraphMonitor)Simulator.getMonitor()).addNewPeer(getLocalContact());
		}
		
		running = true;
	}



	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/

	public DHTNode<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> getLocalNode() {
		return this.localNode;
	}

	@Override
	public ApplicationContact getLocalContact() {
		return this.localContact;
	}

	public CommunicationManager getCommunicationManager() {
		return this.comManager;
	}

	public ServiceOperationManager getOperationManager() {
		return this.opManager;
	}

	public Map<String, SkipgraphNodeController> getSkipgraphNodeControllers() {
		return this.skipgraphNodeControllers;
	}

	public SkipgraphNodeController getEntryNodeController() {
		if (!skipgraphNodeControllers.isEmpty()) {
			return skipgraphNodeControllers.entrySet().iterator().next().getValue();
		}
		return null;
	}
	
	public boolean hasEntryNode() {
		return !skipgraphNodeControllers.isEmpty();
	}
	
	@Override
	public boolean isRunning() {
		return running;
	}
	
	public SkipgraphNodeController getNodeControllerForKey(BigInteger nodeID, AbstractSkipgraphNodeRequestMessage message) {
		SkipgraphNodeController nodeController = null;
		try {
			DHTKey<?> key = getLocalNode().getNewOverlayKey(nodeID);
			// is peer responsible for key?
			if (getLocalNode().isRootOf(key)) {
				// yes: responsible -> get node!
				nodeController = getSkipgraphNodeControllers().get(nodeID.toString());
				// nodeController for nodeID not available? -> get node from DHT and build new controller
				if (nodeController == null) {
					// which alternative is faster?
					if (SkipgraphServiceConstants.cheating) {
						// Alternative 1 for getting the node (cheating):
						boolean nodeAvailableInDHT = false;
						for (DHTObject object : getLocalNode().getLocalDHTEntries()) {
							if (object instanceof SkipgraphNode) {
								SkipgraphNode node = (SkipgraphNode)object;
								if (node.getNodeID().equals(nodeID))  {
									skipgraphNodeControllers.put(nodeID.toString(), 
											new SkipgraphNodeController(this, node, key));
									nodeAvailableInDHT = true;
									break;
								}
							}
						}
						if (!nodeAvailableInDHT) {
							throw new NullPointerException("no SkipgraphNode for nodeID="+SGUtil.formatID(nodeID)+" in DHT available");
						}
					}
					else {
						// Alternative 2 for getting the node (official approach):
						log("no nodeController available. Getting node from local DHT for: "+message+" (seed="+Simulator.getSeed()+")");
						SkipgraphService service = this;
						getLocalNode().valueLookup(key, new OperationCallback<DHTObject>() {
							
							@Override
							public void calledOperationSucceeded(Operation<DHTObject> op) {
								if (op.getResult() instanceof SkipgraphNode) {
									SkipgraphNode node = (SkipgraphNode)op.getResult();
									if (SkipgraphServiceConstants.LOG_DHT)
										node.print();
									skipgraphNodeControllers.put(nodeID.toString(), 
											new SkipgraphNodeController(service, node, key));
									DotFileBuilder.addNode(getLocalContact().getPeerID(), node);
								}
								else {
									//if (SkipgraphServiceConstants.LOG_DHT)
										System.out.println("unexpected result from DHT: "+op.getResult());
								}
							}
							
							@Override
							public void calledOperationFailed(Operation<DHTObject> op) {
								throw new NullPointerException("no SkipgraphNode for nodeID="+SGUtil.formatID(nodeID)+" in DHT available");
							}
						});
					}
				}
			}
			else {
				// no: not responsible -> remove nodeController if still exists
				nodeController = getSkipgraphNodeControllers().get(nodeID.toString());
				if (nodeController != null) {
					getSkipgraphNodeControllers().remove(nodeID.toString());
					nodeController = null;
				}
				throw new NullPointerException("not responsible for nodeID="+SGUtil.formatID(nodeID));
			}
		}
		catch (NullPointerException e) {
			log(e.getMessage());
			e.printStackTrace();
		}
		return nodeController;
	}
	
	

	/* ********************************************************
	 ******************** KBR interface ***********************
	 ******************************************************** */

	@Override
	public void forward(
		KBRForwardInformation<OverlayID<?>, OverlayContact<OverlayID<?>>, DHTKey<?>> information) {
		// not used here but may be interesting ... Always keep an eye on whether your desired overlay supports this!
	}
	
	@Override
	public void deliver(DHTKey<?> key, Message msg) {
		// not used here but may be interesting ... Always keep an eye on whether your desired overlay supports this!
	}

	@Override
	public void update(OverlayContact<OverlayID<?>> contact, boolean joined) {
		// not used here but may be interesting ... Always keep an eye on whether your desired overlay supports this!
		// E.g. chord does!
		if (SkipgraphServiceConstants.LOG_OVERLAY_ACTIVITY)
			log("neighborhood just changed (received from kbr listener)");
	}

	
	
	
	/* ********************************************************
	 ******************* Handle churn events ******************
	 ******************************************************** */

	@Override
	public void connectivityChanged(NetConnectivityEvent ce) {
		if (SkipgraphServiceConstants.LOG_OVERLAY_ACTIVITY) {
			if (ce.isOnline()) {
				log("just went online.");
			}
			else {
				log("just went offline.");
			}
		}
	}

	@Override
	public void connectivityChanged(OverlayConnectivityEvent ce) {
		if (SkipgraphServiceConstants.LOG_OVERLAY_ACTIVITY) {
			if(ce.isPresent()){
				log("joined the p2p overlay!");
			}
			else {
				log("left the p2p overlay!");
			}
		}
	}


	
	/* ********************************************************
	 *************** SkipgraphServiceInterface ****************
	 ******************************************************** */

	/* **********************
	 * global query methods *
	 ********************** */

	/** 
	 * used for optimization: expects one key-value set with old values and a 
	 * second one with new values.
	 * keys: dimensions
	 * values: SkipgraphElements
	 * don't process entries which are identical in both maps (no update needed)
	 */
	@Override
	public void update(
			List<SkipgraphElement> inputElements,
			List<SkipgraphElement> deleteElements,
			UpdateCallback updateCallback) 
	{
		// check if there are equal elements in both lists
		if (!SkipgraphServiceConstants.churnScenario) {
			// no-churn scenario: don't put equals neither in inputList nor in deleteList
			List<SkipgraphElement> intersection = new LinkedList<>(inputElements);
			intersection.retainAll(deleteElements);
			inputElements.removeAll(intersection);
			deleteElements.removeAll(intersection);
		}
		else {
			// churn-scenario: leave all in input list (purge!), but remove equals from deleteList
			deleteElements.removeAll(inputElements);
		}
		if (!inputElements.isEmpty() || !deleteElements.isEmpty()) {
			opManager.callUpdateOperation(inputElements, deleteElements, updateCallback);
		}
	}



	/* ********
	 * search *
	 **********/

	/**
	 * a future addition to the skipgraph. currently not supported 
	 */
	@Override
	public void get(int index, SearchQueryCallback searchQueryCallback) {
		log("get() is currently not supported by this version of skip graph");
	}

	
	@Override
	public void search(
			List<SearchQuery> searchQueries,
			int k,
			SearchQueryCallback searchQueryCallback) {
		opManager.callSearchOperation(searchQueries, k, searchQueryCallback);
	}

	
	
	/* ***********
	 * debugging *
	 *********** */

	@Override
	public void printResult(SkipgraphElement element) {
		System.out.println(element);
	}

	
	@Override
	public void printResult(List<SkipgraphElement> elements) {
		for (SkipgraphElement element : elements) {
			System.out.println(element);
		}
	}

	
	@Override
	public void print() {
		if (skipgraphNodeControllers.isEmpty()) {
			log("no SkipgraphNodes available.");
		}
		else {
			for (Entry<String, SkipgraphNodeController> entry : getSkipgraphNodeControllers().entrySet()) {
				log(entry.getKey());
			}
		}
	}
	

	public void printNetID() {
		log("NetID: "+getHost().getNetLayer().getNetID());
	}

	
	public void printAllSkipgraphNodes() {
		if (skipgraphNodeControllers.isEmpty()) {
			log("no SkipgraphNodes available.");
		}
		else {
			for (Entry<String, SkipgraphNodeController> entry : getSkipgraphNodeControllers().entrySet()) {
				entry.getValue().print();
				if (!entry.getValue().isDisabled()) {
					DotFileBuilder.add(getLocalContact().getPeerID(), entry.getValue().getNode());
				}
			}
		}
	}

	
	public void printSkipgraphNode(BigInteger nodeID) {
		if (skipgraphNodeControllers.isEmpty()) {
			log("no SkipgraphNodes available.");
		}
		else {
			getSkipgraphNodeControllers().get(nodeID.toString()).print();
		}
	}

	
	public static void buildDotFile() {
		DotFileBuilder.build();
	}

	
	public static void printHeadline() {
		if (SkipgraphServiceConstants.printNodesWhileLogging)
			System.out.println("\n-------------------- PRINTING SKIPGRAPH ------------------\n");
	}

	
	/**
	 * Helper method for logging.
	 * @param str
	 */
	@Override
	public void log(String str){
		System.out.println(String.format("\n• %s: %s (%d)", this, str, Simulator.getSeed()));
	}
	
	
	@Override 
	public String toString() {
		return String.format("Peer=%s %s",
				SGUtil.formatContact(getLocalContact()),
				localNode.isPresent() ? "PRESENT" : "NOT PRESENT");
	}



	
	/* *************************************************
	 ****************** METHODS ************************
	 ***************************************************/

	public boolean addSkipgraphNode(SkipgraphNode node) {
		DotFileBuilder.addNode(getLocalContact().getPeerID(), node);
		DHTKey<?> key = getLocalNode().getNewOverlayKey(node.getNodeID());
		if (!getLocalNode().isRootOf(key)) {
			try {
				throw new RuntimeException("peer is not root of SkipgraphNode key");
			} catch (RuntimeException e) {
				log(e.getMessage());
				e.printStackTrace();
			}
			return false;
		}

		getLocalNode().store(key, node, null);
		skipgraphNodeControllers.put(node.getNodeID().toString(), new SkipgraphNodeController(this, node, key));
		// call the monitor and push the data!
		if(Simulator.getMonitor() instanceof SkipgraphMonitor){
			((SkipgraphMonitor)Simulator.getMonitor()).nodeAdded(getLocalContact(), node);
		}
		return true;
	}


	
	public boolean removeNode(SkipgraphNode node) {
		DHTKey<?> key = getLocalNode().getNewOverlayKey(node.getNodeID());
		if (!getLocalNode().isRootOf(key)) {
			try {
				throw new RuntimeException("peer is not root of SkipgraphNode key");
			} catch (RuntimeException e) {
				log(e.getMessage());
				e.printStackTrace();
			}
			return false;
		}

		// call the monitor and push the data!
		if(Simulator.getMonitor() instanceof SkipgraphMonitor){
			((SkipgraphMonitor)Simulator.getMonitor()).nodeRemoved(getLocalContact(), node);
		}

		// removing the nodeController from the map
		skipgraphNodeControllers.remove(node.getNodeID().toString());
		// removing node from the DHT
		getLocalNode().remove(key, null);
		return true;
	}
	
	
}
