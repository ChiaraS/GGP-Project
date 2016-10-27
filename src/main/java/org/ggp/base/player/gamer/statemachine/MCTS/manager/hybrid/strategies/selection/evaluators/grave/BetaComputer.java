package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;

public abstract class BetaComputer implements OnlineTunableComponent {

	/**
	 * Computes the value of the weight beta used by the GRAVE move evaluator during selection.
	 *
	 * @param theMoveStats the statistics for the move being evaluated in the node.
	 * @param theAmafMoveStats the AMAF statistics for the move being evaluated.
	 * @param nodeVisits the visits of the node for which this move is being evaluated.
	 * @return the weight to be used in the computation of the value of the move.
	 */
	public abstract double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats, int nodeVisits);

	public abstract String getBetaComputerParameters();

	public String printBetaComputer() {
		String params = this.getBetaComputerParameters();

		if(params != null){
			return "(BETA_COMPUTER_TYPE = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(BETA_COMPUTER_TYPE = " + this.getClass().getSimpleName() + ")";
		}
	}

	@Override
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printBetaComputer() + ")";
	}

}
