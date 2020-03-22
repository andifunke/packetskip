package org.peerfact.impl.service.skipgraph.node;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.peerfact.Constants;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.util.SGUtil;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphContact implements DHTObject, Cloneable {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final BigInteger nodeID;

	private final BigDecimal rangeStart;
	
	private final BigDecimal rangeEnd;
	
	/**
	 * the prefix is only used for level optimization
	 */
	private final long contactTablePrefix;
	


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SkipgraphContact(
			BigInteger nodeID, 
			BigDecimal rangeStart, 
			BigDecimal rangeEnd, 
			long contactTablePrefix) 
	{
		this.nodeID = nodeID;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.contactTablePrefix = contactTablePrefix;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public BigInteger getNodeID() {
		return nodeID;
	}

	public BigDecimal getRangeStart() {
		return rangeStart;
	}

	public BigDecimal getRangeEnd() {
		return rangeEnd;
	}

	public long getContactTablePrefix() {
		return contactTablePrefix;
	}

	public String getRangeToString() {
		return String.format("%s, %s",
				rangeStart == null ? "(-inf" : "["+rangeStart.toString(),
				rangeEnd == null ? "inf)" : rangeEnd.toString()+")"
				);
	}
	


	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public String toString() {
		return "SkipgraphContact "
				+ SGUtil.formatID(getNodeID())
				+" "+getRangeToString() 
				//+ " {"+contactTablePrefix+"}"
				;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof SkipgraphContact))
			return false;
		
		SkipgraphContact ext = (SkipgraphContact)obj;
		if (!nodeID.equals(ext.getNodeID()))
			return false;
		
		if ((rangeStart == null && ext.getRangeStart() != null) ||
				(rangeStart != null && ext.getRangeStart() == null) ||
				(rangeStart != null && !rangeStart.equals(ext.getRangeStart())))
			return false;
		
		if ((rangeEnd == null && ext.getRangeEnd() != null) ||
				(rangeEnd != null && ext.getRangeEnd() == null) ||
				(rangeEnd != null && !rangeEnd.equals(ext.getRangeEnd())))
			return false;
		
		if (SkipgraphServiceConstants.optimizeLevelPrefix)
			if (contactTablePrefix != ext.getContactTablePrefix())
				return false;
		
		return true;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Long.hashCode(contactTablePrefix);
		result = prime * result + ((nodeID == null) ? 0 : nodeID.hashCode());
		result = prime * result
				+ ((rangeEnd == null) ? 0 : rangeEnd.hashCode());
		if (SkipgraphServiceConstants.optimizeLevelPrefix)
			result = prime * result
					+ ((rangeStart == null) ? 0 : rangeStart.hashCode());
		return result;
	}


	@Override
	public long getTransmissionSize() {
		// nodeID
		long size = nodeID.toByteArray().length;
		// rangeStart + rangeEnd
		size += 2*(SkipgraphServiceConstants.RESOLUTION_SIZE);
		if (SkipgraphServiceConstants.optimizeLevelPrefix)
			size += Constants.LONG_SIZE;
		return size;
	}
	
	
	@Override
	public Object clone() {
		return copy();
	}
	
	
	public SkipgraphContact copy() {
		return new SkipgraphContact(nodeID, rangeStart, rangeEnd, contactTablePrefix);
	}

}
