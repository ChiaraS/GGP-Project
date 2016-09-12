package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.MemorizedStandardPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.GRAVESelection;

public class GRAVEAfterSimulation implements AfterSimulationStrategy {

	private GRAVESelection graveSelection;

	private MemorizedStandardPlayout gravePlayout;

	public GRAVEAfterSimulation(GRAVESelection graveSelection, MemorizedStandardPlayout gravePlayout) {
		this.graveSelection = graveSelection;
		this.gravePlayout = gravePlayout;
	}

	@Override
	public void afterSimulationActions(int[] goals) {
		this.graveSelection.resetCloserAmafStats();
		this.gravePlayout.clearLastMemorizedPlayout();
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
