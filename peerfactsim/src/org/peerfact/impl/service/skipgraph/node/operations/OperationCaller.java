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

package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.node.ContactLevel;
import org.peerfact.impl.service.skipgraph.node.Replacement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.AbstractSkipgraphNodeMessage;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphServiceOperation.TrackerCallback;
import org.peerfact.impl.service.skipgraph.operations.Addressable;
import org.peerfact.impl.service.skipgraph.operations.DelayBuildOperation;
import org.peerfact.impl.service.skipgraph.operations.SearchResultParallelOperation;
import org.peerfact.impl.service.skipgraph.operations.SearchResultSerialOperation;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class OperationCaller {


	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphNodeController nodeController;

	/**
	 * this map stores all operations that accept complex response messages
	 * those response messages are delivered via operationID (-> key) 
	 */
	private final Map<Integer, Addressable> addressableOperations = new LinkedHashMap<>();
	
	/**
	 * limit retries for pass data operations to avoid infinite loops 
	 */
	private final Map<Integer, Integer> updateOperationFailMap = new LinkedHashMap<>();
	private final int retryLimit = 10;
	

	/**
	 * this is basically a busy/lock flag for load balancing and table extension operations.
	 * operations may write a opID into this field to announce that they are currently executing
	 * a table extension services request. If an operation starts such an operation than it writes
	 * its own opID into this field.
	 * Other operations can only start or react to load balancing requests if the field is null.
	 * Therefore they should block or delay these operations to avoid conflicts or unstable states.
	 * operations must ensure to remove themselves once the operation is finished.
	 * if the field is null new load balancing operations can be started.
	 */
	private LoadBalancingResponseOperation loadbalancingResponseOp = null;
	
	private LoadBalancingOperation loadbalancingOp = null;
	
	private AnnounceNodePeriodicOperation announceToBootstrapOp = null;
	
	private PushNodeStatisticsPeriodicOperation pushToAnalyzerOp;
	
	private PurgeElementTablePeriodicOperation purgeOp;
	
	
	// logging
	private boolean logJoin = SkipgraphServiceConstants.logJoin;

	private boolean logLeave = SkipgraphServiceConstants.logLeave;

	private boolean logMaintenance = SkipgraphServiceConstants.logMaintenance;

	private boolean logSearch = SkipgraphServiceConstants.logSearch;



	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public OperationCaller(SkipgraphNodeController nodeController) {
		this.nodeController = nodeController;
		this.pushToAnalyzerOp = new PushNodeStatisticsPeriodicOperation(nodeController);
		this.announceToBootstrapOp = new AnnounceNodePeriodicOperation(nodeController);
		init();
	}
	
	
	private void init() {
		pushToAnalyzerOp.scheduleWithDelay(1*Simulator.SECOND_UNIT+3);
		announceToBootstrapOp.scheduleWithDelay(1*Simulator.SECOND_UNIT+3);
		if (SkipgraphServiceConstants.churnScenario) {
			//log("schedule purgeOp");
			this.purgeOp = new PurgeElementTablePeriodicOperation(nodeController);
			purgeOp.scheduleWithDelay(4);
		}
	}


	public void terminate() {
		if (pushToAnalyzerOp != null) {
			pushToAnalyzerOp.stop();
			pushToAnalyzerOp = null;
		}
		if (announceToBootstrapOp != null) {
			announceToBootstrapOp.stop();
			announceToBootstrapOp = null;
		}
		if (purgeOp != null) {
			purgeOp.stop();
			purgeOp = null;
		}
		if (loadbalancingResponseOp != null) {
			loadbalancingResponseOp = null;
		}
		addressableOperations.clear();
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public Map<Integer, Addressable> getOperationMap() {
		return addressableOperations;
	}

	private SkipgraphContact getContact() {
		return nodeController.getContact();
	}

	boolean acceptsLoadbalancing() {
		return loadbalancingResponseOp == null && loadbalancingOp == null;
	}
	
	boolean isAwaitingTableExtension() {
		return loadbalancingResponseOp != null;
	}
	
	LoadBalancingResponseOperation getAwaitEtExtension() {
		return loadbalancingResponseOp;
	}



	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */


	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	private void log(String str) {
		log(str, true);
	}

	private void log(String str, boolean forceLogging) {
		if (forceLogging)
			nodeController.log(str);
	}



	/* ************************************************
	 ****************** METHODS ***********************
	 ************************************************ */

	public boolean deliver(AbstractSkipgraphNodeMessage message) {
		int operationID = message.getOperationID();
		if (addressableOperations.containsKey(operationID)) {
			log("delivering <"+message+"> to <"+addressableOperations.get(operationID) + ">",
					nodeController.enforcesLogging());
			return addressableOperations.get(operationID).deliverMessage(message);
		}

		log("unable to deliver: can't find an associated operation for "+message + "\n"+addressableOperations);
		return false;
	}
	


	/* ************************************************
	 ************** OPERATION CALLER ******************
	 ************************************************ */

	/**
	 * 
	 */
	public void callJoinRequestOperation() 
	{
		if (logJoin || nodeController.enforcesLogging()) {
			log("############### callJoinOpeation ################");
			//log("before operation:");
			nodeController.printContacts();
		}
		JoinRequestOperation calledOperation = new JoinRequestOperation(
				nodeController,
				new OperationCallback<SkipgraphContact>() {

					@Override
					public void calledOperationFailed(
							Operation<SkipgraphContact> op) {
						nodeController.unlock();
					}

					@Override
					public void calledOperationSucceeded(
							Operation<SkipgraphContact> op) {
						if (logJoin || nodeController.enforcesLogging()) {
							nodeController.print();
						}
					}
				});
		calledOperation.scheduleImmediately();
	}



	/**
	 * a handshake operation is called to inform the receiver of a handshake message 
	 * that you want to link to it on a specifiy level with a specific prefix as prev or next contact.
	 * a handshake operation is usually called after a join or after building an additional contatc level.
	 * 
	 * on success it updates the contact table of the initiator with the contact information of the receiver
	 * which are returned via ACK message.
	 * 
	 * @param receiverNodeID
	 * @param levelIndex
	 * @param prefix
	 * @param route
	 */
	public void callHandshakeOperation(
			BigInteger receiverNodeID, 
			int levelIndex, 
			int prefix, 
			Route route) 
	{
		// may also check joinLevelBusyMap
		if (nodeController.getOperationResponder().getHandshakeBusyMap()
				.containsKey((route==Route.PREV?-1:1)*levelIndex)) {
			if (logJoin)
				log("already busy executing another handshake for level="+levelIndex+" "+route);
			return;
		}
		
		HandshakeOperation calledOperation = new HandshakeOperation(
				nodeController,
				new OperationCallback<SkipgraphContact>() {

					@Override
					public void calledOperationFailed(
							Operation<SkipgraphContact> op) {
						// removing the busy flag for that level
						nodeController.getOperationResponder().getHandshakeBusyMap()
								.remove((route==Route.PREV?-1:1)*levelIndex, receiverNodeID);
						if (logJoin)
							log("handshakeBusyMap="+nodeController.getOperationResponder().getHandshakeBusyMap());
					}

					@Override
					public void calledOperationSucceeded(
							Operation<SkipgraphContact> op) {
						// removing the busy flag for that level
						nodeController.getOperationResponder().getHandshakeBusyMap()
								.remove((route==Route.PREV?-1:1)*levelIndex, receiverNodeID);
						if (logJoin)
							log("handshakeBusyMap="+nodeController.getOperationResponder().getHandshakeBusyMap());

						if (route == Route.PREV) {
							nodeController.setPrevOnLevel(op.getResult(), levelIndex);
						}
						else {
							nodeController.setNextOnLevel(op.getResult(), levelIndex);
						}
						nodeController.updateContacts(op.getResult());
						//node.print();
						if (!nodeController.hasSelfLinkedTopLevel()) {
							prepareJoinLevelRequestOperation();
						}
						else {
							log("joined on all levels", logJoin);
							DelayBuildOperation dbo = new DelayBuildOperation(nodeController.getService());
							dbo.scheduleWithDelay(0);
						}
					}
				},
				receiverNodeID,
				levelIndex,
				prefix,
				route);
		// setting the busy flag for that level and route
		nodeController.getOperationResponder().getHandshakeBusyMap()
				.put((route==Route.PREV?-1:1)*levelIndex, receiverNodeID);
		if (logJoin)
			log("handshakeBusyMap="+nodeController.getOperationResponder().getHandshakeBusyMap());
		calledOperation.scheduleImmediately();
	}



	/**
	 * 
	 */
	public void prepareJoinLevelRequestOperation() 
	{
		/*
		 * given that a node has prev and next contacts on level 0 it builds up prev and next 
		 * contacts on higher levels until it gets to a level where it is its own contact
		 */
		ContactLevel topLevel = nodeController.getContactTable().getTopLevel();

		// add a new level, if the top level is already connected with other nodes.
		if (!nodeController.hasSelfLinkedTopLevel()) {
			nodeController.addDefaultTopLevel();
			topLevel = nodeController.getContactTable().getTopLevel();
			//log("*** While preparing JoinLevelRequestOperation: "
			//		+ "building new level=" +(contactTable.size()-1)+ ", prefix=" +topLevel.getPrefix()+ " ***");
		}
		
		if (nodeController.getContactTable().size() > 1 && topLevel != null) {
			BigInteger receiverID;
			SkipgraphContact requesterContact = getContact();
			int topIndex = nodeController.getContactTable().size()-1;
			int prefix = topLevel.getPrefix();

			// send the JoinLevel request to the next contact on the second highest level
			receiverID = nodeController.getContactTable().getNextOnLevel(topIndex-1).getNodeID();
			callJoinLevelRequestOperation(
					receiverID,
					nodeController.getService().getLocalContact(),
					null, 
					requesterContact, 
					topIndex, 
					prefix, 
					SGUtil.Route.NEXT, 
					false);
		}
		else {
			if (logJoin || nodeController.enforcesLogging())
				log("*** While preparing JoinLevelRequestOperation: no other contacts available **");
		}
	}



	/**
	 * @param receiverNodeID
	 * @param requesterContact
	 * @param levelIndex
	 * @param prefix
	 * @param route
	 */
	public void callJoinLevelRequestOperation(
			BigInteger receiverNodeID, 
			ApplicationContact requesterPeer,
			Integer requestOperationID,
			SkipgraphContact requesterContact,
			int levelIndex, 
			int prefix, 
			SGUtil.Route route,
			boolean forwarding) 
	{
		// avoid sending messages if possible. Might be replaced with test nodeID.equals(receiverNodeID)
		/*
		if (senderContact.getNodeID().equals(receiverNodeID)) {
			log("*** While preparing JoinLevelRequestOperation: "
					+ "receiver "+SkipgraphServiceConstants.formatID(receiverNodeID)
					+ " is the same as sender " + SkipgraphServiceConstants.formatID(senderContact.getNodeID())
					+ ". aborting operation... done. ***");
		}
		*/
		//else {
			JoinLevelRequestOperation calledOperation = new JoinLevelRequestOperation(
					nodeController,
					new OperationCallback<SkipgraphContact[]>() {

						@Override
						public void calledOperationFailed(
								Operation<SkipgraphContact[]> op) {
							addressableOperations.remove(op.getOperationID());
							if (!forwarding) {
								log("ERROR: JoinLevelRequestOperation failed");
							}
						}

						@Override
						public void calledOperationSucceeded(
								Operation<SkipgraphContact[]> op) {
							if (!forwarding) {
								SkipgraphContact[] result = op.getResult();
								nodeController.setNextOnLevel(result[0], levelIndex);
								//node.print();
								addressableOperations.remove(op.getOperationID());
								callHandshakeOperation(result[1].getNodeID(), levelIndex, prefix,
									SGUtil.switchRoute(route));
							}
							else {
								addressableOperations.remove(op.getOperationID());
							}
						}
					},
					receiverNodeID,
					requesterPeer,
					(requestOperationID == null ? 0 : requestOperationID.intValue()),
					requesterContact,
					levelIndex,
					prefix,
					route,
					forwarding);
			addressableOperations.put(calledOperation.getOperationID(), calledOperation);
			calledOperation.scheduleImmediately();
		//}
	}



	/**
	 * @param requestOperationID
	 * @param requesterID
	 * @param forwardedToID
	 */
	public void callJoinLevelNotifyOperation(
			int requestOperationID, 
			ApplicationContact requesterPeer,
			BigInteger requesterNodeID,
			BigInteger forwardedToID) 
	{
		JoinLevelNotifyOperation calledOperation = new JoinLevelNotifyOperation(
				nodeController,
				requesterNodeID,
				requesterPeer,
				requestOperationID,
				forwardedToID);
		calledOperation.scheduleImmediately();
	}



	/**
	 * @param receiverNodeID
	 * @param requestOperationID
	 * @param levelIndex
	 * @param prefix
	 * @param backRoute
	 * @param formerContact
	 */
	public void callJoinLevelReplyOperation(
			int requestOperationID,
			ApplicationContact requesterPeer,
			SkipgraphContact requesterContact,
			int levelIndex,
			int prefix,
			SGUtil.Route backRoute) 
	{
		SkipgraphContact formerContact = null;
		if (backRoute == SGUtil.Route.NEXT) {
			formerContact = nodeController.getContactTable().getLevel(levelIndex).getNextContact();
		}
		else if (backRoute == SGUtil.Route.PREV) {
			// set prev on this level to senderContact and send this contact and former prev back to sender
			formerContact = nodeController.getContactTable().getLevel(levelIndex).getPrevContact();
		}
		JoinLevelReplyOperation calledOperation = new JoinLevelReplyOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {//
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {
						if (backRoute == SGUtil.Route.NEXT) {
							// set next on this level to senderContact and send this contact and former next back to sender
							nodeController.setNextOnLevel(requesterContact, levelIndex);
						}
						else if (backRoute == SGUtil.Route.PREV) {
							// set prev on this level to senderContact and send this contact and former prev back to sender
							nodeController.setPrevOnLevel(requesterContact, levelIndex);
						}
						nodeController.updateContacts(requesterContact);
						//node.print();
					}
				},
				requesterContact.getNodeID(),
				requesterPeer,
				requestOperationID,
				getContact(),
				levelIndex,
				prefix,
				backRoute,
				formerContact);
		calledOperation.scheduleImmediately();
	}



	/**
	 * @param receiverNodeID
	 * @param inputList
	 * @param deleteList
	 */
	public void callUpdateForwardingOperation(
			BigInteger receiverNodeID,
			int updateOperationID,
			List<SkipgraphElement> inputList,
			List<SkipgraphElement> deleteList)
	{
		if ((inputList == null || inputList.isEmpty()) &&
				(deleteList == null || deleteList.isEmpty())) {
			return;
		}
		UpdateForwardingOperation calledOperation = new UpdateForwardingOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {
						log("WARN: update forwarding failed. updateOperationID="+updateOperationID
								+", inputList="+inputList+", deleteListe="+deleteList);
						// we put the data back in the queue, but we limit the amount of retries
						// to avoid infinite loops
						int counter = updateOperationFailMap.getOrDefault(updateOperationID, 1);
						if (counter < retryLimit) {
							nodeController.update(updateOperationID, inputList, deleteList);
							updateOperationFailMap.put(updateOperationID, ++counter);
						}
						else {
							updateOperationFailMap.remove(updateOperationID);
						}
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {//
					}
				},
				receiverNodeID,
				updateOperationID,
				inputList,
				deleteList);
		calledOperation.scheduleImmediately();
	}



	/**
	 * 
	 */
	public void callRangeAdjustmentOperation() 
	{
		RangeAdjustmentOperation calledOperation = new RangeAdjustmentOperation(
				nodeController,
				new OperationCallback<Set<BigInteger>>() {

					@Override
					public void calledOperationFailed(
							Operation<Set<BigInteger>> op) {
						// TODO: revisit op.getResult()
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Set<BigInteger>> op) {
						// TODO: revisit op.getResult()
					}
				});
		calledOperation.scheduleImmediately();
	}
	
	
	
	/** 
	 * the parallel search implementation follows the following approach:
	 * split the query and distributes it to several SkipgraphNodes at the same time.
	 */
	@SuppressWarnings("rawtypes")
	public void prepareSearchParallelForwardingOperation(
			ApplicationContact requester,
			int requestOperationID,
			List<SearchQuery> queries,
			TrackerCallback tracker,
			BigInteger forwardedFrom) 
	{
		Map<String, Set<ApplicationContact>> localResults = new LinkedHashMap<>();
		Set<BigInteger> messageHopTrackerSet = new LinkedHashSet<>();
		
		if (nodeController.isDisabled()) {
			callSearchParallelForwardingOperation(
					nodeController.getContactTable().getNext().getNodeID(),
					requester,
					requestOperationID,
					queries,
					tracker);
			messageHopTrackerSet.add(nodeController.getNodeID());
			messageHopTrackerSet.add(nodeController.getContactTable().getNext().getNodeID());
			callSearchParallelResultOperation(
					requester,
					requestOperationID,
					localResults,
					messageHopTrackerSet,
					tracker);
			return;
		}
		
		TreeMap<BigDecimal, SkipgraphContact> contactRangeMap = nodeController.getContactTable().getContactRangeMap();
		
		// adding this node to the TreeMap - just in case. The node should actually be linked to itself
		// on the top level. If so, there is no need to add the node to the TreeMap again, but we want to
		// be sure it's in the map.
		contactRangeMap.put(nodeController.getElementTable().getRangeStart(), nodeController.getContact());

		// parallel approach
		// splitting the query by the ranges of our known contacts and sending these partitions accordingly
		
		// make sure the lowest (first) entry in the tree-map is responsible for 0
		if (contactRangeMap.firstKey().compareTo(BigDecimal.ZERO) > 0) {
			SkipgraphContact firstContact = contactRangeMap.firstEntry().getValue();
			contactRangeMap.remove(contactRangeMap.firstKey());
			contactRangeMap.put(BigDecimal.ZERO, firstContact);
		}
		
		List<SearchQuery> remainingQueries = new LinkedList<>(queries);
		List<SearchQuery> queriesWeAreResponsibleFor = new LinkedList<>();
		if (forwardedFrom != null) {
			messageHopTrackerSet.add(forwardedFrom);
		}
		
//		log("contactRangeMap="+contactRangeMap);
//		log("queries="+queries);

		for (Entry<BigDecimal, SkipgraphContact> entry : contactRangeMap.descendingMap().entrySet()) {
			BigInteger contactID = entry.getValue().getNodeID();
			BigDecimal contactStart = entry.getKey();
			List<SearchQuery> queriesContactIsResponsibleFor = new LinkedList<>();
			List<SearchQuery> queriesContactIsNotResponsibleFor = new LinkedList<>();

			// each query stands for one dimension
			for (SearchQuery query : remainingQueries) {
				String dimension = query.getDimension();
				BigDecimal queryStart = query.getRangeStart();
				BigDecimal queryEnd = query.getRangeEnd();

				// is the contact responsible for the end of the query?
				if (queryEnd == null || contactStart == null || contactStart.compareTo(queryEnd) < 0
						|| (!query.hasOpenRangeEnd() && contactStart.compareTo(queryEnd) == 0)) {
					// yes -> process the query
					// is the contact resonsible for the entire query or only for a part of it?
					if (contactStart == null || contactStart.compareTo(queryStart) <= 0) {
						// responsible for the entire query -> add it to the currentQueries 
						queriesContactIsResponsibleFor.add(query);
					}
					else {
						// responsible only for the end of the query -> split the query
						// end of the query -> send to this contact
						queriesContactIsResponsibleFor.add(
								new SearchQuery(dimension, contactStart, queryEnd, 0, query.hasOpenRangeEnd()));
						// start of the query -> needs further processing
						queriesContactIsNotResponsibleFor.add(
								new SearchQuery(dimension, queryStart, contactStart, 0, true));
					}
				}
				else {
					// no -> contact is not responsible for the query
					// add the query back to to the remaining queries
					queriesContactIsNotResponsibleFor.add(query);
				}
			}

			// send current queries to the contact or save the queries for local processing and inform requester
			// check if we are responsible for this part of the query
			if (contactID.equals(nodeController.getNodeID())) {
				// yes, we are responsible for it -> keep it for local processing
				queriesWeAreResponsibleFor = queriesContactIsResponsibleFor;
			}
			else if (!queriesContactIsResponsibleFor.isEmpty()) {
				// no, it's a foreign contact -> forward the message to it and add it to the messageHopTrackerSet
//				log("send to contact="+SGUtil.formatID(contactID)+" queries="+queriesContactIsResponsibleFor);
				callSearchParallelForwardingOperation(
						contactID,
						requester,
						requestOperationID,
						queriesContactIsResponsibleFor,
						tracker);
				messageHopTrackerSet.add(contactID);
			}

			// update the remaining queries
			remainingQueries = queriesContactIsNotResponsibleFor;
		}

		// we add ourselves to the hop tracker set even if we are not responsible
		// for the query. This way the requester knows where the query was
		// processed.
		messageHopTrackerSet.add(nodeController.getNodeID());

		// last, but not least: process the part of the query we are responsible for
//		log("processing locally queries="+queriesWeAreResponsibleFor);
//		log("messageHopTrackerSet="+messageHopTrackerSet);
//		log("forwardedFrom="+SGUtil.formatID(forwardedFrom));
		for (SearchQuery query : queriesWeAreResponsibleFor) {
			Set<ApplicationContact> resultsPerDimension 
					= nodeController.getElementTable().getContacts(
							query.getDimension(),
							query.getRangeStart(),
							query.getRangeEnd(),
							0,
							query.hasOpenRangeEnd());
			localResults.put(query.getDimension(), resultsPerDimension);
		}
//		if (nodeController.getNode().getGlobalCountID() == 5
//				|| nodeController.getNode().getGlobalCountID() == 3
//				|| nodeController.getNode().getGlobalCountID() == 4
//				) {
//			nodeController.printElements(true);
//		}

		// and send a reply to the requester
		callSearchParallelResultOperation(
				requester,
				requestOperationID,
				localResults,
				messageHopTrackerSet,
				tracker);
	}



	@SuppressWarnings("rawtypes")
	public void callSearchParallelForwardingOperation(
			BigInteger receiverNodeID,
			ApplicationContact requester,
			int requestOperationID,
			List<SearchQuery> queries,
			TrackerCallback tracker)
	{
		SearchForwardingParallelOperation calledOperation = new SearchForwardingParallelOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {//
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {//
					}
				},
				receiverNodeID,
				requester,
				requestOperationID,
				queries,
				tracker);
		calledOperation.scheduleImmediately();
	}

	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void callSearchParallelResultOperation(
			ApplicationContact requester,
			int requestOperationID,
			Map<String, Set<ApplicationContact>> results,
			Set<BigInteger> forwardedTo,
			TrackerCallback tracker)
	{
		SearchResultParallelOperation calledOperation = new SearchResultParallelOperation(
				nodeController.getService(),
				new OperationCallback<Boolean>() {
					@Override
					public void calledOperationFailed(Operation<Boolean> op) {//
					}
					@Override
					public void calledOperationSucceeded(Operation<Boolean> op) {//
					}
				},
				requester,
				requestOperationID,
				results,
				forwardedTo,
				nodeController.getNodeID(),
				tracker);
		calledOperation.scheduleImmediately();
	}

	
	
	/** 
	 * the serial search implementation follows the following approach:
	 * send the search query to the first node that is responsible for the range start.
	 * Then pass the query along the skip graph.
	 */
	@SuppressWarnings("rawtypes")
	public void prepareSearchSerialDistribution(
			ApplicationContact requester,
			int requestOperationID,
			List<SearchQuery> queries,
			int messageCounter,
			TrackerCallback tracker) 
	{
		TreeMap<BigDecimal, SkipgraphContact> contactRangeMap = nodeController.getContactTable().getContactRangeMap();

		// adding this node to the TreeMap - just in case. The node should actually be linked to itself
		// on the top level. If so, there is no need to add the node to the TreeMap again, but we want to
		// be sure it's in the map.
		if (!nodeController.isDisabled()) {
			contactRangeMap.put(nodeController.getElementTable().getRangeStart(), nodeController.getContact());
		}

		// serial approach
		// getting the node that is responsible for a specific rangeStart and calling a operation for each query
		for (SearchQuery query : queries) {
			Entry<BigDecimal, SkipgraphContact> entry = contactRangeMap.floorEntry(query.getRangeStart());
			if (entry == null) {
				entry = contactRangeMap.firstEntry();
			}
			callSearchSerialForwardingOperation(
					entry.getValue().getNodeID(),
					requester,
					requestOperationID,
					query,
					messageCounter,
					tracker);
		}
	}

	
	
	@SuppressWarnings("rawtypes")
	public void prepareSearchSerialForwardingOperation(
			ApplicationContact requester,
			int requestOperationID,
			SearchQuery query,
			int messageCounter,
			TrackerCallback tracker) 
	{
		if (logSearch || nodeController.enforcesLogging()) {
			log("prepareSearchForwardingOperation:"
					+ " requester="+SGUtil.formatContact(requester)
					+". requestOperationID="+requestOperationID
					+". query="+query
					+". messageCounter="+messageCounter);
		}
		TreeMap<BigDecimal, SkipgraphContact> contactRangeMap = nodeController.getContactTable().getContactRangeMap();
		if (!nodeController.isDisabled()) {
			contactRangeMap.put(nodeController.getElementTable().getRangeStart(), nodeController.getContact());
		}
		Entry<BigDecimal, SkipgraphContact> entry = contactRangeMap.floorEntry(query.getRangeStart());
		if (entry == null) {
			entry = contactRangeMap.firstEntry();
		}
		callSearchSerialForwardingOperation(
				entry.getValue().getNodeID(),
				requester,
				requestOperationID,
				query,
				messageCounter,
				tracker);
	}
	
	
	
	@SuppressWarnings("rawtypes")
	public void callSearchSerialForwardingOperation(
			BigInteger receiverNodeID,
			ApplicationContact requester,
			int requestOperationID,
			SearchQuery query,
			int messageCounter,
			TrackerCallback tracker)
	{
		SearchForwardingSerialOperation calledOperation = new SearchForwardingSerialOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {//
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {//
					}
				},
				receiverNodeID,
				requester,
				requestOperationID,
				query,
				messageCounter,
				tracker);
		calledOperation.scheduleImmediately();
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void callSearchSerialResultOperation(
			ApplicationContact requester,
			int requestOperationID,
			String dimension,
			Set<ApplicationContact> results,
			int messageCounter,
			boolean last,
			TrackerCallback tracker)
	{
		SearchResultSerialOperation calledOperation = new SearchResultSerialOperation(
				nodeController.getService(),
				new OperationCallback<Boolean>() {
					@Override
					public void calledOperationFailed(Operation<Boolean> op) {//
					}
					@Override
					public void calledOperationSucceeded(Operation<Boolean> op) {//
					}
				},
				requester,
				requestOperationID,
				dimension,
				results,
				messageCounter,
				last,
				tracker);
		calledOperation.scheduleImmediately();
	}

	
	
	public void callLoadBalancingOperation(final String succeedingOperation)
	{
		if (!acceptsLoadbalancing()) {
			log("busy with other loadbalancing (response) operation. abort.");
			return;
		}
		
		nodeController.lock();
		LoadBalancingOperation calledOperation = new LoadBalancingOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {
						loadbalancingOp = null;
						if (succeedingOperation.equals("join")) {
							if (logJoin || nodeController.enforcesLogging())
								log("loadbalancing failed - calling join operation");
							callJoinRequestOperation();
						}
						else {
							nodeController.unlock();
						}
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {
						loadbalancingOp = null;
						if (op.getResult()) {
							if (logJoin || logLeave || logMaintenance || nodeController.enforcesLogging())
								log("loadbalancing succeeded");
							if (succeedingOperation.equals("leave")) {
								nodeController.disable();
								callReplaceContactsOperation();
							}
							// unnecessary
							else {
								nodeController.unlock();
							}
						}
					}
				},
				succeedingOperation);
		loadbalancingOp = calledOperation;
		calledOperation.scheduleImmediately();
	}
	
	
	
	/**
	 * this method forwards only replacements from a neighbour which is also disabled
	 * @param replacementsMap
	 */
	public void callReplaceContactsOperation(Map<BigInteger, List<Replacement>> replacementsMap) {
		if (logLeave || nodeController.enforcesLogging())
			log("################# callLeaveOperation #######################");
		ReplaceContactsOperation calledOperation = new ReplaceContactsOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {
						if (logLeave || nodeController.enforcesLogging())
							log("replace contacts forwarding failed");
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {
						if (logLeave || nodeController.enforcesLogging()) {
							if (!op.getResult()) {
								log("forwarding: could not replace all contacts");
							}
							else {
								log("replace contacts forwarding succeeded");
							}
						}
					}
				},
				replacementsMap);
		calledOperation.scheduleImmediately();
	}



	public void callReplaceContactsOperation() {
		if (logLeave || nodeController.enforcesLogging())
			log("################# callLeaveOperation #######################");
		ReplaceContactsOperation calledOperation = new ReplaceContactsOperation(
				nodeController,
				new OperationCallback<Boolean>() {

					@Override
					public void calledOperationFailed(
							Operation<Boolean> op) {
						if (logLeave || nodeController.enforcesLogging())
							log("could not replace all contacts. leaving anyway.\n!!!! LEAVING NOW !!!!");
						nodeController.terminate();
					}

					@Override
					public void calledOperationSucceeded(
							Operation<Boolean> op) {
						if (logLeave || nodeController.enforcesLogging()) {
							if (!op.getResult()) {
								log("could not replace all contacts. leaving anyway.\n!!!! LEAVING NOW !!!!");
							}
							else {
								log("!!!! LEAVING NOW !!!!");
							}
						}
						nodeController.terminate();
					}
				});
		calledOperation.scheduleImmediately();
	}



	public boolean callAwaitElementTableExtensionOperation(
			BigInteger requesterNodeID,
			int requestOperationID)
	{
		if (logJoin || logMaintenance || logLeave || nodeController.enforcesLogging())
			log("################# callAwaitElementTableExtensionOperation ###########################");
		// making sure only one operation is running
		if (loadbalancingResponseOp != null) {
			if (loadbalancingResponseOp.isFinished()) {
				loadbalancingResponseOp = null;
			}
		}
		
		if (acceptsLoadbalancing()) {
			loadbalancingResponseOp = new LoadBalancingResponseOperation(
					nodeController,
					new OperationCallback<Boolean>() {

						@Override
						public void calledOperationFailed(
								Operation<Boolean> op) {
							loadbalancingResponseOp = null;
						}

						@Override
						public void calledOperationSucceeded(
								Operation<Boolean> op) {
							loadbalancingResponseOp = null;
						}
					},
					requesterNodeID,
					requestOperationID);
			loadbalancingResponseOp.scheduleImmediately();
			return true;
		}
		
		if (logJoin || logMaintenance || logLeave || nodeController.enforcesLogging())
			log("callAwaitElementTableExtensionOperation: already awaiting extension.");
		return false;
	}

	
}
