package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public interface MoveEvaluator {

	public double computeMoveValue(int nodeVisits, InternalPropnetMove theMove, MoveStats theMoveStats);

	public String getEvaluatorParameters();

	public String printEvaluator();

}
