package org.peerfact.impl.service.skipgraph.operations;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultParallelMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchResultParallelOperation extends AbstractSkipgraphServiceOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final ApplicationContact requester;
	
	private final int requestOperationID;
	
	private final Map<String, Set<ApplicationContact>> results;
	
	private final Set<BigInteger> messageHopTrackerSet;
	
	private final BigInteger senderNodeID;
	
	private boolean result;
	
	private final long timeout = 30*Simulator.SECOND_UNIT;
	
	private final TrackerCallback tracker;
	
	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchResultParallelOperation(
			SkipgraphService component,
			OperationCallback<Boolean> callback,
			ApplicationContact requester,
			int requestOperationID,
			Map<String, Set<ApplicationContact>> results,
			Set<BigInteger> messageHopTrackerSet,
			BigInteger senderNodeID,
			TrackerCallback tracker) 
	{
		super(component, callback);
		this.requester = requester;
		this.requestOperationID = requestOperationID;
		this.results = results;
		this.messageHopTrackerSet = messageHopTrackerSet;
		this.senderNodeID = senderNodeID;
		this.tracker = tracker;
		
		setLogging(SkipgraphServiceConstants.logSearch);
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
			scheduleOperationTimeout(timeout);
			localContact = getComponent().getLocalContact();
			sendMessage(buildMessage(null));
		}
		else {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
	}


	@Override
	protected SearchResultParallelMessage buildMessage(ApplicationContact entryPoint) {
		return new SearchResultParallelMessage(
				localContact,
				requester,
				requestOperationID,
				results,
				messageHopTrackerSet,
				senderNodeID);
	}


	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		tracker.incrementReplyCount();
		getComponent().getCommunicationManager().send(
				message,
				null,
				null);
		log("sending: "+message);
		finishOperation(result = true);
	}

}
