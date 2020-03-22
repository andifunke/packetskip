package org.peerfact.impl.service.skipgraph.operations;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.api.transport.TransMessageCallback;
import org.peerfact.impl.common.AbstractOperation;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;
import org.peerfact.impl.util.communicationmanager.CommunicationManager.CommunicationCallback;

/**
 * Common asbtract superclass of all skipgraph operations.
 *
 * @param <S> the type of the operation result
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphOperation<S> 
		extends AbstractOperation<SkipgraphService, S> {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private boolean logging = false;
	
	private int maxNumberOfLookups = 5;
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	protected AbstractSkipgraphOperation(SkipgraphService component, OperationCallback<S> callback) {
		super(component, callback);
	}
	
	/* *************************************************
	 ******************** GETTERS **********************
	 ***************************************************/

	protected int getMaxNumberOfLookups() {
		return maxNumberOfLookups;
	}

	/* *************************************************
	 ******************** SETTERS **********************
	 ***************************************************/

	protected void setMaxNumberOfLookups(int maxNumberOfLookups) {
		this.maxNumberOfLookups = maxNumberOfLookups;
	}


	/* ************************************************
	 ******************* METHODS **********************
	 ************************************************ */
	
	protected abstract void sendMessage(AbstractSkipgraphMessage message);
	
	protected abstract AbstractSkipgraphMessage buildMessage(ApplicationContact receiver);
	
	protected abstract void retry();
	
	protected void finishOperation(boolean success) {
		operationFinished(success);
	}
	
	@Override
	protected void operationTimeoutOccured() {
		log("timeout occured", true);
		finishOperation(false);
	}

	
	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */
	
	public boolean isLogging() {
		return logging;
	}
	
	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	public void log(String str) {
		log(str, false);
	}
	
	public void log(String str, boolean forceLogging) {
		if (logging || forceLogging)
			getComponent().log(getClassName()+" [id="+getOperationID()+"]: "+ str
					+" @ "+Simulator.getFormattedTime(Simulator.getCurrentTime())
					+ " ("+Simulator.getSeed()+")");
	}
	
	public void logFull(String str) {
		logFull(str, false);
	}
	
	public void logFull(String str, boolean forceLogging) {
		if (logging || forceLogging)
			getComponent().log(this+": "+ str);
	}
	
	public String getClassName() {
		return getClass().getSimpleName();
	}
	
	

	/* ************************************************
	 ****************** CLASSES ***********************
	 ************************************************ */
	
	public class DefaultSGCommunicationCallback implements CommunicationCallback {

		@Override
		public void gotCommID(AbstractAppMessage msg, int comId) {
			// the given message has been sent
			log("successfully sent:\n"+msg);
		}
		
	}
	
	
	
	public abstract class DefaultSkipGTransMessageCallback implements TransMessageCallback {

		@Override
		public abstract void receive(Message msg, TransInfo senderInfo, int commId);

		@Override
		public void messageTimeoutOccured(int commId) {
			// message timeout occurred!
			String logStr = "commId="+commId+". retries="+getNumberOfStartedLookups()+
					". Timeout occurred on sending a message.";

			if (getNumberOfStartedLookups() < getMaxNumberOfLookups()) {
				// retry
				log(logStr + " retry.", true);
				retry();
			}
			else {
				// all retries failed
				log(logStr + " all retries failed. giving up.");
				finishOperation(false);
			}
		}
		
	}
	
}
