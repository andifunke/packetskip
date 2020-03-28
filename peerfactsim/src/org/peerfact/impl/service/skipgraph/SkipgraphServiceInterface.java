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

package org.peerfact.impl.service.skipgraph;

import java.util.List;
import java.util.Set;

import org.peerfact.api.service.Service;
import org.peerfact.impl.service.demo.extended.ExtendedDemoServiceInterface.ReserveCapacityCallback;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * Represents a service to provide and maintain a distributed skip graph.
 * The graph provides a sorted data-structure which can be used for indexing host-capacities.
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public interface SkipgraphServiceInterface extends Service {

	/**
	 * Initialize the service.
	 */
	public void init();

	public boolean isRunning();



	/* ************************************
	 * store or modify data in skip graph *
	 ************************************ */
	public interface UpdateCallback {
		/**
		 * The successful flag indicates whether the data modification was successful.
		 * @param successful
		 */
		public void gotAnswer(boolean successful);
	}

	public void update(List<SkipgraphElement> inputElements,
			List<SkipgraphElement> deleteElements,
			UpdateCallback updateCallback);



	/* ****************
	 * search queries *
	 **************** */
	public interface SearchQueryCallback {
		/**
		 * The successful flag indicates whether the search was successful.
		 * @param successful
		 * @param contact null, if unsuccessful, otherwise {@link ApplicationContact}.
		 */
		public void gotAnswer(boolean successful, Set<ApplicationContact> contacts);
	}

	public void get(int index, SearchQueryCallback searchQueryCallback);

	public void search(List<SearchQuery> searchQueries, int k, 
			SearchQueryCallback searchQueryCallback);


	
	/* ***********
	 * messaging *
	 *********** */
	public ApplicationContact getLocalContact();
	
	/* ***********
	 * debugging *
	 *********** */
	public void printResult(SkipgraphElement element);

	public void printResult(List<SkipgraphElement> elements);

	public void print();

	public void log(String str);

}