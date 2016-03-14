package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.GRAVEDUCT;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.PnDUCTMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class PnGRAVEDUCTMCTSNode extends PnDUCTMCTSNode{

	/**
	 * Table that collects the AMAF statistics for the node for each move.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	public PnGRAVEDUCTMCTSNode(DUCTMCTSMoveStats[][] movesStats, int[] goals, boolean terminal) {
		super(movesStats, goals, terminal);

		this.amafStats = new HashMap<InternalPropnetMove, MoveStats>();
	}

	public Map<InternalPropnetMove, MoveStats> getAmafStats(){
		return this.amafStats;
	}

}
