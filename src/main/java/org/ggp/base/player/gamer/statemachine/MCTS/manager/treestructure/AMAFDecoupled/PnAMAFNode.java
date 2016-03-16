package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.AMAFDecoupled;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public interface PnAMAFNode {

	public Map<InternalPropnetMove, MoveStats> getAmafStats();

}
