package org.ggp.base.player.gamer.statemachine.FPGAMCTS.manager.treestructure;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class StateMemorizingMctsNode extends DecoupledMctsNode {

	private Map<List<Move>,MachineState> nextStates;

	public StateMemorizingMctsNode(Map<List<Move>,MachineState> nextStates, DecoupledMctsMoveStats[][] movesStats, double[] goals, boolean terminal, int numRoles) {

		super(movesStats, goals, terminal, numRoles);

		this.nextStates = nextStates;

	}

	public Map<List<Move>,MachineState> getNextStates(){
		return this.nextStates;
	}

	/**
	 * Looks in the table with next states for the state reachable by performing the given joint move
	 *
	 * @param jointMove
	 * @return
	 */
	public MachineState getNextState(List<Move> jointMove) {

		return this.nextStates.get(jointMove);

	}

}
