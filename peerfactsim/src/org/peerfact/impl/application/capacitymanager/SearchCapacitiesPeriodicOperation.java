package org.peerfact.impl.application.capacitymanager;

import org.peerfact.impl.common.AbstractPeriodicOperation;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.simengine.Simulator;

/**
 * Searches for capacities in a periodic interval.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchCapacitiesPeriodicOperation 
		extends AbstractPeriodicOperation<CapacityManagerApplication, Object> {
	
	
	private boolean logging = CapacityManagerApplicationConstants.logging || 
			SkipgraphServiceConstants.logSearch;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchCapacitiesPeriodicOperation(CapacityManagerApplication application) {
		super(application);
		setLogging(CapacityManagerApplicationConstants.logging);
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
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	@Override
	protected void executeOnce() {
		if (!getComponent().getLocalNode().isPresent() || isFinished() || isStopped())
			return;
		
		log("executeOnce", false);
		getComponent().searchCapacities();

		incrementLookupCounter();
	}


	/**
	 * adding some randomness: +- 30 seconds
	 */
	@Override
	protected long getInterval() {
		return CapacityManagerApplicationConstants.getSearchCapacityInterval() 
				+ (Simulator.getRandom().nextInt(60)-30)*Simulator.SECOND_UNIT;

	}

}
