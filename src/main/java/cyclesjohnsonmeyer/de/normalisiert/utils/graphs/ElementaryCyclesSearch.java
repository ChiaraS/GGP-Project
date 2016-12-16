package cyclesjohnsonmeyer.de.normalisiert.utils.graphs;

import java.util.ArrayList;
import java.util.List;



/**
 * Searches all elementary cycles in a given directed graph. The implementation
 * is independent from the concrete objects that represent the graph nodes, it
 * just needs an array of the objects representing the nodes the graph
 * and an adjacency-matrix of type boolean, representing the edges of the
 * graph. It then calculates based on the adjacency-matrix the elementary
 * cycles and returns a list, which contains lists itself with the objects of the
 * concrete graphnodes-implementation. Each of these lists represents an
 * elementary cycle.<br><br>
 *
 * The implementation uses the algorithm of Donald B. Johnson for the search of
 * the elementary cycles. For a description of the algorithm see:<br>
 * Donald B. Johnson: Finding All the Elementary Circuits of a Directed Graph.
 * SIAM Journal on Computing. Volumne 4, Nr. 1 (1975), pp. 77-84.<br><br>
 *
 * The algorithm of Johnson is based on the search for strong connected
 * components in a graph. For a description of this part see:<br>
 * Robert Tarjan: Depth-first search and linear graph algorithms. In: SIAM
 * Journal on Computing. Volume 1, Nr. 2 (1972), pp. 146-160.<br>
 *
 * @author Frank Meyer, web_at_normalisiert_dot_de
 * @version 1.2, 22.03.2009
 *
 * @author C.Sironi (modified code to use different data structures)
 *
 */
public class ElementaryCyclesSearch {
	/** List of cycles */
	private List<List<Integer>> cycles = null;

	/** Adjacency-list of graph */
	private List<List<Integer>> adjList = null;

	/** Graphnodes */
	//private Object[] graphNodes = null;

	/** Blocked nodes, used by the algorithm of Johnson */
	private boolean[] blocked = null;

	/** B-Lists, used by the algorithm of Johnson */
	private List<List<Integer>> B = null;

	/** Stack for nodes, used by the algorithm of Johnson */
	private List<Integer> stack = null;

	/**
	 * Constructor.
	 *
	 * @param matrix adjacency-matrix of the graph
	 * @param graphNodes array of the graphnodes of the graph; this is used to
	 * build sets of the elementary cycles containing the objects of the original
	 * graph-representation
	 */
	/*
	public ElementaryCyclesSearch(boolean[][] matrix, Object[] graphNodes) {
		this.graphNodes = graphNodes;
		// Transform the adjacency matrix in an adjacency list
		this.adjList = AdjacencyList.getAdjacencyList(matrix);
	}
	*/

	/**
	 * Constructor.
	 *
	 * @param matrix adjacency-matrix of the graph
	 *
	 */
	public ElementaryCyclesSearch(boolean[][] matrix) {
		// Transform the adjacency matrix in an adjacency list
		this.adjList = AdjacencyList.getAdjacencyList(matrix);
	}

	/**
	 * Constructor.
	 *
	 * @param adjList adjacency list representing the graph. Nodes are identified as integers.
	 *
	 */
	public ElementaryCyclesSearch(List<List<Integer>> adjList) {
		this.adjList = adjList;
	}

	/**
	 * Returns List::List::Object with the Lists of nodes of all elementary
	 * cycles in the graph.
	 *
	 * @return List::List::Object with the Lists of the elementary cycles.
	 */
	public List<List<Integer>> getElementaryCycles() {
		this.cycles = new ArrayList<List<Integer>>();
		this.blocked = new boolean[this.adjList.size()];
		this.B = new ArrayList<List<Integer>>(this.adjList.size());
		for(int i = 0; i < this.adjList.size(); i++){
			this.B.add(null);
		}
		this.stack = new ArrayList<Integer>();
		StrongConnectedComponents sccs = new StrongConnectedComponents(this.adjList);
		int s = 0;

		while (true) {
			SCCResult sccResult = sccs.getAdjacencyList(s);
			if (sccResult != null && sccResult.getAdjList() != null) {
				List<List<Integer>> scc = sccResult.getAdjList();
				s = sccResult.getLowestNodeId();
				for (int j = 0; j < scc.size(); j++) {
					if ((scc.get(j) != null) && (scc.get(j).size() > 0)) {
						this.blocked[j] = false;
						this.B.set(j, new ArrayList<Integer>());
					}
				}

				this.findCycles(s, s, scc);
				s++;
			} else {
				break;
			}
		}

		return this.cycles;
	}

	/**
	 * Calculates the cycles containing a given node in a strongly connected
	 * component. The method calls itself recursively.
	 *
	 * @param v
	 * @param s
	 * @param adjList adjacency-list with the subgraph of the strongly
	 * connected component s is part of.
	 * @return true, if cycle found; false otherwise
	 */
	private boolean findCycles(int v, int s, List<List<Integer>> adjList) {
		boolean f = false;
		this.stack.add(new Integer(v));
		this.blocked[v] = true;

		for(Integer i : adjList.get(v)){
			int w = i.intValue();
			// found cycle
			if (w == s) {
				List<Integer> cycle = new ArrayList<Integer>(this.stack);
				this.cycles.add(cycle);
				f = true;
			} else if (!this.blocked[w]) {
				if (this.findCycles(w, s, adjList)) {
					f = true;
				}
			}
		}

		if (f) {
			this.unblock(v);
		} else {
			for(Integer i : adjList.get(v)){
				int w = i.intValue();
				if (!this.B.get(w).contains(new Integer(v))) {
					this.B.get(w).add(new Integer(v));
				}
			}
		}

		this.stack.remove(new Integer(v));
		return f;
	}

	/**
	 * Unblocks recursivly all blocked nodes, starting with a given node.
	 *
	 * @param node node to unblock
	 */
	private void unblock(int node) {
		this.blocked[node] = false;
		List<Integer> Bnode = this.B.get(node);
		while (Bnode.size() > 0) {
			Integer w = Bnode.remove(0);
			if (this.blocked[w.intValue()]) {
				this.unblock(w.intValue());
			}
		}
	}
}

