package org.peerfact.impl.service.skipgraph.node;

import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind1;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.peerfact.api.overlay.dht.DHTKey;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryMessage;
import org.peerfact.impl.service.skipgraph.node.operations.OperationCaller;
import org.peerfact.impl.service.skipgraph.node.operations.OperationResponder;
import org.peerfact.impl.service.skipgraph.operations.DelayBuildOperation;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.simengine.Simulator;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SkipgraphNodeController {

	/**
	 * tests whether a list of SkipgraphElements is valid (field must not be null).
	 * Accepts or refuses a list as a whole (not individual elements)
	 *  
	 * @param elements
	 * @return false if any of the SkipgraphElements is not valid 
	 */
	@SuppressWarnings("static-method")
	private boolean validate(List<SkipgraphElement> elements) {
		if (!SkipgraphServiceConstants.VALIDATE_ELEMENTS)
			return true;
		
		if (elements == null || elements.isEmpty()) {
			return false;
		}
			
		boolean valid = false;
		valid = true;
		for (SkipgraphElement element : elements) {
			valid &= (element.getDimension() != null);
			valid &= (element.getValue() != null);
			valid &= (element.getContact() != null);
			if (!valid) {
				System.out.println("elements not valid. data corrupt.");
				break;
			}
		}
		return valid;
	}
	
	
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphNode node;
	
	private final DHTKey<?> key;
	
	private final SkipgraphService service;

	private final ElementBuffer elementBuffer;

	private final OperationCaller oc;
	
	private final OperationResponder or;
	
	private boolean locked = false;

	private boolean disabled = false;
	
	private boolean logJoin = SkipgraphServiceConstants.logJoin;

	private boolean logLeave = SkipgraphServiceConstants.logLeave;

	private boolean logMaintenance = SkipgraphServiceConstants.logMaintenance;
	
	private boolean forceLogging = false;
	
	private int incomingMessages = 0;

	private int outgoingMessages = 0;



	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	/**
	 * if a SkipGraphNode is constructed without neighbouring contacts it is assumed that this is
	 * the first node of the SkipGraph.
	 */
	public SkipgraphNodeController(SkipgraphService service, BigInteger nodeID, DHTKey<?> key) {
		this.node = new SkipgraphNode(nodeID);
		this.service = service;
		this.key = key;
		this.elementBuffer = new ElementBuffer(this);
		this.oc = new OperationCaller(this);
		this.or = new OperationResponder(this);
		init();
	}

	public SkipgraphNodeController(SkipgraphService service, SkipgraphNode node, DHTKey<?> key) {
		this.node = node;
		this.service = service;
		this.key = key;
		this.elementBuffer = new ElementBuffer(this);
		this.oc = new OperationCaller(this);
		this.or = new OperationResponder(this);
		init();
		oc.callHandshakeOperation(getContactTable().getNext().getNodeID(), 0, 1, SGUtil.Route.NEXT);
	}

	private void init() {
	}
	
	/**
	 * this method terminates the nodeController AND the node. 
	 * Don't call it if you just want to remove the nodeController.
	 */
	public void terminate() {
		// clear all operations from operation map (operations may still be running!)
		oc.terminate();
		// decrement global node counter (cheating!)
		node.terminate();
		// remove nodeController from map and node from DHT
		service.removeNode(node);
		DelayBuildOperation dbo = new DelayBuildOperation(service);
		dbo.scheduleWithDelay(SkipgraphServiceConstants.DOTBUILDER_DELAY);
	}



	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public SkipgraphService getService() {
		return service;
	}

	public SkipgraphNode getNode() {
		return node;
	}

	public ElementTable getElementTable() {
		return node.getElementTable();
	}

	public ContactTable getContactTable() {
		return node.getContactTable();
	}

	public BigInteger getNodeID() {
		return node.getNodeID();
	}

	public int getGlobalCountID() {
		return node.getGlobalCountID();
	}

	public SkipgraphContact getContact() {
		return disabled ? new SkipgraphContact(getNodeID(), BigDecimal.ZERO, BigDecimal.ZERO, 0)
				: node.getContact();
	}

	public OperationCaller getOperationCaller() {
		return oc;
	}

	public OperationResponder getOperationResponder() {
		return or;
	}

	public boolean isLocked() {
		return disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public int getIncomingMessages() {
		return incomingMessages;
	}

	public int getOutgoingMessages() {
		return outgoingMessages;
	}
	
	public boolean enforcesLogging() {
		return forceLogging;
	}




	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */
	
	public void incrementIncomingMessages() {
		incomingMessages++;
	}

	public void incrementOutgoingMessages() {
		outgoingMessages++;
	}
	
	public void resetMessages() {
		incomingMessages = 0;
		outgoingMessages = 0;
	}

	private void setElementTable(ElementTable elementTable) {
		if (!disabled) node.setElementTable(elementTable);
	}
	
	public void lock() {
		if (locked)
			return;
		
		if (logLeave || logMaintenance || logJoin || forceLogging)
			log("locking elementTable");

		locked = true;
	}
	
	public void disable() {
		if (disabled)
			return;
		
		if (logLeave || logMaintenance || forceLogging)
			log("!!!!! DISABLING NODE !!!!!");
		if (!locked)
			lock();
		node.setElementTable(null);
		disabled = true;
	}



	/* ************************************************
	 *********** PUBLIC FUNCTIONALITY *****************
	 ************************************************ */

	/**
	 * 
	 * @param operationID
	 * @param inputElements
	 * @param deleteElements
	 * @return  true if data is valid
	 */
	public boolean update(int operationID, List<SkipgraphElement> inputElements, List<SkipgraphElement> deleteElements) {
		boolean valid = validate(inputElements) | validate(deleteElements);
		if (!valid) {
			return false;
		}
		
		if (disabled) {
			oc.callUpdateForwardingOperation(getContactTable().getNext().getNodeID(), operationID, 
					inputElements, deleteElements);
			return true;
		}
		
		Map<BigInteger, UpdateTuple> map = mapElementsToContactsByRange(inputElements, deleteElements);
		
		boolean changed = false;
		for (Entry<BigInteger, UpdateTuple> entry : map.entrySet()) {
			BigInteger contactID = entry.getKey();
			List<SkipgraphElement> inputSublist = entry.getValue().getInputList(); 
			List<SkipgraphElement> deleteSublist = entry.getValue().getDeleteList(); 
			
			if (contactID.equals(getNodeID())) {
				changed |= add(inputSublist);
				changed |= remove(deleteSublist);
			}
			else {
				oc.callUpdateForwardingOperation(contactID, operationID, inputSublist, deleteSublist);
			}
		}

		if (changed) {
			save();
			inspectElementTableSize();
		}
		
		return true;
	}
	
	
	private boolean add(List<SkipgraphElement> elements) {
		if (disabled || locked) {
			elementBuffer.addInputs(elements);
			return false;
		}

		return node.add(elements);
	}

	
	private boolean remove(List<SkipgraphElement> elements) {
		if (disabled || locked) {
			elementBuffer.addDeletions(elements);
			return false;
		}

		return node.remove(elements);
	}
		
	
	/** 
	 * @param message
	 * @return true if the query was accepted.
	 */
	public boolean search(SearchQueryMessage message) {
		message.getTracker().addHop(service.getLocalContact().getPeerID());
		if (message.getQueries() == null || message.getQueries().isEmpty()) {
			return false;
		}

		if (SkipgraphServiceConstants.parallelSearch) {
			oc.prepareSearchParallelForwardingOperation(
					message.getSender(),
					message.getOperationID(),
					message.getQueries(),
					message.getTracker(),
					null);
			return true;
		}
		else {
			oc.prepareSearchSerialDistribution(
					message.getSender(),
					message.getOperationID(),
					message.getQueries(),
					0,
					message.getTracker());
			return true;
		}
	}
	
	

	/* ************************************************
	 *********** ELEMENT RANGE INSPECTION *************
	 ************************************************ */

	/**
	 * takes two lists of SkipgraphElements:
	 * 		1) inputs
	 * 		2) deletions
	 * the lists will be mapped to SkipgraphNodes from the contact table according to their ranges.
	 * 
	 * @param 	elements
	 * @return 	returns a LinkedHashMap that maps UpdateTuples to SkipgraphNodes 
	 */
	private Map<BigInteger, UpdateTuple> mapElementsToContactsByRange(
			List<SkipgraphElement> inputElements, 
			List<SkipgraphElement> deleteElements) {
		
		/*
		 * key: nodeID of a contact in the contactTable
		 * value: UpdateTuple, which is a combination of two lists of SkipgraphElements: for input and for delete
		*/
		Map<BigInteger, UpdateTuple> elementsReturnMap = new LinkedHashMap<>();

		/*
		 * this is an ordered map/list extracted from the contactTable, sorted by rangeStart
		 * key: rangeStart of each contact in the table
		 * value: the nodeID that belongs to the rangeStart
		 */
		TreeMap<BigDecimal, SkipgraphContact> contactRangeMap = getContactTable().getContactRangeMap();

		if (inputElements != null && !inputElements.isEmpty()) {
			for (SkipgraphElement element : inputElements) {
				/* Determine the node that is responsible for or as close as possible to an element.
				 * As close as possible means: the known node that is responsible for the rangeStart 
				 * below or equal to the element's value.
				 * If no such node exist we use the node that is responsible for the smallest key 
				 * in our contactTable.
				 */
				Entry<BigDecimal, SkipgraphContact>  entry = contactRangeMap.floorEntry(element.getValue());
				BigInteger responsibleNode;
				if (entry != null) {
					responsibleNode = entry.getValue().getNodeID();
				}
				else {
					responsibleNode = contactRangeMap.firstEntry().getValue().getNodeID();
				}
				UpdateTuple tuple = elementsReturnMap.getOrDefault(responsibleNode, new UpdateTuple());
				tuple.addInputElement(element);
				elementsReturnMap.put(responsibleNode, tuple);
			}
		}
		
		// repeat the same for the delete list
		if (deleteElements != null && !deleteElements.isEmpty()) {
			for (SkipgraphElement element : deleteElements) {
				Entry<BigDecimal, SkipgraphContact>  entry = contactRangeMap.floorEntry(element.getValue());
				BigInteger responsibleNode;
				if (entry != null) {
					responsibleNode = entry.getValue().getNodeID();
				}
				else {
					responsibleNode = contactRangeMap.firstEntry().getValue().getNodeID();
				}
				UpdateTuple tuple = elementsReturnMap.getOrDefault(responsibleNode, new UpdateTuple());
				tuple.addDeleteElement(element);
				elementsReturnMap.put(responsibleNode, tuple);
			}
		}
		
		return elementsReturnMap;
	}
	
	
	public boolean isResponsibleForStart(BigDecimal value) {
		return disabled ? false : getElementTable().isResponsibleForStart(value);
	}

	public boolean isResponsibleForEnd(BigDecimal value) {
		return disabled ? false : getElementTable().isResponsibleForEnd(value);
	}



	/* ************************************************
	 ******* ELEMENT TABLE SIZE INSPECTION ************
	 ************************************************ */

	public void inspectElementTableSize() {
		if (!disabled) {
			inspectElementTableMinThreshold();
			inspectElementTableMaxThreshold();
		}
	}

	public void inspectElementTableMinThreshold() {
		if (getElementTable().size() < SkipgraphServiceConstants.getElementTableMinSize()) {
			if (logLeave || forceLogging)
				log("\n"+ind1+"table too small -> LEAVE?");
			leave();
		}
	}

	public void inspectElementTableMaxThreshold() {
		if (getElementTable().size() > SkipgraphServiceConstants.getElementTableMaxSize()) {
			if (logJoin || logMaintenance) {
				log("\n"+ind1+"size="+getElementTable().size()
						+", max-size="+SkipgraphServiceConstants.getElementTableMaxSize()
						+" -> table too big -> SPLIT");
			}
			split();
		}
	}



	/* ************************************************
	 *********** ELEMENT TABLE UNLOCK *****************
	 ************************************************ */
	
	private boolean flushBuffer() {
		if (disabled || locked) return false;

		boolean success = update(0, elementBuffer.getInputList(), elementBuffer.getDeleteList());
		elementBuffer.clear();
		
		return success;
	}
	

	public boolean unlock() {
		if (disabled || !locked) return false;
		
		if (logJoin || logMaintenance || logLeave || forceLogging)
			log("unlocking");
		
		locked = false;
		return flushBuffer();
	}


	public boolean unlockWithNewElementTable(ElementTable et, SkipgraphContact updatedPrev, SkipgraphContact updatedNext) {
		if (disabled) return false;

		if (locked) {
			setElementTable(et);
			getContactTable().updateContacts(getContact());
			if (updatedPrev != null) {
				getContactTable().updateContacts(updatedPrev);
			}
			if (updatedNext != null) {
				getContactTable().updateContacts(updatedNext);
			}
			
			boolean success = unlock();
			save();
			oc.callRangeAdjustmentOperation();
			return success;
		}

		return false;
	}
	
	
	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	@Override
	public String toString() {
		return node + (disabled ? " (disabled)" : (locked ? " (locked)" : ""));
	}

	public void print() {
		print(false);
	}

	public void print(boolean log) {
		if (SkipgraphServiceConstants.printNodesWhileLogging || forceLogging || log)
			node.print();
	}

	public void printElements() {
		printElements(false);
	}

	public void printElements(boolean log) {
		if (SkipgraphServiceConstants.printNodesWhileLogging || forceLogging || log)
			node.printElement();
	}

	public void printContacts() {
		printContacts(false);
	}

	public void printContacts(boolean log) {
		if (SkipgraphServiceConstants.printNodesWhileLogging || forceLogging || log)
			node.printContact();
	}

	public void log(String str) {
		System.out.println("\n• "+this+": "+str
				+" @ "+Simulator.getFormattedTime(Simulator.getCurrentTime())
				+" (seed="+Simulator.getSeed()+")"
				);
	}



	/* ************************************************
	 **************** MAINTENANCE *********************
	 ************************************************ */

	public void leave() {
		// last node in skip graph must not leave
		if (getContactTable().isSelfLinkedLevel(0)) {
			if (logJoin || logMaintenance || logLeave || forceLogging)
				log("special case: skip graph too small. aborting");
			return;
		}
		oc.callLoadBalancingOperation("leave");
	}


	public void split() {
		if (SkipgraphServiceConstants.loadBalancing) {
			if (logJoin || logMaintenance || logLeave || forceLogging)
				log("split with loadbalancing");
			oc.callLoadBalancingOperation("join");
		}
		else {
			if (logJoin || logMaintenance || logLeave || forceLogging)
				log("split without loadbalancing");
			oc.callJoinRequestOperation();
		}
	}
	
	

	
	/* ************************************************
	 ********** CONTACT TABLE GATE KEEPER *************
	 ************************************************ */

	public void addLevel(ContactLevel level) {
		if (getContactTable().addLevel(level)) {
			save();
		}
	}
	
	public void addDefaultTopLevel() {
		if (getContactTable().addDefaultTopLevel(getContact())) {
			save();
		}
	}
	
	public void setPrevOnLevel(SkipgraphContact contact, int level) {
		if (getContactTable().setPrevOnLevel(contact, level)) {
			save();
		}
	}
	
	public void setNextOnLevel(SkipgraphContact contact, int level) {
		if (getContactTable().setNextOnLevel(contact, level)) {
			save();
		}
	}
	
	public void deleteRedundantLevels() {
		if (getContactTable().deleteRedundantLevels()) {
			save();
		}
	}

	/**
	 * this combined method avoids unnessecary dht store operations
	 * @param replacement
	 * @param levelIndex
	 */
	public boolean setContactOnLevelAndCleanUp(Route route, SkipgraphContact replacement, 
			int levelIndex, Set<BigInteger> deprecatedNodes, BigInteger leavingNodeID) {
		boolean changed = false;
		
		// set the new contact if it's not a deprecated id (i.e. a node that has already left)
		if (!deprecatedNodes.contains(replacement)) {
			if (route == Route.PREV)
				changed |= getContactTable().setPrevOnLevel(replacement, levelIndex);
			else
				changed |= getContactTable().setNextOnLevel(replacement, levelIndex);
		}
		
		changed |= getContactTable()
				.sanitiesLevelWhileExecutingReplaceContact(deprecatedNodes, getContact(), levelIndex);
		changed |= getContactTable().deleteRedundantSelfLinkedLevels();

		return changed;
	}
	
	
	public void updateContacts(SkipgraphContact contact) {
		if (getContactTable().updateContacts(contact)) {
			save();
		}
	}
	
	public boolean optimizeTopLevelPrefix(BigInteger joiningID, int joiningPrefix, Route route) {
		boolean changed = getContactTable().optimizeTopLevelPrefix(joiningID, joiningPrefix, route);
		if (changed) {
			save();
		}
		return changed;
	}
	
	public boolean hasSelfLinkedTopLevel() {
		return getContactTable().isSelfLinkedLevel(getContactTable().getTopLevel());
	}
	
	
	
	/* ************************************************
	 ********** ELEMENT TABLE GATE KEEPER *************
	 ************************************************ */

	public boolean extend(ElementTable extension) {
		if (disabled)
			return false;
		
		if (extension == null || extension.isEmpty())
			return false;
		
		if (logMaintenance || forceLogging) {
			log("extend Element Table");
			print();
		}
		ElementTable mergedTable = ElementTable.merge(getElementTable(), extension);
		if (mergedTable != null) {
			getNode().setElementTable(mergedTable);
			getContactTable().updateContacts(getContact());
			save();
			return true;
		}
		
		return false;
	}
	
	public void purge() {
		if (locked || disabled)
			return;
		
		if (getElementTable().purge()) {
			save();
		}
	}

	
	/* ************************************************
	 ****************** DHT ACCESS ********************
	 ************************************************ */

	/**
	 * this stores the node in the DHT - use only if some data (elementTable or contactTable)
	 * has changed
	 */
	public void save() {
		if (service.getLocalNode().isRootOf(key)) {
			service.getLocalNode().store(key, node, null);
		}
		else {
			try {
				throw new Exception("store failed: peer not responsible for nodeID");
			}
			catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
			}
		}
	}
	
}

