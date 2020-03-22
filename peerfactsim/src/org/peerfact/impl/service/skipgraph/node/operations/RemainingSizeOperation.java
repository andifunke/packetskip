package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeNACKMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class RemainingSizeOperation extends AbstractSkipgraphNodeOperation<Integer> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final BigInteger receiverID;
	
	private final int initialOpID;

	private Integer result;
	

	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public RemainingSizeOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<Integer> callback,
			BigInteger receiverID,
			int initialOpID) 
	{
		super(nodeController, callback, null);
		this.receiverID = receiverID;
		this.initialOpID = initialOpID;

		setTimeout(10*Simulator.SECOND_UNIT);
		//setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance ||
		//		SkipgraphServiceConstants.logLeave);
		//setLogging(true);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Integer getResult() {
		return result;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
		
	@Override
	protected void execute() {
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(false);
		}

		scheduleOperationTimeout(getTimeout());
		ApplicationContact receiver = new ApplicationContact(receiverID, null);
		sendMessage(buildMessage(receiver));
	}

	
	@Override
	protected RemainingSizeMessage buildMessage(ApplicationContact receiver) {
		return new RemainingSizeMessage(
				getComponent().getLocalContact(),
				receiver, 
				initialOpID,
				getNodeController().getNodeID());
	}


	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		getNodeController().incrementOutgoingMessages();
		
		getComponent().getCommunicationManager().send(
				message,

				new DefaultSkipGTransMessageCallback() {

					@Override
					public void receive(Message answerMsg, TransInfo senderInfo, int commId) {
						getNodeController().incrementIncomingMessages();
						
						// successful
						if(answerMsg instanceof RemainingSizeACKMessage) {
							RemainingSizeACKMessage rsMsg = (RemainingSizeACKMessage)answerMsg;
							logFull("ACK received -> "+rsMsg);
							result = rsMsg.getRemainingSize();
							finishOperation(true);
							return;
						} 

						// unsuccessful - no retry
						if (answerMsg instanceof RemainingSizeNACKMessage) {
							if (((RemainingSizeNACKMessage) answerMsg).isSenderDisabled()) {
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
							log("DefaultNACK received -> skip graph node unavailable");
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
	}
	
	
	@Override
	protected void retry() {
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(false);
		}

		log("lookupCounter="+getNumberOfStartedLookups()+". resending message.");
		ApplicationContact receiver = new ApplicationContact(receiverID, null);
		sendMessage(buildMessage(receiver));
	}
	
}
