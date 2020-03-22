package org.peerfact.impl.application.capacitymanager;

import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.DEFAULT_GRANULARITY;

import org.peerfact.impl.common.AbstractPeriodicOperation;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.simengine.Simulator;

/**
 * Update and publishes capacities in a periodic interval.
 * Applies pseudo churn if configured.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class StoreCapacitiesPeriodicOperation 
		extends AbstractPeriodicOperation<CapacityManagerApplication, Object> {
	
	
	/* *******************************************
	 ****************** FIELDS *******************
	 *********************************************/

	private boolean paused = false;
	
	private int currentPauseThreshold = DEFAULT_GRANULARITY;
	
	private boolean logging = CapacityManagerApplicationConstants.logging ||
			SkipgraphServiceConstants.logUpdate;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public StoreCapacitiesPeriodicOperation(CapacityManagerApplication application) {
		super(application);
		setLogging(logging);
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
	
	public String getClassName() {
		return getClass().getSimpleName();
	}
	

	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */
	
	public boolean isLogging() {
		return logging;
	}
	
	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	private void log(String str) {
		log(str, false);
	}
	
	private void log(String str, boolean forceLogging) {
		if (logging || forceLogging) {
			getComponent().log(getClassName()+" [id="+getOperationID()+"]: "+ str
					+ " @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()), forceLogging);
		}
	}
	

	/* ************************************************
	 ******************** METHODS *********************
	 ************************************************ */
	
	@Override
	protected void executeOnce() {
		if (!getComponent().getLocalNode().isPresent() || isFinished() || isStopped())
			return;
		
		if (!CapacityManagerApplicationConstants.pseudoChurn) {
			log("executing (no pseudo churn)", false);
			executeDefault();
			return;
		}
		
		if (CapacityManagerApplicationConstants.pseudoChurnStart > Simulator.getCurrentTime()) {
			log("executing (pseudo churn starts @ "+Simulator.getFormattedTime(CapacityManagerApplicationConstants.pseudoChurnStart)+")", false);
			executeDefault();
			return;
		}
		
		if (CapacityManagerApplicationConstants.pseudoChurnEnd < Simulator.getCurrentTime()) {
			log("executing (pseudo churn ended)", false);
			executeDefault();
			return;
		}
		
		int rand = Simulator.getRandom().nextInt(DEFAULT_GRANULARITY);
		if (paused) {
			log("paused (currentPauseThreshold="+currentPauseThreshold+")", false);
			if (rand > currentPauseThreshold) {
				paused = false;
				currentPauseThreshold = DEFAULT_GRANULARITY;
				log("unpausing now.", false);
				executeDefault();
			}
			else {
//				currentPauseThreshold--;
			}
			return;
		}
		
		if (rand > currentPauseThreshold) {
			paused = true;
			log("pausing. as of now.", false);
			getComponent().pretendChurn();
			return;
		}

		log("executing (pseudo churn)", false);
		executeDefault();
		currentPauseThreshold--;
	}
	
	
	private void executeDefault() {
		if (CapacityManagerApplicationConstants.smoothCapacityChange) {
			getComponent().changeCapacityValues();
		}
		else {
			getComponent().setNewCapacityValues();
		}
		getComponent().storeCapacities();
		incrementLookupCounter();
	}


	@Override
	protected long getInterval() {
		return CapacityManagerApplicationConstants.getStoreCapacityInterval();

	}

}
