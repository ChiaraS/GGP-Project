package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.TunableParameter;

public class PnEvoBeforeSimulation implements PnBeforeSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private TunableParameter tunableParameter;

	private int myRoleIndex;

	public PnEvoBeforeSimulation(SingleParameterEvolutionManager evolutionManager, TunableParameter tunableParameter, int myRoleIndex) {

		this.evolutionManager = evolutionManager;

		this.tunableParameter = tunableParameter;

		this.myRoleIndex = myRoleIndex;

		//this.betaComputer.setK(this.evolutionManager.selectNextK());

	}

	@Override
	public void beforeSimulationActions() {

		int[] indices = this.evolutionManager.selectNextIndividualsIndices();

		if(indices.length == 1){
			this.tunableParameter.setMyRoleNewValue(this.myRoleIndex, indices[0]);
		}else{
			this.tunableParameter.setAllRolesNewValues(indices);
		}

	}

	@Override
	public String getStrategyParameters() {

		return this.tunableParameter.getParameters("") + ", " + this.evolutionManager.printEvolutionManager("");
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
