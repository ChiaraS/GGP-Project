package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledMCTSNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;

public class PnIntermediateTDBackpropagation extends PnTDBackpropagation {

	/**
	 * Index of the episode being currently considered in the computation of the backpropagation values.
	 * Corresponds to (simulationLength - episode depth in the game tree).
	 */
	protected int goalsIndex;

	public PnIntermediateTDBackpropagation(InternalPropnetStateMachine theMachine, int numRoles, double qPlayout,
			double lambda, double gamma) {
		super(theMachine, numRoles, qPlayout, lambda, gamma);

		this.goalsIndex = 0;

	}

	@Override
	protected void decUpdate(PnDecoupledMCTSNode currentNode, CompactMachineState currentState, PnSequDecMCTSJointMove jointMove, PnSimulationResult simulationResult){

		simulationResult.addGoals(this.theMachine.getSafeGoalsAvg(currentState));

		super.decUpdate(currentNode, currentState, jointMove, simulationResult);

	}


	@Override
	public int[] getReturnValuesForRolesInPlayout(PnSimulationResult simulationResult) {

		List<int[]> allIntermediateGoals = simulationResult.getIntermediateGoals();

		if(allIntermediateGoals == null || allIntermediateGoals.size() <= this.goalsIndex + 1){ // Intermediate goals are needed and the ones at indices (this.goalsIndex) and (this.goalsIndex+1) must be available, this the length of the list of intermediate goals must be at least this.goalsIndex+2
			GamerLogger.logError("MCTSManager", "Necessary intermediate goals not found in the simulation result when processing the result for intermediate TD backpropagation. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Necessary intermediate goals not found in the simulation result.");
		}

		int[] intermediateReturnValues = new int[this.numRoles];
		int[] nextStateGoals = allIntermediateGoals.get(this.goalsIndex);
		int[] currentStateGoals = allIntermediateGoals.get(this.goalsIndex+1);

		/*
		System.out.println();
		System.out.print("NextStateGoals=[ ");
		for(int i = 0; i < nextStateGoals.length; i++){
			System.out.print(nextStateGoals[i] + " ");
		}
		System.out.println("]");

		System.out.print("CurrentStateGoals=[ ");
		for(int i = 0; i < currentStateGoals.length; i++){
			System.out.print(currentStateGoals[i] + " ");
		}
		System.out.println("]");

		System.out.print("IntermediateReturnValues=[ ");
		*/

		for(int i = 0; i < intermediateReturnValues.length; i++){

			intermediateReturnValues[i] = nextStateGoals[i] - currentStateGoals[i];
			//System.out.print(intermediateReturnValues[i] + " ");

		}

		//System.out.println("]");

		this.goalsIndex++;

		return intermediateReturnValues;

	}

	@Override
	public void resetSimulationParameters(){

		super.resetSimulationParameters();

		this.goalsIndex = 0;

	}

}
