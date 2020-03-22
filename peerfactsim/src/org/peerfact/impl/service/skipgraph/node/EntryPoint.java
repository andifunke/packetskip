package org.peerfact.impl.service.skipgraph.node;

import org.peerfact.Constants;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class EntryPoint implements DHTObject, Comparable<EntryPoint>, Cloneable {

	/* *******************************************
	 ****************** FIELDS *******************
	 *********************************************/

	private final SkipgraphContact node;

	private final ApplicationContact peer;
	
	private final long timestamp;


	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public EntryPoint(SkipgraphContact node, ApplicationContact peer) {
		this.node = node;
		this.peer = peer;
		this.timestamp = Simulator.getCurrentTime();
	}

	public EntryPoint(SkipgraphContact node, ApplicationContact peer, long timestamp) {
		this.node = node;
		this.peer = peer;
		this.timestamp = timestamp;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/


	public SkipgraphContact getNode() {
		return node;
	}

	public ApplicationContact getPeer() {
		return peer;
	}
		
	public long getTimestamp() {
		return timestamp;
	}


	
	/* *************************************************
	 ****************** METHODS ************************
	 ***************************************************/

	@Override
	public String toString() {
		return String.format("EntryPoint (%s, peer=%s, timestamp=%s)", node, SGUtil.formatContact(peer), timestamp);
	}

	/**
	 * compares via timestamp:
	 * @return	-1 if this object is older than the argument
	 * 			 0 if equally old
	 * 			 1 if this object is younger than the argument
	 */
	@Override
	public int compareTo(EntryPoint ext) {
		if (this.timestamp < ext.timestamp)
			return -1;
		
		if (this.timestamp > ext.timestamp)
			return 1;
		
		return 0;
	}

	/*
	public boolean equals(EntryPoint ext) {
		boolean equal = true;
		equal &= this.node.equals(ext.node);
		equal &= this.peer.equals(ext.peer);
		equal &= this.timestamp == ext.timestamp;
		return equal;
	}
	*/
	
	

	/**
	 * in bytes
	 */
	@Override
	public long getTransmissionSize() {
		long size = 0;
		// node
		size += node.getTransmissionSize();
		// peer
		size += peer.getSize();
		// timestamp
		size += Constants.LONG_SIZE;
		return size;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((peer == null) ? 0 : peer.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntryPoint other = (EntryPoint) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (peer == null) {
			if (other.peer != null)
				return false;
		} else if (!peer.equals(other.peer))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	public EntryPoint copy() {
		return new EntryPoint(
				node.copy(), 
				new ApplicationContact(peer),
				timestamp);
	}
		
	@Override
	public Object clone() {
		return copy();
	}

}
