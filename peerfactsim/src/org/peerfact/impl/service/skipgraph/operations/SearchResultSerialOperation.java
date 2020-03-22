package org.peerfact.impl.service.skipgraph.operations;

import java.util.Set;

import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultSerialMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchResultSerialOperation extends AbstractSkipgraphServiceOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final ApplicationContact requester;
	
	private final int requestOperationID;
	
	private final String dimension;
	
	private final Set<ApplicationContact> results;
	
	private final int messageCounter;

	private final boolean last;

	private boolean result;
	
	private final long timeout = 30*Simulator.SECOND_UNIT;
	
	private final TrackerCallback tracker;
	
	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public SearchResultSerialOperation(
			SkipgraphService component,
			OperationCallback<Boolean> callback,
			ApplicationContact requester,
			int requestOperationID,
			String dimension,
			Set<ApplicationContact> results,
			int messageCounter,
			boolean last,
			TrackerCallback tracker) 
	{
		super(component, callback);
		this.requester = requester;
		this.requestOperationID = requestOperationID;
		this.dimension = dimension;
		this.results = results;
		this.messageCounter = messageCounter;
		this.last = last;
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
	protected SearchResultSerialMessage buildMessage(ApplicationContact entryPoint) {
		return new SearchResultSerialMessage(
				localContact,
				requester,
				requestOperationID,
				dimension,
				results,
				messageCounter,
				last);
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
