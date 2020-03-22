package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestACKMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestNACKMessage;
import org.peerfact.impl.service.skipgraph.node.ElementTable;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinRequestOperation extends AbstractSkipgraphNodeOperation<SkipgraphContact> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private ElementTable thisNodesElementTable;
	
	private ElementTable joiningNodesElementTable;
	
	private SkipgraphContact prevContact;
	
	private SkipgraphContact nextContact;
	
	private SkipgraphContact joiningContact;
	
	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */
	
	public JoinRequestOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<SkipgraphContact> callback) {
		super(nodeController, callback, null);
		
		setTimeout(30*Simulator.SECOND_UNIT);
		setLogging(SkipgraphServiceConstants.logJoin || getNodeController().enforcesLogging());
		setMaxNumberOfLookups(5);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public SkipgraphContact getResult() {
		return joiningContact;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
		
	@Override
	protected void execute() {
		if (getComponent().getLocalNode().isPresent()) {
			log("\n\n>>> SPLIT <<<\n");
			if (isLogging())
				getNodeController().print();

			scheduleOperationTimeout(getTimeout());
			setDataForMessage();
			toRandomReceiver();
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	
	@Override
	protected void operationTimeoutOccured() {
		log("join operation timeout occured", true);
		finishOperation(false);
	}

	
	
	private void toRandomReceiver() {
		BigInteger randomReceiverID = (BigInteger)getComponent().getLocalNode().getRandomOverlayKey().getUniqueValue();
		ApplicationContact receiver = new ApplicationContact(randomReceiverID, null);
		sendMessage(buildMessage(receiver));
		
	}

	
	private void setDataForMessage() {
		getNodeController().lock();
		ElementTable[] et = getNodeController().getElementTable().split();
		this.thisNodesElementTable = et[0];
		this.joiningNodesElementTable = et[1];
		this.prevContact = new SkipgraphContact(
				getNodeController().getNodeID(),
				thisNodesElementTable.getRangeStart(),
				thisNodesElementTable.getRangeEnd(),
				getNodeController().getContactTable().getTablePrefix());
		this.nextContact = getNodeController().getContactTable().getNext();
	}


	@Override
	protected JoinRequestMessage buildMessage(ApplicationContact receiver) {
		return new JoinRequestMessage(
				getComponent().getLocalContact(),
				receiver, 
				getOperationID(),
				receiver.getPeerID(),
				prevContact, 
				nextContact, 
				joiningNodesElementTable);
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
						if (answerMsg instanceof JoinRequestACKMessage) {
							joiningContact = ((JoinRequestACKMessage) answerMsg).getJoiningContact();
							getNodeController().setNextOnLevel(joiningContact, 0);
							getNodeController().unlockWithNewElementTable(thisNodesElementTable, null, null);
							// TODO: send final ACK
							log("ACK received: join succeeded.");
							finishOperation(true);
							return;
						}
						
						// unsuccessful
						if (answerMsg instanceof JoinRequestNACKMessage) {
							log("NACK received: join failed.");
						} 
						else {
							log("An error occurred! We received a message we are not expecting:\n"+answerMsg);
						}
						
						// retry?
						if (getNumberOfStartedLookups() < getMaxNumberOfLookups()) {
							retry();
						}
						else {
							// all retries failed
							log("lookupCounter="+getNumberOfStartedLookups()+". all retries failed. giving up.");
							finishOperation(false);
						}
					}
				},
				
				new DefaultSGCommunicationCallback()
			);
		
		this.incrementLookupCounter();
	}
	
	
	@Override
	protected void retry() {
		if (getComponent().getLocalNode().isPresent()) {
			log("join retry! lookupCounter="+getNumberOfStartedLookups()+". resending message to new random receiver.");
			toRandomReceiver();
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	
	
}
