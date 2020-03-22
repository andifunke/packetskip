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

package org.peerfact.impl.service.skipgraph.analyzer;

import org.peerfact.api.analyzer.Analyzer;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;

/**
 * Interface for the SkipgraphNode analyzer. 
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public interface SkipgraphNodeAnalyzer extends Analyzer {
	
	public void addNewNode(SkipgraphNode node);
	
	public void addElementTableSize(SkipgraphNode node, int size);

	public void addContactTableSize(SkipgraphNode node, int size);
	
	public void addContactTableEffectiveSize(SkipgraphNode node, int size);
	
	public void addNumberOfDistinctContacts(SkipgraphNode node, int size);
	
	public void addNumberOfIncomingMessages(SkipgraphNode node, int number);

	public void addNumberOfOutgoingMessages(SkipgraphNode node, int number);
	
	public void currentNumberOfNodes(SkipgraphNode node, int number);
	
	public void nodeLifespan(SkipgraphNode node, long lifespan);
	
	public void loadBalancingSuccessAndFailur(SkipgraphNode node, boolean success);

	public void nodeAdded(SkipgraphNode node);

	public void nodeRemoved(SkipgraphNode node);
	
}
