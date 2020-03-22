package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.node.ElementTable;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingMessage;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class LoadBalancingOperation extends AbstractSkipgraphNodeOperation<Boolean> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private Boolean result;
	
	private SkipgraphContact prevContact;
	private SkipgraphContact updatedPrev;
	private boolean prevReplied = false;
	private int prevOffering = 0;
	private boolean prevFinished = false;
	private boolean prevSuccess = false;

	private SkipgraphContact nextContact;
	private SkipgraphContact updatedNext;
	private boolean nextReplied = false;
	private int nextOffering = 0;
	private boolean nextFinished = false;
	private boolean nextSuccess = false;

	private ElementTable thisNodesTable;
	private ElementTable prevNodesTable;
	private ElementTable nextNodesTable;
	
	private boolean preparingLeave = false;
	

	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public LoadBalancingOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<Boolean> callback,
			String succeedingOperation) 
	{
		super(nodeController, callback, null);
		if (succeedingOperation.equals("leave")) this.preparingLeave = true;

		setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance ||
				SkipgraphServiceConstants.logLeave);
//		setLogging(true);
		//if (getNodeController().getGlobalCountID() == 19) setLogging(true);
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
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(result = false);
		}
		
		// exclude some special cases when building up the skip graph
		//if (prevContact.getNodeID().equals(getNodeController().getNodeID()) || 
		//		nextContact.getNodeID().equals(getNodeController().getNodeID()) ||
		//		prevContact.getNodeID().equals(nextContact.getNodeID())) {
		if (getNodeController().getContactTable().isSelfLinkedLevel(0)) {
			log("special case: skip graph too small. aborting "+
				(preparingLeave ? "leave." : "LoadBalancingOperation."));
			operationFinished(result = false);
			return;
		}

		scheduleOperationTimeout(getTimeout());
		executeOnce();
	}
	
	
	private void executeOnce() {
		prevContact = getNodeController().getContactTable().getPrev();
		nextContact = getNodeController().getContactTable().getNext();
		
		log("executing" + (preparingLeave ? " (preparing leave)" : ""));
		
		// don't call prev when responsible for rangeStart==0
		if (getNodeController().getElementTable().getRangeStart().equals(BigDecimal.ZERO)) {
			prevFinished = true;
		}
		else {
			log("sending to prev="+SGUtil.formatID(prevContact.getNodeID()));
			RemainingSizeOperation remainingSizeOp = new RemainingSizeOperation(
					getNodeController(), 
					new OperationCallback<Integer>() {

						@Override
						public void calledOperationFailed(
								Operation<Integer> op) {
							prevFinished = true;
							log("RemainingSizeNACK received from prev.");
							evaluateOfferings();
						}

						@Override
						public void calledOperationSucceeded(
								Operation<Integer> op) {
							prevReplied = true;
							prevOffering = op.getResult();
							log("RemainingSizeACK received from prev.");
							evaluateOfferings();
						}
					}, 
					prevContact.getNodeID(), 
					getOperationID());
			remainingSizeOp.scheduleImmediately();
		}
		// don't call next when responsible for rangeEnd==null (inf)
		if (getNodeController().getElementTable().getRangeEnd() == null) {
			nextFinished = true;
		}
		else {
			log("sending to next="+SGUtil.formatID(nextContact.getNodeID()));
			RemainingSizeOperation remainingSizeOp = new RemainingSizeOperation(
					getNodeController(), 
					new OperationCallback<Integer>() {

						@Override
						public void calledOperationFailed(
								Operation<Integer> op) {
							nextFinished = true;
							log("RemainingSizeNACK received from next.");
							evaluateOfferings();
						}

						@Override
						public void calledOperationSucceeded(
								Operation<Integer> op) {
							nextReplied = true;
							nextOffering = op.getResult();
							log("RemainingSizeACK received from next.");
							evaluateOfferings();
						}
					}, 
					nextContact.getNodeID(),
					getOperationID());
			remainingSizeOp.scheduleImmediately();
		}
	}
	
	
	@Override
	protected void retry() {
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(result = false);
		}
		
		log("lookupCounter="+getNumberOfStartedLookups()+". resending message.");
		executeOnce();
	}

	
	
	private void evaluateOfferings() {
		log("evaluate Offerings");
		if ((prevFinished || prevReplied) && (nextFinished || nextReplied)) {
			
			int minimumNumberOfElementsToHandOver;
			if (preparingLeave) {
				minimumNumberOfElementsToHandOver = getNodeController().getElementTable().size();
			}
			else {
				minimumNumberOfElementsToHandOver = (int)(getNodeController().getElementTable().size() - 
						SkipgraphServiceConstants.getElementTableMaxSize()*(1-SkipgraphServiceConstants.headroom));
			}
			
			int combinedOffering = prevOffering+nextOffering;
			log("prevOffering="+prevOffering
					+". nextOffering="+nextOffering
					+". combinedOffering="+(combinedOffering)
					+ ". We want to hand over "
					+ (preparingLeave ? "exactly" : "at least") +"="+minimumNumberOfElementsToHandOver);
			
			if (combinedOffering < minimumNumberOfElementsToHandOver) {
				log("not enough free space on neighbours.");
				// send negative acknowledgment to prev/next to end their AwaitElementTableExtensionOperation 
				// (if their offer was > 0)
				if (prevOffering > 0) {
					log("inform prev about operation abortion");
					LoadBalancingMessage message = 
							buildMessage(prevContact.getNodeID(), null);
					sendLoadBalancingNACK(message);
				}
				if (nextOffering > 0) {
					log("inform next about operation abortion");
					LoadBalancingMessage message = 
							buildMessage(nextContact.getNodeID(), null);
					sendLoadBalancingNACK(message);					
				}
				finishOperation(result = false);
				return;
			}

			log("success - enough free space on neighbours!");
				
			//getNodeController().lock();
			
			// calculating the ratio between the remaining sizes of prev and next and allocating
			// the minimum number of elements on prev and next according to that ratio
			int prevAssignedTo = 
					Math.round(minimumNumberOfElementsToHandOver * ((float)prevOffering/combinedOffering));
			int nextAssignedTo = minimumNumberOfElementsToHandOver-prevAssignedTo;

			log("prevAssignedTo=" + prevAssignedTo + ", nextAssignedTo=" + nextAssignedTo);
			
			int prevHeadroom = 0;
			int nextHeadroom = 0;
			
			if (!preparingLeave) {
				// trying to distribute more elements than the minimum number to prev and next
				// we want to use only 2/3 of the headroom
				prevHeadroom = (int)((prevOffering-prevAssignedTo)*0.66);
				nextHeadroom = (int)((nextOffering-nextAssignedTo)*0.66);
			}

			log("prevAssignedTo="+prevAssignedTo
					+", prevHeadroom="+prevHeadroom
					+", nextAssignedTo="+nextAssignedTo
					+", nextHeadroom="+nextHeadroom);
			
			ElementTable[] et = getNodeController().getElementTable()
					.split(prevAssignedTo+prevHeadroom, nextAssignedTo+nextHeadroom);
			
			if (et == null) {
				log("couldn't split ElementTable - operation failed.");
				finishOperation(result = false);
				return;
			}
			
			prevNodesTable = et[0];
			thisNodesTable = et[1];
			nextNodesTable = et[2];

			log("\nprev:"+prevNodesTable + (prevNodesTable==null?"\n":"")
					+"this:"+thisNodesTable + (thisNodesTable==null?"\n":"")
					+"next:"+nextNodesTable);
			
			if (prevNodesTable != null && prevNodesTable.size() > 0) {
				// send element extension to prev
				LoadBalancingMessage message = 
						buildMessage(prevContact.getNodeID(), prevNodesTable);
				LoadBalancingSingleOperation singleOp = new LoadBalancingSingleOperation(
						getNodeController(), 
						new OperationCallback<SkipgraphContact>() {

							@Override
							public void calledOperationFailed(
									Operation<SkipgraphContact> op) {
								log("NACK received from prev.");
								prevFinished = true;
								evaluateSuccess();
							}

							@Override
							public void calledOperationSucceeded(
									Operation<SkipgraphContact> op) {
								log("ACK received from prev.");
								// we need this update for the buffer flush
								updatedPrev = op.getResult();
								prevFinished = true;
								prevSuccess = true;
								evaluateSuccess();
							}
						}, 
						message);
				singleOp.scheduleImmediately();
			}
			else {
				// send negative acknowledgment to prev to end its AwaitElementTableExtensionOperation 
				// (if its offer was > 0)
				if (prevOffering > 0) {
					log("inform prev about operation abortion");
					LoadBalancingMessage message = 
							buildMessage(prevContact.getNodeID(), null);
					sendLoadBalancingNACK(message);
				}
				prevFinished = true;
			}

			if (nextNodesTable != null && nextNodesTable.size() > 0) {
				// send element extension to next
				LoadBalancingMessage message = 
						buildMessage(nextContact.getNodeID(), nextNodesTable);
				LoadBalancingSingleOperation singleOp = new LoadBalancingSingleOperation(
						getNodeController(), 
						new OperationCallback<SkipgraphContact>() {

							@Override
							public void calledOperationFailed(
									Operation<SkipgraphContact> op) {
								log("NACK received from next.");
								nextFinished = true;
								evaluateSuccess();
							}

							@Override
							public void calledOperationSucceeded(
									Operation<SkipgraphContact> op) {
								log("ACK received from next.");
								// we need this update for the buffer flush
								updatedNext = op.getResult();
								nextFinished = true;
								nextSuccess = true;
								evaluateSuccess();
							}
						}, 
						message);
				singleOp.scheduleImmediately();
			}
			else {
				// send negative acknowledgment to next to end its AwaitElementTableExtensionOperation 
				// (if its offer was > 0)
				if (nextOffering > 0) {
					log("inform next about operation abortion");
					LoadBalancingMessage message = 
							buildMessage(nextContact.getNodeID(), null);
					sendLoadBalancingNACK(message);					
				}
				nextFinished = true;
			}
			
			if (prevFinished && nextFinished) {
				log("no asignments to either neighbor possible: operation finished");
				finishOperation(result = false);
			}
		}
	}
	
	@Override
	protected AbstractSkipgraphMessage buildMessage(
			ApplicationContact receiver) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private LoadBalancingMessage buildMessage(
			BigInteger receiverNodeID, ElementTable elementTable) {
		// build the message with sender, receiver and the list of skip graph elements
		return new LoadBalancingMessage(
				getComponent().getLocalContact(),
				new ApplicationContact(receiverNodeID, null),
				getOperationID(),
				getNodeController().getNodeID(),
				elementTable);
	}
	
	
	private void sendLoadBalancingNACK(LoadBalancingMessage message) {
		getNodeController().incrementOutgoingMessages();
		getComponent().getCommunicationManager().send(
				message,
				null,
				null
			);
	}

	
	private void evaluateSuccess() {
		if (!prevFinished || !nextFinished)
			return;
		
		// if we want to leave, both table extensions must be accepted
		if (preparingLeave) {
			// both were successful
			if ((prevSuccess || prevNodesTable == null) 
					&& (nextSuccess || nextNodesTable == null)) {
				log("prev success and next success -> leaving");
				getNodeController().disable();
				finishOperation(result = true);
				return;
			}
			// only prev was successful
			if (prevSuccess || prevNodesTable == null) {
				thisNodesTable = ElementTable.merge(nextNodesTable, thisNodesTable);
				log("only prev success -> updating ElementTable:" +thisNodesTable + " can't leave!");
				getNodeController().unlockWithNewElementTable(thisNodesTable, 
						updatedPrev, updatedNext);
				finishOperation(result = false);
				return;
			}
			// only next was successful
			if (nextSuccess || nextNodesTable == null) {
				thisNodesTable = ElementTable.merge(prevNodesTable, thisNodesTable);
				log("only next success -> updating ElementTable:" +thisNodesTable + " can't leave!");
				getNodeController().unlockWithNewElementTable(thisNodesTable, 
						updatedPrev, updatedNext);
				finishOperation(result = false);
				return;
			}
			// none was successful
			finishOperation(result = false);
			return;
		}
		
		// if both extensions were successful we can update our element table
		if (prevSuccess && nextSuccess) {
			log("prev success and next success -> updating ElementTable:" +thisNodesTable);
			getNodeController().unlockWithNewElementTable(thisNodesTable, 
					updatedPrev, updatedNext);
			finishOperation(result = true);
			return;
		}
		
		// if only prev was successful we have to glue the extension for next back to our table
		if (prevSuccess) {
			thisNodesTable = ElementTable.merge(nextNodesTable, thisNodesTable);
			log("only prev success -> updating ElementTable:" +thisNodesTable);
			getNodeController().unlockWithNewElementTable(thisNodesTable, 
					updatedPrev, updatedNext);
			finishOperation(result = true);
			return;
		}

		// if only next was successful we have to glue the extension for prev back to our table
		if (nextSuccess) {
			thisNodesTable = ElementTable.merge(prevNodesTable, thisNodesTable);
			log("only next success -> updating ElementTable:" +thisNodesTable);
			getNodeController().unlockWithNewElementTable(thisNodesTable, 
					updatedPrev, updatedNext);
			finishOperation(result = true);
			return;
		}
		log("no success -> aborting");
		
		// if none was successful
		finishOperation(result = false);
	}
	
	
	@Override
	protected void finishOperation(boolean success) {
		if (!preparingLeave) {
			if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
				((SkipgraphMonitor)Simulator.getMonitor())
						.loadBalancingSuccessAndFailur(getNodeController().getNode(), success);
			}
		}
		operationFinished(success);
	}


	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		// not used
	}


}
