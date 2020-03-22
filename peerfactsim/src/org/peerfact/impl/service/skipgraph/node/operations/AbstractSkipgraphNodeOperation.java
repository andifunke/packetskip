package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphOperation;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * Common asbtract superclass of all SkipgraphNode operations.
 *
 * @param <S> the type of the operation result
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphNodeOperation<S> extends AbstractSkipgraphOperation<S> {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphNodeController nodeController;
	
	private BigInteger receiverNodeID;
	
	private ApplicationContact receiver;
	
	private long timeout = SkipgraphServiceConstants.NODE_OP_TIMEOUT;
	 
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	AbstractSkipgraphNodeOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<S> callback,
			BigInteger receiverNodeID) 
	{
		super(nodeController.getService(), callback);
		this.nodeController = nodeController;
		this.receiverNodeID = receiverNodeID;
		setMaxNumberOfLookups(3);
		if (nodeController.enforcesLogging()) setLogging(true);
	}
	
	
	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public SkipgraphNodeController getNodeController() {
		return this.nodeController;
	}
	
	public ApplicationContact getReceiver() {
		return receiver;
	}

	public BigInteger getReceiverNodeID() {
		return this.receiverNodeID;
	}
	
	public long getTimeout() {
		return timeout;
	}
	

	
	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	public void setReceiver(ApplicationContact receiver) {
		this.receiver = receiver;
	}

	public void setReceiverNodeID(BigInteger receiverNodeID) {
		this.receiverNodeID = receiverNodeID;
	}
	
	void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public void setLogging(boolean logging) {
		super.setLogging(logging || nodeController.enforcesLogging());
	}
	

	
	/* ************************************************
	 *********** AbstractOperation METHODS ************
	 ************************************************ */

	@Override
	protected void execute() {
		if (getComponent().getLocalNode().isPresent()) {
			logFull("executing");
			scheduleOperationTimeout(getTimeout());
			receiver = new ApplicationContact(receiverNodeID, null);
			sendMessage(buildMessage(getReceiver()));
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	
	
	@Override
	protected void retry() {
		if (getComponent().getLocalNode().isPresent()) {
			log("lookupCounter="+getNumberOfStartedLookups()+". resending message.");
			sendMessage(buildMessage(getReceiver()));
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	

	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */
	
	@Override
	public void log(String str) {
		log(str, false);
	}
	
	@Override
	public void log(String str, boolean forceLogging) {
		if (isLogging() || forceLogging)
			nodeController.log(getClassName()+" [id="+getOperationID()+"]: "+ str);
	}
	
	@Override
	public void logFull(String str) {
		logFull(str, false);
	}
	
	@Override
	public void logFull(String str, boolean forceLogging) {
		if (isLogging() || forceLogging)
			nodeController.log(this+": "+str);
	}
	
}
