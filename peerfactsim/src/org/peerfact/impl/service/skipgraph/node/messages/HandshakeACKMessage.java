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

import org.peerfact.Constants;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class HandshakeACKMessage extends AbstractSkipgraphNodeResponseMessage
								 implements HandshakeCategory {

	private static final long serialVersionUID = -1112711935760493788L;


	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphContact replyContact;
	        
	private final int levelIndex;
	        
	private final int prefix;
 
	private final Route route;
	
	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public HandshakeACKMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			SkipgraphContact replyContact,
			int levelIndex,
			int prefix,
			Route route) 
	{
		super(sender, receiver, operationID);
		this.replyContact = replyContact;
		this.levelIndex = levelIndex;
		this.prefix = prefix;
		this.route = route;
	}
	


	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/

	public SkipgraphContact getReplyContact() {
		return this.replyContact;
	}
	
	public int getLevelIndex() {
		return levelIndex;
	}

	public int getPrefix() {
		return prefix;
	}

	public Route getRoute() {
		return route;
	}

	
	
	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		return new HandshakeACKMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				this.replyContact.copy(),
				this.levelIndex,
				this.prefix,
				this.route);
	}
	
	
	/**
	 * @return message size in bytes
	 */
	@Override
	public long getSize() {
		// SkipgraphContact senderContact;
		long size = replyContact.getTransmissionSize();
		// int levelIndex;
		size += Constants.INT_SIZE;
		// int prefix;
		size += Constants.INT_SIZE;
		// Route route;
		size += SkipgraphServiceConstants.ROUTE_SIZE;
		return super.getSize() + size;
	}


	@Override
	public String toString() {
		return super.toString()
				+ "\nreplyContact=" + replyContact
				+ ", levelIndex=" + levelIndex
				+ ", prefix=" + prefix
				+ ", route=" + route;
	}

}
