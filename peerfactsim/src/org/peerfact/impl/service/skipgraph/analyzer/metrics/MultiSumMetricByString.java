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

package org.peerfact.impl.service.skipgraph.analyzer.metrics;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.peerfact.Constants;
import org.peerfact.impl.analyzer.AbstractFileAnalyzer;
import org.peerfact.impl.analyzer.AbstractFileMetricAnalyzer;
import org.peerfact.impl.analyzer.metric.Metric;


/**
 * A copied version of the MultiSumMetric that uses strings instead of classes as keys.
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class MultiSumMetricByString<Peer, Num extends Number> implements Metric<Peer, List<Num>> {

	private String name;

	private String unit;

	private Map<Peer, Map<String, Double>> peerValues = new LinkedHashMap<>();

	private Map<String, Double> currentSum = new LinkedHashMap<>();

	private List<String> relevantValues;

	private int topX;

	protected String titlePrefix = "Sum of ";

	private boolean withAggregated;

	public MultiSumMetricByString(String name, String unit, List<String> relevantValues,int topX,boolean withAggregated) {
		super();
		this.name = name;
		this.unit = unit;
		this.relevantValues = relevantValues;
		this.topX = topX;
		this.withAggregated = withAggregated;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getUnit() {
		return this.unit;
	}

	@Override
	public List<String> getHeadlines() {
		List<String> ret = new LinkedList<>();
		for(String string : relevantValues){
			ret.add(this.name+"_"+string);
		}
		if(withAggregated) ret.add(this.name+"_aggregated");
		// top X (only for peer plots!)
		for(int i = topX; i > 0; i-- ) ret.add(this.name+"_top+"+i);
		return ret;
	}

	@Override
	public void generateTimePlots(Writer script, String analyzer,
			int startIndex) throws IOException {
		script.write(Constants.COMMENT_LINE
				+ "------------------------------------------------------------"
				+ Constants.LINE_END);
		script.write(Constants.COMMENT_LINE
				+ " Plot " + getName() + Constants.LINE_END);
		script.write(Constants.COMMENT_LINE
				+ "------------------------------------------------------------"
				+ Constants.LINE_END);
		script.write("set term png giant size 1024,768 font 'Helvetica,20'"
				+ Constants.LINE_END);
		script.write("set output '" + analyzer + "-"
				+ getName().replaceAll(" ", "_")
				+ AbstractFileMetricAnalyzer.FILE_PNG_EXTENSION + "'"
				+ Constants.LINE_END);
		script.write("set key top left" + Constants.LINE_END);
		script.write("set xlabel 'Time [minutes]'"
				+ Constants.LINE_END);
		script.write("set ylabel '" + getName() + " [" + getUnit() + "]'"
				+ Constants.LINE_END);
		script.write("plot ");
		// one per relevant value
		int index = startIndex; 
		for(String key : relevantValues){
			script.write("'" + analyzer + AbstractFileAnalyzer.FILE_EXTENSION
					+ "' using 1:" + (index++) + " title '"+this.titlePrefix+getName()+"_"+key
					+ "' with lines axis x1y1 smooth unique,");
		}
		// aggregated
		if(withAggregated)script.write("'" + analyzer + AbstractFileAnalyzer.FILE_EXTENSION
				+ "' using 1:" + (index++) + " title '"+this.titlePrefix+ getName()+"_aggregated'"
				+ " with lines axis x1y1 smooth unique");
		script.write(Constants.LINE_END);
		script.write(Constants.LINE_END);
		script.write("set terminal pdf monochrome font 'Helvetica,10'"
				+ Constants.LINE_END);
		script.write("set output '" + analyzer + "-"
				+ getName().replaceAll(" ", "_")
				+ AbstractFileMetricAnalyzer.FILE_PDF_EXTENSION + "'"
				+ Constants.LINE_END);
		script.write("replot" + Constants.LINE_END);
	}

	@Override
	public List<String> getTimeMeasurementValues(long currentTime) {
		List<String> ret = new LinkedList<>();
		for(String string : relevantValues){
			if(currentSum.containsKey(string)){
				ret.add(currentSum.get(string).toString());
			} else {
				ret.add("0");
			}
		}
		// aggregated
		if(withAggregated){
			double aggregated = 0;
			for(Double val : this.currentSum.values()) aggregated+=val;
			ret.add(String.valueOf(aggregated));			
		}
		// dummy for topX
		for(int i = topX; i > 0; i-- ) ret.add("0");
		return ret;
	}

	@Override
	public void resetTimeMeasurement() {
		this.currentSum.clear();
	}

	@Override
	public void generatePeerPlots(Writer script, String analyzer,
			int startIndex) throws IOException {
		// relevant and aggregated
		int index = startIndex;
		int aggregatedIndex = index;
		// if we have to plot something in the relevantValues/Aggregated plot
		if(relevantValues.size()>0 || withAggregated){
			script.write(Constants.COMMENT_LINE
					+ "------------------------------------------------------------"
					+ Constants.LINE_END);
			script.write(Constants.COMMENT_LINE
					+ " Plot " + getName() + Constants.LINE_END);
			script.write(Constants.COMMENT_LINE
					+ "------------------------------------------------------------"
					+ Constants.LINE_END);
			script.write("set term png giant size 1024,768 font 'Helvetica,20'"
					+ Constants.LINE_END);
			script.write("set output '" + analyzer + "-"
					+ getName().replaceAll(" ", "_")
					+ AbstractFileMetricAnalyzer.FILE_PNG_EXTENSION + "'"
					+ Constants.LINE_END);
			script.write("set key top right" + Constants.LINE_END);
			script.write("set xlabel 'Peer'" + Constants.LINE_END);
			script.write("set ylabel '" + getName() + " [" + getUnit() + "]'"
					+ Constants.LINE_END);
			script.write("plot ");
			for(String key : relevantValues){
				script.write("'" + analyzer + AbstractFileAnalyzer.FILE_EXTENSION
						+ "' using 1:" + (index++) + " title '"+this.titlePrefix+ getName()+"_"+key
						+ "' with lines axis x1y1 smooth unique,");
			}
			// aggregated
			aggregatedIndex = index;
			if(withAggregated){
				script.write("'" + analyzer + AbstractFileAnalyzer.FILE_EXTENSION
						+ "' using 1:" + (index++) + " title '"+this.titlePrefix+ getName()+"_aggregated'"
						+ " with lines axis x1y1 smooth unique,");
			}
			script.write(Constants.LINE_END);
			script.write(Constants.LINE_END);
			script.write("set terminal pdf monochrome font 'Helvetica,10'"
					+ Constants.LINE_END);
			script.write("set output '" + analyzer + "-"
					+ getName().replaceAll(" ", "_")
					+ AbstractFileMetricAnalyzer.FILE_PDF_EXTENSION + "'"
					+ Constants.LINE_END);
			script.write("replot" + Constants.LINE_END);
		}

		// top 5 stuff
		script.write(Constants.COMMENT_LINE
				+ "------------------------------------------------------------"
				+ Constants.LINE_END);
		script.write(Constants.COMMENT_LINE
				+ " Plot top"+this.topX+" " + getName() + Constants.LINE_END);
		script.write(Constants.COMMENT_LINE
				+ "------------------------------------------------------------"
				+ Constants.LINE_END);
		script.write("set term png giant size 1024,768 font 'Helvetica,20'"
				+ Constants.LINE_END);
		script.write("set output '" + analyzer + "-"
				+ getName().replaceAll(" ", "_") + "_top"+this.topX
				+ AbstractFileMetricAnalyzer.FILE_PNG_EXTENSION + "'"
				+ Constants.LINE_END);
		script.write("set key top right" + Constants.LINE_END);
		script.write("set xlabel 'Peer'" + Constants.LINE_END);
		script.write("set ylabel '" + getName() + " [" + getUnit() + "]'"
				+ Constants.LINE_END);
		script.write("plot ");
		LinkedList<Entry<String,Double>> topAsc = examineTopAsc();
		Entry<String,Double> current;
		for(int i = 0; i < this.topX; i++){
			try {
				current = topAsc.get(i);				
			} catch(IndexOutOfBoundsException e){
				continue;
			}
			script.write("'" + analyzer + AbstractFileAnalyzer.FILE_EXTENSION
					+ "' using 1:" + (index++) + " title '"+this.titlePrefix+ getName()+"_"+current.getKey()
					+ " (top"+(i+1)+")' with lines axis x1y1 smooth unique,");
		}
		// aggregated
		if(withAggregated){
			script.write("'" + analyzer + AbstractFileAnalyzer.FILE_EXTENSION
					+ "' using 1:" + (aggregatedIndex) + " title '"+this.titlePrefix+ getName()+"_aggregated'"
					+ " with lines axis x1y1 smooth unique,");			
		}
		script.write(Constants.LINE_END);
		script.write(Constants.LINE_END);
		script.write("set terminal pdf monochrome font 'Helvetica,10'"
				+ Constants.LINE_END);
		script.write("set output '" + analyzer + "-"
				+ getName().replaceAll(" ", "_")+ "_top"+this.topX
				+ AbstractFileMetricAnalyzer.FILE_PDF_EXTENSION + "'"
				+ Constants.LINE_END);
		script.write("replot" + Constants.LINE_END);
	}

	/**
	 * Helper method to examine the top this {@link #topX}
	 * messages peers have sent throughout the simulation.
	 * @return
	 */
	private LinkedList<Entry<String,Double>> examineTopAsc(){
		// first: examine the overall sum - just sum up all peer values
		Map<String,Double> summedup = new LinkedHashMap<>();
		Double tmp;
		for (Entry<Peer,Map<String,Double>> val : this.peerValues.entrySet()){
			for(Entry<String, Double> entry : val.getValue().entrySet()){
				tmp = summedup.get(entry.getKey());
				if(tmp == null) tmp = 0d;
				tmp+=entry.getValue();
				summedup.put(entry.getKey(), tmp);
			}
		}
		// second: sort the entries and return
		LinkedList<Entry<String,Double>> sortedEntries = 
				new LinkedList<>(summedup.entrySet());
		Collections.sort(sortedEntries, 
				new Comparator<Entry<String,Double>>() {
			@Override
			public int compare(Entry<String,Double> e1, Entry<String,Double> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		}
				);
		return sortedEntries;
	}

	@Override
	public List<String> getPeerMeasurementValues(Object peer) {
		List<String> ret = new LinkedList<>();
		// relevant classes
		for(String str : relevantValues){
			if (peerValues.containsKey(peer)) {
				if(peerValues.get(peer).containsKey(str)){
					ret.add(peerValues.get(peer).get(str).toString());					
				} else {
					ret.add("0");
				}
			} else {
				ret.add("0");
			}
		}
		// aggregated
		if(withAggregated){
			if(peerValues.containsKey(peer)){
				Map<String, Double> map = peerValues.get(peer);
				double aggregated = 0;
				for(Double val : map.values()) aggregated+=val;
				ret.add(String.valueOf(aggregated));
			} else {
				ret.add("0");
			}			
		}
		// topX
		LinkedList<Entry<String,Double>> topAsc = examineTopAsc();
		Entry<String,Double> entry =null;
		String current;
		Double val;
		for(int i = 0; i < this.topX; i++){
			try {
				entry = topAsc.get(i);
				current = entry.getKey();
				val = this.peerValues.get(peer).get(current);
				if(val==null) val = 0d;
			} catch(IndexOutOfBoundsException | NullPointerException e){
				val = 0d;
			}
			ret.add(val.toString());
		}
		return ret;
	}

	@Override
	public void resetPeerMeasurement() {
		this.peerValues.clear();
	}

	public void addValue(Peer peer, String key, Num value) {
		// for peer values
		Map<String, Double> map = peerValues.get(peer);
		if (map == null) map = new LinkedHashMap<>();

		Double oldValue = map.get(key);
		if(oldValue == null) oldValue = 0d;
		map.put(key, oldValue+value.doubleValue());

		// write back to map peerValues
		peerValues.put(peer, map);

		// for current sum
		if(currentSum.containsKey(key)){
			currentSum.put(key, currentSum.get(key) + value.doubleValue());
		} else {
			currentSum.put(key, value.doubleValue());
		}
	}

}
