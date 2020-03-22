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
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelNotifyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestACKMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestNACKMessage;
import org.peerfact.impl.service.skipgraph.operations.Addressable;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinLevelRequestOperation 
		extends AbstractSkipgraphNodeOperation<SkipgraphContact[]> 
		implements Addressable {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final ApplicationContact requesterPeer;

	private final int requestOperationID;
	
	/** the contact of the node that initiated the operation */
	private final SkipgraphContact requesterContact;
	
	private final int levelIndex;
	
	private final int prefix;
	
	private final Route route;
	
	private final boolean forwarding;
	
	/** the contatc of the node that finally accepted the operations request.
	 * not necessarily the receiver of the first message since the message could have
	 * been past to susequent skipgraph nodes.
	 */
	private SkipgraphContact replyContact;
	
	/** the contact of the node that was replaced in the contactTable of the final receiver
	 * with the contact of the joining node. The joining node (i.e. the initiator of the operation)
	 * should then handshake with this formerContact to finish the JoinLevel operation. 
	 */
	private SkipgraphContact formerContact;
	
	private SkipgraphContact[] result = new SkipgraphContact[2];
	
	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public JoinLevelRequestOperation(
			SkipgraphNodeController node,
			OperationCallback<SkipgraphContact[]> callback,
			BigInteger receiverNodeID,
			ApplicationContact requesterPeer,
			int requestOperationID,
			SkipgraphContact requesterContact,
			int levelIndex,
			int prefix,
			Route route,
			boolean forwarding) 
	{
		super(node, callback, receiverNodeID);
		this.requesterPeer = requesterPeer;
		this.requestOperationID = requestOperationID;
		this.requesterContact = requesterContact;
		this.levelIndex = levelIndex;
		this.prefix = prefix;
		this.route = route;
		this.forwarding = forwarding;
		setReceiver(new ApplicationContact(getReceiverNodeID(), null));

		setTimeout(15*Simulator.SECOND_UNIT);
		setMaxNumberOfLookups(5);
		setLogging(SkipgraphServiceConstants.logJoin);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	/**
	 * @return  array of size 2.
	 * 			result[0] = contact of the node that accepted the JoinLevel request.
	 * 			result[1] = contact of the node the initiator should address for a handshake.
	 */
	@Override
	public SkipgraphContact[] getResult() {
		result[0] = replyContact;
		result[1] = formerContact;
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
//			log("adding level="+levelIndex+ " to busyMap="+getNodeController()
//					.getOperationResponder().getJoinLevelBusyMap());
//			getNodeController().getOperationResponder()
//					.getJoinLevelBusyMap().put(levelIndex, getReceiverNodeID());
			sendMessage(buildMessage(getReceiver()));
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	
	
	@Override
	public boolean deliverMessage(AbstractSkipgraphMessage message) {
		if (message instanceof JoinLevelNotifyMessage) {
			log("notification received: "+message);
			return handleNotification((JoinLevelNotifyMessage)message);
		}
		else if (message instanceof JoinLevelReplyMessage) {
			log("reply received: "+message);
			return handleReply((JoinLevelReplyMessage)message);
		}

		log("received a message we can not understand "+message);
		return false;
	}
	
	
	@Override
	protected JoinLevelRequestMessage buildMessage(ApplicationContact receiver) {
		return new JoinLevelRequestMessage(
				getComponent().getLocalContact(),
				receiver, 
				(forwarding ? requestOperationID : getOperationID()),
				requesterPeer,
				requesterContact, 
				levelIndex, 
				prefix, 
				route);
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
						if(answerMsg instanceof JoinLevelRequestACKMessage) {
							if (forwarding) {
								result = null;
								log("ACK received. forwarding succeeded.");
								finishOperation(true);
							}
							else {
								log("ACK received. still waiting for final reply");
							}
							return;
						} 

						// unsuccessful
						if (answerMsg instanceof JoinLevelRequestNACKMessage) {
							if (((JoinLevelRequestNACKMessage) answerMsg).isSenderDisabled()) {
								log("NACK received -> failed! receiving node disabled.");
								finishOperation(false);
								return;
							}
							else if (((JoinLevelRequestNACKMessage) answerMsg).isSenderBusy()) {
								log("NACK received -> receiver is busy. retry with delay.");
								scheduleWithDelay(100*Simulator.MILLISECOND_UNIT);
								return;
							}
							else {
								log("NACK received -> failed! not sure what's going on.", true);
								finishOperation(false);
								return;
							}
						} 
						if (answerMsg instanceof DefaultNACKMessage) {
							log("DefaultNACK received -> failed! no skip graph node available.", true);
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
						log("lookupCounter="+getNumberOfStartedLookups()+". all retries failed. giving up.", true);
						finishOperation(false);
					}
				},

				new DefaultSGCommunicationCallback() {
					
					@Override
					public void gotCommID(AbstractAppMessage msg, int comId) {
						super.gotCommID(msg, comId);
						//System.out.println(forwarding ? " (forwarding)" : "");
					}
				}
			);
		this.incrementLookupCounter();
	}

	

	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
		
	private boolean handleNotification(JoinLevelNotifyMessage messsage) {
		log("received a forwarding notification. Still waiting for final reply.");
		return true;
	}
	
	
	private boolean handleReply(JoinLevelReplyMessage message) {
		if (message.getLevelIndex() == levelIndex && message.getPrefix() == prefix &&
				message.getRoute() == SGUtil.switchRoute(route)) {
			replyContact = message.getReplyContact();
			formerContact = message.getFormerContact();
			log("final reply received. JoinLevelRequest successfull.");
			operationFinished(true);
			return true;
		}

		log("final reply doesn't match the request. JoinLevelRequest aborted.");
		return false;
	}
	
	
	@Override
	protected void finishOperation(boolean success) {
//		if (getNodeController().getOperationResponder()
//				.getJoinLevelBusyMap().remove(levelIndex, getReceiverNodeID())) {
//			logFull("removing operation from busyMap="
//					+ getNodeController().getOperationResponder().getJoinLevelBusyMap());
//		}
//		else {
//			logFull("could not remove operation from busyMap="
//					+ getNodeController().getOperationResponder().getJoinLevelBusyMap(), true);
//		}
		operationFinished(success);
	}
	
	

	@Override
	public String toString() {
		return super.toString()
				+ " requestOperationID="+ requestOperationID
				+ ", requesterContact=" + requesterContact
				+ ", levelIndex=" + levelIndex 
				+ ", prefix=" + prefix
				+ ", route=" + route 
				+ ", forwarding=" + forwarding
				+ ", replyContact=" + replyContact 
				+ ", formerContact="+ formerContact;
	}

}
