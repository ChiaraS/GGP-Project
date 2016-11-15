package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public abstract class BetaComputer extends SearchManagerComponent implements OnlineTunableComponent {

	public BetaComputer(GameDependentParameters gameDependentParameters, Random random,
			Properties properties, SharedReferencesCollector sharedReferencesCollector){
		super(gameDependentParameters, random, properties, sharedReferencesCollector);
	}

	/**
	 * Computes the value of the weight beta used by the GRAVE move evaluator during selection.
	 *
	 * @param theMoveStats the statistics for the move being evaluated in the node.
	 * @param theAmafMoveStats the AMAF statistics for the move being evaluated.
	 * @param nodeVisits the visits of the node for which this move is being evaluated.
	 * @return the weight to be used in the computation of the value of the move.
	 */
	public abstract double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats, int nodeVisits, int roleIndex);

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "(BETA_COMPUTER_TYPE = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(BETA_COMPUTER_TYPE = " + this.getClass().getSimpleName() + ")";
		}
	}

	@Override
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printComponent() + ")";
	}

}
