package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingMessage;
import org.peerfact.impl.service.skipgraph.operations.Addressable;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class LoadBalancingResponseOperation extends AbstractSkipgraphNodeOperation<Boolean>
		implements Addressable {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final BigInteger requesterNodeID;
	
	private final int requestOperationID;
	
	private boolean result;
	 
	

	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public LoadBalancingResponseOperation(
			SkipgraphNodeController node,
			OperationCallback<Boolean> callback,
			BigInteger requesterNodeID,
			int requestOperationID) 
	{
		super(node, callback, null);
		this.requesterNodeID = requesterNodeID;
		this.requestOperationID = requestOperationID;

		setTimeout(15*Simulator.SECOND_UNIT);
		setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance ||
				SkipgraphServiceConstants.logLeave);
		//if (getNodeController().getGlobalCountID() == 27) setLogging(true);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Boolean getResult() {
		return result;
	}

	

	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */



	
	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	@Override
	public void execute() {
		if (getComponent().getLocalNode().isPresent() && !getNodeController().isDisabled()) {
			logFull("awaiting element table extension");
			scheduleOperationTimeout(getTimeout());
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}

	@Override
	protected AbstractSkipgraphMessage buildMessage(ApplicationContact receiver) {
		return null;
	}

	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		//
	}
	
	@Override
	protected void retry() {
		//
	}


	@Override
	public boolean deliverMessage(AbstractSkipgraphMessage message_arg) {
		log("delivering "+message_arg);
		
		if (!(message_arg instanceof LoadBalancingMessage)) {
			log("message has wrong type.");
			return false;
		}
		LoadBalancingMessage message = (LoadBalancingMessage)message_arg;
		if (message.getOperationID() != requestOperationID) {
			log("operationID does not match");
			return false;
		}
		if (!message.getSenderNodeID().equals(requesterNodeID)) {
			log("not expecting a message from this node.");
			return false;
		}
		if (message.getElementTable() == null) {
			logFull("NACK received. Aborting opertion");
			result = false;
			finishOperation(true);
			return false;
		}
		
		log("message accepted.");
		if (getNodeController().extend(message.getElementTable())) {
			log("extension accepted.");
			// send range updates only if the extension worked
			getNodeController().getOperationCaller().callRangeAdjustmentOperation();
			finishOperation(result = true);
			return true;
		}		

		log("element table rejects extension.");
		finishOperation(result = false);
		return false;
	}
	
	
	@Override
	protected void operationTimeoutOccured() {
		logFull("operation timeout occured.", true);
		finishOperation(result = false);
	}
	
	
	@Override
	protected void finishOperation(boolean success) {
		if (success) 
			logFull("operation succeeded.");
		else
			logFull("no table extension received. operation failed.");
		operationFinished(success);
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ ", requesterNodeID="+SGUtil.formatID(requesterNodeID)
				+", requestOperationID="+requestOperationID
				;
	}

}
