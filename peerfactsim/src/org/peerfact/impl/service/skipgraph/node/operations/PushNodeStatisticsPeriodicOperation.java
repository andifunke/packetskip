package org.peerfact.impl.service.skipgraph.node.operations;

import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.simengine.Simulator;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class PushNodeStatisticsPeriodicOperation extends AbstractSkipgraphNodePeriodicOperation<Object> {
	
	
	private final SkipgraphNode node;
	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public PushNodeStatisticsPeriodicOperation(SkipgraphNodeController nodeController) {
		super(nodeController);
		this.node = nodeController.getNode();
		//setLogging(SkipgraphServiceConstants.logJoin || SkipgraphServiceConstants.logMaintenance ||	SkipgraphServiceConstants.logLeave);
//		setLogging(true);
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

		//log("executeOnce @ "+Simulator.getFormattedTime(Simulator.getCurrentTime()));
		//System.out.println("number of nodes currently="+SkipgraphNode.getCurrentNumberOfNodes());
		
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			((SkipgraphMonitor)Simulator.getMonitor())
					.currentNumberOfNodes(node, SkipgraphNode.getCurrentNumberOfNodes());
			int contactTableSize = node.getContactTable().size();
			int effectiveContactTableSize = node.getContactTable().effectiveSize();
			int distinctContacts = node.getContactTable().getNumberOfDistinctContacts();
			log("contactTableSize="+contactTableSize
					+", effectiveContactTableSiz="+effectiveContactTableSize
					+", distinctContacts="+distinctContacts);
			((SkipgraphMonitor)Simulator.getMonitor())
					.addContactTableSize(node, contactTableSize);
			((SkipgraphMonitor)Simulator.getMonitor())
					.addContactTableEffectiveSize(node, effectiveContactTableSize);
			((SkipgraphMonitor)Simulator.getMonitor())
					.addNumberOfDistinctContacts(node, distinctContacts);
			((SkipgraphMonitor)Simulator.getMonitor())
					.addElementTableSize(node, node.getElementTable().size());
			((SkipgraphMonitor)Simulator.getMonitor())
					.addNumberOfIncomingMessages(node, getNodeController().getIncomingMessages());
			((SkipgraphMonitor)Simulator.getMonitor())
					.addNumberOfOutgoingMessages(node, getNodeController().getOutgoingMessages());
			((SkipgraphMonitor)Simulator.getMonitor())
					.nodeLifespan(node, Simulator.getCurrentTime()
							-getNodeController().getNode().getStartTime());
		}
		getNodeController().resetMessages();
		
	}

	@Override
	public void stop() {
		super.stop();
		operationFinished(true);
	}


	@Override
	protected long getInterval() {
		return 1*Simulator.MINUTE_UNIT;
	}
	
}
