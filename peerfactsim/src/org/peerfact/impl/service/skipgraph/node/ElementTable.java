package org.peerfact.impl.service.skipgraph.node;

import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind1;
import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind3;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.exception.NullArgumentException;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class ElementTable implements DHTObject, Cloneable {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	// TODO: data structure should be sorted: e.g. treemap
	private List<SkipgraphElement> elementList = new LinkedList<>();

	// TODO: allow negative values; null for -infinity;
	private BigDecimal rangeStart;
	
	// initial null-value means: no upper limit (= infinity)
	private BigDecimal rangeEnd;
	


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public ElementTable() {
		this(new LinkedList<>(), BigDecimal.ZERO, null);
	}

	
	public ElementTable(BigDecimal rangeStart, BigDecimal rangeEnd) {
		this(new LinkedList<>(), rangeStart, rangeEnd);
	}

	
	public ElementTable(List<SkipgraphElement> elementTable, BigDecimal rangeStart, BigDecimal rangeEnd) {
		this.elementList = elementTable;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public List<SkipgraphElement> getTable() {
		return elementList;
	}

	public BigDecimal getRangeStart() {
		return rangeStart;
	}

	public BigDecimal getRangeEnd() {
		return rangeEnd;
	}
	
	public String getRange() {
		return String.format("%s, %s",
				rangeStart == null ? "(-inf" : "["+rangeStart.toString(),
				rangeEnd == null ? "inf)" : rangeEnd.toString()+")"
				);
	}
	
	public int size() {
		return elementList.size();
	}
	
	
	public Set<ApplicationContact> getContacts(
			String dimension,
			BigDecimal start,
			BigDecimal end,
			int maxNumberOfValues,
			boolean excludeRangeEnd)
	{
		BigDecimal intervalStart = start == null ? rangeStart : start;
		BigDecimal intervalEnd = end == null ? rangeEnd : end;
		
		Set<ApplicationContact> results = new LinkedHashSet<>();
		Collections.sort(elementList);
		
		long expirationDate = Simulator.getCurrentTime()
				- (CapacityManagerApplicationConstants.getStoreCapacityInterval()
						+SkipgraphServiceConstants.purgeTolerance);

		for (SkipgraphElement element : elementList) {
			// in churn scenario: don't send expired elements (experimental)
			if (SkipgraphServiceConstants.churnScenario && SkipgraphServiceConstants.ignoreExpiredResults) {
				if (element.getTimestamp() < expirationDate) {
					continue;
				}
			}
			
			// since the list is sorted we can break out of the loop once the upper interval limit is crossed
			if (intervalEnd != null) {
				if (excludeRangeEnd && element.getValue().compareTo(intervalEnd) == 0) {
					break;
				}
				if (element.getValue().compareTo(intervalEnd) > 0) {
					break;
				}
			}
			// if the element is higher or equal to the lower interval limit and if the dimension is identical
			// we add the element to the results list
			if (element.getValue().compareTo(intervalStart) >= 0 &&
					element.getDimension().equals(dimension)) {
				results.add(element.getContact());
			}
			// we can also break the loop once we have collected enough elements
			if (maxNumberOfValues > 0 && results.size() >= maxNumberOfValues) {
				break;
			}
		}
		return results;
	}
	
	
	public boolean isEmpty() {
		return elementList.isEmpty();
	}


	
	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	void setTable(List<SkipgraphElement> elementTable) {
		this.elementList = elementTable;
	}

	void setRangeStart(BigDecimal rangeStart) {
		this.rangeStart = rangeStart;
	}

	void setRangeEnd(BigDecimal rangeEnd) {
		this.rangeEnd = rangeEnd;
	}
	

	
	/* *************************************************
	 ************* DATA MODIFICATION *******************
	 ***************************************************/

	boolean add(SkipgraphElement element) {
		// in a no-churn scenario no update is required
		if (!SkipgraphServiceConstants.churnScenario) {
			if (elementList.contains(element)) {
				element.confirmInput();
				return false;
			}
		}
		if (elementList.remove(element)) {
		}
		boolean success = elementList.add(element);
		if (success)
			element.confirmInput();
		return success;
	}


	boolean addAll(Collection<SkipgraphElement> elements) {
		boolean success = true;
		for (SkipgraphElement element : elements) {
			success &= add(element);
		}
		return success;
	}

	
	boolean remove(SkipgraphElement element) {
		boolean success = elementList.remove(element);
		element.confirmDelete();
		return success;
	}

	
	boolean removeAll(Collection<SkipgraphElement> elements) {
		boolean success = true;
		for (SkipgraphElement element : elements) {
			success &= remove(element);
		}
		return success;
	}
	
	
	/**
	 * removes all elements from the table which are older than a predefined time 
	 * @return	true if elements were removed, false it the element table was unaltered
	 */
	boolean purge() {
		boolean purged = false;
		long expirationDate = Simulator.getCurrentTime()
				- (CapacityManagerApplicationConstants.getStoreCapacityInterval()
						+SkipgraphServiceConstants.purgeTolerance);
		List<SkipgraphElement> remainingElements = new LinkedList<>();

		for (SkipgraphElement element : elementList) {
			if (element.getTimestamp() > expirationDate) {
				remainingElements.add(element);
			}
			else {
				purged = true;
			}
		}
		elementList = remainingElements;

		return purged;
	}
	
	

	/* *************************************************
	 ****************** METHODS ************************
	 ***************************************************/

	public void sort() {
		Collections.sort(elementList);
	}
	
	
	
	/* ************************************************
	 ************* SPECIALIZED GETTERS ****************
	 ************************************************ */

	/**
	 * take care: if value == null than it is undefined if the value is positive infinity
	 * or negative infinity. Therefore null is not accepted as an argument. Use the more 
	 * specific {@link #isResponsibleForStart} or {@link #isResponsibleForEnd} in this case. 
	 * @param value
	 * @return
	 */
	public boolean isResponsibleFor(BigDecimal value)
			throws NullArgumentException {
		if (value == null) {
			throw new NullArgumentException();
		}
		return (rangeStart == null || rangeStart.compareTo(value) <= 0) && 
				(rangeEnd == null || rangeEnd.compareTo(value) > 0 );
	}
	

	public boolean isResponsibleForStart(BigDecimal value) {
		if (value == null) 
			return rangeStart == null;
		
		return isResponsibleFor(value);
	}
	

	public boolean isResponsibleForEnd(BigDecimal value) {
		if (value == null) 
			return rangeEnd == null;
		
		return isResponsibleFor(value);
	}
	

	/**
	 * checks if a given value is below or equal to the minimum value of the element table
	 *
	 * @param value
	 * @return
	 */
	public boolean isBelowElementTablesMinimum(BigDecimal value) {
		boolean ret;
		if (value == null) {
			ret = !(rangeStart == null);
		}
		else {
			ret = !(rangeStart != null && rangeStart.compareTo(value) <= 0);
		}
		return ret;
	}

	
	/**
	 * checks if a given value is above the maxmimum value of the element table
	 *
	 * @param value
	 * @return
	 */
	public boolean isAboveElementTablesMaximum(BigDecimal value) {
		boolean ret;
		if (value == null) {
			ret = !(rangeEnd == null);
		}
		else {
			ret = rangeEnd != null && rangeEnd.compareTo(value) < 0;
		}
		return ret;
	}

	
	public SkipgraphElement get(int i) {
		return elementList.get(i);
	}

	
	public int getNumberOfFreeSlots() {
		return SkipgraphServiceConstants.getElementTableMinSize() - size();
	}

	
	public int getRemainingSizeOffer() {
		int remainingSizeOffer = (int)(SkipgraphServiceConstants.getElementTableMaxSize()*
				(1-SkipgraphServiceConstants.headroom)) - size();
		return remainingSizeOffer > 0 ? remainingSizeOffer : 0;
	}

	
	/**
	 * splits the elementTable in two tables of same size.
	 * the original table stays unchanged.
	 * @return 	an Array with two ElementTables.
	 */
	public ElementTable[] split() {
		return split(0.5);
	}

	
	/**
	 * splits the elementTable in two tables via a given ratio.
	 * the original table stays unchanged.
	 * @param 	ratio
	 * @return 	an Array with two ElementTables.
	 */
	public ElementTable[] split(double ratio) {
		ElementTable[] et = new ElementTable[2];
		sort();
		int splitIndex = (int)Math.ceil( size()*ratio );
		if (splitIndex >= size()) {
			splitIndex = size()-1;
		}
		
		// increase the index where the table will be splitted until there is a different key
		if (splitIndex == 0) splitIndex = 1;
		while (splitIndex < size() &&
				elementList.get(splitIndex-1).compareTo(elementList.get(splitIndex)) == 0) {
			splitIndex++;
		}

		// build second table
		LinkedList<SkipgraphElement> et2List = new LinkedList<>(this.elementList.subList(splitIndex, size()));
		BigDecimal et2Start = et2List.get(0).getValue();
		BigDecimal et2End = this.rangeEnd;
		et[1] = new ElementTable(et2List, et2Start, et2End);

		// build first table
		LinkedList<SkipgraphElement> et1List = new LinkedList<>(this.elementList.subList(0, splitIndex));
		BigDecimal et1Start = this.rangeStart;
		BigDecimal et1End = et2Start;
		et[0] = new ElementTable(et1List, et1Start, et1End);

		if (SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logLeave || SkipgraphServiceConstants.logMaintenance)
			System.out.println(String.format("    split: %d:%d", et[0].size(), et[1].size()));
		return et;
	}



	/**
	 * splits the elementTable in three tables. the ranges of those tables are adjusted according to the
	 * original range limits and the element values in the new table parts.
	 * the original table stays unchanged.
	 * @param 	m	the size of the table that will be returned in array index 0.
	 * @param 	n	the size of the table that will be returned in array index 2.
	 * @return 	an Array of size 3 with 3 ElementTables.
	 * 			[0] first m elements of the current table or null if 0 elements
	 * 			[2] last n elements of the current table or null if 0 elements
	 * 			[1] remaining elements of the current table or null if m+n=size
	 * 			returns null if (m or n is negativ) or 
	 * 							(m and n are both zero) or 
	 * 							(m+n is greater than the size of the current table).
	 */
	public ElementTable[] split(int m, int n) {
		if (m < 0 || n < 0 || m+n == 0 || m+n > size()) {
			return null;
		}

		boolean splitInTwo = (m+n)==size() ? true : false;
		int mIndex = m;
		int nIndex = size() - n;

		ElementTable[] et = new ElementTable[3];
		sort();
		
		// reduce the m-index where the table will be splitted until there is a different key
		if (mIndex < size()) {
			while (mIndex > 0 && elementList.get(mIndex-1).compareTo(elementList.get(mIndex)) == 0) {
				mIndex--;
			}
		}
		
		if (splitInTwo) {
			nIndex = mIndex;
		}
		else {
			// increase the n-index where the table will be splitted until there is a different key
			while (nIndex < size() && elementList.get(nIndex-1).compareTo(elementList.get(nIndex)) == 0) {
				nIndex++;
			}
		}

		LinkedList<SkipgraphElement> etList;
		BigDecimal etStart = this.rangeStart;
		BigDecimal etEnd;
		
		if (mIndex > 0) {
			etList = new LinkedList<>(this.elementList.subList(0, mIndex));
			etEnd = mIndex < size() ? this.elementList.get(mIndex).getValue() : this.rangeEnd;
			et[0] = new ElementTable(etList, etStart, etEnd);
			etStart = etEnd;
		}
		
		if (!splitInTwo) {
			// build second table
			etList = new LinkedList<>(this.elementList.subList(mIndex, nIndex));
			etEnd = nIndex < size() ? this.elementList.get(nIndex).getValue() : this.rangeEnd;
			et[1] = new ElementTable(etList, etStart, etEnd);
			etStart = etEnd;
		}

		// build third table
		if (nIndex < size()) {
			etList = new LinkedList<>(this.elementList.subList(nIndex, size()));
			etEnd = this.rangeEnd;
			et[2] = new ElementTable(etList, etStart, etEnd);
		}

		// Testing
		etStart = null;
		etEnd = null;
		int etSize = 0;
		for (ElementTable e : et) {
			if (e != null) {
				if (etStart == null) {
					etStart = e.getRangeStart();
				}
				else {
					etStart = etStart.compareTo(e.getRangeStart()) < 0 ? etStart : e.getRangeStart();
				}
				if (etEnd == null) {
					etEnd = e.getRangeEnd();
				}
				else {
					if (e.getRangeEnd() == null) {
						etEnd = null;
					}
					else {
						etEnd = etEnd.compareTo(e.getRangeEnd()) > 0 ? etEnd : e.getRangeEnd();
					}
				}
				etSize += e.size();
			}
		}
		
		assert (rangeStart.equals(etStart) &&
				((etEnd == null && rangeEnd == null) || rangeEnd.equals(etEnd)) && 
				size() == etSize);
		
		return et;
	}
	
	

	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(ind1+"- ElementTable %s <size:%d (min:%d, max:%d)>\n",
						getRange(),
						size(), 
						SkipgraphServiceConstants.getElementTableMinSize(), 
						SkipgraphServiceConstants.getElementTableMaxSize()
				));
		sort();
		for (int i = 0; i < size(); i++) {
			String index = ind3 /*+ " " + i*/;
			sb.append(index + get(i) + "\n");
		}
		return sb.toString();
	}


	
	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public long getTransmissionSize() {
		// 2 * BigDecimal (rangeStart+rangeEnd)
		long size = 2*SkipgraphServiceConstants.RESOLUTION_SIZE;
		// elementList
		for (SkipgraphElement element : elementList) {
			size += element.getTransmissionSize();
		}
		return size;
	}
	
	@Override
	public Object clone() {
		return copy();
	}
	
	public ElementTable copy() {
		List<SkipgraphElement> elementListCopy = new LinkedList<>();
		for (SkipgraphElement element : elementList) {
			elementListCopy.add(element.copy());
		}
		return new ElementTable(elementListCopy, rangeStart, rangeEnd);
	}



	/* ************************************************
	 ******************* STATIC ***********************
	 ************************************************ */

	/**
	 * merge ElementTable (A) with another ElementTable (B)
	 * to ElementTable (C).
	 * 
	 * the boundaries of A will be adjusted to:
	 * rangeStart = min{rangeStart(A),rangeStart(B)} 
	 * rangeEnd = max{rangeEnd(A),rangeEnd(B)}
	 * 
	 * the operation is only executed if:
	 * (rangeEnd(B) >= rangeStart(A)) and (rangeStart(B) <= rangeEnd(A))
	 * 
	 * remember:
	 * rangeStart == null -> -inf
	 * rangeEnd == null -> inf
	 * 
	 * @param A		an ElementTable (A) that will be merged with B
	 * @param B		an ElementTable (B) that will be merged with A
	 * @return 		ElementTable (C) = (A+B) or null if merge failed 
	 */
	public static ElementTable merge(ElementTable A, ElementTable B) {
		if (A == null || A.isEmpty())
			return B.copy();

		if (B == null || B.isEmpty())
			return A.copy();
		
		BigDecimal aStart = A.getRangeStart();
		BigDecimal aEnd = A.getRangeEnd();
		BigDecimal bStart = B.getRangeStart();
		BigDecimal bEnd = B.getRangeEnd();
		BigDecimal cStart = B.getRangeStart();
		BigDecimal cEnd = B.getRangeEnd();
		List<SkipgraphElement> cList = new LinkedList<>();
		
		if ((aStart == null || bEnd == null || bEnd.compareTo(aStart) >= 0) && 
			(aEnd == null || bStart == null || bStart.compareTo(aEnd) <= 0)) {
			cList.addAll(A.getTable());
			cList.addAll(B.getTable());
			cStart = (aStart == null || bStart == null) ? null :
					(aStart.compareTo(bStart) < 0) ? aStart : bStart;
			cEnd = (aEnd == null || bEnd == null) ? null : 
					(aEnd.compareTo(bEnd) > 0) ? aEnd : bEnd;
			return new ElementTable(cList, cStart, cEnd);
		}
		
		return null;
	}


}