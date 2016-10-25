package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;

public class PnGRAVEAfterSimulation implements PnAfterSimulationStrategy {

	private PnGRAVESelection graveSelection;

	public PnGRAVEAfterSimulation(PnGRAVESelection graveSelection) {
		this.graveSelection = graveSelection;
	}

	@Override
	public void afterSimulationActions(PnSimulationResult simulationResult) {
		this.graveSelection.resetCloserAmafStats();
	}

	@Override
	public String getStrategyParameters() {
		return null;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[AFTER_SIM_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[AFTER_SIM_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
