package org.peerfact.impl.service.skipgraph.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.peerfact.impl.service.skipgraph.SkipgraphServiceConstants;
import org.peerfact.impl.service.skipgraph.node.ContactLevel;
import org.peerfact.impl.service.skipgraph.node.ContactTable;
import org.peerfact.impl.service.skipgraph.node.ElementTable;
import org.peerfact.impl.service.skipgraph.node.SkipgraphContact;
import org.peerfact.impl.service.skipgraph.node.SkipgraphNode;
import org.peerfact.impl.simengine.Simulator;



/**
 * A proprietary 'visual' analyzer.
 * Creates a representation of the skip graph in the dot-file format.
 * Can plot files with each build if configured. 
 * 
 * @author Andreas Funke
 * @version 1.0, 03/22/2017
 */
public class DotFileBuilder {
	
	private static class LevelMapTuple {

		private int id;
		private int index;
		private ContactLevel level;

		public LevelMapTuple(int id, int index, ContactLevel level) {
			this.id = id;
			this.index = index;
			this.level = level;
		}

		public int getId() {
			return id;
		}

		public int getIndex() {
			return index;
		}

		public ContactLevel getLevel() {
			return level;
		}
	}


	private static class NodeTuple implements Comparable<NodeTuple> {

		private BigInteger peerID;
		private SkipgraphNode node;

		public NodeTuple(BigInteger peerID, SkipgraphNode node) {
			this.peerID = peerID;
			this.node = node;
		}

		public BigInteger getPeerID() {
			return peerID;
		}

		public SkipgraphNode getNode() {
			return node;
		}

		@Override
		public int compareTo(NodeTuple ext) {
			return node.compareTo(ext.getNode());
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			
			if (!(o instanceof NodeTuple))
				return false;
			
			NodeTuple ext = (NodeTuple)o;
			if (node.getNodeID().equals(ext.getNode().getNodeID()))
				return true;
						
			return false;
		}

		@Override
		public int hashCode() {
			return node.getNodeID().hashCode();
		}

		@Override
		public String toString() {
			return "NodeTuple [peer=" + SGUtil.formatID(peerID) + ", node=" + node + "]";
		}

	}


	private static final List<NodeTuple> globalNodeList = new LinkedList<>();
	private static final Map<String, LinkedList<LevelMapTuple>> graphPerLevelAndPrefixMap = new LinkedHashMap<>();
	private static final Map<String, Integer> nodeIdToCountId = new LinkedHashMap<>();
	private static final String FILE_NAME = "aaa_dot_test";
	private static final String FILE_EXTENSION = ".dot";
	private static int fileCounter = 0;
	private static boolean plotAutomatically = true;
	private static boolean cluster = false;
	private static boolean strict = true;
	private static boolean concentrate = true;
	private static String code = null;
	

	@Deprecated
	public static void add(BigInteger peerID, SkipgraphNode node) {
		//globalNodeList.add(new NodeTuple(peerID, node));
	}
	
