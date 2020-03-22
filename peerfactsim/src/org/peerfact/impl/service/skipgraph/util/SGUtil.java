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

package org.peerfact.impl.service.skipgraph.util;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;
import org.peerfact.impl.util.toolkits.HashToolkit;

/**
 * Provides some static global helper methods.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SGUtil {

	/* ************************************************
	 ********************* ROUTE **********************
	 ************************************************ */

	public enum Route {
		PREV, NEXT
	}


	public static Route switchRoute(SGUtil.Route route) {
		if (route == SGUtil.Route.PREV) {
			return SGUtil.Route.NEXT;
		}
		return SGUtil.Route.PREV;
	}

	
	/* ************************************************
	 ************* FORMAT ID and PEER *****************
	 ************************************************ */

	public static String formatID(BigInteger bi) {
		if (bi == null)
			return "{null}";
			
		return "{" 
				+ (bi.toString().length() < 5 ? bi : bi.toString().substring(0,5))
				+ "...}";
	}
	
	public static String formatIDs(Collection<BigInteger> col) {
		if (col == null)
			return "null";
		
		if (col.isEmpty())
			return "[]";
		
		StringBuilder sb = new StringBuilder("[");
		Iterator<BigInteger> it = col.iterator();
		sb.append(formatID(it.next()));
		while (it.hasNext()) {
			sb.append(", ").append(formatID(it.next()));
		}
		return sb + "]";
	}

	public static String formatContact(ApplicationContact contact) {
		return formatID(contact.getPeerID());
	}


	/* ************************************************
	 ****************** BOOTSTRAP *********************
	 ************************************************ */

	/**
	 * This function returns a bootstrapID. a bootstrapID is a location in the DHT where SkipgraphNodes
	 * store their identities. unlike the nodeIDs of SkipgraphNodes (which are determined randomly) are
	 * bootstrapIDs always known to all peers in that use the SkipgraphService. Since SkipgraphNodes
	 * announce their identities to bootstrapIDs periodically there is a high probability that peers
	 * who want to send messages to the skip graph will find a valid entry point at a bootstrap location.
	 * 
	 * The function here choses a random bootstrapID. random in the sense that there. 
	 * are exactly as many bootstrapIDs as there a skip graph nodes. This method returns one of those
	 * IDs randomly. 
	 * 
	 * @return a BigInteger hash value that is used as on overlay ID for an entry point 
	 */
	public static BigInteger getRandomBootstrapID() {
//		System.out.println("numberOfnodes="+SkipgraphNode.getCurrentNumberOfNodes()
//				+" @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()));
		int random = Simulator.getRandom().nextInt(SkipgraphNode.getCurrentNumberOfNodes());
		return HashToolkit.getSHA1Hash("entry"+random);
	}


	/**
	 * this function returns a randomly chosen bootstrapID. The function accepts a set
	 * of blacklisted bootstrapIDs which will then be excluded from the returned IDs.
	 * 
	 * @param   blacklistedIDs -> a set of peerIDs which should not be returned (because they didn't contain
	 * 							  valid or current information)
	 * @return  a BigInteger value of a randomly chosen bootstrapID. Excludes all IDs which were given
	 * 			in the blacklist. returns null, if there are no other bootstrapIDs beside the blacklist.
	 */
	public static BigInteger getRandomBootstrapID(Set<BigInteger> blacklistedIDs) {
		if (blacklistedIDs == null || blacklistedIDs.isEmpty())
			return getRandomBootstrapID();
		
		Set<BigInteger> availableIDs = new LinkedHashSet<>();
		for (int i=0; i<SkipgraphNode.getCurrentNumberOfNodes(); i++) {
			availableIDs.add(HashToolkit.getSHA1Hash("entry"+i));
		}
		availableIDs.removeAll(blacklistedIDs);
		
		if (availableIDs.isEmpty())
			return null;
		
		BigInteger[] retArr = new BigInteger[availableIDs.size()];
		availableIDs.toArray(retArr);
		return retArr[Simulator.getRandom().nextInt(retArr.length)];
	}
	
}
