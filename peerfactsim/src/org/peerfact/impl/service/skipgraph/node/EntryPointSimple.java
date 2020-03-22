package org.peerfact.impl.service.skipgraph.node;

import java.math.BigInteger;

import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class EntryPointSimple implements DHTObject, Cloneable {

	/* *******************************************
	 ****************** FIELDS *******************
	 *********************************************/

	private final BigInteger nodeID;

	private final ApplicationContact peer;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public EntryPointSimple(BigInteger nodeID, ApplicationContact peer) {
		this.nodeID = nodeID;
		this.peer = peer;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/


	public BigInteger getNodeID() {
		return nodeID;
	}

	public ApplicationContact getPeer() {
		return peer;
	}
		

	
	/* *************************************************
	 ****************** METHODS ************************
	 ***************************************************/

	@Override
	public String toString() {
		return String.format("EntryPointSimple (nodeID=%s, peer=%s)", 
				SGUtil.formatID(nodeID), SGUtil.formatContact(peer));
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeID == null) ? 0 : nodeID.hashCode());
		result = prime * result + ((peer == null) ? 0 : peer.hashCode());
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
		EntryPointSimple other = (EntryPointSimple) obj;
		if (nodeID == null) {
			if (other.nodeID != null)
				return false;
		} else if (!nodeID.equals(other.nodeID))
			return false;
		if (peer == null) {
			if (other.peer != null)
				return false;
		} else if (!peer.equals(other.peer))
			return false;
		return true;
	}



	/**
	 * in bytes
	 */
	@Override
	public long getTransmissionSize() {
		// nodeID
		long size = nodeID.toByteArray().length;
		// peer
		size += peer.getSize();
		return size;
	}
	
	
	public EntryPointSimple copy() {
		return new EntryPointSimple(
				nodeID, 
				new ApplicationContact(peer));
	}
		
	@Override
	public Object clone() {
		return copy();
	}

}
