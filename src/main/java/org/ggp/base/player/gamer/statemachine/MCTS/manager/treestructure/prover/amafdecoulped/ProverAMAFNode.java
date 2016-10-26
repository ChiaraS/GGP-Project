package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.amafdecoulped;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public interface ProverAMAFNode {

	public Map<ExplicitMove, MoveStats> getAmafStats();

}
