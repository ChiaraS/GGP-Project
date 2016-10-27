package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.Move;

public interface MoveEvaluator {

	public double computeMoveValue(int nodeVisits, Move theMove, MoveStats theMoveStats);

	public String getEvaluatorParameters();

	public String printEvaluator();


}
