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
import org.peerfact.impl.service.demo.extended.ExtendedDemoServiceConstants;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.logging.SimLogger;

/**
 * Global constants for the CapacityManagerApplication.

 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class CapacityManagerApplicationConstants {

	/* ************************
	 * ****** PREDEFINED ******
	 ************************* */
	
	public static final String ind1 = "  ";
	public static final String ind2 = "    ";
	public static final String ind3 = "      ";
	public static final String ind4 = "        ";

	/** A logger instance. */
	final static Logger log = SimLogger.getLogger(ExtendedDemoServiceConstants.class);

	static final int DEFAULT_GRANULARITY = 20;

	public static final double PSEUDO_CHURN_LIMIT = 0.5;
	
	/* ************************
	 * **** USER-DEFINABLE ****
	 ************************* */

	/** The maximum number of each capacity's value */
	private static int maxCapacity = 10000;
	
	public static boolean logging = false;

	public static boolean smoothCapacityChange = true;

	public static boolean pseudoChurn = false;
	
	public static long pseudoChurnStart = 0;

	public static long pseudoChurnEnd = 0;

	public static int kSearch = -1;

	/** Interval for a store capacity operation. */
	private static long storeCapacityInterval = 2*Simulator.MINUTE_UNIT;	
	
	/** Interval for a capacity search operation. */
	private static long searchCapacityInterval = 5*Simulator.MINUTE_UNIT;
	


	/* ************************
	 * ******** GETTER ********
	 ************************* */

	/**
	 * Get max capacity 
	 * @return
	 */
	public static int getMaxCapacity() {
		return maxCapacity;
	}


	/**
	 * Get the store capacity interval
	 * @return
	 */
	public static long getStoreCapacityInterval() {
		return storeCapacityInterval;
	}

	/**
	 * Get the search capacity interval
	 * @return
	 */
	public static long getSearchCapacityInterval() {
		return searchCapacityInterval;
	}


	/* ************************
	 * ******** SETTER ********
	 ************************* */

	public static void setMaxCapacity(int arg) {
		maxCapacity = arg;
	}
	
	public static void setLogging(boolean arg) {
		logging = arg;
	}
	
	public static void setSmoothCapacityChange(boolean arg) {
		smoothCapacityChange = arg;
	}
	
	public static void setPseudoChurn(boolean arg) {
		pseudoChurn = arg;
	}
	
	public static void setPseudoChurnStart(long arg) {
		pseudoChurnStart = arg;
	}
	
	public static void setPseudoChurnEnd(long arg) {
		pseudoChurnEnd = arg;
	}
	
	public static void setKSearch(int arg) {
		kSearch = arg;
	}
	
	public static void setStoreCapacityInterval(long arg) {
		// if the user uses a timeout interval which is not very clever - warn him!
		if( arg < Simulator.SECOND_UNIT * 5) {
			log.warn("Store capacity interval less than 5 seconds? This will cause a lot of traffic!");
		}
		storeCapacityInterval = arg;
	}

	public static void setSearchCapacityInterval(long arg) {
		// if the user uses a timeout interval which is not very clever - warn him!
		if( arg < Simulator.SECOND_UNIT * 10) {
			log.warn("Search capacity interval less than 10 seconds? This will cause a lot of traffic!");
		}
		searchCapacityInterval = arg;
	}

}
