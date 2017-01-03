package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;

public class PnEvoBeforeSimulation implements PnBeforeSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private OnlineTunableComponent tunableComponent;

	public PnEvoBeforeSimulation(SingleParameterEvolutionManager evolutionManager, OnlineTunableComponent tunableComponent) {

		this.evolutionManager = evolutionManager;

		this.tunableComponent = tunableComponent;

		//this.betaComputer.setK(this.evolutionManager.selectNextK());

	}

	@Override
	public void beforeSimulationActions() {

		this.tunableComponent.setNewValues(this.evolutionManager.selectNextIndividuals());

	}

	@Override
	public String getStrategyParameters() {

		return this.tunableComponent.printOnlineTunableComponent("") + ", " + this.evolutionManager.printEvolutionManager("");
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
