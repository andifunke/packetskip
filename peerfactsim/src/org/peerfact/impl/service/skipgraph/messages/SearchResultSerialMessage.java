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

import java.util.LinkedHashSet;
import java.util.Set;

import org.peerfact.Constants;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchResultSerialMessage extends AbstractSkipgraphServiceMessage 
								 implements SearchResultCategory {

	private static final long serialVersionUID = 4122969667473317199L;
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final String dimension;
	
	private final Set<ApplicationContact> results;
	
	private final int messageCounter;

	private final boolean lastMessage;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchResultSerialMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			String dimension,
			Set<ApplicationContact> results,
			int messageCounter,
			boolean lastMessage) 
	{
		super(sender, receiver, operationID);
		this.dimension = dimension;
		this.results = results;
		this.messageCounter = messageCounter;
		this.lastMessage = lastMessage;
	}
	
	
	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public String getDimension() {
		return dimension;
	}
	
	public Set<ApplicationContact> getResults() {
		return results;
	}
	
	public int getCounter() {
		return messageCounter;
	}
	
	public boolean isLastMessage() {
		return lastMessage;
	}
	


	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		Set<ApplicationContact> resultsClone = new LinkedHashSet<>();
		for (ApplicationContact contact : results) {
			resultsClone.add(new ApplicationContact(contact));
		}
		return new SearchResultSerialMessage (
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				this.dimension,
				resultsClone,
				this.messageCounter,
				this.lastMessage);
	}
	
	
	@Override
	public long getSize() {
		long size = 0;
		// results
		for (ApplicationContact contact : results) {
			size += contact.getSize();
		}
		// messageCounter
		size += Constants.INT_SIZE;
		// lastMessage
		size += Constants.BOOLEAN_SIZE;
		// dimension
		size += 2*dimension.length();
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ "\ndimension="+dimension
				+ ", results="+results
				+ ", counter="+messageCounter
				+ ", last="+lastMessage;
	}

}
