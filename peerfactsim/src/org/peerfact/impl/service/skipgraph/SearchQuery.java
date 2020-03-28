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

import java.math.BigDecimal;

import org.peerfact.Constants;
import org.peerfact.api.overlay.dht.DHTObject;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchQuery implements DHTObject, Comparable<SearchQuery>, Cloneable {


	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final String dimension;
	private final BigDecimal rangeStart;
	private final BigDecimal rangeEnd;
	private final boolean openRangeEnd;
	private int maxNumberOfValues;


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	/**
	 * a query for one dimension
	 * @param dimension			  null -> ""
	 * @param rangeStart		  null -> (-inf)
	 * @param rangeEnd			  null -> (inf)
	 * @param maxNumberOfValues   <=0 -> no limit
	 */
	public SearchQuery(
			String dimension,
			BigDecimal rangeStart,
			BigDecimal rangeEnd, 
			int maxNumberOfValues) 
	{
		this(dimension, rangeStart, rangeEnd, maxNumberOfValues, false);
	}

	public SearchQuery(
			String dimension,
			BigDecimal rangeStart,
			BigDecimal rangeEnd, 
			int maxNumberOfValues,
			boolean openRangEnd) 
	{
		this.dimension = dimension == null ? "" : dimension;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.maxNumberOfValues = maxNumberOfValues;
		this.openRangeEnd = openRangEnd;
	}

	

	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public String getDimension() {
		return dimension;
	}

	public BigDecimal getRangeStart() {
		return rangeStart;
	}

	public BigDecimal getRangeEnd() {
		return rangeEnd;
	}

	public int getMaxNumberOfValues() {
		return maxNumberOfValues;
	}
	
	public boolean hasOpenRangeEnd() {
		return openRangeEnd;
	}
	
	

	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	public void setMaxNumberOfValues(int maxNumberOfValues) {
		this.maxNumberOfValues = maxNumberOfValues;
	}


	/* ************************************************
	 ****************** INTERFACES ********************
	 ************************************************ */

	@Override
	public long getTransmissionSize() {
		long size = dimension.length() * 2;
		size += 2*SkipgraphServiceConstants.RESOLUTION_SIZE;
		if (SkipgraphServiceConstants.parallelSearch) {
			size += Constants.BOOLEAN_SIZE;
		}
		else {
			size += Constants.INT_SIZE;
		}
		return size;
	}


	@Override
	public int compareTo(SearchQuery external) {
		return rangeStart.compareTo(external.rangeStart);
	}
	
	
	@Override
	public String toString() {
		return "SearchQuery <"
				+ dimension + " ["
				+ rangeStart + ", "
				+ rangeEnd 
				+ (openRangeEnd ? ") " : "] ")
				;
	}
	
	
	@Override
	public Object clone() {
		return copy();
	}
	
	
	public SearchQuery copy() {
		return new SearchQuery(
				dimension, 
				rangeStart, 
				rangeEnd, 
				maxNumberOfValues,
				openRangeEnd
				);
	}

}
