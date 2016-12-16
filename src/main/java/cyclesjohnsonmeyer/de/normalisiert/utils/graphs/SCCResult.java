package cyclesjohnsonmeyer.de.normalisiert.utils.graphs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SCCResult {
	private Set<Integer> nodeIDsOfSCC = null;
	private List<List<Integer>> adjList = null;
	private int lowestNodeId = -1;

	public SCCResult(List<List<Integer>> adjList, int lowestNodeId) {
		this.adjList = adjList;
		this.lowestNodeId = lowestNodeId;
		this.nodeIDsOfSCC = new HashSet<Integer>();
		if (this.adjList != null) {
			for (int i = this.lowestNodeId; i < this.adjList.size(); i++) {
				if (this.adjList.get(i).size() > 0) {
					this.nodeIDsOfSCC.add(new Integer(i));
				}
			}
		}
	}

	public List<List<Integer>> getAdjList() {
		return adjList;
	}

	public int getLowestNodeId() {
		return lowestNodeId;
	}
}
