package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingNACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingSerialMessage;
import org.peerfact.impl.service.skipgraph.operations.AbstractSkipgraphServiceOperation.TrackerCallback;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchForwardingSerialOperation extends AbstractSkipgraphNodeOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final ApplicationContact requesterContact;
	
	private final int requestOperationID;
	
	private final SearchQuery query;

	private final int messageCounter;
	
	private final TrackerCallback tracker;
	
	private Boolean result;
	

	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public SearchForwardingSerialOperation(
			SkipgraphNodeController node,
			OperationCallback<Boolean> callback,
			BigInteger receicerNodeID,
			ApplicationContact requesterContact,
			int requestOperationID,
			SearchQuery query,
			int messageCounter,
			TrackerCallback tracker) 
	{
		super(node, callback, receicerNodeID);
		this.requesterContact = requesterContact;
		this.requestOperationID = requestOperationID;
		this.query = query;
		this.messageCounter = messageCounter;
		this.tracker = tracker;
		
		setLogging(SkipgraphServiceConstants.logSearch);
		setTimeout(15*Simulator.SECOND_UNIT);
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
	protected SearchForwardingSerialMessage buildMessage(ApplicationContact receiver) {
		return new SearchForwardingSerialMessage(
				getComponent().getLocalContact(),
				receiver, 
				getOperationID(), 
				requesterContact,
				requestOperationID,
				query,
				messageCounter,
				tracker);
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
						tracker.incrementN_ACKCount();

						// successful
						if(answerMsg instanceof SearchForwardingACKMessage) {
							log("ACK received -> succeeded - search has been forwarded");
							finishOperation(result = true);
							return;
						} 

						// unsuccessful - no retry
						if (answerMsg instanceof SearchForwardingNACKMessage) {
							if (((SearchForwardingNACKMessage) answerMsg).isSenderDisabled()) {
								log("NACK received -> failed! receiving node disabled.");
								finishOperation(false);
								return;
							}
							else {
								log("NACK received -> failed! query not valid.");
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
		tracker.incrementForwardingCount();
	}

}
