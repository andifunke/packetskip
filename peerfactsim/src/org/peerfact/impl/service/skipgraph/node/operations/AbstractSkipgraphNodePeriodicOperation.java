package org.peerfact.impl.service.skipgraph.node.operations;

import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphPeriodicOperation;

/**
 * Common asbtract superclass of all periodic SkipgraphNode operations.
 *
 * @param <S> the type of the operation result
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphNodePeriodicOperation<S> extends AbstractSkipgraphPeriodicOperation<S> 
 {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphNodeController nodeController;
	
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	AbstractSkipgraphNodePeriodicOperation(
			SkipgraphNodeController nodeController) 
	{
		super(nodeController.getService());
		this.nodeController = nodeController;
	}
	
	
	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public SkipgraphNodeController getNodeController() {
		return this.nodeController;
	}
	

	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */
	
	@Override
	public void log(String str) {
		if (isLogging()) {
			nodeController.log(getClassName()+" [id="+getOperationID()+"]: "+ str);
		}
	}
	
}
