package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.AbstractStateMachine;
import org.ggp.base.util.statemachine.structure.Role;

public class EvoAfterSimulation implements AfterSimulationStrategy {

	private AbstractStateMachine theMachine;

	private SingleParameterEvolutionManager evolutionManager;

	private Role myRole;

	public EvoAfterSimulation(AbstractStateMachine theMachine, SingleParameterEvolutionManager evolutionManager, Role myRole) {

		this.theMachine = theMachine;

		this.evolutionManager = evolutionManager;

		this.myRole = myRole;

	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {

		this.evolutionManager.updateFitness(simulationResult.getTerminalGoals()[this.theMachine.getRoleIndex(this.myRole)]);

	}

	@Override
	public String getStrategyParameters() {

			return this.evolutionManager.printEvolutionManager();

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
