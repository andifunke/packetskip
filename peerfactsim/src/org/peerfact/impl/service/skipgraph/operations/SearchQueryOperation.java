package org.peerfact.impl.service.skipgraph.operations;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.peerfact.api.common.Message;
import org.peerfact.api.common.OperationCallback;
import org.peerfact.api.transport.TransInfo;
import org.peerfact.impl.service.skipgraph.SearchQuery;
import org.peerfact.impl.service.skipgraph.SkipgraphService;
import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.analyzer.SkipgraphMonitor;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryACKMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryNACKMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultParallelMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultSerialMessage;
import org.peerfact.impl.simengine.Simulator;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;


/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class SearchQueryOperation
		extends AbstractSkipgraphServiceOperation<Set<ApplicationContact>>
		implements Addressable {
	
	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private Set<ApplicationContact> results = new LinkedHashSet<>();
	
	/** this map contains a set of ApplicationContacts per capacity 
	 *  we will then create an intersection of all sets to retain 
	 *  only those Contacts that fit to all single search queries =>
	 *  multi-dimensional search */
	private Map<String, Set<ApplicationContact>> resultsPerCapacity;
	
	/**
	 * the value is an Integer array of size=2.
	 * [0] => current count of returned messages per capacity
	 * [1] => the target number of returned messages per capacity
	 * finish operation when [0] == [1]
	 */
	private Map<String, int[]> counterPerCapacity;
	
	private int k;

	private final List<SearchQuery> queries;
	
	private final Set<BigInteger> expectResultsFrom = new LinkedHashSet<>();
	
	private final Set<BigInteger> receivedResultsFrom = new LinkedHashSet<>();
	
	private TrackerCallback tracker;
	
	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public SearchQueryOperation(
			SkipgraphService component, 
			OperationCallback<Set<ApplicationContact>> callback,
			List<SearchQuery> queries,
			int k)
	{
		super(component, callback);
		this.queries = queries;
		this.k = k;
		this.resultsPerCapacity = new LinkedHashMap<>();
		this.counterPerCapacity = new LinkedHashMap<>();
		for (SearchQuery query : queries) {
			if (!resultsPerCapacity.containsKey(query.getDimension())) {
				resultsPerCapacity.put(query.getDimension(), new LinkedHashSet<>());
				int[] counter = new int[2];
				counter[0]=0;
				counter[1]=-1;
				counterPerCapacity.put(query.getDimension(), counter);
			}
		}
		
		setLogging(SkipgraphServiceConstants.logSearch);
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public Set<ApplicationContact> getResult() {
		return results;
	}
	


	/* ************************************************
	 ***************** Helper METHODS *****************
	 ************************************************ */

	@Override
	protected void execute() {
		if (getComponent().getLocalNode().isPresent()) {
			scheduleOperationTimeout(SkipgraphServiceConstants.NODE_OP_TIMEOUT);
			localContact = getComponent().getLocalContact();
			tracker = new TrackerCallback();
			toEntryPoint();
			//log("send search query="+queries, true);
		}
		else {
			log("not present!");
			// finish without informing the monitor
			operationFinished(false);
		}
	}

	

	@Override
	protected SearchQueryMessage buildMessage(ApplicationContact entryPoint) {
		return new SearchQueryMessage(
				localContact, 
				entryPoint, 
				getOperationID(),
				queries,
				tracker);
	}
	
	
	
	@Override
	protected void sendMessage(AbstractSkipgraphMessage message) {
		this.getComponent().getCommunicationManager().send(
				message,
				
				new DefaultSkipGTransMessageCallback() {

					@Override
					public void receive(Message answerMsg, TransInfo senderInfo, int commId) {
						bootstrapMessages++;
						// successful
						if (answerMsg instanceof SearchQueryACKMessage) {
							log("ACK received -> waiting for search results.");
							return;
						} 
						
						// unsuccessful
						if (answerMsg instanceof SearchQueryNACKMessage){
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
							log("lookupCounter="+getNumberOfStartedLookups()+". all retries failed. giving up.");
							finishOperation(false);
						}
					}

				},

				new DefaultSGCommunicationCallback()
			);
		
		this.incrementLookupCounter();
		bootstrapMessages++;
	}
	
	
	
	@Override
	public boolean deliverMessage(AbstractSkipgraphMessage message) {
		if (message instanceof SearchResultSerialMessage) {
			hops.add(getComponent().getLocalContact().getPeerID());
			handleSerialResults((SearchResultSerialMessage)message);
			return true;
		}
		if (message instanceof SearchResultParallelMessage) {
			hops.add(getComponent().getLocalContact().getPeerID());
			handleParallelResults((SearchResultParallelMessage)message);
			return true;
		}
		else {
			log("received a message we can not understand "+message);
			return false;
		}
	}
	
	
	
	private void handleParallelResults(SearchResultParallelMessage message) {
		log("handle "+message);
		
		receivedResultsFrom.add(message.getSenderNodeID());
		expectResultsFrom.addAll(message.getHopTrackerSet());
		log("receivedResultsFrom="+receivedResultsFrom);
		log("expectResultsFrom="+expectResultsFrom);
		
		// add results from the message to all results
		for (Entry<String, Set<ApplicationContact>> entry : resultsPerCapacity.entrySet()) {
			if (message.getResults().containsKey(entry.getKey())) {
				entry.getValue().addAll(message.getResults().get(entry.getKey()));
			}
		}
		
		// creates an intersection of all capacities
		Set<ApplicationContact> intersection = new LinkedHashSet<>();
		boolean first = true;
		for (Entry<String, Set<ApplicationContact>> entry : resultsPerCapacity.entrySet()) {
			if (first) {
				intersection.addAll(entry.getValue());
				first = false;
			}
			else {
				intersection.retainAll(entry.getValue());
			}
		}
		log("results per capacity: "+resultsPerCapacity+"\nintersection: "+intersection);
		
		// we end the search if we have received enough results
		if (0 < k && k <= intersection.size()) {
			if (k < intersection.size()) {
				// limit the size of the set to <= k values
				this.results = new LinkedHashSet<>((new LinkedList<>(intersection)).subList(0, k));
			}
			else {
				this.results = intersection;
			}
			log("final k="+k+" results="+this.results+"\ndone!");
			finishOperation(true);
			return;
		}
		
		// if we have received all messages we end the operation
		expectResultsFrom.removeAll(receivedResultsFrom);
		log("expectResultsFrom="+expectResultsFrom);
		
		if (expectResultsFrom.isEmpty()) {
			log("all result messages received");
			this.results = intersection;
			finishOperation(true);
			return;
		}
		
		log("not done");
	}
	
	

	private void handleSerialResults(SearchResultSerialMessage message) {
		// results for which capacity?
		String dimension = message.getDimension();
		counterPerCapacity.get(dimension)[0]++;
		resultsPerCapacity.get(dimension).addAll(message.getResults());
		String logStr = "\nhandle "+message;
		
		// creates an intersection of all capacities
		Set<ApplicationContact> intersection = new LinkedHashSet<>();
		boolean first = true;
		for (Entry<String, Set<ApplicationContact>> entry : resultsPerCapacity.entrySet()) {
			if (first) {
				intersection.addAll(entry.getValue());
				first = false;
			}
			else {
				intersection.retainAll(entry.getValue());
			}
		}
		logStr += "\nresults per capacity: "+resultsPerCapacity+"\nintersection: "+intersection;
		if (0 < k && k <= intersection.size()) {
			logStr += (logStr+"\nfinal k="+k+" results: ");
			
			if (k < intersection.size()) {
				// limit the size of the set to <= k values
				this.results = new LinkedHashSet<>((new LinkedList<>(intersection)).subList(0, k));
			}
			else {
				this.results = intersection;
			}
			logStr += this.results + "\ndone!";
			finishOperation(true);
			log(logStr, false);
			return;
		}
		
		if (message.isLastMessage()) {
			log("dimension="+dimension+". isLastMessage. counter="+message.getCounter(), false);
			counterPerCapacity.get(dimension)[1] = message.getCounter();
		}

		boolean done = true;
		for (Entry<String, int[]> entry : counterPerCapacity.entrySet()) {
			logStr += "\ncounter status for "+entry.getKey()+": "
					+entry.getValue()[0]+" messages received from "+entry.getValue()[1];
			done &= (entry.getValue()[1] > 0) && (entry.getValue()[0] >= entry.getValue()[1]);
		}
		logStr += "\ndone? "+done;
		
		if (done) {
			log(logStr+"\nall result messages received", false);
			this.results = intersection;
			finishOperation(true);
		}
		else {
			log(logStr+"\nresult message received. final results still pending", false);
		}
	}
	
	
	@Override
	protected void operationTimeoutOccured() {
		log("operation timeout occured. ", true);
		finishOperation(false);
	}


	@Override
	protected void finishOperation(boolean success) {
		//System.out.println(hopCounter);
		log("results="+results);
		// call the callback
		operationFinished(success);
		// call the monitor and push the data!
		if (Simulator.getMonitor() instanceof SkipgraphMonitor) {
			((SkipgraphMonitor)Simulator.getMonitor())
					.searchFinished(localContact, success);
			((SkipgraphMonitor)Simulator.getMonitor())
					.addSearchQueryResults(localContact, queries, k, results);
			((SkipgraphMonitor)Simulator.getMonitor())
					.addSearchQueryDuration(localContact, getDuration());
			((SkipgraphMonitor)Simulator.getMonitor())
					.addSearchQueryMessageCount(localContact, bootstrapMessages,
							n_ACKs, forwardingMessages, replyMessages);
			Set<BigInteger> distinctHops = new LinkedHashSet<>(hops);
//			if (hops.size() != distinctHops.size()) {
//				log("hops="+SGUtil.formatIDs(hops)+" size="+hops.size()+", disctinctHops="+SGUtil.formatIDs(distinctHops), true);
//			}
			((SkipgraphMonitor)Simulator.getMonitor())
					.addSearchQueryHopCount(localContact, hops.size(), distinctHops.size());
			((SkipgraphMonitor)Simulator.getMonitor())
					.bootstrapAttempts(localContact, bootstrapAttempts);
		}
	}
	
	
}
