package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public interface PnMoveEvaluator{

	public double computeMoveValue(int nodeVisits, CompactMove theMove, MoveStats theMoveStats);

	public String getEvaluatorParameters();

	public String printEvaluator();

}
