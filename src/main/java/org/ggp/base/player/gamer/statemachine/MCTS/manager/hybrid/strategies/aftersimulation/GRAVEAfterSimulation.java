package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class GRAVEAfterSimulation implements AfterSimulationStrategy {

	private GRAVESelection graveSelection;

	public GRAVEAfterSimulation(GRAVESelection graveSelection) {
		this.graveSelection = graveSelection;
	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {
		this.graveSelection.resetClosestAmafStats();
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
