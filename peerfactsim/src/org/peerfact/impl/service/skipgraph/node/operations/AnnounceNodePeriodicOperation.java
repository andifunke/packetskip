package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;
import java.util.Set;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.overlay.OverlayContact;
import org.peerfact.api.overlay.OverlayID;
import org.peerfact.api.overlay.dht.DHTKey;
import org.peerfact.impl.service.skipgraph.node.EntryPointSimple;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.simengine.Simulator;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class AnnounceNodePeriodicOperation extends AbstractSkipgraphNodePeriodicOperation<Object> {
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public AnnounceNodePeriodicOperation(
			SkipgraphNodeController nodeController) {
		super(nodeController);
//		setLogging(true);
		//setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance);
		log("construct new "+this);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Object getResult() {
		// not used
		return null;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */
	
	@Override
	protected void executeOnce() {
		if (!getComponent().getLocalNode().isPresent() || getNodeController().isDisabled()) {
			stop();
			log("-> stopping");
			return;
		}
		if (isStopped() || isFinished())
			return;

//		log("executeOnce");
		
		/**
		 * Stores entry information about this node in the DHT at two random locations 
		 * out of a set of possible locations. The size of the set is equal to the number of skipgraph nodes.
		 * This is of course a simplified method. We assume that the number of skipgraph nodes can be estimated
		 * from the size of the overlay.
		 */
		EntryPointSimple entryPoint = new EntryPointSimple(getNodeController().getNodeID(), getComponent().getLocalContact());
		BigInteger bootstrapID = SGUtil.getRandomBootstrapID();
		log("trying to store entryPointID @ bootstrapID "+SGUtil.formatID(bootstrapID));
		DHTKey<?> key_1 = getComponent().getLocalNode().getNewOverlayKey(bootstrapID);
		getComponent().getLocalNode().store(key_1, 
				entryPoint, 
				new OperationCallback<Set<OverlayContact<OverlayID<?>>>>() {

			@Override
			public void calledOperationSucceeded(
					Operation<Set<OverlayContact<OverlayID<?>>>> op) {
				log("store succeeded.");
			}

			@Override
			public void calledOperationFailed(
					Operation<Set<OverlayContact<OverlayID<?>>>> op) {
				log("store failed.");
			}
		});
		getNodeController().incrementOutgoingMessages();

		/**
		 * a more complex approach announcing to two different bootstrapIDs. Not much benefit.
		 */
		//EntryPointSimple entryPoint = new EntryPointSimple(getNodeController().getNodeID(), getComponent().getLocalContact());
		//BigInteger bootstrapID_1 = SGUtil.getRandomBootstrapID();
		//DHTKey<?> key_1 = getComponent().getLocalNode().getNewOverlayKey(bootstrapID_1);
		//getComponent().getLocalNode().store(key_1, entryPoint, null);
		//
		//if (SkipgraphNode.getCurrentNumberOfNodes() > 1) {
		//	BigInteger bootstrapID_2 = SGUtil.getRandomBootstrapID();
		//	if (SkipgraphNode.getCurrentNumberOfNodes() > 3) {
		//		// if there are enough skip graph nodes make sure we announce at exactly two different locations
		//		while (bootstrapID_1.equals(bootstrapID_2)) {
		//			bootstrapID_2 = SGUtil.getRandomBootstrapID();
		//		}
		//	}
		//	else if (!bootstrapID_1.equals(bootstrapID_2)) {
		//		// if the skip graph is small we announce at maximum at at two different locations
		//		// but never to the same location twice
		//		DHTKey<?> key_2 = getComponent().getLocalNode().getNewOverlayKey(bootstrapID_2);
		//		getComponent().getLocalNode().store(key_2, entryPoint, null);
		//	}
		//}
	}


	@Override
	protected long getInterval() {
		return 1*Simulator.MINUTE_UNIT + ((Simulator.getRandom().nextInt(20)-10)*Simulator.SECOND_UNIT);
	}
	
	
	@Override
	public void stop() {
		super.stop();
		operationFinished(true);
	}

}
