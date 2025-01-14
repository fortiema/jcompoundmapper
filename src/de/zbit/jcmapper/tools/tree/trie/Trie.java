package de.zbit.jcmapper.tools.tree.trie;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import de.zbit.jcmapper.tools.tree.trie.gml.ConstantsYEditColor;
import de.zbit.jcmapper.tools.tree.trie.gml.GMLEdge;
import de.zbit.jcmapper.tools.tree.trie.gml.GMLNode;
import de.zbit.jcmapper.tools.tree.trie.pattern.PatternContainer;


public class Trie implements Serializable, Cloneable {

	/**
	 * The id manager assignes unique labels to the nodes in a GML file
	 * 
	 * @author hinselma
	 * 
	 */
	public class IdManager {
		private int id = 0;

		public int getLastId() {
			return this.id;
		}

		public void incrID() {
			this.id++;
		}

		public void setId(int id) {
			this.id = id;
		}
	}

	private static final long serialVersionUID = 1L;
	private int nfeatures = 0;
	private final TrieNode root = new TrieNode();
	private int totalfeaturecount = 0;
	private int totalnodecount;

	private Float weight = 0.0f;

	/**
	 * 
	 * creates a consensus trie which contains the maximum common subtree
	 * 
	 * @param trie2
	 * @return
	 */
	public Trie and(Trie trie2) {

		// clone one of the tries
		final Trie trie1clone = (Trie) this.clone();
		final Trie trie2clone = (Trie) trie2.clone();

		// create root of consensus trie
		final Trie consensus = new Trie();

		this.getConsensusTrie(0, trie1clone.getRoot(), trie2clone.getRoot(), consensus.getRoot());
		consensus.init();

		return consensus;
	}

	/**
	 * returns a clone of this trie
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (final CloneNotSupportedException e) {
			// This should never happen
			throw new InternalError(e.toString());
		}
	}

	/**
	 * computes the percent match of this trie onto the other tree
	 * 
	 * @param trie2
	 * @return
	 */
	public double computePercentMatch(Trie trie2) {

		final double sim_aa = this.getTotalFeatureCount();
		final double sim_bb = trie2.getTotalFeatureCount();
		final double sim_ab = this.computeSimilaritySpectrum(trie2);

		return sim_ab / Math.min(sim_aa, sim_bb);

	}

	/**
	 * returns the the sum of each pairwise matches
	 * 
	 * @param depth
	 * @param node1
	 * @param node2
	 * @param r
	 */
	private void computeSimilarityDirac(int depth, final TrieNode node1, final TrieNode node2, TrieResultContainer r) {

		if ((node1.isFeature() && node2.isFeature()) && (node1.getNumlabel() == node2.getNumlabel())) {
			r.setSimilarity(r.getSimilarity() + 1);
		}

		final ArrayList<TrieNode> children1 = node1.getChildren();
		final ArrayList<TrieNode> children2 = node2.getChildren();

		for (int i = 0; i < children1.size(); i++) {
			for (int j = 0; j < children2.size(); j++) {
				if (children1.get(i).getNumlabel() == children2.get(j).getNumlabel()) {
					this.computeSimilarityDirac(depth + 1, children1.get(i), children2.get(j), r);
				}
			}
		}
	}

	/**
	 * gets the sum of products of equals nodes
	 * 
	 * @param depth
	 * @param node1
	 * @param node2
	 * @param r
	 */
	private void computeSimilarityMin(int depth, TrieNode node1, TrieNode node2, TrieResultContainer r) {

		if ((node1.isFeature() && node2.isFeature()) && (node1.getNumlabel() == node2.getNumlabel())) {
			r.setSimilarity(r.getSimilarity() + (Math.min(node1.getCount(), node2.getCount())));
		}

		final ArrayList<TrieNode> children1 = node1.getChildren();
		final ArrayList<TrieNode> children2 = node2.getChildren();

		for (int i = 0; i < children1.size(); i++) {
			for (int j = 0; j < children2.size(); j++) {
				if (children1.get(i).getNumlabel() == children2.get(j).getNumlabel()) {
					this.computeSimilaritySpectrum(depth + 1, children1.get(i), children2.get(j), r);
				}
			}
		}
	}

