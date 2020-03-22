package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.RangeAdjustmentMessage;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class RangeAdjustmentOperation extends AbstractSkipgraphNodeOperation<Set<BigInteger>> {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private final SkipgraphContact thisContact;

	private Set<BigInteger> contactSet;
	
	private Set<BigInteger> failedSet = new LinkedHashSet<>();
	
	private int numberOfContacts = 0;

	
	 
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public RangeAdjustmentOperation(
			SkipgraphNodeController nodeController,
			OperationCallback<Set<BigInteger>> callback) 
	{
		super(nodeController, callback, null);
		thisContact = getNodeController().getContact();

		setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance ||
				SkipgraphServiceConstants.logLeave);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Set<BigInteger> getResult() {
		return failedSet;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
		
	@Override
	protected void execute() {
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(false);
		}
		scheduleOperationTimeout(getTimeout());

		// sending a message to each contact in the contactTable
		contactSet = getNodeController().getContactTable().getContactSet();
		numberOfContacts = contactSet.size();
		
		for (BigInteger receiverNodeID : contactSet) {
			ApplicationContact receiver = new ApplicationContact(receiverNodeID, null);
			RangeAdjustmentMessage message = buildMessage(receiver);
			RangeAdjustmentSingleOperation calledOp = new RangeAdjustmentSingleOperation(
					getNodeController(), 
					new OperationCallback<BigInteger>() {

						@Override
						public void calledOperationFailed(
								Operation<BigInteger> op) {
							contactSet.remove(op.getResult());
							failedSet.add(op.getResult());
							if (contactSet.isEmpty()) {
								finishOperation(true);
							}
						}

						@Override
						public void calledOperationSucceeded(
								Operation<BigInteger> op) {
							contactSet.remove(op.getResult());
							if (contactSet.isEmpty()) {
								finishOperation(true);
							}
						}
					}, 
					message);
			calledOp.scheduleImmediately();
		}
	}
	
	
	@Override
	protected void finishOperation(boolean b) {
		boolean success = true;
		if (failedSet.isEmpty()) {
			log("operation finished. all contacts successfully updated.");
		}
		else if (failedSet.size() == numberOfContacts) {
			log("operation finished. range update failed for all contacts: "
					+ failedSet);
			success = false;
		}
		else {
			log("operation finished. range update failed for the following contacts: "
					+ failedSet);
		}
		operationFinished(success);
	}

	
	@Override
	protected RangeAdjustmentMessage buildMessage(ApplicationContact receiver) {
		return new RangeAdjustmentMessage(
				getComponent().getLocalContact(),
				receiver, 
				getOperationID(),
				thisContact);
	}


	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		// not used
	}

	@Override
	protected void retry() {
		// not used
	}

}
