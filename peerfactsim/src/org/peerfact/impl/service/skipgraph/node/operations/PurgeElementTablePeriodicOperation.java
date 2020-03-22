package org.peerfact.impl.service.skipgraph.node.operations;

import org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class PurgeElementTablePeriodicOperation extends AbstractSkipgraphNodePeriodicOperation<Object> {
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public PurgeElementTablePeriodicOperation(
			SkipgraphNodeController nodeController) {
		super(nodeController);
		//setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance);
		log("construct new "+this);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Object getResult() {
		// not used
		return null;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	@Override
	protected void executeOnce() {
		
		if (!getComponent().getLocalNode().isPresent() || getNodeController().isDisabled()) {
			stop();
			log("-> stopping");
			return;
		}
		if (isStopped() || isFinished())
			return;

		log("executeOnce");
		
		getNodeController().purge();
	}


	@Override
	protected long getInterval() {
		return CapacityManagerApplicationConstants.getStoreCapacityInterval()
//				+ SkipgraphServiceConstants.purgeTolerance
				;
	}
	
	
	@Override
	public void stop() {
		super.stop();
		operationFinished(true);
	}

}
