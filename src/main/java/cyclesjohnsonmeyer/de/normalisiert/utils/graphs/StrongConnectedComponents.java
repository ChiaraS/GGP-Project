package cyclesjohnsonmeyer.de.normalisiert.utils.graphs;


import java.util.ArrayList;
import java.util.List;

/**
 * This is a helpclass for the search of all elementary cycles in a graph
 * with the algorithm of Johnson. For this it searches for strong connected
 * components, using the algorithm of Tarjan. The constructor gets an
 * adjacency-list of a graph. Based on this graph, it gets a nodenumber s,
 * for which it calculates the subgraph, containing all nodes
 * {s, s + 1, ..., n}, where n is the highest nodenumber in the original
 * graph (e.g. it builds a subgraph with all nodes with higher or same
 * nodenumbers like the given node s). It returns the strong connected
 * component of this subgraph which contains the lowest nodenumber of all
 * nodes in the subgraph.<br><br>
 *
 * For a description of the algorithm for calculating the strong connected
 * components see:<br>
 * Robert Tarjan: Depth-first search and linear graph algorithms. In: SIAM
 * Journal on Computing. Volume 1, Nr. 2 (1972), pp. 146-160.<br>
 * For a description of the algorithm for searching all elementary cycles in
 * a directed graph see:<br>
 * Donald B. Johnson: Finding All the Elementary Circuits of a Directed Graph.
 * SIAM Journal on Computing. Volumne 4, Nr. 1 (1975), pp. 77-84.<br><br>
 *
 * @author Frank Meyer, web_at_normalisiert_dot_de
 * @version 1.1, 22.03.2009
 *
 * @author C.Sironi (modified code to use different data structures)
 *
 */
public class StrongConnectedComponents {
	/** Adjacency-list of original graph */
	private List<List<Integer>> adjListOriginal = null;

	/** Adjacency-list of currently viewed subgraph */
	private List<List<Integer>> adjList = null;

	/** Helpattribute for finding scc's */
	private boolean[] visited = null;

	/** Helpattribute for finding scc's */
	private List<Integer> stack = null;

	/** Helpattribute for finding scc's */
	private int[] lowlink = null;

	/** Helpattribute for finding scc's */
	private int[] number = null;

	/** Helpattribute for finding scc's */
	private int sccCounter = 0;

	/** Helpattribute for finding scc's */
	private List<List<Integer>> currentSCCs = null;

	/**
	 * Constructor.
	 *
	 * @param adjList adjacency-list of the graph
	 */
	public StrongConnectedComponents(List<List<Integer>> adjList) {
		this.adjListOriginal = adjList;
	}

