package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledMCTSNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;


public abstract class PnTDBackpropagation implements PnBackpropagationStrategy {

	protected InternalPropnetStateMachine theMachine;

	protected int numRoles;

	/**
	 * Strategy parameters.
	 */

	protected double qPlayout;

	protected double lambda;

	protected double gamma;

	/**
	 * Simulation parameters that must be reset after each simulation is concluded
	 * and before starting the backpropagation for the next simulation.
	 */

	protected double[] deltaSum;

	protected double[] qNext;


	public PnTDBackpropagation(InternalPropnetStateMachine theMachine, int numRoles, double qPlayout, double lambda, double gamma) {

		this.theMachine = theMachine;
		this.numRoles = numRoles;

		this.qPlayout = qPlayout;
		this.lambda = lambda;
		this.gamma = gamma;

		this.deltaSum = new double[numRoles];
		this.qNext = new double[numRoles];

		this.resetSimulationParameters();

	}

	@Override
	public void update(MCTSNode currentNode, CompactMachineState currentState,
			PnMCTSJointMove jointMove, PnSimulationResult simulationResult) {

		if(currentNode instanceof PnDecoupledMCTSNode && jointMove instanceof PnSequDecMCTSJointMove){
			this.decUpdate((PnDecoupledMCTSNode)currentNode, currentState, (PnSequDecMCTSJointMove)jointMove, simulationResult);
		}else{
			throw new RuntimeException("StandardBackpropagation-update(): no method implemented to manage backpropagation for node type (" + currentNode.getClass().getSimpleName() + ") and joint move type (" + jointMove.getClass().getSimpleName() + ").");
		}

	}

	protected void decUpdate(PnDecoupledMCTSNode currentNode, CompactMachineState currentState, PnSequDecMCTSJointMove jointMove, PnSimulationResult simulationResult){

		currentNode.incrementTotVisits();

		PnDecoupledMCTSMoveStats[][] moves = currentNode.getMoves();
		int[] movesIndices = jointMove.getMovesIndices();

		PnDecoupledMCTSMoveStats currentMoveStat;

		int[] returnValuesForRoles = this.getReturnValuesForRolesInPlayout(simulationResult); // Here the index is useless so we set it to 0

		double qCurrent;
		double delta;
		double alpha;

		double newScore;

		for(int i = 0; i < this.numRoles; i++){

			currentMoveStat = moves[i][movesIndices[i]];

			if(currentMoveStat.getVisits() == 0){
				qCurrent = 0.0;
			}else{
				qCurrent = currentMoveStat.getScoreSum()/((double)currentMoveStat.getVisits());
			}

			delta = returnValuesForRoles[i] + this.gamma * this.qNext[i] - qCurrent;
			this.deltaSum[i] = this.lambda * this.gamma * this.deltaSum[i] + delta;

			currentMoveStat.incrementVisits();
			alpha = 1.0/((double)currentMoveStat.getVisits());

			newScore = (qCurrent + alpha * this.deltaSum[i]);

			if(newScore < 0.0){
				GamerLogger.logError("MCTSManager", "Computed negative score when backpropagating: " + newScore);

				newScore = 0.0;
			}

			currentMoveStat.setScoreSum(newScore*((double)currentMoveStat.getVisits())); // Note that the statistics memorize the total sum of move values, thus we must multiply the new expected value by the number of visits of the move.


			this.qNext[i] = qCurrent;
		}

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode,	CompactMachineState leafState,
			PnSimulationResult simulationResult) {

		int playoutLength = simulationResult.getPlayoutLength();

		if(playoutLength <= 0){ // This method should be called only if the playout was actually performed, thus the length must be at least 1!
			GamerLogger.logError("MCTSManager", "Playout length equals 0 when processing the playout result for TD backpropagation. Probably a wrong combination of strategies has been set or there is something wrong in the code!");
			throw new RuntimeException("Playout length equals 0.");
		}

		int[] returnValuesForRoles;
		double delta;
		//double qCurrent = this.qPlayout; Redundant! Basically qCurrent = qPlayout for the whole backpropagation in the playout part of the simulation

		// Update deltaSum for each non-terminal episode in the playout.
		for(int i = 0; i < playoutLength-1; i++){

			returnValuesForRoles = this.getReturnValuesForRolesInPlayout(simulationResult);

			for(int j = 0; j < this.numRoles; j++){

				delta = ((double)returnValuesForRoles[j]) + this.gamma * this.qNext[j] - this.qPlayout;

				this.deltaSum[j] = this.lambda * this.gamma * this.deltaSum[j] + delta;

				this.qNext[j] = this.qPlayout; // Would be enough to do this only for the 1st iteration of the cycle

			}
		}
	}

	@Override
	public String getStrategyParameters() {
		return "Q_PLAYOUT = " + this.qPlayout + ", LAMBDA = " + this.lambda + ", GAMMA = " + this.gamma;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

	public void resetSimulationParameters(){

		for(int i = 0; i < this.deltaSum.length; i++){
			this.deltaSum[i] = 0.0;
			this.qNext[i] = 0.0;
		}
	}

	public abstract int[] getReturnValuesForRolesInPlayout(PnSimulationResult simulationResult);

}
