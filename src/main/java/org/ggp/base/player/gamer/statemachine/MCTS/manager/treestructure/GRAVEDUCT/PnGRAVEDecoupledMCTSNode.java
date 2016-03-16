package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.GRAVEDUCT;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.decoupled.PnDecoupledMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class PnGRAVEDecoupledMCTSNode extends PnDecoupledMCTSNode implements PnGRAVENode{

	/**
	 * Table that collects the AMAF statistics for the node for each move.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	public PnGRAVEDecoupledMCTSNode(DecoupledMCTSMoveStats[][] movesStats, int[] goals, boolean terminal) {
		super(movesStats, goals, terminal);

		this.amafStats = new HashMap<InternalPropnetMove, MoveStats>();
	}

	@Override
	public Map<InternalPropnetMove, MoveStats> getAmafStats(){
		return this.amafStats;
	}

}