	/**
	 * returns the minmax similarity for a pair of tries
	 * 
	 * @param tree2
	 * @return
	 */
	public double computeSimilarityMin(Trie tree2) {
		final TrieResultContainer r = new TrieResultContainer();
		this.computeSimilarityMin(0, this.root, tree2.getRoot(), r);
		return r.getSimilarity();
	}

	/**
	 * gets the sum of products of equals nodes
	 * 
	 * @param depth
	 * @param node1
	 * @param node2
	 * @param r
	 */
	private void computeSimilaritySpectrum(int depth, TrieNode node1, TrieNode node2, TrieResultContainer r) {

		if ((node1.isFeature() && node2.isFeature()) && (node1.getNumlabel() == node2.getNumlabel())) {
			r.setSimilarity(r.getSimilarity() + (node1.getCount() * node2.getCount()));
		}

		final ArrayList<TrieNode> children1 = node1.getChildren();
		final ArrayList<TrieNode> children2 = node2.getChildren();

		for (int i = 0; i < children1.size(); i++) {
			for (int j = 0; j < children2.size(); j++) {
				if (children1.get(i).getNumlabel() == children2.get(j).getNumlabel()) {
					this.computeSimilaritySpectrum(depth + 1, children1.get(i), children2.get(j), r);
				}
			}
		}
	}

	/**
	 * returns the spectrum similarity of two trees
	 * 
	 * @param tree2
	 * @return
	 */
	public int computeSimilaritySpectrum(Trie tree2) {
		if ((this.getNumberOfFeatures() == 0) && ((tree2.getNumberOfFeatures()) == 0)) {
			return 0;
		}
		final TrieResultContainer r = new TrieResultContainer();
		this.computeSimilaritySpectrum(0, this.root, tree2.getRoot(), r);
		return r.getSimilarity();
	}

	/**
	 * gets the sum of products of equals nodes: Final similarity is defined by
	 * the sum of the products of equal features
	 * 
	 * @param depth
	 * @param node1
	 * @param node2
	 * @param r
	 */
	private void computeSimilaritySpectrumWeighted(int depth, TrieNode node1, TrieNode node2, TrieResultContainer r) {

		if ((node1.isFeature() && node2.isFeature()) && (node1.getNumlabel() == node2.getNumlabel())) {
			final double sim = r.getDsimilarity() + (node1.getWeight() * (node2.getWeight()));
			// sim = sim + node1.getCount() * node2.getCount();
			r.setDsimilarity(sim);
		}

		final ArrayList<TrieNode> children1 = node1.getChildren();
		final ArrayList<TrieNode> children2 = node2.getChildren();

		for (int i = 0; i < children1.size(); i++) {
			for (int j = 0; j < children2.size(); j++) {
				if (children1.get(i).getNumlabel() == children2.get(j).getNumlabel()) {
					this.computeSimilaritySpectrumWeighted(depth + 1, children1.get(i), children2.get(j), r);
				}
			}
		}
	}

	/**
	 * returns the spectrum similarity of two trees
	 * 
	 * @param tree2
	 * @return
	 */
	public double computeSimilaritySpectrumWeighted(Trie tree2) {
		if ((this.getNumberOfFeatures() == 0) && ((tree2.getNumberOfFeatures()) == 0)) {
			return 0;
		}
		final TrieResultContainer r = new TrieResultContainer();
		try {
			this.computeSimilaritySpectrumWeighted(0, this.root, tree2.getRoot(), r);
		} catch (final Exception e) {
			System.out.println("An error occured while computing the weighted similarity. Probably features weights are missing.");
			System.exit(1);
		}
		return r.getDsimilarity();
	}

