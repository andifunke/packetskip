package org.peerfact.impl.service.skipgraph.operations;

import org.peerfact.impl.common.AbstractOperation;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.util.DotFileBuilder;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class DelayBuildOperation extends AbstractOperation<SkipgraphService, Boolean> {

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public DelayBuildOperation(SkipgraphService component) {
		super(component, null);
	}
	
	/* *************************************************
	 ******************** GETTERS **********************
	 ***************************************************/

	@Override
	protected void execute() {
		DotFileBuilder.buildAndPrint();
		operationFinished(true);
	}

	@Override
	public Boolean getResult() {
		return true;
	}

}
