package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.GRAVESelection;

public class GRAVEAfterSimulation implements AfterSimulationStrategy {

	private GRAVESelection graveSelection;

	public GRAVEAfterSimulation(GRAVESelection graveSelection) {
		this.graveSelection = graveSelection;
	}

	@Override
	public void afterSimulationActions() {
		this.graveSelection.resetAmafStats();
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
