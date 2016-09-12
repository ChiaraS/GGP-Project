package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.CADIABetaComputer;

public class EvoGRAVEBeforeSimulation implements BeforeSimulationStrategy {

	private EvolutionManager evolutionManager;

	private CADIABetaComputer betaComputer;

	public EvoGRAVEBeforeSimulation(EvolutionManager evolutionManager, CADIABetaComputer betaComputer) {

		this.evolutionManager = evolutionManager;

		this.betaComputer = betaComputer;

		//this.betaComputer.setK(this.evolutionManager.selectNextK());

	}

	@Override
	public void beforeSimulationActions() {

		this.betaComputer.setK(this.evolutionManager.selectNextK());

	}

	@Override
	public String getStrategyParameters() {

		return this.betaComputer.printBetaComputer() + ", " + this.evolutionManager.printEvolutionManager();
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[BEFORE_SIM_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[BEFORE_SIM_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
