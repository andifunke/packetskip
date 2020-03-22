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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceInterface.UpdateCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceInterface.SearchQueryCallback;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphServiceMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestACKMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestNACKMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultParallelMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultSerialMessage;
import org.peerfact.impl.service.skipgraph.node.ContactTable;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.service.skipgraph.operations.Addressable;
import org.peerfact.impl.service.skipgraph.operations.SearchQueryOperation;
import org.peerfact.impl.service.skipgraph.operations.UpdateOperation;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class ServiceOperationManager {


	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphService service;

	private final Map<Integer, Addressable> addressableOperations = new LinkedHashMap<>();



	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public ServiceOperationManager(SkipgraphService service) {
		this.service = service;
	}



	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public Map<Integer, Addressable> getOperationMap() {
		return addressableOperations;
	}



	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	private void log(String str) {
		service.log(str);
	}



	/* ************************************************
	 ************** OPERATION CALLER ******************
	 ************************************************ */

	public void callUpdateOperation(
			List<SkipgraphElement> inputElements,
			List<SkipgraphElement> deleteElements,
			UpdateCallback modifyDataCallback) {
		UpdateOperation calledOperation = new UpdateOperation(
				service, 
				new OperationCallback<String>() {

					@Override
					public void calledOperationFailed(Operation<String> op) {
						modifyDataCallback.gotAnswer(false);
					}

					@Override
					public void calledOperationSucceeded(Operation<String> op) {
						modifyDataCallback.gotAnswer(true);
					}

				},
				inputElements,
				deleteElements);
		calledOperation.scheduleImmediately();
	}
	
	

	public void callSearchOperation(
			List<SearchQuery> searchQueries,
			int k,
			SearchQueryCallback searchQueryCallback) {
		SearchQueryOperation calledOperation = new SearchQueryOperation(
				service,
				new OperationCallback<Set<ApplicationContact>>() {

					@Override
					public void calledOperationFailed(Operation<Set<ApplicationContact>> op) {
						searchQueryCallback.gotAnswer(false, null);
						addressableOperations.remove(op.getOperationID());
					}
		
					@Override
					public void calledOperationSucceeded(Operation<Set<ApplicationContact>> op) {
						searchQueryCallback.gotAnswer(true, op.getResult());
						addressableOperations.remove(op.getOperationID());
					}
		
				},
				searchQueries,
				k);
		addressableOperations.put(calledOperation.getOperationID(), calledOperation);
		calledOperation.scheduleImmediately();
	}
	
	
	
	/* ************************************************
	 ************ OPERATION RESPONDER *****************
	 ************************************************ */

	public boolean deliver(AbstractSkipgraphServiceMessage message) {
		int operationID = message.getOperationID();
		if (addressableOperations.containsKey(operationID)) {
			//log("delivering to <" + operationMap.get(operationID) + ">\n"+message);
			return addressableOperations.get(operationID).deliverMessage(message);
		}

		if (SkipgraphServiceConstants.logSearch)
			log("unable to deliver: can't find an associated operation for "+message + "\n"+addressableOperations);
		return false;
	}

	
	public void executeSearchSerialResult(SearchResultSerialMessage message) {
		//log("executing SearchResult. operationID="+message.getOperationID());
		deliver(message);
	}
	
	
	public void executeSearchParallelResult(SearchResultParallelMessage message) {
		//log("executing SearchResult. operationID="+message.getOperationID());
		deliver(message);
	}
	
	
	/**
	 * initializes a SkipGraphNode with certain parameters
	 * (e.g. contact table and element table) 
	 */
	public AbstractSkipgraphMessage executeJoinRequest(JoinRequestMessage message) {
		SkipgraphNode node = new SkipgraphNode(
					message.getJoiningNodeID(),
					message.getElementTable(),
					new ContactTable(message.getJoiningNodeID(), message.getPrevContact(), message.getNextContact()));
		if (service.addSkipgraphNode(node)) {
			return new JoinRequestACKMessage(
					service.getLocalContact(), 
					message.getSender(), 
					message.getOperationID(), 
					node.getContact());
		}
		return new JoinRequestNACKMessage(
				service.getLocalContact(),
				message.getSender(), 
				message.getOperationID());
	}
	
	
}

