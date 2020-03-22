package org.peerfact.impl.service.skipgraph.node;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.peerfact.Constants;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphServiceOperation.TrackerCallback;
import org.peerfact.impl.service.skipgraph.operations.UpdateOperation.ConfirmationCallback;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphElement implements DHTObject, Comparable<SkipgraphElement>, Cloneable {

	/* *******************************************
	 ****************** FIELDS *******************
	 *********************************************/

	private final String dimension;
	private final BigDecimal value;
	private final ApplicationContact contact;
	private final long timestamp;
	
	
	/** 
	 * this callback is used to measure the operation duration -> 
	 * not a real P2P feature and therefore not adding to the transmissionSize 
	 * */
	private ConfirmationCallback cc;
	private TrackerCallback tc;

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public SkipgraphElement(
			String dimension, 
			BigDecimal value, 
			ApplicationContact contact) 
	{
		this.dimension = dimension;
		this.value = value;
		this.contact = contact;
		this.timestamp = Simulator.getCurrentTime();
	}

	public SkipgraphElement(
			String dimension, 
			BigDecimal value, 
			ApplicationContact contact, 
			long timestamp) 
	{
		this.dimension = dimension;
		this.value = value;
		this.contact = contact;
		this.timestamp = timestamp;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 **************************************************/

	public String getDimension() {
		return dimension;
	}

	public BigDecimal getValue() {
		return value;
	}

	public ApplicationContact getContact() {
		return this.contact;
	}

	public long getTimestamp() {
		return timestamp;
	}


	
	/* ************************************************
	 ****************** SETTERS ***********************
	 **************************************************/

	public void setConfirmationCallback(ConfirmationCallback cc) {
		this.cc = cc;
	}
	
	public void setTrackerCallback(TrackerCallback tc) {
		this.tc = tc;
	}
	
	public void addInputHop(BigInteger peerID) {
		if (tc != null)
			this.tc.addHop(peerID);
	}
	
	public void addDeleteHop(BigInteger peerID) {
		if (tc != null)
			this.tc.addDeleteHop(peerID);
	}
	
	public void confirmInput() {
		if (cc != null) {
			cc.reportInput(this);
			cc = null;
			tc = null;
		}
	}
	
	public void confirmDelete() {
		if (cc != null) {
			cc.reportDelete(this);
			cc = null;
			tc = null;
		}
	}

	
	
	/* *************************************************
	 ****************** METHODS ************************
	 ***************************************************/

	@Override
	public String toString() {
		return String.format("(%s, %s, peer=%s)", value, dimension, SGUtil.formatContact(contact));
	}

	public String toStringFull() {
		return String.format("(%s, %s, peer=%s, %s)", value, dimension, SGUtil.formatContact(contact),
				Simulator.getFormattedTime(timestamp));
	}

	@Override
	public int compareTo(SkipgraphElement t) {
		return this.value.compareTo(t.getValue());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contact == null) ? 0 : contact.hashCode());
		result = prime * result
				+ ((dimension == null) ? 0 : dimension.hashCode());
		// TODO: this is not ideal but fine for the current implementation
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SkipgraphElement other = (SkipgraphElement) obj;
		if (contact == null) {
			if (other.contact != null)
				return false;
		} else if (!contact.equals(other.contact))
			return false;
		if (dimension == null) {
			if (other.dimension != null)
				return false;
		} else if (!dimension.equals(other.dimension))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!(this.value.compareTo(other.value) == 0))
			return false;
		return true;
	}

	/**
	 * in bytes
	 */
	@Override
	public long getTransmissionSize() {
		long size = 0;
		// dimension
		size += dimension.length() * 2;
		// value - BigDecimal approximated
		size += SkipgraphServiceConstants.RESOLUTION_SIZE;
		// contact
		size += contact.getSize();
		// timestamp
		size += Constants.LONG_SIZE;
		return size;
	}
		
	public SkipgraphElement copy() {
		SkipgraphElement copy = new SkipgraphElement(
				dimension, 
				value, 
				new ApplicationContact(contact),
				timestamp);
		return copy;
	}
	
	@Override
	public Object clone() {
		return copy();
	}
		
}
