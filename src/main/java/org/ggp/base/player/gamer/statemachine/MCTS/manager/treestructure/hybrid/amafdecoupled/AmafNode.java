package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.Move;

public interface AmafNode {

	public Map<Move, MoveStats> getAmafStats();

}
