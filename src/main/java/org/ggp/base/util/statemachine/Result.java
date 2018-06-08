package org.ggp.base.util.statemachine;

import java.util.List;
import java.util.Map;

import org.ggp.base.util.Pair;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

/**
 * Placeholder for the result of fucntion2.
 *
 * @author C.Sironi
 *
 */
public class Result {

	/**
	 * List of legal moves for each role. A move is represented as the index that the moves has in the bit array
	 * that represents moves in the propnet.
	 */
	private int[][] legalMovesPerRole;

	/**
	 * For each joint move this map memorizes the corresponding next state and list of score sums for each role.
	 */
	private Map<List<CompactMove>,Pair<CompactMachineState,double[]>> nextStatesAndScores;

	public int[][] getLegalMovesPerRole() {
		return legalMovesPerRole;
	}

	public Map<List<CompactMove>, Pair<CompactMachineState, double[]>> getNextStatesAndScores() {
		return nextStatesAndScores;
	}

}
