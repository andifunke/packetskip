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

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchResultParallelMessage extends AbstractSkipgraphServiceMessage 
								 implements SearchResultCategory {

	private static final long serialVersionUID = 4122969667473317199L;
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final Map<String, Set<ApplicationContact>> results;
	
	private final Set<BigInteger> messageHopTrackerSet;
	
	private final BigInteger senderNodeID;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchResultParallelMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			Map<String, Set<ApplicationContact>> results,
			Set<BigInteger> messageHopTrackerSet,
			BigInteger senderNodeID) 
	{
		super(sender, receiver, operationID);
		this.results = results;
		this.messageHopTrackerSet = messageHopTrackerSet;
		this.senderNodeID = senderNodeID;
	}
	
	
	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public Map<String, Set<ApplicationContact>> getResults() {
		return results;
	}
	
	public Set<BigInteger> getHopTrackerSet() {
		return messageHopTrackerSet;
	}
	
	public BigInteger getSenderNodeID() {
		return senderNodeID;
	}

	
	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		Map<String, Set<ApplicationContact>> resultsCopy = new LinkedHashMap<>();
		for (Entry<String, Set<ApplicationContact>> entry : results.entrySet()) {
			Set<ApplicationContact> dimensionCopy = new LinkedHashSet<>();
			for (ApplicationContact contact : entry.getValue()) {
				dimensionCopy.add(new ApplicationContact(contact));
			}
			resultsCopy.put(entry.getKey(), dimensionCopy);
		}
		Set<BigInteger> forwardedToCopy = new LinkedHashSet<>();
		for (BigInteger contact : messageHopTrackerSet) {
			forwardedToCopy.add(contact);
		}
		return new SearchResultParallelMessage (
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				resultsCopy,
				forwardedToCopy,
				senderNodeID);
	}
	
	
	@Override
	public long getSize() {
		long size = 0;
		// results
		for (Entry<String, Set<ApplicationContact>> entry : results.entrySet()) {
			// dimension
			size += 2*entry.getKey().length();
			for (ApplicationContact contact : entry.getValue()) {
				size += contact.getSize();
			}
		}
		for (BigInteger contact : messageHopTrackerSet) {
			size += contact.toByteArray().length;
		}
		size += senderNodeID.toByteArray().length;
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ ", results="+results
				+ ", messageHopTrackerSet="+messageHopTrackerSet
				+ ", sederNodeID="+senderNodeID
				;
	}

}
