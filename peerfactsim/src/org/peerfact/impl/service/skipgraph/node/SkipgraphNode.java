package org.peerfact.impl.service.skipgraph.node;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.util.DotFileBuilder;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphNode implements DHTObject, Comparable<SkipgraphNode> {

	/* *******************************************
	 ****************** STATIC *******************
	 ******************************************* */

	/** for analysis and debugging - not for p2p-usage */

	private static int idCounter = 0;
	
	private static int currentNumberOfNodes = 0;

	/** creates a unique ID for each SkipgraphNode */
	private static int nextID() {
		return ++idCounter;
	}
	
	public static int getCurrentNumberOfNodes() {
		return idCounter;
	}
	
	private static void incrementNumberOfNodes() {
		++currentNumberOfNodes;
	}

	private static void decrementNumberOfNodes() {
		--currentNumberOfNodes;
	}


	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final BigInteger nodeID;
	
	private ElementTable elementTable;

	private final ContactTable contactTable;
	
	/** for analysis and debugging -- not a P2P-feature */
	private final int globalCountID;
	private final long startTime = Simulator.getCurrentTime();
	


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	/**
	 * if a SkipGraphNode is constructed without neighbouring contacts it is assumed that this is
	 * the first node of the SkipGraph.
	 * @param service      reference to the underlying SkipgraphNode
	 */
	public SkipgraphNode(BigInteger nodeID) {
		this.globalCountID = nextID();
		this.nodeID = nodeID;
		this.elementTable = new ElementTable();
		SkipgraphContact selfContact = getContact();
		this.contactTable = new ContactTable(nodeID, selfContact, selfContact);
		init();
	}

	public SkipgraphNode(BigInteger nodeID, ElementTable elementTable, ContactTable contactTable) {
		this.globalCountID = nextID();
		this.nodeID = nodeID;
		this.elementTable = elementTable;
		this.contactTable = contactTable;
		init();
	}

	private void init() {
		incrementNumberOfNodes();
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			((SkipgraphMonitor)Simulator.getMonitor()).addNewNode(this);
			((SkipgraphMonitor)Simulator.getMonitor()).nodeAdded(this);
		}
		if (SkipgraphServiceConstants.logJoin) {
			System.out.println(this+" initialized. number of nodes currently: "+currentNumberOfNodes);
			print();
		}
	}
	
	public void terminate() {
		decrementNumberOfNodes();
		DotFileBuilder.removeNode(null, this);
		if (SkipgraphServiceConstants.logLeave) {
			System.out.println(this+" terminated. number of nodes currently: "+currentNumberOfNodes);
		}
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			((SkipgraphMonitor)Simulator.getMonitor()).nodeRemoved(this);
		}
	}



	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public ElementTable getElementTable() {
		return elementTable;
	}

	public ContactTable getContactTable() {
		return contactTable;
	}

	public BigInteger getNodeID() {
		return nodeID;
	}

	public int getGlobalCountID() {
		return globalCountID;
	}

	public long getStartTime() {
		return startTime;
	}

	public SkipgraphContact getContact() {
		return new SkipgraphContact(
				nodeID,
				elementTable == null ? BigDecimal.ZERO : elementTable.getRangeStart(), 
				elementTable == null ? BigDecimal.ZERO : elementTable.getRangeEnd(),
				(contactTable != null ? contactTable.getTablePrefix() : 0 ));
	}


	
	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	void setElementTable(ElementTable elementTable) {
		this.elementTable = elementTable;
	}



	/* ************************************************
	 ************** DATA MODIFICATION *****************
	 ************************************************ */

	boolean add(List<SkipgraphElement> elements) {
		if (elements == null || elements.isEmpty()) {
			return false;
		}
		try {
			return elementTable.addAll(elements);
		}
		catch (NullPointerException e) {
			log("seed");
			e.printStackTrace();
		}
		return false;
	}

	
	boolean remove(List<SkipgraphElement> elements) {
		if (elements == null || elements.isEmpty()) {
			return false;
		}
		try {
			return elementTable.removeAll(elements);
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	@Override
	public String toString() {
		return String.format("SkipgraphNode <%d> %s %s",
				globalCountID,
				nodeIDformat(),
				elementTable == null ? "[---,---]" : elementTable.getRange()
				);
	}


	public String nodeIDformat() {
		return SGUtil.formatID(nodeID);
	}
	
	public void print() {
		log("\n"+(elementTable == null ? "- ElementTable=null\n" : elementTable)+contactTable);
	}

	public void printElement() {
		log("\n"+(elementTable == null ? "- ElementTable=null\n" : elementTable));
	}

	public void printContact() {
		log("\n"+contactTable);
	}

	public void printElementTable() {
		System.out.print(elementTable);
	}

	public void printContactTable() {
		System.out.print(contactTable);
	}

	public void log(String str) {
		System.out.println("\n• "+this+": "+str);
	}

	@Override
	public int compareTo(SkipgraphNode externalNode) {
		BigDecimal internalValue = elementTable == null ? null : elementTable.getRangeStart();
		BigDecimal externalValue = externalNode.elementTable == null ? null : externalNode.elementTable.getRangeStart();

		if (internalValue == null) {
			if (externalValue == null) {
				return 0;
			}
			else {
				return -1;
			}
		}
		else if (externalValue == null) {
			return 1;
		}
		else {
			return internalValue.compareTo(externalValue);
		}
	}



	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public long getTransmissionSize() {
		long size = nodeID.toByteArray().length;
		size += elementTable == null ? 0 : elementTable.getTransmissionSize();
		size += contactTable.getTransmissionSize();
		return size;
	}
	
}

