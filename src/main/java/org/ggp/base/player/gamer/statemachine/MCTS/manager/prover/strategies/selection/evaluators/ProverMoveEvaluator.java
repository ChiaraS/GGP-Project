package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.Move;

public interface ProverMoveEvaluator {

	public double computeMoveValue(int allMoveVisits, Move theMove, MoveStats theMoveStats);

	public String getEvaluatorParameters();

	public String printEvaluator();

}
