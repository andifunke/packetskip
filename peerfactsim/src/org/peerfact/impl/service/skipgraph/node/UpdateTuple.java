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

import java.util.LinkedList;
import java.util.List;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
class UpdateTuple {
	
	private final List<SkipgraphElement> inputList;
	
	private final List<SkipgraphElement> deleteList;

	
	public UpdateTuple() {
		this.inputList = new LinkedList<>();
		this.deleteList = new LinkedList<>();
	}

	
	public UpdateTuple(List<SkipgraphElement> inputList,
			List<SkipgraphElement> deleteList) {
		this.inputList = inputList;
		this.deleteList = deleteList;
	}


	List<SkipgraphElement> getInputList() {
		return inputList;
	}

	void addInputElement(SkipgraphElement element) {
		inputList.add(element);
	}

	List<SkipgraphElement> getDeleteList() {
		return deleteList;
	}

	void addDeleteElement(SkipgraphElement element) {
		deleteList.add(element);
	}
	
	@Override
	public String toString() {
		return "\ninputList: " + inputList 
				+ "\ndeleteList: " + deleteList + "\n";
	}
	
}
