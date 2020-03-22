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
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinLevelRequestMessage extends AbstractSkipgraphNodeRequestMessage 
									 implements JoinLevelCategory {

	private static final long serialVersionUID = -7114807715693073234L;
	

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final ApplicationContact requesterPeer;
	
	private final SkipgraphContact requesterContact;
	
	private final int levelIndex;
	
	private final int prefix;
	
	private final Route route;
	
	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public JoinLevelRequestMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			ApplicationContact requesterPeer,
			SkipgraphContact requesterContact,
			int levelIndex,
			int prefix,
			Route route) 
	{
		super(sender, receiver, operationID);
		this.requesterPeer = requesterPeer;
		this.requesterContact = requesterContact;
		this.levelIndex = levelIndex;
		this.prefix = prefix;
		this.route = route;
	}
	

	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public ApplicationContact getRequesterPeer() {
		return this.requesterPeer;
	}

	public SkipgraphContact getRequesterContact() {
		return this.requesterContact;
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
		return new JoinLevelRequestMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				new ApplicationContact(requesterPeer),
				this.requesterContact.copy(),
				this.levelIndex,
				this.prefix,
				this.route);
	}
	

	/**
	 * @return message size in bytes
	 */
	@Override
	public long getSize() {
		long size = 0l;
		// ApplicationContact requesterPeer
		size += requesterPeer.getSize();
		// SkipgraphContact requesterContact
		size += requesterContact.getTransmissionSize();
		// int levelIndex
		size += Constants.INT_SIZE;
		// int prefix
		size += Constants.INT_SIZE;
		// Route route
		size += SkipgraphServiceConstants.ROUTE_SIZE;
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ "\nrequesterPeer=" + SGUtil.formatContact(requesterPeer)
				+ ", requesterContact=" + requesterContact
				+ ", levelIndex=" + levelIndex
				+ ", prefix=" + prefix
				+ ", route=" + route;
	}

}
