/*
 * Copyright (c) 2012-2013 Open Source Community - <http://www.peerfact.org>
 * Copyright (c) 2011-2012 University of Paderborn - UPB
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.peerfact.impl.service.skipgraph;

import org.peerfact.api.common.Host;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphMessage;
import org.peerfact.impl.service.skipgraph.messages.AbstractSkipgraphServiceMessage;
import org.peerfact.impl.service.skipgraph.messages.DefaultNACKMessage;
import org.peerfact.impl.service.skipgraph.messages.JoinRequestMessage;
import org.peerfact.impl.service.skipgraph.messages.PingMessage;
import org.peerfact.impl.service.skipgraph.messages.PongMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryACKMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchQueryNACKMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultParallelMessage;
import org.peerfact.impl.service.skipgraph.messages.SearchResultSerialMessage;
import org.peerfact.impl.service.skipgraph.messages.UpdateACKMessage;
import org.peerfact.impl.service.skipgraph.messages.UpdateMessage;
import org.peerfact.impl.service.skipgraph.messages.UpdateNACKMessage;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNodeController;
import org.peerfact.impl.service.skipgraph.node.messages.AbstractSkipgraphNodeRequestMessage;
import org.peerfact.impl.service.skipgraph.node.messages.LoadBalancingMessage;
import org.peerfact.impl.service.skipgraph.node.messages.HandshakeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelNotifyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelReplyMessage;
import org.peerfact.impl.service.skipgraph.node.messages.JoinLevelRequestMessage;
import org.peerfact.impl.service.skipgraph.node.messages.UpdateForwardingMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RangeAdjustmentMessage;
import org.peerfact.impl.service.skipgraph.node.messages.RemainingSizeMessage;
import org.peerfact.impl.service.skipgraph.node.messages.ReplaceContactsMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingParallelMessage;
import org.peerfact.impl.service.skipgraph.node.messages.SearchForwardingSerialMessage;
import org.peerfact.impl.transport.TransMsgEvent;
import org.peerfact.impl.util.communicationmanager.AbstractAppMessage;
import org.peerfact.impl.util.communicationmanager.AppMessageHandlerInterface;
import org.peerfact.impl.util.communicationmanager.ApplicationContact;

/**
 * Demultiplexes incoming messages and distributes them to the 
 * skip graph service or an addressed skip graph node, if the 
 * node exists on the service.
 * May also send ACKs or NACKs.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class MessageDistributor implements AppMessageHandlerInterface {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private SkipgraphService service;
	
	@SuppressWarnings("unused")
	private Host host;
	
	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ***************************************************/

	public MessageDistributor(SkipgraphService service) {
		this.service = service;
		setHost(service.getHost());
	}

	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	@Override
	public ApplicationContact getLocalContact() {
		return service.getLocalContact();
	}

	public SkipgraphService getService() {
		return service;
	}

	@Override
	public Host getHost() {
		return service.getHost();
	}
	
	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	@Override
	public void setHost(Host host) {
		this.host = host;
	}

	public void setService(SkipgraphService service) {
		this.service = service;
	}


	/* ************************************************
	 *********** AppMessageHandlerInterface ***********
	 ************************************************ */

	/**
	 * Called from the CommunicationManager to push messages this node receives.
	 * @param message
	 * @param receivingEvent
	 */
	@Override
	public void processMsg(AbstractAppMessage message, TransMsgEvent receivingEvent) {
		//service.log("  \n############### WE GOT MAIL ############ ----> \n" + message);
		boolean sendResponse = true;

		if (message instanceof AbstractSkipgraphMessage) {
			AbstractSkipgraphMessage responseMsg = null;

			if (message instanceof AbstractSkipgraphServiceMessage) {

				// messages to the service itself
				if (message instanceof PingMessage) {
					//service.log("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ PING @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					responseMsg = new PongMessage(message.getReceiver(), message.getSender(), 
							message.getOperationID());
				}
				else if (message instanceof SearchResultSerialMessage) {
					// doesn't send (N)ACK
					service.getOperationManager()
							.executeSearchSerialResult((SearchResultSerialMessage)message.getPayload());
					sendResponse = false;
				}
				else if (message instanceof SearchResultParallelMessage) {
					// doesn't send (N)ACK
					service.getOperationManager()
							.executeSearchParallelResult((SearchResultParallelMessage)message.getPayload());
					sendResponse = false;
				}
				else if (message instanceof JoinRequestMessage) {
					responseMsg = service.getOperationManager()
							.executeJoinRequest((JoinRequestMessage)message.getPayload());
				}

				// messages from a (foreign) service to an entry SkipgraphNode on this service
				// data modification
				else if (message instanceof UpdateMessage) {
					((UpdateMessage) message).addHop(service.getLocalContact().getPeerID());
					SkipgraphNodeController entryNodeController = service.getEntryNodeController(); 
					if (entryNodeController == null) {
						responseMsg = new UpdateNACKMessage(getLocalContact(), message.getSender(),
								message.getOperationID(), false);
					}
					else {
						boolean success = entryNodeController
									.update(message.getOperationID(),
											((UpdateMessage)message.getPayload()).getInputElements(), 
											((UpdateMessage)message.getPayload()).getDeleteElements());
						// create (N)ACK messages
						if (success) {
							responseMsg = new UpdateACKMessage(getLocalContact(), message.getSender(),
									message.getOperationID());
						}
						else {
							responseMsg = new UpdateNACKMessage(getLocalContact(), message.getSender(),
									message.getOperationID());
						}
					}
				}
				// search query
				else if (message instanceof SearchQueryMessage) {
					SkipgraphNodeController entryNodeController = service.getEntryNodeController(); 
					if (entryNodeController == null) {
						responseMsg = new SearchQueryNACKMessage(getLocalContact(), message.getSender(),
								message.getOperationID(), false);
					}
					else {
						if (service.getEntryNodeController()
								.search((SearchQueryMessage)message.getPayload())) {
							responseMsg = new SearchQueryACKMessage(getLocalContact(), message.getSender(),
									message.getOperationID());
						}
						else {
							if (!SkipgraphServiceConstants.parallelSearch) {
								responseMsg = new SearchQueryNACKMessage(getLocalContact(), message.getSender(),
										message.getOperationID());
							}
						}
					}
				}

			}

			// messages from a (foreign) SkipgraphNode to a specific SkipgraphNode on this service
			else if (message instanceof AbstractSkipgraphNodeRequestMessage) {
				AbstractSkipgraphNodeRequestMessage requestMsg = (AbstractSkipgraphNodeRequestMessage) message;
				
				if (SkipgraphServiceConstants.LOG_DHT) 
					service.log(message.toString());
				SkipgraphNodeController nodeController = 
						service.getNodeControllerForKey(requestMsg.getReceivingNodeID(), requestMsg);
				// test whether the addressed SkipgraphNode is available
				if (nodeController != null) {
					nodeController.incrementIncomingMessages();
					if (requestMsg instanceof UpdateForwardingMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeUpdateForwarding((UpdateForwardingMessage)requestMsg.getPayload());
					}
					else if (message instanceof SearchForwardingSerialMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeSearchSerialForwarding((SearchForwardingSerialMessage)requestMsg.getPayload());
					}
					else if (message instanceof SearchForwardingParallelMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeSearchParallelForwarding((SearchForwardingParallelMessage)requestMsg.getPayload());
					}
					else if (requestMsg instanceof RangeAdjustmentMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeRangeAdjustment((RangeAdjustmentMessage)requestMsg.getPayload());
					}
					else if (message instanceof RemainingSizeMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeRemainingSize((RemainingSizeMessage)requestMsg.getPayload());
					}
					else if (requestMsg instanceof JoinLevelRequestMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeJoinLevelRequest((JoinLevelRequestMessage)requestMsg.getPayload());
					}
					else if (requestMsg instanceof JoinLevelNotifyMessage) {
						nodeController.getOperationResponder()
						.executeJoinLevelNotify((JoinLevelNotifyMessage)requestMsg.getPayload());
						sendResponse = false;
					}
					else if (message instanceof JoinLevelReplyMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeJoinLevelReply((JoinLevelReplyMessage)requestMsg.getPayload());
					}
					else if (requestMsg instanceof HandshakeMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeHandshake((HandshakeMessage)requestMsg.getPayload()); 
					}
					else if (message instanceof LoadBalancingMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeLoadBalancing((LoadBalancingMessage)requestMsg.getPayload());
					}
					else if (message instanceof ReplaceContactsMessage) {
						responseMsg = nodeController.getOperationResponder()
								.executeReplaceContacts((ReplaceContactsMessage)requestMsg.getPayload());
					}
				}
			}

			else {
				// throw runtime exception as we do not expect any other message here
				throw new RuntimeException("We do not except the message "+message
						+". Please adapt your processMsg(AbstractExtendedDemoMessage, TransMsgEvent) method.");
			}

			if (sendResponse) {
				// create default NACK message if no other
				if (responseMsg == null) {
					responseMsg = new DefaultNACKMessage(getLocalContact(), message.getSender(),
							message.getOperationID());
				}
				/*
				 *  send the ACK or NACK as a reply to the requesting node.
				 *  Please note that the node receiving the ACK/NACK will receive
				 *  it via a callback it state in the CommunicationManager#send() method!
				 */
				service.getCommunicationManager().sendReply(responseMsg, receivingEvent);
			}
		}
	}
	
}