	public static void addNode(BigInteger peerID, SkipgraphNode node) {
		NodeTuple nodeTuple = new NodeTuple(peerID, node);

		// if the node is already in the global list then it was moved to another peer
		// update with the new peer
		if (globalNodeList.contains(nodeTuple)) {
			System.out.println("globalNodeList contains node:" +nodeTuple);
			System.out.print("updating globalNodeList with: "+nodeTuple);
			globalNodeList.remove(nodeTuple);
		}
		else {
			nodeIdToCountId.put(node.getNodeID().toString(), node.getGlobalCountID());
			System.out.print("adding to globalNodeList: "+nodeTuple);
		}
		globalNodeList.add(nodeTuple);
		System.out.println(" @ "+Simulator.getFormattedTime(Simulator.getCurrentTime())
				+ " SGsize="+globalNodeList.size()
				+" (seed="+Simulator.getSeed()+")"
				);
	}
	
	
	public static void removeNode(BigInteger peerID, SkipgraphNode node) {
		NodeTuple nodeTuple = new NodeTuple(peerID, node);
		if (globalNodeList.remove(nodeTuple)) {
			System.out.println("removing from globalNodeList: "+nodeTuple
					+" @ "+Simulator.getFormattedTime(Simulator.getCurrentTime())
					+ " SGsize="+globalNodeList.size()
					);
		}
		else {
			System.out.println("could not remove from globalNodeList: "+nodeTuple
					+" @ "+Simulator.getFormattedTime(Simulator.getCurrentTime())
					+ " SGsize="+globalNodeList.size()
					+" (seed="+Simulator.getSeed()+")"
					);
		}
	}
	
	
	public static void buildAndPrint() {
		Collections.sort(globalNodeList);
		print();
		build();
	}
	
	
	private static void print() {
		if (SkipgraphServiceConstants.printNodesWhileLogging) {
			System.out.println("\n-------------------- PRINTING SKIPGRAPH ------------------\n");
			for (NodeTuple tuple : globalNodeList) {
				System.out.println("peer="+SGUtil.formatID(tuple.getPeerID()));
				tuple.getNode().print();
			}
		}
	}
	
	
	private static int getFileCounter() {
		return ++fileCounter;
	}
	
	
	public static void build() {
		// format outer frame
		StringBuilder sbMain = new StringBuilder();
		sbMain.append(String.format("%sstrict\n digraph SkipGraph {\n\n",
				strict ? "" : "#"));
		sbMain.append("\tranksep=1.0\n" +
				"\tconcentrate=" + concentrate + "\n\n");

		// format vertical subgraphs with "pseudo"-edges
		int clusterInt = cluster ? 1 : 50;
		StringBuilder sbVertical = new StringBuilder();
		sbVertical.append("\t# vertical\n" +
				"\tedge [dir=none style=dashed, weight=" + clusterInt + "]\n");

		// cluster for element table content
		StringBuilder sbContent = new StringBuilder();
		sbContent.append("\n\t# horizontal\n" +
				"\tedge [dir=forward, style=solid, weight=1]\n" +
				"\tsubgraph cluster_content {\n" +
				"\t\t#rank=same\n");

		// format horizontal subgraphs with "real"-edges
		StringBuilder sbHorizontal = new StringBuilder();

		for (NodeTuple nt : globalNodeList) {
			addToLevelMap(nt.getNode());
		}

		for (NodeTuple nt : globalNodeList) {
			SkipgraphNode node = nt.getNode();
			sbVertical.append(formatVerticalForNode(node));
			sbContent.append("\t\t\"" + node.getGlobalCountID() + "\" [shape=box, label=\n");
			sbContent.append("\t\t\t<\n");
			sbContent.append(formatElementTableForNode(nt.getPeerID(), node));
			sbContent.append(formatContactTableForNode(node));
			sbContent.append("\t\t\t>]\n");
		}

		sbHorizontal.append(generateHorizontalSubgraphsFromLevelMap());
		sbContent.append("\t}\n\n");
		sbMain.append(sbVertical).append(sbContent).append(sbHorizontal);
		sbMain.append("\n}\n");
		code = sbMain.toString();
		writeFile(FILE_NAME+"_"+getFileCounter());
		
		//globalNodeList.clear();
		graphPerLevelAndPrefixMap.clear();
		//nodeIdToCountId.clear();
	}


	private static String formatVerticalForNode(SkipgraphNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append("\tsubgraph {\n");
		sb.append(String.format("\t\t\"%d\"", node.getGlobalCountID()));
		for (int i=0; i<node.getContactTable().size(); i++) {
			sb.append(String.format(" -> \"%d.%d\"", node.getGlobalCountID(), i));
		}
		sb.append("\n\t\t}\n");

		return sb.toString();
	}


