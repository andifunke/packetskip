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
import java.util.Set;

import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.node.ContactLevel;
import org.peerfact.impl.service.skipgraph.node.Replacement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingMessage;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelNotifyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingMessage;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RangeAdjustmentACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RangeAdjustmentMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RangeAdjustmentNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.ReplaceContactsACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.ReplaceContactsMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingSerialMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingParallelMessage;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class OperationResponder {
	
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final BigInteger nodeID;
	
	private final SkipgraphNodeController nodeController;
	
	@SuppressWarnings("unused")
	private boolean logJoinLevelOp = false;
	
	private Set<BigInteger> deprecatedNodeIDs = new LinkedHashSet<>();
	
	private boolean logJoin = SkipgraphServiceConstants.logJoin;

	private boolean logLeave = SkipgraphServiceConstants.logLeave;

	private boolean logMaintenance = SkipgraphServiceConstants.logMaintenance;

	private boolean logSearch = SkipgraphServiceConstants.logSearch;

	/**
	 * to avoid concurrent joins a level this map indicates if a join request 
	 * is currently processed
	 * key = level
	 * value = nodeID that has send a request for this level
	 */
	private final Map<Integer, BigInteger> joinLevelBusyMap = new LinkedHashMap<>();
	
	/**
	 * to avoid concurrent linking on a level this map indicates if a handshake 
	 * is currently processed
	 * key = level. positive values: handshake with next pointer. 
	 *              negative values: handshake with prev pointer.
	 *              handshakes on level 0 are always with next pointer.
	 * value = nodeID that has send a request for this level
	 */
	private Map<Integer, BigInteger> handshakeBusyMap = new LinkedHashMap<>();
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public OperationResponder(SkipgraphNodeController nodeController) {
		this.nodeController = nodeController;
		this.nodeID = nodeController.getNodeID();
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	private SkipgraphService getService() {
		return nodeController.getService();
	}
	
	private SkipgraphContact getContact() {
		return nodeController.getContact();
	}
	
	public Map<Integer, BigInteger> getJoinLevelBusyMap() {
		return joinLevelBusyMap;
	}

	public Map<Integer, BigInteger> getHandshakeBusyMap() {
		return handshakeBusyMap;
	}


	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	private void log(String str) {
		nodeController.log(str);
	}
	
	
	
	/* ************************************************
	 *********** EXECUTE MESSAGE REQUESTS *************
	 ************************************************ */

	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeUpdateForwarding(UpdateForwardingMessage message) {
		//log("executing PassData. operationID="+message.getOperationID());
		
		message.addHop(nodeController.getService().getLocalContact().getPeerID());
		if (nodeController.update(message.getOperationID(), message.getInputElements(), message.getDeleteElements()))
			return new UpdateForwardingACKMessage(nodeController.getService().getLocalContact(), 
					message.getSender(), message.getOperationID());
		
		return new UpdateForwardingNACKMessage(nodeController.getService().getLocalContact(), 
				message.getSender(), message.getOperationID());
	}



	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeSearchSerialForwarding(SearchForwardingSerialMessage message) {
		//log("executing SearchForwarding. operationID="+message.getOperationID());
		message.getTracker().addHop(nodeController.getService().getLocalContact().getPeerID());
		SearchQuery query = message.getQuery();
		
		if (nodeController.isDisabled()) {
			// forward message!
			nodeController.getOperationCaller().prepareSearchSerialForwardingOperation(
					message.getRequesterContact(),
					message.getRequestOperationID(),
					query,
					message.getMessageCounter(),
					message.getTracker());
			return new SearchForwardingACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID());
		}

		if (query == null) {
			log("no valid query. aborting... sending NACK");
			return new SearchForwardingNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID());
		}
		
		// here we only test if the start of a query is inside our range.
		// - for the serial search approach this should be sufficient
		// - for a parallel search approach we need to test, if the query range and our range 
		// have any intersections.
		boolean last = false;
		int counter = message.getMessageCounter();
		if (nodeController.isResponsibleForStart(query.getRangeStart())) {
			// increment counter only if this node is involved in the search
			counter++;
			// 1) adding results to the result-set -> send to requester
			// 2) adjusting query (updatedQuery) -> forward along the skip graph

			BigDecimal rangeStart = query.getRangeStart();
			BigDecimal rangeEnd = query.getRangeEnd();
			int maxNumberOfValues = query.getMaxNumberOfValues();
			Set<ApplicationContact> results = nodeController.getElementTable().getContacts(
					query.getDimension(),
					rangeStart,
					rangeEnd,
					maxNumberOfValues,
					false);

			// do we have to continue the search for this dimension?
			last = nodeController.isResponsibleForEnd(rangeEnd);
			// our node is responsible for the upper limit of the search interval
			// the search for this dimension is finished.
			last |= maxNumberOfValues > 0 && results.size() >= maxNumberOfValues;
			// we have already enough results. We can end the search for this dimension here.

			// send results to requester
			nodeController.getOperationCaller().callSearchSerialResultOperation(
					message.getRequesterContact(),
					message.getRequestOperationID(),
					query.getDimension(),
					results,
					counter,
					last,
					message.getTracker());
			
			if (!last) {
				// if not finished: let's update our query
				maxNumberOfValues = maxNumberOfValues > 0 ? (maxNumberOfValues-results.size()) : 0;
				rangeStart = nodeController.getElementTable().getRangeEnd();
				query = new SearchQuery(
						query.getDimension(), 
						rangeStart, 
						rangeEnd, 
						maxNumberOfValues);
			}
		}
		
		// forward (updated) query along the skipgraph if necessary
		if (!last) {
			nodeController.getOperationCaller().prepareSearchSerialForwardingOperation(
					message.getRequesterContact(),
					message.getRequestOperationID(),
					query,
					counter,
					message.getTracker());
		}
	
		return new SearchForwardingACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID());
	}



	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeSearchParallelForwarding(SearchForwardingParallelMessage message) {
		if (logSearch)
			log("executing SearchParallelForwarding. operationID="+message.getOperationID());
		
		message.getTracker().addHop(nodeController.getService().getLocalContact().getPeerID());
		nodeController.getOperationCaller().prepareSearchParallelForwardingOperation(
				message.getRequesterContact(), 
				message.getRequestOperationID(), 
				message.getQueries(), 
				message.getTracker(),
				message.getSenderNodeID());

		return new SearchForwardingACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID());
	}



	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeRangeAdjustment(RangeAdjustmentMessage message) {
		//log("executing RangeUpdate for "+message.getSenderContact());
		
		if (nodeController.isDisabled())
			return new RangeAdjustmentNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), true);

		nodeController.updateContacts(message.getSenderContact());
		return new RangeAdjustmentACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID());
	}



	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeRemainingSize(RemainingSizeMessage message) {
		if (logJoin)
			log("executing RemainingSize request: "+message);

		if (nodeController.isDisabled())
			return new RemainingSizeNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), true);
		
		
		int remainingSize = 0;
		if (nodeController.getOperationCaller().acceptsLoadbalancing()) {
			remainingSize = nodeController.getElementTable().getRemainingSizeOffer();
			if (logJoin)
				log("idle: offering="+remainingSize);
		}
		else {
			if (logJoin)
				log("busy: rejecting remaining size request.");
		}
		
		if (remainingSize > 0) {
			nodeController.getOperationCaller()
					.callAwaitElementTableExtensionOperation(message.getSenderNodeID(), message.getOperationID());
		}
		
		//log("executing RemainingSize. offering="+remainingSize);
		return new RemainingSizeACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID(), nodeController.getNodeID(), remainingSize);
	}



	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeJoinLevelRequest(JoinLevelRequestMessage message) {
		if (logJoin)
			log("executing JoinLevelRequest for "+message);

		//String logStr = "";
		int requestOperationID = message.getOperationID();
		int levelIndex = message.getLevelIndex();
		int prefix = message.getPrefix();
		Route route = message.getRoute();
		Route backroute = SGUtil.switchRoute(route);
		SkipgraphContact requesterContact = message.getRequesterContact();
		nodeController.updateContacts(requesterContact);
		boolean success = false;

		
		// if the node controller is disabled we forward the message
		if (nodeController.isDisabled()) {
			log("OperationResponder executing JoinLevelRequest: "
					+ "nodeController is disabled. forwarding message");
			if (levelIndex > 0) {
				BigInteger newReceiverID = null;
				ContactLevel parentLevel = nodeController.getContactTable().getLevel(levelIndex-1);

				// which route?
				if (route == Route.PREV) {
					newReceiverID = parentLevel.getPrevContact().getNodeID();
				}
				else if (route == Route.NEXT) {
					newReceiverID = parentLevel.getNextContact().getNodeID();
				}

				if (newReceiverID != null) {
					// forwarding the joinLevelRequest
					nodeController.getOperationCaller().callJoinLevelRequestOperation(
							newReceiverID,
							message.getRequesterPeer(),
							requestOperationID, 
							requesterContact, 
							levelIndex, 
							prefix, 
							route, 
							true);
					// notifiying requester about forwarding
					nodeController.getOperationCaller().callJoinLevelNotifyOperation(
							requestOperationID,
							message.getRequesterPeer(),
							requesterContact.getNodeID(), 
							newReceiverID);
					success = true;
				}
			}
			if (success)
				return new JoinLevelRequestACKMessage(getService().getLocalContact(), message.getSender(), 
						message.getOperationID());
			log("OperationResponder executing JoinLevelRequest: ERROR: something went wrong on disabled node."); 
			return new JoinLevelRequestNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), true);
		}
		
		
		//logStr += String.format("executing JoinLevel request: level=%d, prefix=%d, route=%s, "
		//		+ "requesterContact=%s, operationID=%d",
		//		levelIndex, prefix, route, requesterContact, requestOperationID) + "\n";
		boolean levelExists = false;
		boolean levelOptimized = false;

		ContactLevel level = null;
		// level already existent?
		if (-1 < levelIndex && levelIndex < nodeController.getContactTable().size()) {
			// level does exist
			level = nodeController.getContactTable().getLevel(levelIndex);
			levelExists = true;
		//	logStr += "level does exist: "+level + "\n";
		}
		else if (levelIndex == nodeController.getContactTable().size()) {
			// level does not exists
			// add new level
			nodeController.addDefaultTopLevel();
			level = nodeController.getContactTable().getTopLevel();
		//	logStr += "level="+levelIndex+" does not exist - add new level: "+level + "\n";
		}
		else {
			// level does not exist - but even more levels are missing as well
			try {
				throw new Exception("OperationResponder executing JoinLevelRequest: "
						+ "ERROR: more than 1 levels are missing. this should not happen!");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			log("ERROR: more levels are missing. this should not happen.");
		}

		if (level != null) {
				// test, if the requester is already linked as prev contact on the second highest level 
				// -> avoid linking again on top level if possible
				// logStr += nodeController.optimizeTopLevelPrefix(requesterContact.getNodeID(), prefix, backRoute);
				// test, if the requester is already linked as next contact on the second highest level
				// -> avoid linking again on top level if possible
				// test only, if the range of the requester is higher the range of the executor
				// -> avoid feedback loops
				// different level optimization approach - experimental - not used
				//if (requesterContact.getRangeStart().compareTo(nodeController.getElementTable().getRangeStart()) > 0)
				//	levelOptimized = nodeController.optimizeTopLevelPrefix(requesterContact.getNodeID(), prefix, route);
			// same prefix on this level? -> join with requesting node on this level
			if (prefix == level.getPrefix()) {
		//		if (requesterContact.getNodeID().equals(nodeID)) {
		//			logStr += "executing JoinLevel request: We received our own message. "
		//					+ "no other node for this level and prefix available. "
		//					+ "sending reply anyway." + "\n";
		//		}
		//		logStr += "calling JoinLevelReplyOperation" + "\n";
				
				// check if node is already busy processing a join level request for another node on this level
				if (joinLevelBusyMap.containsKey(levelIndex)) {
					if (!joinLevelBusyMap.get(levelIndex).equals(requesterContact.getNodeID())) {
						if (logJoin)
							log("busy: cannot process "+message+ "\nbusyMap="+joinLevelBusyMap);
						return new JoinLevelRequestNACKMessage(getService().getLocalContact(), message.getSender(), 
								message.getOperationID(), false, true);
					}
				}
				// check if node is already busy processing a handshake with another node on this level
				if (handshakeBusyMap.containsKey((backroute==Route.PREV?-1:1)*levelIndex)) {
					if (!handshakeBusyMap.get((backroute==Route.PREV?-1:1)*levelIndex)
							.equals(requesterContact.getNodeID())) {
						if (logJoin)
							log("already busy executing another handshake for level="+levelIndex+" "+backroute);
						return new JoinLevelRequestNACKMessage(getService().getLocalContact(), message.getSender(), 
								message.getOperationID(), false, true);
					}
				}


				// else: execute request
				nodeController.getOperationCaller().callJoinLevelReplyOperation(
						requestOperationID,
						message.getRequesterPeer(),
						requesterContact,
						levelIndex,
						prefix,
						backroute);
				success = true;
			}
			else {
				// same level, but different prefix -> forward message to prev/next hop on levelIndex-1
				// 									-> notify JoinLevelRequester
				if (levelIndex > 0) {
					BigInteger newReceiverID = null;
					ContactLevel parentLevel = nodeController.getContactTable().getLevel(levelIndex-1);

					// which route?
					if (route == Route.PREV) {
						newReceiverID = parentLevel.getPrevContact().getNodeID();
					}
					else if (route == Route.NEXT) {
						newReceiverID = parentLevel.getNextContact().getNodeID();
					}

					if (newReceiverID != null) {
						// forwarding the joinLevelRequest
						nodeController.getOperationCaller().callJoinLevelRequestOperation(
								newReceiverID, 
								message.getRequesterPeer(),
								requestOperationID, 
								requesterContact, 
								levelIndex, 
								prefix, 
								route, 
								true);
						// notifiying requester about forwarding
						nodeController.getOperationCaller().callJoinLevelNotifyOperation(
								requestOperationID,	
								message.getRequesterPeer(),
								requesterContact.getNodeID(), 
								newReceiverID);
						success = true;

		//				logStr += "different prefix="+level.getPrefix()+" -> forwaring request to "
		//						+ SGUtil.formatID(newReceiverID)+" & notifying requester " 
		//						+ SGUtil.formatID(requesterContact.getNodeID())+ "\n";
					}
		//			else {
		//				logStr += "newReveiverID == null" + "\n";
		//			}

					// initialize an own JoinLevelRequest, if the top level was already existent, 
					// and the prefix was changed during optimization (which can only be true, if the top level
					// was linked with itself.
					if (levelExists && levelOptimized) {
		//				logStr += "top level already exists, is self-linked and the prefix has changed -> "
		//						+ "preparing a new JoinLevelRequest";
						nodeController.getOperationCaller().prepareJoinLevelRequestOperation();
					}
				}
			}
		}
		//if (logJoinLevelOp) {
		//	log(logStr);
		//	nodeController.print();
		//}
		if (success)
			return new JoinLevelRequestACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID());
		log("OperationResponder executing JoinLevelRequest: ERROR: something went wrong"); 
		return new JoinLevelRequestNACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID());
	}



	/**
	 * 
	 * @param message
	 */
	public void executeJoinLevelNotify(JoinLevelNotifyMessage message) {
		//log("executing JoinLevelNotify. operationID="+message.getOperationID());

		nodeController.getOperationCaller().deliver(message);
	}



	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeJoinLevelReply(JoinLevelReplyMessage message) {
		//log("executing JoinLevelReply. operationID="+message.getOperationID());

		if (nodeController.isDisabled())
			return new JoinLevelReplyNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), true);

		if (nodeController.getOperationCaller().deliver(message))
			return new JoinLevelReplyACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID());

		return new JoinLevelReplyNACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID());
	}


		
	/**
	 * the method assumes that a new node has joined the skip graph and has become
	 * predecessor to this node on a given level. The new node has sent its contact information
	 * for that level. -> update the ContactTable
	 * and return contact information about this node.
	 * 
	 * TODO: a handshake with a new successor is currently not possible. 
	 * 		 May be implemented if needed.
	 *  
	 * @param receivingContact
	 * @return this contact
	 */
	public AbstractSkipgraphMessage executeHandshake(HandshakeMessage message) {
		if (logJoin)
			log("executing Handshake for "+message.getSenderContact());
		
		if (nodeController.isDisabled())
			return new HandshakeNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), true);
		
		int levelIndex = message.getLevelIndex();
		int prefix = message.getPrefix();
		Route route = message.getRoute();
		Route backroute = SGUtil.switchRoute(route);
		SkipgraphContact senderContact = message.getSenderContact();
		
		if (handshakeBusyMap.containsKey((backroute==Route.PREV?-1:1)*levelIndex)) {
			if (!handshakeBusyMap.get((backroute==Route.PREV?-1:1)*levelIndex)
					.equals(senderContact.getNodeID())) {
			log("already busy executing another handshake for level="+levelIndex+" "+backroute);
			return new HandshakeNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), false, true);
			}
		}

		
		if (-1 < levelIndex && levelIndex < nodeController.getContactTable().size()) {
			int levelPrefix = nodeController.getContactTable().getLevel(levelIndex).getPrefix();
			if (prefix == levelPrefix) {
				if (route == Route.PREV) {
					nodeController.setNextOnLevel(senderContact, levelIndex);
					route = Route.NEXT;
				}
				else {
					nodeController.setPrevOnLevel(senderContact, levelIndex);
					route = Route.PREV;
				}
				nodeController.updateContacts(senderContact);
				if(logJoin) {
					log("handshake accepted: ");
					nodeController.printContacts();
				}
				return new HandshakeACKMessage(getService().getLocalContact(), message.getReceiver(),
						message.getOperationID(), getContact(), levelIndex, levelPrefix, route);
			}
			log("handshake not accepted: different prefix.");
			return new HandshakeNACKMessage(getService().getLocalContact(), message.getSender(), 
						message.getOperationID());
		}
		log("handshake not accepted: level does not exist.");
		return new HandshakeNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID());
		
	}


	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeLoadBalancing(LoadBalancingMessage message) {
		if (logJoin || logLeave || logMaintenance || nodeController.enforcesLogging())
			log("executing ElementTableExtension: \n"+message.getElementTable());
		
		if (nodeController.isDisabled()) {
			return new LoadBalancingNACKMessage(getService().getLocalContact(), message.getSender(), 
					message.getOperationID(), true);
		}

		if (nodeController.getOperationCaller().isAwaitingTableExtension()) {
			if (nodeController.getOperationCaller().getAwaitEtExtension().deliverMessage(message)) {
				// send ACK only if we're awaiting this element table extension
				return new LoadBalancingACKMessage(getService().getLocalContact(), message.getSender(), 
						message.getOperationID(), nodeController.getContact());
			}
		}

		return new LoadBalancingNACKMessage(getService().getLocalContact(), message.getSender(), 
				message.getOperationID());
	}


	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public AbstractSkipgraphMessage executeReplaceContacts(ReplaceContactsMessage message) {
		if (logLeave || nodeController.enforcesLogging()) {
			log("executing ReplaceContact: before replacing contact:\n"+message);
			nodeController.printContacts();
		}
		
		// if a sender is leaving we add him to a set of deprecated nodeIDs
		// we will not accept deprecatedNodeIDs as replacement
		// and we will use this set for a contact cleanup
		// don't mark yourself as deprecated
		if (!message.getSenderNodeID().equals(nodeID) && message.isSenderDisabled()) {
			deprecatedNodeIDs.add(message.getSenderNodeID());
			if (logLeave || nodeController.enforcesLogging())
				log(SGUtil.formatID(message.getSenderNodeID())+ " added to deprecated set");
		}

		// forward the replacement if the node is itself disabled
		if (nodeController.isDisabled()) {
			Map<BigInteger, List<Replacement>> replacementsMap = new LinkedHashMap<>();
			List<Replacement> replaceList;
			for (Replacement replacement : message.getReplacements()) {
				SkipgraphContact replacementContact = replacement.getReplacementContact();
				
				// we don't send ourself as replacement since we are disabled
				if (replacementContact.getNodeID().equals(nodeID))
					continue;
				// we don't send the sender as replacement since he is disabled (unlikely case)
				if (replacementContact.getNodeID().equals(message.getSenderNodeID()))
					continue;

				int levelIndex = replacement.getLevelIndex();
				BigInteger receiverID = null;

				ContactLevel level = nodeController.getContactTable().getLevel(levelIndex);
				if (replacement.getRoute() == Route.PREV) {
					receiverID = level.getNextContact().getNodeID();
				}
				else {
					receiverID = level.getPrevContact().getNodeID();
				}
				
				// we don't send a message TO ourselves
				if (receiverID.equals(nodeID))
					continue;
				// we don't send a message to ANYONE in the deprecated set
				if (deprecatedNodeIDs.contains(receiverID))
					continue;

				replaceList = replacementsMap.getOrDefault(receiverID, new LinkedList<>());
				replaceList.add(replacement);
				replacementsMap.put(receiverID, replaceList);
			}
			if (!replacementsMap.isEmpty()) {
				nodeController.getOperationCaller().callReplaceContactsOperation(replacementsMap);
			}

			return new ReplaceContactsACKMessage(
					getService().getLocalContact(), 
					message.getSender(), 
					message.getOperationID(), 
					nodeID);
		}

		BigInteger leavingNodeID = message.getSenderNodeID();
		boolean changed = false;
		
		for (Replacement replacement : message.getReplacements()) {

			SkipgraphContact replacementContact = replacement.getReplacementContact();
			
			if (logLeave || nodeController.enforcesLogging())
				if (deprecatedNodeIDs.contains(replacementContact.getNodeID()))
					log("replacementContact "+replacementContact+ " is in deprecated set");

			// check if the level is existing
			if (nodeController.getContactTable().size() > replacement.getLevelIndex()) {
				int levelIndex = replacement.getLevelIndex();
				ContactLevel level = nodeController.getContactTable().getLevel(levelIndex); 
				// check if the level has the same prefix
				if (level.getPrefix() == replacement.getLevelPrefix()) {
					Route route = replacement.getRoute(); 
					// replace prev
					if (route == Route.PREV) {
						// check if the current contact on that position is identical with the contact that is leaving
						if (level.getPrevContact().getNodeID().equals(message.getSenderNodeID())) {
							changed |= nodeController.setContactOnLevelAndCleanUp(
									route, 
									replacementContact, 
									levelIndex, 
									deprecatedNodeIDs, 
									leavingNodeID);
							// TODO: could be followed by a handshake operation, but not mandatory
						}
					}
					// replace next
					else {
						// check if the current contact on that position is identical with the contact that is leaving
						if (level.getNextContact().getNodeID().equals(message.getSenderNodeID())) {
							changed |= nodeController.setContactOnLevelAndCleanUp(
									route, 
									replacementContact, 
									levelIndex, 
									deprecatedNodeIDs, 
									leavingNodeID);
							// TODO: could be followed by a handshake operation, but not mandatory
						}
					}
					if (logLeave || nodeController.enforcesLogging()) 
						nodeController.printContacts();
				}
			}
		}
		
		if (changed) nodeController.save();
		
		return new ReplaceContactsACKMessage(
				getService().getLocalContact(), 
				message.getSender(), 
				message.getOperationID(), 
				nodeID);
	}

}
