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
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeNACKMessage;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class HandshakeOperation extends AbstractSkipgraphNodeOperation<SkipgraphContact> {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private SkipgraphContact replyContact;

	private int levelIndex;

	private int prefix;

	private SGUtil.Route route;


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public HandshakeOperation(
			SkipgraphNodeController node,
			OperationCallback<SkipgraphContact> callback,
			BigInteger nodeID,
			int levelIndex,
			int prefix,
			SGUtil.Route route) {
		super(node, callback, nodeID);
		this.levelIndex = levelIndex;
		this.prefix = prefix;
		this.route = route;
		
		setTimeout(15*Simulator.SECOND_UNIT);
		setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance);
	}



	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public SkipgraphContact getResult() {
		return replyContact;
	}



	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	public void setReplyContact(SkipgraphContact replyContact) {
		this.replyContact = replyContact;
	}



	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */

	@Override
	protected HandshakeMessage buildMessage(ApplicationContact receiver) {
		return new HandshakeMessage(
				getComponent().getLocalContact(),
				receiver,
				getOperationID(), 
				getNodeController().getContact(),
				levelIndex,
				prefix,
				route);
	}



	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		getNodeController().incrementOutgoingMessages();
		
		this.getComponent().getCommunicationManager().send(
				message, 

				new DefaultSkipGTransMessageCallback() {

					@Override
					public void receive(Message answerMsg, TransInfo senderInfo, int commId) {
						getNodeController().incrementIncomingMessages();
						
						// successful
						if (answerMsg instanceof HandshakeACKMessage) {
							HandshakeACKMessage answer = (HandshakeACKMessage)answerMsg;
							if (answer.getLevelIndex() == levelIndex &&
									answer.getPrefix() == prefix &&
									answer.getRoute() == SGUtil.switchRoute(route)) {
								setReplyContact(((HandshakeACKMessage) answerMsg).getReplyContact());
								log("ACK received -> succeeded.");
								finishOperation(true);
							}
							else {
								log("ACK received -> failed: ACK doesn't match handshake Request. "+ answerMsg);
								finishOperation(false);
							}
							return;
						} 
						
						// unsuccessful - no retry
						if (answerMsg instanceof HandshakeNACKMessage) {
							if (((HandshakeNACKMessage) answerMsg).isSenderDisabled()) {
								log("NACK received -> failed! receiving node disabled.");
								finishOperation(false);
								return;
							}
							else if (((HandshakeNACKMessage) answerMsg).isSenderBusy()) {
								log("NACK received -> sender is busy. retry with delay.");
								scheduleWithDelay(100*Simulator.MILLISECOND_UNIT);
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
	}


	@Override
	public String toString() {
		return super.toString()
				+ " replyContact=" + replyContact
				+ ", levelIndex=" + levelIndex 
				+ ", prefix=" + prefix
				+ ", route=" + route;
	}

}
