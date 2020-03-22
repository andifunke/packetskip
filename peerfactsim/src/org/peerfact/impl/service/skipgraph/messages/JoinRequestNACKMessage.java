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

import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinRequestNACKMessage extends DefaultNACKMessage 
							 implements JoinRequestCategory {

	private static final long serialVersionUID = 1L;

	public JoinRequestNACKMessage(
			ApplicationContact sender, 
			ApplicationContact receiver,
			int operationID) 
	{
		super(sender, receiver, operationID);
	}


	public JoinRequestNACKMessage(
			ApplicationContact sender, 
			ApplicationContact receiver,
			int operationID, 
			boolean nodeAvailable) {
		super(sender, receiver, operationID, nodeAvailable);
	}


	@Override
	public AbstractAppMessage copy() {
		return new JoinRequestNACKMessage(
				new ApplicationContact(getSender()), 
				new ApplicationContact(getReceiver()),
				getOperationID(),
				isNodeAvailable()
				);
	}

}