	/**
	 * returns the Tanimoto similarity for this pair of tries
	 * 
	 * @param tree2
	 * @return
	 */
	public double computeSimilarityTanimoto(Trie tree2) {
		final TrieResultContainer r = new TrieResultContainer();
		this.computeSimilarityDirac(0, this.root, tree2.getRoot(), r);
		final int common = r.getSimilarity();
		final int leaves_a = this.getNumberOfFeatures();
		final int leaves_b = tree2.getNumberOfFeatures();
		final double tanimoto = (double) common / (double) (leaves_a + leaves_b - common);
		// if we compare an empty tries return 1;
		if ((leaves_a == 0) && (leaves_b == 0)) {
			return 1.0;
		}
		return tanimoto;
	}

	/**
	 * returns the the sum of each pairwise matches
	 * 
	 * @param depth
	 * @param node1
	 * @param node2
	 */
	private int getConsensusTrie(int depth, final TrieNode node1, final TrieNode node2, TrieNode consensus) {

		// add a leaf
		if ((node1.isFeature() && node2.isFeature()) && (node1.getNumlabel() == node2.getNumlabel())) {
			consensus.setFeature(true);
			final int count = Math.min(node1.getCount(), node2.getCount());
			consensus.setCount(count);
			consensus.setWeight(node1.getWeight());
		}

		// extend trie if possible
		final ArrayList<TrieNode> children1 = node1.getChildren();
		final ArrayList<TrieNode> children2 = node2.getChildren();

		for (int i = 0; i < children1.size(); i++) {
			for (int j = 0; j < children2.size(); j++) {
			if (children1.get(i).getNumlabel() == children2.get(j).getNumlabel()) {
					// create add a new node
					final TrieNode node = new TrieNode();
					node.setLabel(children1.get(i).getLabel());
					node.setNumlabel(children1.get(i).getNumlabel());
					node.setWeight(children1.get(i).getWeight());
					consensus.addChildNode(node);
					// extend recursively the consensus trie
					this.getConsensusTrie(depth + 1, children1.get(i), children2.get(j), node);
				}
			}
		}
		return 0;
	}

	/**
	 * returns a GML representation of this trie
	 * 
	 * @return
	 */
	public String getGMLString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("graph [\n");
		String root = "\tnode [\n"; 
			root = root + "\t\tid -1\n";
			root = root +  "\t\tlabel \"root\"\n" ;
			root = root + "\t\tattribute 0\n" ;
			
			root = root + "\t\tgraphics [\n";
			root = root + "\t\ttype\t\""+"roundrectangle\"\n";
			root = root + "\t\tdropShadowColor\t\"#B3A691\"\n";
			root = root + "\t\tdropShadowOffsetX	5\n";
			root = root + "\t\t]\n";

			root = root + "\t]\n";;
		sb.append(root);
		final IdManager idm = new IdManager();
		idm.setId(-1);
		this.printGMLTrieColoredByElement(1, this.root, sb, idm);
		sb.append("]\n");

