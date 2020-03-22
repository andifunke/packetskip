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
import java.util.LinkedList;
import java.util.List;

import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class UpdateMessage extends AbstractSkipgraphServiceMessage 
		implements UpdateCategory {

	private static final long serialVersionUID = 332205246530346559L;
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	/** a list of capacities that the sender wants to add. */
	private List<SkipgraphElement> inputElements;

	/** a list of capacities that the sender wants to remove. */
	private List<SkipgraphElement> deleteElements;


	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public UpdateMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			List<SkipgraphElement> inputElements,
			List<SkipgraphElement> deleteElements) 
	{
		super(sender, receiver, operationID);
		this.inputElements = inputElements;
		this.deleteElements = deleteElements;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public List<SkipgraphElement> getInputElements() {
		return inputElements;
	}

	public List<SkipgraphElement> getDeleteElements() {
		return deleteElements;
	}

	

	/* ************************************************
	 ****************** METHODS ***********************
	 ************************************************ */

	public void addHop(BigInteger peerID) {
		if (inputElements != null) {
			for (SkipgraphElement element : inputElements) {
				element.addInputHop(peerID);
			}
		}
		if (deleteElements != null) {
			for (SkipgraphElement element : deleteElements) {
				element.addDeleteHop(peerID);
			}
		}
	}
	
	
	@Override
	public AbstractAppMessage copy() {
		return new UpdateMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				cloneInputElements(),
				cloneDeleteElements());
	}


	/**
	 * @return message size in bytes
	 */
	@Override
	public long getSize() {
		long size = 0;
		if (inputElements != null) {
			for (SkipgraphElement element : inputElements) {
				size += element.getTransmissionSize();
			}
		}
		if (deleteElements != null) {
			for (SkipgraphElement element : deleteElements) {
				size += element.getTransmissionSize();
			}
		}
		return super.getSize() + size;
	}
	
	
	@Override
	public String toString() {
		return super.toString() 
				+ "\ninputElements: "+inputElements
				+ "\ndeleteElements: "+deleteElements;
	}

	
	public List<SkipgraphElement> cloneInputElements() {
		List<SkipgraphElement> clone = null;
		if (inputElements != null) {
			clone = new LinkedList<>();
			for (SkipgraphElement element : inputElements) {
				clone.add(element.copy());
			}
		}
		return clone;
	}
	
	
	public List<SkipgraphElement> cloneDeleteElements() {
		List<SkipgraphElement> clone = null;
		if (deleteElements != null) {
			clone = new LinkedList<>();
			for (SkipgraphElement element : deleteElements) {
				clone.add(element.copy());
			}
		}
		return clone;
	}
	
}
