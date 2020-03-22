package org.peerfact.impl.service.skipgraph.node;

import static org.peerfact.impl.application.capacitymanager.CapacityManagerApplicationConstants.ind4;

import org.peerfact.Constants;
import org.peerfact.api.overlay.dht.DHTObject;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class ContactLevel implements DHTObject {

	/* *******************************************
	 ****************** FIELDS *******************
	 ******************************************* */

	private int prefix;

	private SkipgraphContact prevContact;
	
	private SkipgraphContact nextContact;


	
	/* *************************************************
	 ****************** CONSTRUCTORS *******************
	 ************************************************* */

	public ContactLevel(SkipgraphContact prevContact, SkipgraphContact nextContact, int prefix) {
		this.prefix = prefix;
		this.prevContact = prevContact;
		this.nextContact = nextContact;
	}


	
	/* ************************************************
	 ****************** GETTERS ***********************
	 ************************************************ */

	public int getPrefix() {
		return prefix;
	}

	public SkipgraphContact getPrevContact() {
		return prevContact;
	}

	public SkipgraphContact getNextContact() {
		return nextContact;
	}


	
	/* ************************************************
	 ****************** SETTERS ***********************
	 ************************************************ */

	boolean setPrevContact(SkipgraphContact contactUpdate) {
		if (contactUpdate == null) {
			try {
				throw new NullPointerException("cannot set prev contact to 'null'");
			}
			catch (NullPointerException e) {
				e.getMessage();
				e.printStackTrace();
			}
		}
		
		if (prevContact != null && prevContact.equals(contactUpdate))
			return false;

		prevContact = contactUpdate;
		return true;
	}


	boolean setNextContact(SkipgraphContact contactUpdate) {
		if (contactUpdate == null) {
			try {
				throw new NullPointerException("cannot set next contact to 'null'");
			}
			catch (NullPointerException e) {
				e.getMessage();
				e.printStackTrace();
			}
		}
		
		if (nextContact != null && nextContact.equals(contactUpdate))
			return false;

		nextContact = contactUpdate;
		return true;
	}
	
	
	boolean setPrefix(int prefixUpdate) {
		if (prefix == prefixUpdate) {
			return false;
		}
		prefix = prefixUpdate;
		return true;
	}


	
	/* ************************************************
	 ****************** LOGGING ***********************
	 ************************************************ */

	@Override
	public String toString() {
		return "prefix="+prefix+":\n"
				+ind4+"prev: "+getPrevContact()+"\n"
				+ind4+"next: "+getNextContact();
	}



	/* ************************************************
	 ****************** INTERFACE *********************
	 ************************************************ */

	@Override
	public long getTransmissionSize() {
		long size = 0;
		// prefix
		size += Constants.INT_SIZE;
		// prev + next
		size += prevContact.getTransmissionSize() + nextContact.getTransmissionSize();
		return size;
	}


	public boolean equalsExceptPrefix(ContactLevel other) {
		if (other == null)
			return false;
		if (nextContact == null) {
			if (other.nextContact != null)
				return false;
		} else if (!nextContact.equals(other.nextContact))
			return false;
		if (prevContact == null) {
			if (other.prevContact != null)
				return false;
		} else if (!prevContact.equals(other.prevContact))
			return false;
		return true;
	}
	
	

}