	/**
	 * This method returns the adjacency-structure of the strong connected
	 * component with the least vertex in a subgraph of the original graph
	 * induced by the nodes {s, s + 1, ..., n}, where s is a given node. Note
	 * that trivial strong connected components with just one node will not
	 * be returned.
	 *
	 * @param node node s
	 * @return SCCResult with adjacency-structure of the strong
	 * connected component; null, if no such component exists
	 */
	public SCCResult getAdjacencyList(int node) {
		this.visited = new boolean[this.adjListOriginal.size()];
		this.lowlink = new int[this.adjListOriginal.size()];
		this.number = new int[this.adjListOriginal.size()];
		//this.visited = new boolean[this.adjListOriginal.size()];
		this.stack = new ArrayList<Integer>();
		this.currentSCCs = new ArrayList<List<Integer>>();

		this.makeAdjListSubgraph(node);

		for (int i = node; i < this.adjListOriginal.size(); i++) {
			if (!this.visited[i]) {
				this.getStrongConnectedComponents(i);
				List<Integer> nodes = this.getLowestIdComponent();
				if (nodes != null && !nodes.contains(new Integer(node)) && !nodes.contains(new Integer(node + 1))) {
					return this.getAdjacencyList(node + 1);
				} else {
					List<List<Integer>> adjacencyList = this.getAdjList(nodes);
					if (adjacencyList != null) {
						for (int j = 0; j < this.adjListOriginal.size(); j++) {
							if (adjacencyList.get(j).size() > 0) {
								return new SCCResult(adjacencyList, j);
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Builds the adjacency-list for a subgraph containing just nodes
	 * >= a given index.
	 *
	 * @param node Node with lowest index in the subgraph
	 */
	private void makeAdjListSubgraph(int node) {
		this.adjList = new ArrayList<List<Integer>>(this.adjListOriginal.size()); // int[this.adjListOriginal.size()][0];

		// Fill first node-1 nodes with empty array
		for (int i = 0; i < node; i++){
			this.adjList.add(new ArrayList<Integer>());
		}

		for (int i = node; i < this.adjListOriginal.size(); i++) {
			List<Integer> successors = new ArrayList<Integer>();
			for(Integer j : this.adjListOriginal.get(i)){
				if (j.intValue() >= node) {
					successors.add(new Integer(j.intValue()));
				}
			}

			this.adjList.add(successors);

			/*
			if (successors.size() > 0) {
				this.adjList[i] = new int[successors.size()];

				//!
				for (int j = 0; j < successors.size(); j++) {
					Integer succ = (Integer) successors.get(j);
					this.adjList[i][j] = succ.intValue();
				}
			}
			*/
		}
	}

	/**
	 * Calculates the strong connected component out of a set of scc's, that
	 * contains the node with the lowest index.
	 *
	 * @return Vector::Integer of the scc containing the lowest nodenumber
	 */
	private List<Integer> getLowestIdComponent() {
		int min = this.adjList.size();
		List<Integer> currScc = null;

		for (int i = 0; i < this.currentSCCs.size(); i++) {
			List<Integer> scc = this.currentSCCs.get(i);
			for (int j = 0; j < scc.size(); j++) {
				Integer node = scc.get(j);
				if (node.intValue() < min) {
					currScc = scc;
					min = node.intValue();
				}
			}
		}

		return currScc;
	}

	/**
	 * @return Vector[]::Integer representing the adjacency-structure of the
	 * strong connected component with least vertex in the currently viewed
	 * subgraph
	 */
	private List<List<Integer>> getAdjList(List<Integer> nodes) {
		List<List<Integer>> lowestIdAdjacencyList = null;

		if (nodes != null) {
			lowestIdAdjacencyList = new ArrayList<List<Integer>>(this.adjList.size());
			for (int i = 0; i < this.adjList.size(); i++) {
				lowestIdAdjacencyList.add(new ArrayList<Integer>());
			}
			for (int i = 0; i < nodes.size(); i++) {
				int node = nodes.get(i).intValue();
				for (int j = 0; j < this.adjList.get(node).size(); j++) {
					int succ = this.adjList.get(node).get(j);
					if (nodes.contains(new Integer(succ))) {
						lowestIdAdjacencyList.get(node).add(new Integer(succ));
					}
				}
			}
		}

		return lowestIdAdjacencyList;
	}

	/**
	 * Searches for strong connected components reachable from a given node.
	 *
	 * @param root node to start from.
	 */
	private void getStrongConnectedComponents(int root) {
		this.sccCounter++;
		this.lowlink[root] = this.sccCounter;
		this.number[root] = this.sccCounter;
		this.visited[root] = true;
		this.stack.add(new Integer(root));

		for(Integer i : this.adjList.get(root)){
			int w = i.intValue();
			if (!this.visited[w]) {
				this.getStrongConnectedComponents(w);
				this.lowlink[root] = Math.min(lowlink[root], lowlink[w]);
			} else if (this.number[w] < this.number[root]) {
				if (this.stack.contains(new Integer(w))) {
					lowlink[root] = Math.min(this.lowlink[root], this.number[w]);
				}
			}
		}
		/*
		for (int i = 0; i < this.adjList.get(root).size(); i++) {
			int w = this.adjList.get(root).get(i);
			if (!this.visited[w]) {
				this.getStrongConnectedComponents(w);
				this.lowlink[root] = Math.min(lowlink[root], lowlink[w]);
			} else if (this.number[w] < this.number[root]) {
				if (this.stack.contains(new Integer(w))) {
					lowlink[root] = Math.min(this.lowlink[root], this.number[w]);
				}
			}
		}*/

		// found scc
		if ((lowlink[root] == number[root]) && (stack.size() > 0)) {
			int next = -1;
			List<Integer> scc = new ArrayList<Integer>();

			do {
				//next = this.stack.get(stack.size() - 1).intValue();
				next = this.stack.remove(stack.size() - 1).intValue();
				scc.add(new Integer(next));
			} while (this.number[next] > this.number[root]);

			// simple scc's with just one node will not be added
			if (scc.size() > 1) {
				this.currentSCCs.add(scc);
			}
		}
	}

	public static void main(String[] args) {
		boolean[][] adjMatrix = new boolean[10][];

		for (int i = 0; i < 10; i++) {
			adjMatrix[i] = new boolean[10];
		}

		/*adjMatrix[0][1] = true;
		adjMatrix[1][2] = true;
		adjMatrix[2][0] = true;
		adjMatrix[2][4] = true;
		adjMatrix[1][3] = true;
		adjMatrix[3][6] = true;
		adjMatrix[6][5] = true;
		adjMatrix[5][3] = true;
		adjMatrix[6][7] = true;
		adjMatrix[7][8] = true;
		adjMatrix[7][9] = true;
		adjMatrix[9][6] = true;*/

        adjMatrix[0][1] = true;
        adjMatrix[1][2] = true;
        adjMatrix[2][0] = true; adjMatrix[2][6] = true;
        adjMatrix[3][4] = true;
        adjMatrix[4][5] = true; adjMatrix[4][6] = true;
        adjMatrix[5][3] = true;
        adjMatrix[6][7] = true;
        adjMatrix[7][8] = true;
        adjMatrix[8][6] = true;

        adjMatrix[6][1] = true;

        List<List<Integer>> adjList = AdjacencyList.getAdjacencyList(adjMatrix);
		StrongConnectedComponents scc = new StrongConnectedComponents(adjList);
		for (int i = 0; i < adjList.size(); i++) {
			System.out.print("i: " + i + "\n");
			SCCResult r = scc.getAdjacencyList(i);
			if (r != null) {
				List<List<Integer>> al = scc.getAdjacencyList(i).getAdjList();
				for (int j = i; j < al.size(); j++) {
					if (al.get(j).size() > 0) {
						System.out.print("j: " + j);
						for (int k = 0; k < al.get(j).size(); k++) {
							System.out.print(" _" + al.get(j).get(k));
						}
						System.out.print("\n");
					}
				}
				System.out.print("\n");
			}
		}
	}
}
