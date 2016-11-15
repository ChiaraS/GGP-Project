package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;

public class TerminalTDBackpropagation extends TDBackpropagation {

	/**
	 *  True if for the current simulation the backpropagation hasn't been called once yet
	 *  (i.e. if the node being updated is also the one before the last in the simulated path),
	 *  so the final reward must be considered when computing the value to update.
	 */
	private boolean firstUpdate;

	public TerminalTDBackpropagation(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, GlobalExtremeValues globalExtremeValues, double qPlayout,
			double lambda, double gamma){
		super(gameDependentParameters, random, properties, sharedReferencesCollector, globalExtremeValues, qPlayout, lambda, gamma);

		this.firstUpdate = true;
	}

	@Override
	public int[] getReturnValuesForRolesInPlayout(SimulationResult simulationResult){

		if(this.firstUpdate){

			this.firstUpdate = false;

			if(simulationResult.getTerminalGoals() == null){
				GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when processing the result for terminal TD backpropagation. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			return simulationResult.getTerminalGoals();

		}else{
			return new int[this.gameDependentParameters.getNumRoles()];
		}

	}

	@Override
	public void resetSimulationParameters(){

		super.resetSimulationParameters();

		this.firstUpdate = true;

	}

}
