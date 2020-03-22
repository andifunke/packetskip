package org.peerfact.impl.service.skipgraph.node;

import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind1;
import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class ContactTable implements DHTObject {

	/* *******************************************
	 ****************** STATIC *******************
	 ******************************************* */

	private static int generatePrefix() {
		return Simulator.getRandom().nextInt(2);
	}
	
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final LinkedList<ContactLevel> contactLevelList;
	
	private final BigInteger nodeID;

	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	/**
	 * constructor for a joining node. Set the node's successor and predecessor on level 0.
	 * 
	 * @param prev
	 * @param next
	 */
	public ContactTable(BigInteger nodeID, SkipgraphContact prev, SkipgraphContact next) {
		this.nodeID = nodeID;
		this.contactLevelList = new LinkedList<>();

		// build level 0 -- always with fixed prefix '1'
		ContactLevel level0 = new ContactLevel(prev, next, 1);
		addLevel(level0);
		buildAllLevels();
	}
	
	private void buildAllLevels() {
		/*
		// build level 1
		ContactLevel level1 = new ContactLevel(null, null, generatePrefix());
		addLevel(level1);
		*/
		// TODO: add more levels
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public ContactLevel getLevel(int i) {
		return contactLevelList.get(i);
	}
	
	public ContactLevel getTopLevel() {
		return contactLevelList.getLast();
	}

	public SkipgraphContact getPrev() {
		return getPrevOnLevel(0);
	}

	public SkipgraphContact getNext() {
		return getNextOnLevel(0);
	}

	public SkipgraphContact getPrevOnLevel(int i) {
		return getLevel(i).getPrevContact();
	}

	public SkipgraphContact getNextOnLevel(int i) {
		return getLevel(i).getNextContact();
	}
	
	public int size() {
		return contactLevelList.size();
	}
	
	public int effectiveSize() {
		Iterator<ContactLevel> it = contactLevelList.descendingIterator();
		int counter = 1;
		ContactLevel currentLevel = it.next();
		while(it.hasNext()) {
			ContactLevel nextLevel = it.next();
			if (!currentLevel.equalsExceptPrefix(nextLevel)) {
				counter++;
			}
			currentLevel = nextLevel;
		}
		return counter;
	}
	
	public int getNumberOfDistinctContacts() {
		return getContactSet().size();
	}
	
	public int getTablePrefix() {
		int levelPrefix = 1;
		for (int i=1; i<size(); i++) {
			levelPrefix = levelPrefix<<1;
			levelPrefix += getLevel(i).getPrefix();
		}
		return levelPrefix;
	}
	

	
	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	boolean setPrevOnLevel(SkipgraphContact skipgraphContatc, int index) {
		return getLevel(index).setPrevContact(skipgraphContatc);
	}

	boolean setNextOnLevel(SkipgraphContact skipgraphContatc, int index) {
		return getLevel(index).setNextContact(skipgraphContatc);
	}

	boolean addLevel(ContactLevel level) {
		return contactLevelList.add(level);
	}

	boolean addDefaultTopLevel(SkipgraphContact selfContact) {
		//String log = "addDefaultLevel: ";
		ContactLevel currentTopLevel = getTopLevel();
		
		// if the current top level is already self-linked then there should be no need
		// to build another level. So we return the current top level and abort the operation.
		// TODO: evaulate if this is rather help- or harmful
		if (isSelfLinkedLevel(currentTopLevel)) {
			//log += "there is already a self-linked top level. Not adding another.\n";
			//node.log(log);
			return false;
		}
		
		if (!SkipgraphServiceConstants.optimizeLevelPrefix) {
			return addLevel(new ContactLevel(selfContact, selfContact, generatePrefix()));
		}
		
		// we want a prefix for the new level that is different from our predecessor's prefix 
		// on that new level. So we take a look at the contactTablePrefix of the previous node. 
		// Here we can see, if the level we want to add already exists on the predecessor and 
		// and if so choose a different prefix.
		int prefix;
		long prevTablePrefix = currentTopLevel.getPrevContact().getContactTablePrefix();
		//log += "prev on level="+(size-1)+": "+currentTopLevel.getPrevContact();
		// we are shifting the table prefix to the right while it's greater than 0 to get the 
		// size of the prev's table.
		int prevTableSize = 0;
		long tmp = prevTablePrefix;
		while (tmp > 0) {
			prevTableSize++;
			tmp >>>= 1;
		}
		//log += " -- prevContactTableSize="+prevTableSize;
		// if the size of the predecessor's table is smaller or equal our table's size, then 
		// the level we want to add does not exist on the predecessor and we generate a random prefix. 
		if (prevTableSize <= size()) {
			prefix = generatePrefix();
			//log += " is lower or equal to our own contactTableSize="+size
			//		+ ". generating random prefix="+prefix;
		}
		// else we isolate the prefix bit for that level, switch it and that is our new prefix 
		else {
			tmp = prevTablePrefix >>> (prevTableSize-(size()+1));
			//log += "\n   prevTablePrefix after shifting by the size-difference-1: "+tmp;
			tmp = ~tmp;
			//log += "\n   prevTablePrefix after bitwise complement: "+tmp;
			tmp &= 1;
			//log += "\n   prevTablePrefix after applying bitmask '1': "+tmp;
			prefix = (int)tmp;
			//log += "\n   => our prefix for level="+size+" is: "+prefix;
		}
		
		return addLevel(new ContactLevel(selfContact, selfContact, prefix));
	}



	/* ************************************************
	 ******************* METHODS **********************
	 ************************************************ */
	
	/* ********** specialized Getters *************** */

	/**
	 * this method finds the PrevNode on the highest possible level which range start is 
	 * below or equal the given value
	 * 
	 * @param value	the value a query starts with
	 * @return			returns the highest PrevNode on the highest possible level that 
	 * 					doesn't exceed the value
	 */
	public SkipgraphContact getPrevNodeForValue(BigDecimal value) {
		for (int i=size()-1; i>-1; i--) {
			if (getLevel(i).getPrevContact().getRangeStart().compareTo(value) > 0) continue;
			return getPrevOnLevel(i);
		}
		return getPrev();
	}
	
	
	
	/**
	 * this method returns an ordered map/list extracted from the contactTable, sorted by rangeStart
	 * key: rangeStart of each contact in the table
	 * value: the nodeID that belongs to the rangeStart
	 * @return	TreeMap
	 */
	public TreeMap<BigDecimal, SkipgraphContact> getContactRangeMap() {
		/*
		 * this is an ordered map/list extracted from the contactTable, sorted by rangeStart
		 * key: rangeStart of each contact in the table
		 * value: the nodeID that belongs to the rangeStart
		 */
		TreeMap<BigDecimal, SkipgraphContact> contactRangeMap = new TreeMap<>();
		
		for (ContactLevel level : contactLevelList) {
			SkipgraphContact prev = level.getPrevContact();
			SkipgraphContact next = level.getNextContact();
			contactRangeMap.put(prev.getRangeStart(), prev);
			contactRangeMap.put(next.getRangeStart(), next);
		}
		
		return contactRangeMap;
	}

	

	/**
	 * @return  a set of all contacts
	 */
	public Set<BigInteger> getContactSet() {
		Set<BigInteger> contactSet = new LinkedHashSet<>();
		
		for (ContactLevel level : contactLevelList) {
			contactSet.add(level.getPrevContact().getNodeID());
			contactSet.add(level.getNextContact().getNodeID());
		}
		
		return contactSet;
	}
	
	
	public boolean isSelfLinkedLevel(int levelIndex) {
		if (levelIndex >= 0 && levelIndex < size()) {
			return isSelfLinkedLevel(getLevel(levelIndex));
		}
		return false;
	}


	public boolean isSelfLinkedLevel(ContactLevel level) {
		BigInteger localNodeID = nodeID;
		return level.getNextContact().getNodeID().equals(localNodeID) &&
				level.getPrevContact().getNodeID().equals(localNodeID);
	}
	
	

	/* ********** Maintenance *************** */

	/**
	 * this method checks if there is more than one level which is only self referencing
	 */
	boolean deleteRedundantLevels() {
		int max = contactLevelList.size()-1;
		int counter = 0;
		while (getLevel(max-counter) != null &&
				getLevel(max-counter).getPrevContact().getNodeID().equals(nodeID) &&
				getLevel(max-counter).getNextContact().getNodeID().equals(nodeID)) {
			counter++;
			//System.out.println("number of selfcontact levels: " + counter);
		}
		boolean removed = false;
		for ( ; counter>1; counter--) {
			contactLevelList.removeLast();
			removed = true;
			//System.out.println("deleting redundant level");
		}
		return removed;
	}

	
	/** 
	 * if there are n > 1 self-linked top-levels reduce to n = 1
	 */
	boolean deleteRedundantSelfLinkedLevels() {
		boolean removed = false;
		for (int i=size()-1; i>0; i--) {
			if (isSelfLinkedLevel(getLevel(i)) && isSelfLinkedLevel(getLevel(i-1))) {
				contactLevelList.removeLast();
				removed = true;
			}
			else {
				break;
			}
		}
		return removed;
	}


	/**
	 * if we have any contacts left in the table which are outdated replace them with either ourself
	 * or the other prev/next.
	 * @param deprecatedNodes
	 * @param self
	 * @return
	 */
	boolean removeDeprecatedContacts(Set<BigInteger> deprecatedNodes, SkipgraphContact self) {
		boolean changed = false;
		// make sure we are not in the deprecated list
		if (deprecatedNodes.contains(nodeID)) {
			deprecatedNodes.remove(nodeID);
		}
		// go through all levels
		for (ContactLevel level : contactLevelList) {
			SkipgraphContact prev = level.getPrevContact();
			SkipgraphContact next = level.getNextContact();
			// prev or next are deprecated?
			if (deprecatedNodes.contains(prev.getNodeID())) {
				if (next.getNodeID().equals(nodeID)) {
					// if next is selfContact we set prev as selfContact as well => self-linked level 
					changed |= level.setPrevContact(self);
					continue;
				}
				else {
					if (deprecatedNodes.contains(next.getNodeID())) {
						// if next node is deprecated as well we will set both as selfContatcs => self-linked level
						changed |= level.setPrevContact(self);
						changed |= level.setNextContact(self);
						continue;
					}
					else {
						// if next is not deprecated we set prev to next to form a circle
						// in some circumstances this might not be ideal
						changed |= level.setPrevContact(next);
					}
				}
			}
			if (deprecatedNodes.contains(next.getNodeID())) {
				if (prev.getNodeID().equals(nodeID)) {
					// if prev is selfContact we set next as selfContact as well => self-linked level 
					changed |= level.setNextContact(self);
				}
				else {
					// the case were both contacts are deprecated is already covered
					// so the last case is that next is deprecated, but prev is not and both are not selfContacts
					// => set next to prev
					changed |= level.setNextContact(prev);
				}
			}
		}
		return changed;
	}
	
	
	
	/**
	 * 
	 * @param deprecatedNodes
	 * @param self
	 * @param levelIndex
	 * @return    false if nothing has changed, true if a contact was replaced.
	 */
	boolean sanitiesLevelWhileExecutingReplaceContact(Set<BigInteger> deprecatedNodes, 
			SkipgraphContact self, int levelIndex) {
		// if the level is already self-linked we don't have to sanities
		ContactLevel level = getLevel(levelIndex);
		if (isSelfLinkedLevel(level))
			return false;
		
		/*
		// make sure we are not in the deprecated list
		if (deprecatedNodes.contains(nodeID)) {
			deprecatedNodes.remove(nodeID);
		}
		*/

		BigInteger prevID = level.getPrevContact().getNodeID();
		BigInteger nextID = level.getNextContact().getNodeID();
		
		// if prev and next are the same and deprecated we make the level self-linked
		// if prev and next are the same and not deprecated we leave the level as is
		if (prevID.equals(nextID)) {
			if (deprecatedNodes.contains(prevID)) {
				level.setPrevContact(self);
				level.setNextContact(self);
				return true;
			}
			return false;
		}

		// if prev and next are different
		// if both are deprecated we make the level self-linked
		// TODO: this could have side-effects -> a new JoinLevel might be better
		if (deprecatedNodes.contains(prevID) && deprecatedNodes.contains(nextID)) {
			return level.setPrevContact(self) | level.setNextContact(self);
		}

		// if only one of them is deprecated we set the contact to the one which is not 
		if (deprecatedNodes.contains(prevID) && !deprecatedNodes.contains(nextID)) {
			return level.setPrevContact(level.getNextContact()); 
		}
		
		if (!deprecatedNodes.contains(prevID) && deprecatedNodes.contains(nextID)) {
			return level.setPrevContact(level.getPrevContact());
		}
		
		// if none of them is deprecated we leave the level as is
		return false;
	}
	


	boolean updateContacts(SkipgraphContact updatedContact) {
		if (updatedContact == null)
			return false;
		
		boolean updated = false;
		BigInteger updatedNodeID = updatedContact.getNodeID();
		
		for (ContactLevel level : contactLevelList) {
			SkipgraphContact prev = level.getPrevContact();
			SkipgraphContact next = level.getNextContact();

			if (prev != null && prev.getNodeID().equals(updatedNodeID)) {
				updated |= level.setPrevContact(updatedContact);
			}
			if (next != null && next.getNodeID().equals(updatedNodeID)) {
				updated |= level.setNextContact(updatedContact);
			}
		}
		return updated;
	}
	
	
	/**
	 * if a node wants to join on the top level which has already joined on the second highest level
	 * than we generate a different prefix for the top level. This optimization helps to get as close as
	 * possible to O(log(n)). 
	 * @param joiningID
	 * @param joiningPrefix
	 * @param route
	 */
	boolean optimizeTopLevelPrefix(BigInteger joiningID, int joiningPrefix, SGUtil.Route route) {
		if (route == null) {
			System.out.println("level prefix optimizer: no route given.\n");
			return false;
		}

		if (size() < 2) {
			System.out.println("level prefix optimizer: cannot optimize level 0.\n");
			return false;
		}

		ContactLevel topLevel = getTopLevel();
		long oldPrefix = topLevel.getPrefix();

		if (oldPrefix != joiningPrefix) {
			System.out.println("level prefix optimizer: different prefixes. no need for optimization. "
					+ "remaining prefix="+oldPrefix+"\n");
			return false;
		}

		if (!isSelfLinkedLevel(topLevel)) {
			System.out.println("level prefix optimizer: top level is already linked to other nodes. cannot optimize."
						+ " remaining prefix="+oldPrefix+"\n");
			return false;
		}

		int secondHighestLevel = size()-2;
		BigInteger idToTestAgainst = null;

		if (route == SGUtil.Route.PREV) {
			idToTestAgainst = getLevel(secondHighestLevel).getPrevContact().getNodeID();
		}
		else if (route == SGUtil.Route.NEXT) {
			idToTestAgainst = getLevel(secondHighestLevel).getNextContact().getNodeID();
		}

		if (!joiningID.equals(idToTestAgainst)) {
			System.out.println("not linked to requester "+SGUtil.formatID(joiningID)
			+" on level="+secondHighestLevel+" as "+route+" contact "
			+ "-> no optimization on top level needed. remaining prefix="+oldPrefix+"\n");
			return false;
		}

		int newPrefix;
		do {
			newPrefix = generatePrefix();
		} while (newPrefix == oldPrefix);
		topLevel.setPrefix(newPrefix);
		System.out.println("optimized: "
				+ "already linked to requester "+SGUtil.formatID(joiningID)
				+" on level="+secondHighestLevel+" as "+route+" contact "
				+ "-> optimizing top level. new prefix="+newPrefix+"\n");
		return true;
	}
	
	
	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(ind1+"- ContactTable <size:%d>\n", size()));
		for (int i=0; i<size(); i++) {
			String index = ind3+"level "+i+", ";
			sb.append(index + getLevel(i) + "\n");
		}
		return sb.toString();
	}


	
	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public long getTransmissionSize() {
		// BigDecimal nodeID
		long size = nodeID.toByteArray().length;
		// List<ContactLevel>
		for (ContactLevel level : contactLevelList) {
			size += level.getTransmissionSize();
		}
		return size;
	}

}
