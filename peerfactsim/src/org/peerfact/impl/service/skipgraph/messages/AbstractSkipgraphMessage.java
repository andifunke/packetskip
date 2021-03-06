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

import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphMessage extends AbstractAppMessage {

	private static final long serialVersionUID = -2027179761450540700L;
	
	private boolean logging = false;
	
	public boolean isLogging() {
		return logging;
	}
	
	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	

	/**
	 * Set sender / receiver contact.
	 * Apply the given operationID.
	 * @param sender
	 * @param receiver
	 * @param operationID
	 */
	public AbstractSkipgraphMessage(
			ApplicationContact sender,
			ApplicationContact receiver,
			int operationID) 
	{
		super(sender, receiver, operationID);
	}

	public String getClassName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "✉ " + getClassName() + " ["
				+ "sender=" + SGUtil.formatID(getSender().getPeerID()) 
				+ ", receiver=" + SGUtil.formatID(getReceiver().getPeerID())
				+ ", operationID=" + getOperationID() 
				+ ", timestamp=" + getTimestamp() + "]";
	}
	
	public void log(String str) {
		log(str, false);
	}

	public void log(String str, boolean forceLogging) {
		if (logging || forceLogging)
			System.out.println(this + str);
	}

}
