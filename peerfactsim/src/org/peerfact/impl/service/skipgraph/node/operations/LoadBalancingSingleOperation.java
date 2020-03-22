package org.peerfact.impl.service.skipgraph.node.operations;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.AbstractSkipgraphNodeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingMessage;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingNACKMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class LoadBalancingSingleOperation extends AbstractSkipgraphNodeOperation<SkipgraphContact> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final LoadBalancingMessage message;
	
	private SkipgraphContact result;


	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public LoadBalancingSingleOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<SkipgraphContact> callback,
			LoadBalancingMessage message) 
	{
		super(nodeController, callback, null);
		this.message = message;

		setTimeout(5*Simulator.SECOND_UNIT);
		setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance ||
				SkipgraphServiceConstants.logLeave);
		//setLogging(true);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public SkipgraphContact getResult() {
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
		sendMessage(message);
	}

	
	@Override
	protected AbstractSkipgraphNodeMessage buildMessage(ApplicationContact receiver) {
		return message;
	}


	@Override
	protected void sendMessage(AbstractSkipgraphMessage m) {
		getNodeController().incrementOutgoingMessages();
		
		getComponent().getCommunicationManager().send(
				message,

				new DefaultSkipGTransMessageCallback() {

					@Override
					public void receive(Message answerMsg, TransInfo senderInfo, int commId) {
						getNodeController().incrementIncomingMessages();
						
						// successful
						if(answerMsg instanceof LoadBalancingACKMessage) {
							log("ACK received -> succeeded.");
							result = ((LoadBalancingACKMessage)answerMsg).getReplyContact();
							finishOperation(true);
							return;
						}
						
						// unsuccessful - no retry
						if (answerMsg instanceof LoadBalancingNACKMessage) {
							if (((LoadBalancingNACKMessage) answerMsg).isSenderDisabled()) {
								log("NACK received -> failed! receiving node disabled.");
								finishOperation(false);
								return;
							}
							else {
								log("NACK received -> failed! extension was rejected.");
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
	}
	
	
	@Override
	protected void retry() {
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(false);
		}

		log("lookupCounter="+getNumberOfStartedLookups()+". resending message.");
		sendMessage(message);
	}
	
}
