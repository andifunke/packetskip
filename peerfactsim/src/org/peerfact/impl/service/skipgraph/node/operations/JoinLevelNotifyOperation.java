package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;

import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelNotifyMessage;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class JoinLevelNotifyOperation extends AbstractSkipgraphNodeOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final int requestOperationID;
	
	private final BigInteger forwardedToNodeID;
	
	private final boolean result = true;
	
	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public JoinLevelNotifyOperation(
			SkipgraphNodeController node,
			BigInteger receiverNodeID,
			ApplicationContact requesterPeer,
			int requestOperationID,
			BigInteger forwardedToNodeID) 
	{
		super(node, null, receiverNodeID);
		setReceiver(requesterPeer);
		this.requestOperationID = requestOperationID;
		this.forwardedToNodeID = forwardedToNodeID;
		
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
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	
	@Override
	protected void execute() {
		if (getComponent().getLocalNode().isPresent()) {
			logFull("executing");
			scheduleOperationTimeout(getTimeout());
			sendMessage(buildMessage(getReceiver()));
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}
	
	
	@Override
	protected JoinLevelNotifyMessage buildMessage(ApplicationContact receiver) {
		return new JoinLevelNotifyMessage(
				getComponent().getLocalContact(),
				receiver,
				requestOperationID,
				getReceiverNodeID(),
				forwardedToNodeID);
	}


	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		getNodeController().incrementOutgoingMessages();
		getComponent().getCommunicationManager().send(
				message,
				null,
				null
			);
		operationFinished(true);
	}
	
	
	@Override
	public String toString() {
		return super.toString()
				+ " requestOperationID="+requestOperationID
				+ ", forwardedToNode="+SGUtil.formatID(forwardedToNodeID);
	}

}
