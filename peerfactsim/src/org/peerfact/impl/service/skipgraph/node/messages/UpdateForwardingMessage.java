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

import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class UpdateForwardingMessage extends AbstractSkipgraphNodeRequestMessage 
							 implements UpdateForwardingCategory {

	private static final long serialVersionUID = -7114807715693073234L;
	

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final List<SkipgraphElement> inputElements;
	
	private final List<SkipgraphElement> deleteElements;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public UpdateForwardingMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID,
			List<SkipgraphElement> inputElements,
			List<SkipgraphElement> deleteElements) 
	{
		super(sender, receiver, operationID);
		this.inputElements = inputElements == null ? null : new LinkedList<>(inputElements);
		this.deleteElements = deleteElements == null ? null : new LinkedList<>(deleteElements);
	}
	

	
	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/

	public List<SkipgraphElement> getInputElements() {
		return inputElements;
	}

	public List<SkipgraphElement> getDeleteElements() {
		return deleteElements;
	}


	
	/* ************************************************
	 ********** AbstractAppMessage METHODS ************
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
		List<SkipgraphElement> inputElementsClone = null;
		if (inputElements != null) {
			inputElementsClone = new LinkedList<>();
			for (SkipgraphElement element : inputElements) {
				inputElementsClone.add(element.copy());
			}
		}
		List<SkipgraphElement> deleteElementsClone = null;
		if (deleteElements != null) {
			deleteElementsClone = new LinkedList<>();
			for (SkipgraphElement element : deleteElements) {
				deleteElementsClone.add(element.copy());
			}
		}
		return new UpdateForwardingMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				this.getOperationID(),
				inputElementsClone,
				deleteElementsClone);
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

}