		return sb.toString();
	}

	/**
	 * returns a GML representation of this trie
	 * 
	 * @return
	 */
	public String getGMLStringPPP() {
		final StringBuffer sb = new StringBuffer();
		sb.append("graph [\n");
		String root = "\tnode [\n"; 
			root = root + "\t\tid -1\n";
			root = root +  "\t\tlabel \"root\"\n" ;
			root = root + "\t\tattribute 0\n" ;
			
			root = root + "\t\tgraphics [\n";
			root = root + "\t\ttype\t\""+"roundrectangle\"\n";
			root = root + "\t\tdropShadowColor\t\"#B3A691\"\n";
			root = root + "\t\tdropShadowOffsetX	5\n";
			root = root + "\t\t]\n";

			root = root + "\t]\n";;
		sb.append(root);
		final IdManager idm = new IdManager();
		idm.setId(-1);
		this.printGMLTrieColoredByPPP(1, this.root, sb, idm);
		sb.append("]\n");

		return sb.toString();
	}
	
	/**
	 * returns the number of leaves
	 * 
	 * @return
	 */
	public int getNumberOfFeatures() {
		return this.nfeatures;
	}

	/**
	 * returns the root of this trie
	 * 
	 * @return
	 */
	public TrieNode getRoot() {
		return this.root;
	}

	/**
	 * returns the total number of features, taking the counts of the features
	 * into account
	 * 
	 * @return
	 */
	public int getTotalFeatureCount() {
		return this.totalfeaturecount;
	}

	/**
	 * returns the number of all nodes in the trie
	 * 
	 * @return
	 */
	public int getTotalNodeCount() {
		return this.totalnodecount;
	}

	/**
	 * returns the total weight of all features of this trie, returns null if no
	 * weights are set
	 * 
	 * @return
	 */
	public Float getWeight() {
		return this.weight;
	}

	/**
	 * refreshes the number of leaves, e.g. after a new insert operation refrehs
	 * the total number of feature counts
	 * 
	 * @return
	 */
	public void init() {
		final TrieResultContainer r = new TrieResultContainer();
		this.refreshTrie(0, this.root, r);
		this.setNumberOfFeatures(r.getSomevalue1());
		this.setTotalFeatureCount(r.getSomevalue2());
		this.setTotalNodeCount(r.getSomevalue3());
		this.weight = new Float(r.getSomevalue4());
	}

	/**
	 * inserts a Simple pattern recursively into the trie
	 * 
	 * @param depth
	 * @param p
	 * @param node
	 */

	private void insertPattern(int depth, PatternContainer p, TrieNode node) {
		if (depth > (p.getPatterns().size() - 1)) {
			return;
		}

		boolean isLeaf = false;
		if (depth == p.getPatterns().size() - 1) {
			isLeaf = true;
		}

		final ArrayList<TrieNode> children = node.getChildren();

		boolean flag = false;
		if (children.size() > 0) {
			for (int i = 0; i < children.size(); i++) {
				// found equal node
				if (children.get(i).getNumlabel() == p.getPatterns().get(depth).getNumLabel()) {
					flag = true;
					// update count of existing leaf
					if (depth == (p.getPatterns().size() - 1)) {
						children.get(i).setCount(children.get(i).getCount() + p.getCount());
					}
					this.insertPattern(depth + 1, p, children.get(i));
				}
			}
		} else {
			final TrieNode newNode = new TrieNode(p.getPatterns().get(depth));
			if (isLeaf) {
				newNode.setCount(p.getCount());
				newNode.setWeight(new Float(p.getNumericValue()));
				newNode.setFeature(true);
			}
			node.addChildNode(newNode);
			this.insertPattern(depth + 1, p, newNode);
			flag = true;
		}
		// create the missing node
		if (!flag) {
			final TrieNode newNode = new TrieNode(p.getPatterns().get(depth));
			if (isLeaf) {
				newNode.setFeature(true);
				newNode.setCount(p.getCount());
				newNode.setWeight(new Float(p.getNumericValue()));
			}
			node.addChildNode(newNode);
			this.insertPattern(depth + 1, p, newNode);
		}
	}

	/**
	 * insert a pattern into the trie
	 * 
	 * @param c
	 */
	public void insertPattern(PatternContainer c) {
		this.insertPattern(0, c, this.root);
	}
	
	/**
	 * prints out the trie in GML format recursively ()
	 */
	private void printGMLTrieColoredByPPP(int depth, TrieNode n, StringBuffer sb, IdManager idm) {
		final ArrayList<TrieNode> l = n.getChildren();

		final int selectedNodeId = idm.getLastId();

		for (int i = 0; i < l.size(); i++) {

			idm.incrID();

			final GMLNode node = new GMLNode(idm.getLastId());
			node.setLabel("" + l.get(i).getLabel());

			DecimalFormat df = new DecimalFormat();
			// mark leaves
			if (l.get(i).isFeature()) {
				//node.setLabel(node.getLabel() + " ("+l.get(i).getCount()+")");
				node.setLabel(node.getLabel() + " ("+df.format(l.get(i).getWeight().doubleValue())+")");
				node.setColor(ConstantsYEditColor.SANDY_BROWN);
			}

			// set color
			if (node.getLabel().startsWith("D")) {
				node.setColor(ConstantsYEditColor.BLUE);
			}
			if (node.getLabel().startsWith("P")) {
				node.setColor(ConstantsYEditColor.BLUE);
			}
			if (node.getLabel().startsWith("A")) {
				node.setColor(ConstantsYEditColor.RED);
			}
			if (node.getLabel().startsWith("N")) {
				node.setColor(ConstantsYEditColor.RED);
			}
			if (node.getLabel().startsWith("L")) {
				node.setColor(ConstantsYEditColor.GREEN);
			}

			final GMLEdge edge = new GMLEdge();
			edge.setSource(selectedNodeId);
			edge.setTarget(idm.getLastId());

			sb.append(node.getGMLNodeString());
			sb.append(edge.getGMLEdgeString());

			this.printGMLTrieColoredByPPP(depth + 1, l.get(i), sb, idm);
		}
	}
	
	/**
	 * prints out the trie in GML format recursively ()
	 */
	private void printGMLTrieColoredByElement(int depth, TrieNode n, StringBuffer sb, IdManager idm) {
		final ArrayList<TrieNode> l = n.getChildren();

		final int selectedNodeId = idm.getLastId();

		for (int i = 0; i < l.size(); i++) {

			idm.incrID();

			final GMLNode node = new GMLNode(idm.getLastId());
			node.setLabel("" + l.get(i).getLabel());

			// mark leaves
			if (l.get(i).isFeature()) {
				node.setLabel(node.getLabel() + " ("+l.get(i).getCount()+")");
				node.setColor(ConstantsYEditColor.SANDY_BROWN);
			}

			// set color
			if (node.getLabel().startsWith("C")) {
				node.setColor(ConstantsYEditColor.GREY);
			}
			if (node.getLabel().startsWith("O")) {
				node.setColor(ConstantsYEditColor.RED);
			}
			if (node.getLabel().startsWith("N")) {
				node.setColor(ConstantsYEditColor.BLUE);
			}
			if (node.getLabel().startsWith("F")) {
				node.setColor(ConstantsYEditColor.GREEN);
			}
			if (node.getLabel().startsWith("S")) {
				node.setColor(ConstantsYEditColor.YELLOW);
			}

			final GMLEdge edge = new GMLEdge();
			edge.setSource(selectedNodeId);
			edge.setTarget(idm.getLastId());

			sb.append(node.getGMLNodeString());
			sb.append(edge.getGMLEdgeString());

			this.printGMLTrieColoredByElement(depth + 1, l.get(i), sb, idm);
		}
	}

	/**
	 * gets the number of leaves in a tree
	 * 
	 * @param depth
	 * @param n
	 */
	private void refreshTrie(int depth, TrieNode n, TrieResultContainer r) {
		final ArrayList<TrieNode> l = n.getChildren();
		for (int i = 0; i < l.size(); i++) {
			if (l.get(i).isFeature()) {
				// count unique features
				r.setSomevalue1(r.getSomevalue1() + 1);
				// count number of features
				r.setSomevalue2(r.getSomevalue2() + l.get(i).getCount());

				// count weights, if applicable
				if (l.get(i).getWeight() != null) {
					r.setSomevalue4(r.getSomevalue4() + l.get(i).getWeight());
				}
			}
			// count every node
			r.setSomevalue3(r.getSomevalue3() + 1);
			this.refreshTrie(depth + 1, l.get(i), r);
		}
	}

	/**
	 * sets the number of leaves (i.e. total number of unique features in the
	 * trie)
	 * 
	 * @param nfeatures
	 */
	private void setNumberOfFeatures(int nfeatures) {
		this.nfeatures = nfeatures;
	}

	/**
	 * sets the total number of features (i.e. total number of all features at
	 * the leaves in the trie)
	 * 
	 * @param totalfeaturecount
	 */
	private void setTotalFeatureCount(int totalfeaturecount) {
		this.totalfeaturecount = totalfeaturecount;
	}

	/**
	 * 
	 * sets number of nodes
	 * 
	 * @param totalnodecount
	 */
	private void setTotalNodeCount(int totalnodecount) {
		this.totalnodecount = totalnodecount;
	}

}
