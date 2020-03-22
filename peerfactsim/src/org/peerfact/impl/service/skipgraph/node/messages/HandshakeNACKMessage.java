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
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class HandshakeNACKMessage extends AbstractSkipgraphNodeResponseMessage 
								  implements HandshakeCategory {

	private static final long serialVersionUID = -7114807715693073234L;
	
	
	private boolean senderIsBusy = false;

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public HandshakeNACKMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID)
	{
		super(sender, receiver, operationID);
	}
	
	public HandshakeNACKMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			boolean senderIsDisabled) 
	{
		super(sender, receiver, operationID, senderIsDisabled);
	}
	
	public HandshakeNACKMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			boolean senderIsDisabled,
			boolean senderIsBusy) 
	{
		super(sender, receiver, operationID, senderIsDisabled);
		this.senderIsBusy = senderIsBusy;
	}
	
	
	
	public boolean isSenderBusy() {
		return senderIsBusy;
	}
	

	
	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		return new HandshakeNACKMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				getOperationID(),
				isSenderDisabled(),
				senderIsBusy
				);
	}
	
	@Override
	public long getSize() {
		return super.getSize() + Constants.BOOLEAN_SIZE;
	}
	
	
	@Override
	public String toString() {
		return super.toString() + ", senderIsBusy"+senderIsBusy;
	}
	
}