	private static String formatElementTableForNode(BigInteger peerID, SkipgraphNode node) {
		ElementTable elementTable = node.getElementTable();
		if (elementTable == null) {
			return String.format("\t\t\t\t<B>ID: %d %s [...,...]</B> on peer=%s<BR ALIGN=\"LEFT\"/>\n",
					node.getGlobalCountID(),
					SGUtil.formatID(node.getNodeID()),
					SGUtil.formatID(peerID));
		}
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\t\t\t\t<B>ID: %d %s %s, %s)</B> on peer=%s<BR ALIGN=\"LEFT\"/>\n",
				node.getGlobalCountID(),
				SGUtil.formatID(node.getNodeID()),
				elementTable.getRangeStart() == null ? "(-inf" : "["+elementTable.getRangeStart(),
				elementTable.getRangeEnd() == null ? "inf" : elementTable.getRangeEnd(),
				SGUtil.formatID(peerID)));
		elementTable.sort();
		for (int i = 0; i < elementTable.size(); i++) {
			sb.append(String.format("\t\t\t\t%s<BR ALIGN=\"LEFT\"/>\n", elementTable.get(i)));
		}
		sb.append(String.format("\t\t\t<B>size:</B>%d, <B>min-size:</B>%d, <B>max-size:</B>%d<BR ALIGN=\"LEFT\"/>\n",
				elementTable.size(), SkipgraphServiceConstants.getElementTableMinSize(), 
				SkipgraphServiceConstants.getElementTableMaxSize()));

		return sb.toString();
	}


	private static String formatContactTableForNode(SkipgraphNode node) {
		ContactTable contactTable = node.getContactTable();
		StringBuilder sb = new StringBuilder();
		sb.append((String.format("\t\t\t\t<B>contacts:</B> (<B>size:</B>%d)<BR ALIGN=\"LEFT\"/>\n", contactTable.size())));
		for (int i = 0; i < contactTable.size(); i++) {
			int prefix = contactTable.getLevel(i).getPrefix();
			
			SkipgraphContact prev = contactTable.getLevel(i).getPrevContact(); 
			Integer prevID = nodeIdToCountId.get(prev.getNodeID().toString());
			String prevStart = prev.getRangeStart() == null ? "(-inf" : "["+prev.getRangeStart().toString();
			String prevEnd = prev.getRangeEnd() == null ? "inf)" : prev.getRangeEnd().toString()+")";

			SkipgraphContact next = contactTable.getLevel(i).getNextContact(); 
			Integer nextID = nodeIdToCountId.get(next.getNodeID().toString());
			String nextStart = next.getRangeStart() == null ? "(-inf" : "["+next.getRangeStart().toString();
			String nextEnd = next.getRangeEnd() == null ? "inf)" : next.getRangeEnd().toString()+")";

			sb.append(String.format("\t\t\t\t%d_%d: prev=%d %s, %s, next=%d %s, %s<BR ALIGN=\"LEFT\"/>\n", 
					i, prefix, prevID, prevStart, prevEnd, nextID, nextStart, nextEnd));
		}
		return sb.toString();
	}


	private static void addToLevelMap(SkipgraphNode node) {
		ContactTable contactTable = node.getContactTable();
		String key = "";
		for (int i=0; i<contactTable.size(); i++) {
			key += Integer.toString(contactTable.getLevel(i).getPrefix());
			if (graphPerLevelAndPrefixMap.containsKey(key)) {
				LevelMapTuple tuple = new LevelMapTuple(node.getGlobalCountID(), i, contactTable.getLevel(i));
				graphPerLevelAndPrefixMap.get(key).add(tuple);
			}
			else {
				LinkedList<LevelMapTuple> levelList = new LinkedList<>();
				levelList.add(new LevelMapTuple(node.getGlobalCountID(), i, contactTable.getLevel(i)));
				graphPerLevelAndPrefixMap.put(key, levelList);
			}
		}
	}

	
	private static StringBuilder generateHorizontalSubgraphsFromLevelMap() {
		if (graphPerLevelAndPrefixMap.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		String clusterStr = cluster ? "cluster_" : "";

		for (Map.Entry<String, LinkedList<LevelMapTuple>> entry : graphPerLevelAndPrefixMap.entrySet()) {
			sb.append(String.format("\tedge [color=%s]\n", colorByInt(keyToInt(entry.getKey()))));
			sb.append(String.format("\tsubgraph %s%s {\n", clusterStr, entry.getKey()));
			sb.append("\t\trank=same\n");
			sb.append(String.format("\t\tlabel=\"Level %d (%s)\"\n", entry.getValue().getFirst().getIndex(),
					entry.getKey()));
			for (LevelMapTuple contactTupel : entry.getValue()) {
				sb.append("\t\tsubgraph {\n");
				sb.append(String.format("\t\t\t\"%d.%d\" -> \"%s.%d\"\n",
						contactTupel.getId(),
						contactTupel.getIndex(),
						contactTupel.getLevel().getNextContact() == null ? "null" :
							nodeIdToCountId.get(contactTupel.getLevel().getNextContact().getNodeID().toString()), 
						contactTupel.getIndex()));
				sb.append(String.format("\t\t\t\"%d.%d\" -> \"%s.%d\"\n",
						contactTupel.getId(),
						contactTupel.getIndex(),
						contactTupel.getLevel().getPrevContact() == null ? "null" :
							nodeIdToCountId.get(contactTupel.getLevel().getPrevContact().getNodeID().toString()),
						contactTupel.getIndex()));
				sb.append("\t\t}\n");
			}
			sb.append("\t}\n\n");
		}

		return sb;
	}


	private static int keyToInt(String key) {
		int keyInt = 0;
		for (int i=0; i<key.length(); i++) {
			keyInt = keyInt << 1;
			switch (key.charAt(i)) {
				case '0':
					break;
				case '1':
					keyInt++;
					break;
				default:
					return -1;
			}
		}
		return keyInt;
	}


	private static String colorByInt(int i) {
		switch (i % 23) {
			case 0:
				return "blue";
			case 1:
				return "red";
			case 2:
				return "green";
			case 3:
				return "orange";
			case 4:
				return "cyan";
			case 5:
				return "violet";
			case 6:
				return "grey";
			case 7:
				return "yellow";
			case 8:
				return "aquamarine";
			case 9:
				return "darkslategray";
			case 10:
				return "brown";
			case 11:
				return "burlywood";
			case 12:
				return "cadetblue";
			case 13:
				return "chartreuse";
			case 14:
				return "chocolate";
			case 15:
				return "cornflowerblue";
			case 16:
				return "cornsilk4";
			case 17:
				return "crimson";
			case 18:
				return "darkgoldenrod";
			case 19:
				return "darkolivegreen";
			case 20:
				return "deeppink";
			case 21:
				return "indigo";
			case 22:
				return "navy";
		}
		return "black";
	}


	private static void writeFile(String filename) {
		try {
			File file = new File(Simulator.getOuputDir(), filename + FILE_EXTENSION);

			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(code);
			bw.close();
			if (plotAutomatically) {
				plot(file.getAbsolutePath());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void plot(String filename) {
		if (SkipgraphServiceConstants.plotDotFiles) {
			String outfile = filename.substring(0, filename.lastIndexOf(".")) + ".png";
			try {
				Runtime r = Runtime.getRuntime();
				r.exec(String.format("dot -Tpng %s -o %s", filename, outfile));
				//r.exec("xdg-open " + outfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
