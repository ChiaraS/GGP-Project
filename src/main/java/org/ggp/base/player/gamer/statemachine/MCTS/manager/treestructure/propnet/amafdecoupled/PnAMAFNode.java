package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public interface PnAMAFNode {

	public Map<CompactMove, MoveStats> getAmafStats();

}
