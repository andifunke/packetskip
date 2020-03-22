package org.peerfact.impl.service.skipgraph.node.operations;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.peerfact.api.common.Operation;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.node.ContactLevel;
import org.peerfact.impl.service.skipgraph.node.ContactTable;
import org.peerfact.impl.service.skipgraph.node.Replacement;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.ReplaceContactsMessage;
import org.peerfact.impl.service.skipgraph.util.SGUtil;
import org.peerfact.impl.service.skipgraph.util.SGUtil.Route;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class ReplaceContactsOperation extends AbstractSkipgraphNodeOperation<Boolean> {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */
	
	private boolean result = false;
	
	//private Set<Integer> messageSet = new LinkedHashSet<>();
	/**
	 * this map contains an entry for each unique contact in the contact list
	 * the entry-value consists of a list of all replacements which will be sent 
	 * later in one go to the contact.
	 */
	private Map<BigInteger, List<Replacement>> replacementsMap;



	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public ReplaceContactsOperation(
			SkipgraphNodeController node,
			OperationCallback<Boolean> callback)
	{
		super(node, callback, null);
		init();
	}

	public ReplaceContactsOperation(
			SkipgraphNodeController node,
			OperationCallback<Boolean> callback,
			Map<BigInteger, List<Replacement>> replacementsMap) 
	{
		super(node, callback, null);
		this.replacementsMap = replacementsMap;
		init();
	}
	
	private void init() {
		setLogging(true);
		setLogging(SkipgraphServiceConstants.logLeave);
		
	}



	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Boolean getResult() {
		return result;
	}



	/* ************************************************
	 ******************** METHODS *********************
	 ************************************************ */

	@Override
	protected void execute() {
		if (!getComponent().getLocalNode().isPresent()) {
			log("not present!");
			// finish straight
			operationFinished(false);
		}

		ContactTable contactTable = getNodeController().getContactTable();

		// special case, where a skipgraphNode is the only node in the graph
		if (contactTable.isSelfLinkedLevel(contactTable.getLevel(0))) {
			log("last remaining SkipgraphNode. Can't leave.");
			operationFinished(false);
			return;
		}

		scheduleOperationTimeout(getTimeout());
		
		if (replacementsMap == null) {
			log("building new replacementsMap");
			replacementsMap = new LinkedHashMap<>();

			for (int i=0; i<contactTable.size(); i++) {
				ContactLevel level = contactTable.getLevel(i);

				// skip to avoid sending messages to ourselves
				if (getNodeController().getContactTable().isSelfLinkedLevel(level)) {
					continue;
				}

				List<Replacement> replaceList;
				//prev (we set route=NEXT because its the next contact from the receivers point of view)
				replaceList = replacementsMap.getOrDefault(level.getPrevContact().getNodeID(), new LinkedList<>());
				replaceList.add(new Replacement(i, level.getPrefix(), Route.NEXT, level.getNextContact()));
				replacementsMap.put(level.getPrevContact().getNodeID(), replaceList);

				//next (we set route=PREV)
				replaceList = replacementsMap.getOrDefault(level.getNextContact().getNodeID(), new LinkedList<>());
				replaceList.add(new Replacement(i, level.getPrefix(), Route.PREV, level.getPrevContact()));
				replacementsMap.put(level.getNextContact().getNodeID(), replaceList);
			}
		}
		else {
			log("forwaring replacements from other disabled node", true);
		}
		
		log("replacementsMap="+replacementsMap);
		if (!replacementsMap.isEmpty())
			result = true;
		
		// preparing the messages for all unique contacts
		ApplicationContact localContact = getComponent().getLocalContact();
		BigInteger nodeID = getNodeController().getNodeID(); 

		for (Entry<BigInteger, List<Replacement>> entry : replacementsMap.entrySet()) {
			BigInteger receiverID = entry.getKey();
			ReplaceContactsMessage message = new ReplaceContactsMessage(
					localContact, 
					new ApplicationContact(receiverID, null),
					getOperationID(),
					nodeID,
					entry.getValue(),
					true);
			ReplaceContactsSingleOperation rcsOp = new ReplaceContactsSingleOperation(
					getNodeController(), 
					new OperationCallback<BigInteger>() {

						@Override
						public void calledOperationFailed(
								Operation<BigInteger> op) {
							log("replace contact failed for receiverID="
								+SGUtil.formatContact(message.getReceiver()), true);
							result = false;
							evaluateResults(receiverID);
						}

						@Override
						public void calledOperationSucceeded(
								Operation<BigInteger> op) {
							evaluateResults(receiverID);
						}
						
					}, 
					message);
			rcsOp.scheduleImmediately();
		}
	}
	
	
	private void evaluateResults(BigInteger contactID) {
		replacementsMap.remove(contactID);
		if (replacementsMap.isEmpty()) {
			finishOperation(true);
		}
	}
	
	

	@Override
	protected ReplaceContactsMessage buildMessage(ApplicationContact receiver) {
		return null;
	}



	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		// not used
	}
	
}
