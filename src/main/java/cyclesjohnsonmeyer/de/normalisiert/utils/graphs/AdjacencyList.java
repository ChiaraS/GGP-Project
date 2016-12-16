package cyclesjohnsonmeyer.de.normalisiert.utils.graphs;

import java.util.ArrayList;
import java.util.List;


/**
 * Calculates the adjacency-list for a given adjacency-matrix.
 *
 *
 * @author Frank Meyer, web@normalisiert.de
 * @version 1.0, 26.08.2006
 *
 * @author C.Sironi (modified code to use different data structures)
 *
 */
public class AdjacencyList {
	/**
	 * Calculates a adjacency-list for a given array of an adjacency-matrix.
	 *
	 * @param adjacencyMatrix array with the adjacency-matrix that represents
	 * the graph
	 * @return int[][]-array of the adjacency-list of given nodes. The first
	 * dimension in the array represents the same node as in the given
	 * adjacency, the second dimension represents the indicies of those nodes,
	 * that are direct successornodes of the node.
	 */
	public static List<List<Integer>> getAdjacencyList(boolean[][] adjacencyMatrix) {
		List<List<Integer>> list = new ArrayList<List<Integer>>(adjacencyMatrix.length);

		for (int i = 0; i < adjacencyMatrix.length; i++) {
			ArrayList<Integer> v = new ArrayList<Integer>();
			for (int j = 0; j < adjacencyMatrix[i].length; j++) {
				if (adjacencyMatrix[i][j]) {
					v.add(new Integer(j));
				}
			}

			list.add(v);

		}

		return list;
	}
}
