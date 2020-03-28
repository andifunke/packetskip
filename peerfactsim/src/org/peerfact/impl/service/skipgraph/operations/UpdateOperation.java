package org.peerfact.impl.service.skipgraph.operations;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.messages.UpdateACKMessage;
import org.peerfact.impl.service.skipgraph.messages.UpdateMessage;
import org.peerfact.impl.service.skipgraph.messages.UpdateNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphElement;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * announces data to the skip graph.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class UpdateOperation extends
		AbstractSkipgraphServiceOperation<String> {
	

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	/** SkipgraphElements to add to the skip graph */
	private final List<SkipgraphElement> inputElements;
	 
	/** SkipgraphElements to remove from the skip graph */
	private final List<SkipgraphElement> deleteElements;
	 
	/** result string */
	private String resultStr = "";
	 
	/** success msg */
	private String resultSuccess = "Update announcement to skip graph successful";
	 
	/** failure msg */
	private String resultFailure = "Update announcement to skip graph failed.";
	
	private final long timeout = Simulator.MINUTE_UNIT;
	
	private long startTime = Simulator.getCurrentTime();
	
	private long firstConfirmationTimestamp = 0;
	
	private long lastConfirmationTimestamp = 0;
	
	private TrackerCallback tracker;
	


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public UpdateOperation(
			SkipgraphService component, 
			OperationCallback<String> callback,
			List<SkipgraphElement> inputElements, 
			List<SkipgraphElement> deleteElements) 
	{
		super(component, callback);
		this.inputElements = inputElements;
		this.deleteElements = deleteElements;

		setLogging(SkipgraphServiceConstants.logUpdate);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public List<SkipgraphElement> getInputElements() {
		return inputElements;
	}

	public List<SkipgraphElement> getDeleteElements() {
		return deleteElements;
	}

	public String getResultSuccess() {
		return resultSuccess;
	}

	public String getResultFailure() {
		return resultFailure;
	}

	@Override
	public String getResult() {
		return resultStr;
	}
	
	public ApplicationContact getLocalContact() {
		return localContact;
	}
	


	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	public void setResultSuccess(String resultSuccess) {
		this.resultSuccess = resultSuccess;
	}

	public void setResultFailure(String resultFailure) {
		this.resultFailure = resultFailure;
	}

	

	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	@Override
	protected void execute() {
		if (getComponent().getLocalNode().isPresent()) {
			scheduleOperationTimeout(timeout);
			tracker = new TrackerCallback();
			localContact = getComponent().getLocalContact();
			
			// add kind of callback for duration metric
			if (inputElements != null) {
				for (SkipgraphElement element : inputElements) {
					element.setConfirmationCallback(new ConfirmationCallback());
					element.setTrackerCallback(tracker);
				}
			}
			if (deleteElements != null) {
				for (SkipgraphElement element : deleteElements) {
					element.setConfirmationCallback(new ConfirmationCallback());
					element.setTrackerCallback(tracker);
				}
			}
			
			logFull("executing.");
			toEntryPoint();
		}
		else {
			log("not present!");
			// finish without informing the monitor
			operationFinished(false);
		}
	}
	
	
	@Override
	protected UpdateMessage buildMessage(ApplicationContact entryPoint) {
		return new UpdateMessage(
				getLocalContact(), 
				entryPoint, 
				getOperationID(), 
				new LinkedList<>(getInputElements()), 
				new LinkedList<>(getDeleteElements()));
	}

	
	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		log("send message");
		this.getComponent().getCommunicationManager().send(
				message, 

				new DefaultSkipGTransMessageCallback() {

					@Override
					public void receive(Message answerMsg, TransInfo senderInfo, int commId) {
						tracker.incrementBootstrapCount();
						// successful
						if (answerMsg instanceof UpdateACKMessage) {
							log("ACK received -> update successfully passed to Skipgraph.");
							//finishOperation(true);
							return;
						} 
						
						// unsuccessful
						if (answerMsg instanceof UpdateNACKMessage){
							log("NACK received -> no skip graph node available.");
						} 
						else if (answerMsg instanceof DefaultNACKMessage){
							log("DefaultNACK received -> no skip graph node available.");
						} 
						else {
							log("an error occurred! We received a message we are not expecting: "+answerMsg);
						}
						
						if (getNumberOfStartedLookups() < getMaxNumberOfLookups()) {
							retry();
						}
						else {
							// all retries failed
							log("lookupCounter="+getNumberOfStartedLookups()+". all retries failed. giving up.", false);
							finishOperation(false);
						}
					}
				}, 

				new DefaultSGCommunicationCallback()
			);
		this.incrementLookupCounter();
		this.tracker.incrementBootstrapCount();
	}

	
	private void evaluateConfirmationCallback() {
		if (firstConfirmationTimestamp == 0) {
			firstConfirmationTimestamp = Simulator.getCurrentTime();
		}
		lastConfirmationTimestamp = Simulator.getCurrentTime();
		if ((inputElements == null || inputElements.isEmpty()) && (deleteElements == null || deleteElements.isEmpty()))
			finishOperation(true);
	}
	
		
	@Override
	protected void operationTimeoutOccured() {
		log("timeout occured @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()), true);
		log("remaining inputElement: "+inputElements+"\nremaining deleteElement: "+deleteElements, true);
		finishOperation(false);
	}

	
	@Override
	protected void finishOperation(boolean success) {
		operationFinished(success);
		log("finished operation successfully? "+success);
		log("number of bootstrap lookups="+bootstrapMessages
				+", number of entryPoint lookups="+getNumberOfStartedLookups());
		
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			((SkipgraphMonitor)Simulator.getMonitor())
					.bootstrapAttempts(localContact, bootstrapAttempts);
			if (!success) {
				((SkipgraphMonitor)Simulator.getMonitor())
						.addUpdateTimeout(localContact);
			}
			if (success) {
				log("duration="+Simulator.getFormattedTime(Simulator.getCurrentTime()-startTime));
				((SkipgraphMonitor)Simulator.getMonitor())
						.addUpdateDurationFirst(localContact, firstConfirmationTimestamp-startTime);
				((SkipgraphMonitor)Simulator.getMonitor())
						.addUpdateDuration(localContact, lastConfirmationTimestamp-startTime);
				((SkipgraphMonitor)Simulator.getMonitor())
						.addUpdateDurationTimespan(localContact, 
								lastConfirmationTimestamp-firstConfirmationTimestamp);
				Set<BigInteger> distinctHops = new LinkedHashSet<>(hops);
				Set<BigInteger> distinctDeleteHops = new LinkedHashSet<>(deletehops);
				((SkipgraphMonitor)Simulator.getMonitor())
						.addUpdateInputHopCount(localContact, hops.size(), distinctHops.size());
				((SkipgraphMonitor)Simulator.getMonitor())
						.addUpdateDeleteHopCount(localContact, deletehops.size(), distinctDeleteHops.size());
			}
		}
	}
	
		
	@Override
	public String toString() {
		return super.toString() 
				+ "\ninputElements=" + inputElements + " (size="+inputElements.size()+")"
				+ "\ndeleteElements=" + deleteElements + " (size="+deleteElements.size()+")"
				;
	}



	/* ************************************************
	 ***************** CALLBACKS **********************
	 ************************************************ */

	public class ConfirmationCallback {

		public void reportInput(SkipgraphElement element) {
			if (inputElements.remove(element)) {
				log(element +" input confirmed @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()), false);
				evaluateConfirmationCallback();
			}
			else {
				log(element +" could not be confirmed from inputList @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()), true);
			}
		}

		public void reportDelete(SkipgraphElement element) {
			if (deleteElements.remove(element)) {
				log(element +" deletion confirmed @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()), false);
				evaluateConfirmationCallback();
			}
			else {
				log(element +" could not be confirmed from deleteList @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()), true);
			}
		}
		
	}
	
}
