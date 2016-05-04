package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.AMAFDecoupled;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.Move;

public interface ProverAMAFNode {

	public Map<Move, MoveStats> getAmafStats();

}
