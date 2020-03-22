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

package org.peerfact.impl.service.skipgraph.node;

import org.peerfact.Constants;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class Replacement implements DHTObject, Cloneable {
	private int levelIndex;
	private int levelPrefix;
	private Route route;
	private SkipgraphContact replacementContact;
	
	public Replacement(
			int levelIndex, 
			int levelPrefix, 
			Route route,
			SkipgraphContact replacementContact) 
	{
		this.levelIndex = levelIndex;
		this.levelPrefix = levelPrefix;
		this.route = route;
		this.replacementContact = replacementContact;
	}

	public int getLevelIndex() {
		return levelIndex;
	}

	public int getLevelPrefix() {
		return levelPrefix;
	}

	public Route getRoute() {
		return route;
	}

	public SkipgraphContact getReplacementContact() {
		return replacementContact;
	}
	
	@Override
	public String toString() {
		return "Replacement ("
				+ "levelIndex="+levelIndex
				+ ", levelPrefix="+levelPrefix
				+ ", route="+route
				+ ", replacement="+replacementContact
				;
	}

	@Override
	public long getTransmissionSize() {
		// int levelIndex
		long size = Constants.INT_SIZE;
		// int prefix
		size += Constants.INT_SIZE;
		// Route route
		size += SkipgraphServiceConstants.ROUTE_SIZE;
		// SkipgraphContact replacement
		size += replacementContact.getTransmissionSize();
		return size;
	}
	
	
	public Replacement copy() {
		return new Replacement(
				levelIndex,
				levelPrefix,
				route,
				replacementContact.copy()
				);
	}
		
	@Override
	public Object clone() {
		return copy();
	}
}

