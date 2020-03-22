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

import org.peerfact.impl.service.skipgraph.node.ElementTable;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinRequestMessage extends AbstractSkipgraphServiceMessage
						 implements JoinRequestCategory {

	private static final long serialVersionUID = 2202990182276671973L;

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final BigInteger joiningNodeID;

	private final SkipgraphContact prevContact;
	
	private final SkipgraphContact nextContact;

	private final ElementTable elementTable;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	/**
	 * @param sender
	 * @param receiver
	 * @param operationID
	 * @param elementTable
	 * @param prevContact
	 * @param nextContact
	 * @param joiningNodeID
	 */
	public JoinRequestMessage(
			ApplicationContact sender, 
			ApplicationContact receiver, 
			int operationID,
			BigInteger joiningNodeID,
			SkipgraphContact prevContact,
			SkipgraphContact nextContact,
			ElementTable elementTable) 
	{
		super(sender, receiver, operationID);
		if (joiningNodeID == null) {
			try {
				throw new NullPointerException("joiningNodeID must not be null");
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		this.joiningNodeID = joiningNodeID;
		this.prevContact = prevContact;
		this.nextContact = nextContact;
		this.elementTable = elementTable;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public ElementTable getElementTable() {
		return elementTable;
	}

	public SkipgraphContact getPrevContact() {
		return prevContact;
	}

	public SkipgraphContact getNextContact() {
		return nextContact;
	}

	public BigInteger getJoiningNodeID() {
		return joiningNodeID;
	}



	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
	 ************************************************ */

	@Override
	public AbstractAppMessage copy() {
		return new JoinRequestMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				this.joiningNodeID,
				this.prevContact.copy(),
				this.nextContact.copy(),
				this.elementTable.copy()
				);
	}

	
	@Override
	public long getSize() {
		long size = 0;
		size += getElementTable().getTransmissionSize();
		size += getPrevContact().getTransmissionSize();
		size += getNextContact().getTransmissionSize();
		size += joiningNodeID.toByteArray().length;
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ "\njoiningNodeID="+SGUtil.formatID(joiningNodeID)
				+ ", prev="+prevContact
				+ ", next="+nextContact
				+ "elementTable="+elementTable
				;
	}

}
