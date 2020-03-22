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

import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphNodeRequestMessage extends AbstractSkipgraphNodeMessage {

	private static final long serialVersionUID = 7723198541658991134L;
	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public AbstractSkipgraphNodeRequestMessage(
			ApplicationContact sender, 
			ApplicationContact receiver, 
			int operationID) 
	{
		super(sender, receiver, operationID);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/

	public BigInteger getReceivingNodeID() {
		return getReceiver().getPeerID();
	}
	
	
	@Override
	public String toString() {
		return super.toString() + " receivingNodeID="+SGUtil.formatID(getReceivingNodeID());
	}
	
}
