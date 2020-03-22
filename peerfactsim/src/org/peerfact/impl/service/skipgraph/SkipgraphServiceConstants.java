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

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.peerfact.Constants;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.logging.SimLogger;

/**
 * Global constants for the SkipgraphService.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphServiceConstants {
	
	/** A logger instance. */
	final static Logger log = SimLogger.getLogger(SkipgraphServiceConstants.class);

	public static final BigInteger defaultID = BigInteger.ZERO;
	              
	public static final long BIG_DECIMAL_SIZE = 8;
	              
	public static final long RESOLUTION_SIZE = BIG_DECIMAL_SIZE;
	              
	public static final long ROUTE_SIZE = Constants.BOOLEAN_SIZE;
	
	public static final boolean LOG_OVERLAY_ACTIVITY = false;
	
	public static final boolean VALIDATE_ELEMENTS = true;
	
	public static final boolean LOG_DHT = false;
	
	public static final long OP_TIMEOUT = 2*Simulator.MINUTE_UNIT;
	
	public static final long NODE_OP_TIMEOUT = 30*Simulator.SECOND_UNIT;
	
	public static final long DOTBUILDER_DELAY = 1000*Simulator.MILLISECOND_UNIT;
	

	// set preferences via config (TODO: some are missing in the factory)
	
	/** Message timeout for a single message. */
	private static long messageTimeout;
	
	/** minimum size/threshold of the SkipgraphElement table */
	public static int elementTableMinSize = 10;

	/** maximum size/threshold of the SkipgraphElement table */
	public static int elementTableMaxSize = 50;
	
	public static boolean parallelSearch = true;

	public static boolean loadBalancing = true;

	public static boolean churnScenario = false;

	public static boolean ignoreExpiredResults = churnScenario;

	public static boolean cheating = false;
	
	public static boolean optimizeLevelPrefix = false;

	public static double headroom = 0.1;
	
	public static long purgeTolerance = 350*Simulator.MILLISECOND_UNIT;
	
	public static boolean logSearch = false;
	
	public static boolean logUpdate = false;
	
	public static boolean logJoin = false;
	
	public static boolean logLeave = false;
	
	public static boolean logMaintenance = false;
	
	public static boolean printNodesWhileLogging = false;
	
	public static boolean plotDotFiles = false;
	

	
	/* ************************
	 * ******** GETTER ********
	 ************************* */

	/**
	 * Get the message timeout
	 * @return
	 */
	public static long getMessageTimeout() {
		return messageTimeout;
	}


	public static int getElementTableMinSize() {
		return elementTableMinSize;
	}

	
	public static int getElementTableMaxSize() {
		return elementTableMaxSize;
	}

	public static boolean useLoadBalancing() {
		return loadBalancing;
	}



	/* ************************
	 * ******** SETTER ********
	 ************************* */

	/**
	 * Set the message timeout
	 * @param timeout
	 */
	public static void setMessageTimeout(long timeout) {
		// if the user uses a timeout interval which is not very clever - warn him!
		if (timeout < Simulator.SECOND_UNIT * 1) {
			log.warn("Message timeout below 1 second is pretty strict, isn't it?");
		}
		messageTimeout = timeout;
	}
	
	public static void setPurgeTolerance(long arg) {
		purgeTolerance = arg;
	}
	
	public static void setElementTableMinSize(int arg) {
		elementTableMinSize = arg;
	}

	public static void setElementTableMaxSize(int arg) {
		elementTableMaxSize = arg;
	}

	public static void setParallelSearch(boolean arg) {
		parallelSearch = arg;
	}

	public static void setLoadBalancing(boolean arg) {
		loadBalancing = arg;
	}

	public static void setChurnScenario(boolean arg) {
		churnScenario = arg;
	}

	public static void setIgnoreExpiredResults(boolean arg) {
		ignoreExpiredResults = arg;
	}

	public static void setCheating(boolean arg) {
		cheating = arg;
	}

	public static void setOptimizeLevelPrefix(boolean arg) {
		optimizeLevelPrefix = arg;
	}

	public static void setHeadroom(double arg) {
		// if the user uses a timeout interval which is not very clever - warn him!
		if (arg > 0.5) {
			log.warn("Load balancing headroom must not exceed 50%.");
			try {
				throw new IllegalArgumentException("Load balancing headroom must not exceed 50%.");
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		headroom = arg;
	}
	
	public static void setLogSearch(boolean arg) {
		logSearch = arg;
	}
	
	public static void setLogUpdate(boolean arg) {
		logUpdate = arg;
	}
	
	public static void setLogJoin(boolean arg) {
		logJoin = arg;
	}
	
	public static void setLogLeave(boolean arg) {
		logLeave = arg;
	}
	
	public static void setLogMaintenance(boolean arg) {
		logMaintenance = arg;
	}
	
	public static void setPrintNodesWhileLogging(boolean arg) {
		printNodesWhileLogging = arg;
	}
	
	public static void setPlotDotFiles(boolean arg) {
		plotDotFiles = arg;
	}
	
}
