package org.peerfact.impl.service.skipgraph.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.simengine.Simulator;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class ElementBuffer {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphNodeController nodeController;

	private final List<SkipgraphElement> inputList = new LinkedList<>();

	private final List<SkipgraphElement> deleteList = new LinkedList<>();
	
	private boolean logging = SkipgraphServiceConstants.logJoin || 
			SkipgraphServiceConstants.logMaintenance || 
			SkipgraphServiceConstants.logLeave;



	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public ElementBuffer(SkipgraphNodeController nodeController) {
		super();
		this.nodeController = nodeController;
		if (nodeController.enforcesLogging())
			logging = true;
	}




	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public List<SkipgraphElement> getInputList() {
		return inputList;
	}

	public List<SkipgraphElement> getDeleteList() {
		return deleteList;
	}



	/* *************************************************
	 ****************** METHODS ************************
	 ***************************************************/

	public boolean addAll(Collection<SkipgraphElement> input, Collection<SkipgraphElement> delete) {
		boolean success = false;
		if (input != null) {
			success |= addInputs(input);
		}
		if (delete != null) {
			success |= addDeletions(delete);
		}
		return success;
	}

	
	public boolean addInputs(Collection<SkipgraphElement> input) {
		//log("adding to inputList: "+input);
		return inputList.addAll(input);
	}

	
	public boolean addDeletions(Collection<SkipgraphElement> delete) {
		//log("adding to deleteList: "+delete);
		return deleteList.addAll(delete);
	}


	public void clear() {
		log("clearing buffer -> "+this);
		inputList.clear();
		deleteList.clear();
	}
	
	
	@Override
	public String toString() {
		return "[ElementBuffer] inputList="+inputList+" , deleteList="+deleteList;
	}
	
	
	private void log(String str) {
		log(str, false);
	}
	

	private void log(String str, boolean forceLogging) {
		if (logging || forceLogging)
			nodeController.log("[ElementBuffer] " + str 
					+ " @ " +Simulator.getFormattedTime(Simulator.getCurrentTime()));
	}
	
}