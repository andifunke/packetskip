package org.peerfact.impl.service.skipgraph.operations;

import org.peerfact.impl.common.AbstractPeriodicOperation;
import org.peerfact.impl.service.skipgraph.SkipgraphService;

/**
 * Common asbtract superclass of all periodic skipgraph operations.
 *
 * @param <S> the type of the operation result
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphPeriodicOperation<S> 
		extends AbstractPeriodicOperation<SkipgraphService, S> {

	private boolean logging = false;
	
	protected AbstractSkipgraphPeriodicOperation(SkipgraphService component) {
		super(component);
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
	
	public void log(String str) {
		log(str, false);
	}
	
	public void log(String str, boolean forceLogging) {
		if (logging || forceLogging)
			getComponent().log(getClassName()+" [id="+getOperationID()+"]: "+ str);
	}
	
}
