package org.peerfact.impl.service.skipgraph.operations;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.overlay.dht.DHTKey;
import org.peerfact.api.overlay.dht.DHTObject;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.node.EntryPointSimple;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * Common asbtract superclass of all SkipgraphService operations.
 *
 * @param <S>
 *            the type of the operation result
 *
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public abstract class AbstractSkipgraphServiceOperation<S> extends AbstractSkipgraphOperation<S> {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	Set<BigInteger> badBootstrapIDs = new LinkedHashSet<>();
	
	BigInteger lastBootstrapID;
	
	ApplicationContact localContact;


	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	AbstractSkipgraphServiceOperation(
			SkipgraphService component,
			OperationCallback<S> callback) 
	{
		super(component, callback);
	}
	
	
	
	/* ************************************************
	 ******************* METHODS **********************
	 ************************************************ */
	
	void toEntryPoint() {
		// get an entry node.
		if (getComponent().hasEntryNode()) {
			// local: if the peer has at least one SkipgraphNode: use one of them.
			BigInteger entryID = getComponent().getEntryNodeController().getNodeID();
			log("ENTRY POINT: using local node: "+SGUtil.formatID(entryID));
			ApplicationContact entryPoint = new ApplicationContact(entryID, null);
			sendMessage(buildMessage(entryPoint));
		}
		else {
			// remote: lookup nodeID from bootstrapID
			toRemoteEntryPoint();
		}
	}
	
	
	/**
	 * Helper method to send an search to an entry point of the skip graph.
	 * May be called more than once.
	 */
	void toRemoteEntryPoint() {
		lastBootstrapID = SGUtil.getRandomBootstrapID(badBootstrapIDs);
		if (lastBootstrapID == null) {
			badBootstrapIDs.clear();
			lastBootstrapID = SGUtil.getRandomBootstrapID(badBootstrapIDs);
		}
		log("to remote entry for bootstrapID: "+SGUtil.formatID(lastBootstrapID));
		DHTKey<?> key = getComponent().getLocalNode().getNewOverlayKey(lastBootstrapID);
		getComponent().getLocalNode().valueLookup(key, new OperationCallback<DHTObject>() {

			@Override
			public void calledOperationSucceeded(Operation<DHTObject> op) {
				bootstrapMessages++;
				if (op.getResult() instanceof EntryPointSimple) {
					log("ENTRY POINT: successfully looked up the dht object: "
							+op.getResult()+" for bootstrapID: "+SGUtil.formatID(lastBootstrapID));
					// TODO: try ApplicationContact from EntryPointSimple first instead of new ApplicationContact from nodeID
					//BigInteger entryID = ((EntryPointSimple)op.getResult()).getNodeID();
					ApplicationContact entryPoint = ((EntryPointSimple)op.getResult()).getPeer();
					sendMessage(buildMessage(entryPoint));
				}
				else if (op.getResult() == null) {
					log("ENTRY POINT: value lookup is null for bootstrapID: "+SGUtil.formatID(lastBootstrapID)
					+ " RETRY");
					badBootstrapIDs.add(lastBootstrapID);
					toRemoteEntryPoint();
				}
				else {
					log("ENTRY POINT: value lookup error for bootstrapID: "+SGUtil.formatID(lastBootstrapID)
					+ " has wrong type: "+op.getResult().getClass()
					+ " RETRY");
					badBootstrapIDs.add(lastBootstrapID);
					toRemoteEntryPoint();
				}
			}

			@Override
			public void calledOperationFailed(Operation<DHTObject> op) {
				log("ENTRY POINT: value lookup failed for bootstrapID: "+SGUtil.formatID(lastBootstrapID)
				+ " RETRY");
				badBootstrapIDs.add(lastBootstrapID);
				toRemoteEntryPoint();
			}
		});
		bootstrapMessages++;
		bootstrapAttempts++;
	}
	
	
	
	@Override
	protected void retry() {
		// retry using a different bootstrapID
		log("lookupCounter="+getNumberOfStartedLookups()+". retry with different bootstrapID");
		badBootstrapIDs.add(lastBootstrapID);
		toRemoteEntryPoint();
	}
	
	
	/* *******************************************
	 *********** TRACKING THE QUERY **************
	 ******************************************* */

	int bootstrapAttempts = 0;
	int bootstrapMessages = 0;
	int forwardingMessages = 0;
	int n_ACKs = 0;
	int replyMessages = 0;
	List<BigInteger> hops = new LinkedList<>();
	List<BigInteger> deletehops = new LinkedList<>();
	
	public class TrackerCallback {

		public void incrementBootstrapCount() {
			bootstrapMessages++;
		}

		public void incrementForwardingCount() {
			forwardingMessages++;
		}

		public void incrementN_ACKCount() {
			n_ACKs++;
		}

		public void incrementReplyCount() {
			replyMessages++;
		}
		
		public void addHop(BigInteger nodeID) {
			hops.add(nodeID);
		}
		
		public void addDeleteHop(BigInteger nodeID) {
			deletehops.add(nodeID);
		}
		
	}


}