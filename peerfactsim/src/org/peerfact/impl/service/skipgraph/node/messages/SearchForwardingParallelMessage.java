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

package org.peerfact.impl.service.skipgraph.node.messages;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.peerfact.Constants;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphServiceOperation.TrackerCallback;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchForwardingParallelMessage extends AbstractSkipgraphNodeRequestMessage 
									 implements SearchForwardingCategory {

	private static final long serialVersionUID = -7114807715693073234L;
	

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final ApplicationContact requesterContact;
	
	private final int requestOperationID;
	
	private final List<SearchQuery> queries;

	/** 
	 * this is for traffic analyzing only. It's not a real P2P feature.
	 * Therefor it doesn't add to the size of the message.
	 */
	private final TrackerCallback tracker;
	
	private final BigInteger senderNodeID;

	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchForwardingParallelMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			ApplicationContact requesterContact,
			int requestOperationID,
			List<SearchQuery> queries,
			TrackerCallback tracker,
			BigInteger senderNodeID) 
	{
		super(sender, receiver, operationID);
		this.requesterContact = requesterContact;
		this.requestOperationID = requestOperationID;
		this.queries = queries;
		this.tracker = tracker;
		this.senderNodeID = senderNodeID;
	}
	

	
	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/

	public ApplicationContact getRequesterContact() {
		return requesterContact;
	}

	public int getRequestOperationID() {
		return requestOperationID;
	}

	public List<SearchQuery> getQueries() {
		return queries;
	}

	public TrackerCallback getTracker() {
		return tracker;
	}
	
	public BigInteger getSenderNodeID() {
		return senderNodeID;
	}
	
	
	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		List<SearchQuery> queriesCopy = new LinkedList<>();
		for (SearchQuery query : queries) {
			queriesCopy.add(query.copy());
		}
		return new SearchForwardingParallelMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				new ApplicationContact(this.requesterContact),
				this.requestOperationID,
				queriesCopy,
				tracker,
				senderNodeID);
	}
	

	/**
	 * @return message size in bytes
	 */
	@Override
	public long getSize() {
		// requester
		long size = requesterContact.getSize();
		// requestOperationID
		size += Constants.INT_SIZE;
		// queries
		for (SearchQuery query : queries) {
			size += query.getTransmissionSize();
		}
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ ", requester="+SGUtil.formatID(requesterContact.getPeerID())
				+ ", requestOperationID="+requestOperationID
				+ ", queries="+queries
				+ ", senderNodeID="+senderNodeID
				;
	}

}
