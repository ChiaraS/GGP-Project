/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.Map;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

/**
 * @author C.Sironi
 *
 */
public class DUCTTranspositionTable {

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private Map<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode> transpositionTable;

	/**
	 *
	 */
	public DUCTTranspositionTable() {
		// TODO Auto-generated constructor stub
	}

}
