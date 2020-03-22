package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyNACKMessage;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinLevelReplyOperation extends AbstractSkipgraphNodeOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final int requestOperationID;
	
	private final SkipgraphContact replyContact;
	
	/** the contact of the node that was replaced in the contactTable of the final receiver
	 * with the contact of the joining node. The joining node (i.e. the initiator of the operation)
	 * should then handshake with this formerContact to finish the JoinLevel operation. 
	 */
	private final SkipgraphContact formerContact;
	
	private final int levelIndex;
	
	private final int prefix;
	
	private final Route backRoute;
	
	private boolean result;
	
	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public JoinLevelReplyOperation(
			SkipgraphNodeController node,
			OperationCallback<Boolean> callback,
			BigInteger receiverNodeID,
			ApplicationContact requesterPeer,
			int requestOperationID,
			SkipgraphContact replyContact,
			int levelIndex,
			int prefix,
			Route backRoute,
			SkipgraphContact formerContact) 
	{
		super(node, callback, receiverNodeID);
		setReceiver(requesterPeer);
		this.requestOperationID = requestOperationID;
		this.replyContact = replyContact;
		this.levelIndex = levelIndex;
		this.prefix = prefix;
		this.backRoute = backRoute;
		this.formerContact = formerContact;
		
		setTimeout(10*Simulator.SECOND_UNIT);
		setLogging(SkipgraphServiceConstants.logJoin);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Boolean getResult() {
		return result;
	}

	

	/* ************************************************
	 ******** AbstractSkipgraphNodeOperation **********
	 ************************************************ */
	
	@Override
	protected void execute() {
		if (getComponent().getLocalNode().isPresent()) {
			scheduleOperationTimeout(getTimeout());
			logFull("executing");
			getNodeController().getOperationResponder()
					.getJoinLevelBusyMap().put(levelIndex, getReceiverNodeID());
			log("adding level="+levelIndex+ " to busyMap="+getNodeController()
					.getOperationResponder().getJoinLevelBusyMap());
			sendMessage(buildMessage(getReceiver()));
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	
	@Override
	protected JoinLevelReplyMessage buildMessage(ApplicationContact receiver) {
		return new JoinLevelReplyMessage(
				getComponent().getLocalContact(),
				receiver, 
				requestOperationID,
				getReceiverNodeID(),
				replyContact, 
				levelIndex, 
				prefix, 
				backRoute, 
				formerContact);
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
						if(answerMsg instanceof JoinLevelReplyACKMessage) {
							log("ACK received -> succeeded.");
							finishOperation(result = true);
							return;
						} 

						// unsuccessful - no retry
						if (answerMsg instanceof JoinLevelReplyNACKMessage) {
							if (((JoinLevelReplyNACKMessage) answerMsg).isSenderDisabled()) {
								log("NACK received -> failed! receiving node disabled.");
								finishOperation(false);
								return;
							}
							else {
								log("NACK received -> failed! probably due to operation timeout.");
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
	public String toString() {
		return super.toString() 
				+ " requestOperationID="+ requestOperationID 
				+ ", replyContact=" + replyContact
				+ ", formerContact=" + formerContact 
				+ ", levelIndex="+ levelIndex 
				+ ", prefix=" + prefix 
				+ ", backRoute=" + backRoute
				;
	}
	

	@Override
	protected void finishOperation(boolean success) {
		if (getNodeController().getOperationResponder()
					.getJoinLevelBusyMap().remove(levelIndex, getReceiverNodeID())) {
			logFull("removing operation from busyMap="
					+ getNodeController().getOperationResponder().getJoinLevelBusyMap());
		}
		else {
			logFull("could not remove operation from busyMap="
					+ getNodeController().getOperationResponder().getJoinLevelBusyMap(), true);
		}
		operationFinished(success);
	}

}
