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

package org.peerfact.impl.application.capacitymanager;

import org.apache.log4j.Logger;
import org.peerfact.api.common.ComponentFactory;
import org.peerfact.api.common.Host;
import org.peerfact.api.scenario.ConfigurationException;
import org.peerfact.api.service.Service;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceInterface;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.logging.SimLogger;

/**
 * The factory class for the {@link CapacityManagerApplication}.
 * if no service implementing the interface {@link SkipgraphServiceInterface}
 * is running.
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class CapacityManagerApplicationFactory implements ComponentFactory {

	final static Logger log = SimLogger
			.getLogger(CapacityManagerApplicationFactory.class);


	/**
	 * Constructs a new CapacityManagerApplication for the given host.
	 * Additional components for the application are created.
	 * @param host
	 */
	@Override
	public CapacityManagerApplication createComponent(Host host) {

		// check whether a SkipgraphServiceInterface is running
		Service loadedService;
		if( (loadedService = host.getService(SkipgraphServiceInterface.class)) == null) {
			throw new ConfigurationException("The host " + host	+ " is not supported by "
					+ "the Capacity Manager Application because the SkipgraphService is not loaded.");
		}
		else {
			return new CapacityManagerApplication(host, (SkipgraphServiceInterface)loadedService);
		}
	}


	
	/* **************************************************
	 ***************** Config Setters ******************* 
	 ************************************************** */

	/**
	 * Called from the config: set the maximum capacity value per capacity.
	 * @param arg
	 */
	public static void setMaxCapacity(int arg) {
		System.out.println("INFO: setting initial capacity maximum to: "+arg);
		CapacityManagerApplicationConstants.setMaxCapacity(arg);
	}


	public static void setLogging(boolean arg) {
		System.out.println("INFO: log capacity manager application? "+arg);
		CapacityManagerApplicationConstants.setLogging(arg);
	}


	public static void setSmoothCapacityChange(boolean arg) {
		System.out.println("INFO: use smooth capacity change behavior? "+arg);
		CapacityManagerApplicationConstants.setSmoothCapacityChange(arg);
	}


	public static void setPseudoChurn(boolean arg) {
		System.out.println("INFO: use 'pseudo' churn? "+arg);
		CapacityManagerApplicationConstants.setPseudoChurn(arg);
	}


	public static void setPseudoChurnStart(long arg) {
		System.out.println("INFO: set 'pseudo' churn start to: "+arg/Simulator.MINUTE_UNIT+"m");
		CapacityManagerApplicationConstants.setPseudoChurnStart(arg);
	}


	public static void setPseudoChurnEnd(long arg) {
		System.out.println("INFO: set 'pseudo' churn end to: "+arg/Simulator.MINUTE_UNIT+"m");
		CapacityManagerApplicationConstants.setPseudoChurnEnd(arg);
	}


	public static void setKSearch(int arg) {
		System.out.println("INFO: search uses k="+(arg>0?arg:arg==0?"full":"random"));
		CapacityManagerApplicationConstants.setKSearch(arg);
	}


	public static void setStoreCapacityInterval(long arg) {
		System.out.println("INFO: setting store capacity interval to: "+arg/Simulator.SECOND_UNIT+"s");
		CapacityManagerApplicationConstants.setStoreCapacityInterval(arg);
	}


	public static void setSearchCapacityInterval(long arg) {
		System.out.println("INFO: setting search capacity interval to: "+arg/Simulator.SECOND_UNIT+"s");
		CapacityManagerApplicationConstants.setSearchCapacityInterval(arg);
	}

}
