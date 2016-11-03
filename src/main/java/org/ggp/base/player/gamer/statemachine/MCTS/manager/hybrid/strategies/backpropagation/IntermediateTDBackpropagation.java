package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled.TDDecoupledMCTSNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.structure.MachineState;

public class IntermediateTDBackpropagation extends TDBackpropagation {

	/**
	 * Index of the episode being currently considered in the computation of the backpropagation values.
	 * Corresponds to (simulationLength - episode depth in the game tree).
	 */
	protected int goalsIndex;

	public IntermediateTDBackpropagation(AbstractStateMachine theMachine, int numRoles, GlobalExtremeValues globalExtremeValues, double qPlayout,
			double lambda, double gamma) {
		super(theMachine, numRoles, globalExtremeValues, qPlayout, lambda, gamma);

		this.goalsIndex = 0;

	}

	@Override
	protected void decUpdate(TDDecoupledMCTSNode currentNode, MachineState currentState, SequDecMCTSJointMove jointMove, SimulationResult simulationResult){

		simulationResult.addGoals(this.theMachine.getSafeGoalsAvgForAllRoles(currentState));

		super.decUpdate(currentNode, currentState, jointMove, simulationResult);

	}


	@Override
	public int[] getReturnValuesForRolesInPlayout(SimulationResult simulationResult) {

		List<int[]> allIntermediateGoals = simulationResult.getIntermediateGoals();

		if(allIntermediateGoals == null || allIntermediateGoals.size() <= this.goalsIndex + 1){ // Intermediate goals are needed and the ones at indices (this.goalsIndex) and (this.goalsIndex+1) must be available, this the length of the list of intermediate goals must be at least this.goalsIndex+2
			GamerLogger.logError("MCTSManager", "Necessary intermediate goals not found in the simulation result when processing the result for intermediate TD backpropagation. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Necessary intermediate goals not found in the simulation result.");
		}

		int[] intermediateReturnValues = new int[this.numRoles];
		int[] nextStateGoals = allIntermediateGoals.get(this.goalsIndex);
		int[] currentStateGoals = allIntermediateGoals.get(this.goalsIndex+1);

		for(int i = 0; i < this.numRoles; i++){

			intermediateReturnValues[i] = nextStateGoals[i] - currentStateGoals[i];

		}

		this.goalsIndex++;

		return intermediateReturnValues;

	}

	@Override
	public void resetSimulationParameters(){

		super.resetSimulationParameters();

		this.goalsIndex = 0;

	}

}
