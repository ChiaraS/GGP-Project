package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;

public class ProverGRAVEAfterSimulation implements ProverAfterSimulationStrategy {

	private ProverGRAVESelection graveSelection;

	public ProverGRAVEAfterSimulation(ProverGRAVESelection graveSelection) {
		this.graveSelection = graveSelection;
	}

	@Override
	public void afterSimulationActions(ProverSimulationResult simulationResult) {
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
