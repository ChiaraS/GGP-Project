package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.AfterSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverMemorizedStandardPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverGRAVESelection;

public class ProverGRAVEAfterSimulation implements AfterSimulationStrategy {

	private ProverGRAVESelection graveSelection;

	private ProverMemorizedStandardPlayout gravePlayout;

	public ProverGRAVEAfterSimulation(ProverGRAVESelection graveSelection, ProverMemorizedStandardPlayout gravePlayout) {
		this.graveSelection = graveSelection;
		this.gravePlayout = gravePlayout;
	}

	@Override
	public void afterSimulationActions() {
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
