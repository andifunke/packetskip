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

package org.peerfact.impl.service.skipgraph.messages;

import java.util.LinkedList;
import java.util.List;

import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphServiceOperation.TrackerCallback;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchQueryMessage extends AbstractSkipgraphServiceMessage 
								implements SearchQueryCategory {

	private static final long serialVersionUID = 4122969667473317199L;
	

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final List<SearchQuery> queries;
	
	/** 
	 * this is for traffic analyzing only. It's not a real P2P feature.
	 * Therefor it doesn't add to the size of the message.
	 */
	private final TrackerCallback tracker;


	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchQueryMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			List<SearchQuery> queries,
			TrackerCallback tracker) 
	{
		super(sender, receiver, operationID);
		this.queries = queries;
		this.tracker = tracker;
	}
	
	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public List<SearchQuery> getQueries() {
		return queries;
	}
	
	public TrackerCallback getTracker() {
		return tracker;
	}
	

	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		List<SearchQuery> queriesClone = new LinkedList<>();
		for (SearchQuery query : queries) {
			queriesClone.add(query.copy());
		}
		return new SearchQueryMessage (
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				queriesClone,
				tracker);
	}
	
	
	@Override
	public long getSize() {
		long size = 0;
		for (SearchQuery query : queries) {
			size += query.getTransmissionSize();
		}
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ " "+queries;
	}

}
