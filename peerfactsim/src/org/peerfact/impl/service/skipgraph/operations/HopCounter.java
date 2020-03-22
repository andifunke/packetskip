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

package org.peerfact.impl.service.skipgraph.operations;

/**
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class HopCounter {

	private int bootstrap = 0;
	private int skipgraph = 0;
	private int n_ACK = 0;
	private int notify = 0;
	
	public int getBootstrap() {
		return bootstrap;
	}

	public int getSkipgraph() {
		return skipgraph;
	}

	public int getN_ACK() {
		return n_ACK;
	}

	public int getNotify() {
		return notify;
	}

	public int getSum() {
		return bootstrap+skipgraph+n_ACK+notify;
	}
	
	public void incrementBootstrapCount() {
		bootstrap++;
	}

	public void incrementSkipgraphCount() {
		skipgraph++;
	}

	public void incrementN_ACKCount() {
		n_ACK++;
	}

	public void incrementNotifyCount() {
		notify++;
	}
	
	@Override
	public String toString() {
		return "sum="+getSum()+", bootstrap="+bootstrap+", skipgraph="+skipgraph+", (N)ACK="+n_ACK+", notify="+notify;
	}
}
