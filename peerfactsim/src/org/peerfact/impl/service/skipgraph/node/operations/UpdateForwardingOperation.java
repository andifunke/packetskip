package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;
import java.util.List;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingMessage;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingNACKMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class UpdateForwardingOperation extends AbstractSkipgraphNodeOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final int updateOperationID;
	
	private final List<SkipgraphElement> inputList;
	
	private final List<SkipgraphElement> deleteList;
	
	private Boolean result;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public UpdateForwardingOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<Boolean> callback,
			BigInteger receicerNodeID,
			int updateOperationID,
			List<SkipgraphElement> inputList,
			List<SkipgraphElement> deleteList) 
	{
		super(nodeController, callback, receicerNodeID);
		this.updateOperationID = updateOperationID;
		this.inputList = inputList;
		this.deleteList = deleteList;

		setLogging(SkipgraphServiceConstants.logUpdate);
		setTimeout(10*Simulator.SECOND_UNIT);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Boolean getResult() {
		return result;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	@Override
	protected UpdateForwardingMessage buildMessage(ApplicationContact receiver) {
		return new UpdateForwardingMessage(
				getComponent().getLocalContact(),
				receiver, 
				updateOperationID < 1 ? getOperationID() : updateOperationID,
				inputList,
				deleteList);
	}



	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		getComponent().getCommunicationManager().send(
				message, 

				new DefaultSkipGTransMessageCallback() {

					@Override
					public void receive(Message answerMsg, TransInfo senderInfo, int commId) {
						getNodeController().incrementIncomingMessages();
						
						// successful
						if(answerMsg instanceof UpdateForwardingACKMessage) {
							log("ACK received -> succeeded: data forwarded to the receiving skip graph node");
							finishOperation(result = true);
							return;
						} 
						
						// unsuccessful - no retry
						if (answerMsg instanceof UpdateForwardingNACKMessage) {
							if (((UpdateForwardingNACKMessage) answerMsg).isSenderDisabled()) {
								log("NACK received -> failed! receiving node disabled.");
								finishOperation(false);
								return;
							}
							else {
								log("NACK received -> failed! not sure what's going on.");
								finishOperation(false);
								return;
							}
						} 
						if (answerMsg instanceof DefaultNACKMessage) {
							log("DefaultNACK received -> failed! no skip graph node available.");
							finishOperation(false);
							return;
						} 

						// unsuccessful - retry?
						log("an error occurred! We received a message we are not expecting:\n"+answerMsg);
						if (getNumberOfStartedLookups() < getMaxNumberOfLookups()) {
							retry();
							return;
						}

						// all retries failed
						log("lookupCounter="+getNumberOfStartedLookups()+". all retries failed. giving up.");
						finishOperation(false);
					}
				}, 
				
				new DefaultSGCommunicationCallback()
			);
		this.incrementLookupCounter();
		getNodeController().incrementOutgoingMessages();
	}

}
