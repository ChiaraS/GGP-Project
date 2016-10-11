package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.PnDecoupledMCTSNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;


public abstract class TDBackpropagation implements BackpropagationStrategy {

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


	public TDBackpropagation(InternalPropnetStateMachine theMachine, int numRoles, double qPlayout, double lambda, double gamma) {

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
	public void update(MCTSNode currentNode, MCTSJointMove jointMove,
			InternalPropnetMachineState nextState, SimulationResult simulationResult) {

		if(currentNode instanceof PnDecoupledMCTSNode && jointMove instanceof SequDecMCTSJointMove){
			this.decUpdate((PnDecoupledMCTSNode)currentNode, (SequDecMCTSJointMove)jointMove, nextState, simulationResult);
		}else{
			throw new RuntimeException("StandardBackpropagation-update(): no method implemented to manage backpropagation for node type (" + currentNode.getClass().getSimpleName() + ") and joint move type (" + jointMove.getClass().getSimpleName() + ").");
		}

	}

	private void decUpdate(PnDecoupledMCTSNode currentNode, SequDecMCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult){

		currentNode.incrementTotVisits();

		DecoupledMCTSMoveStats[][] moves = currentNode.getMoves();
		int[] movesIndices = jointMove.getMovesIndices();

		DecoupledMCTSMoveStats currentMoveStat;
		double moveVisits;

		int[] returnValuesForRoles = this.getReturnValuesForRolesInPlayout(simulationResult); // Here the index is useless so we set it to 0

		double qCurrent;
		double delta;
		double alpha;

		for(int i = 0; i < this.numRoles; i++){

			currentMoveStat = moves[i][movesIndices[i]];
			currentMoveStat.incrementVisits();
			moveVisits = currentMoveStat.getVisits();
			qCurrent = currentMoveStat.getScoreSum()/moveVisits;

			delta = returnValuesForRoles[i] + this.gamma * this.qNext[i] - qCurrent;
			this.deltaSum[i] = this.lambda * this.gamma * this.deltaSum[i] + delta;

			alpha = 1.0/moveVisits;

			currentMoveStat.setScoreSum((qCurrent + alpha * this.deltaSum[i])*moveVisits); // Note that the statistics memorize the total sum of move values, thus we must multiply the new expected value by the number of visits of the move.

			this.qNext[i] = qCurrent;
		}

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode,	InternalPropnetMachineState leafState,
			SimulationResult simulationResult) {

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

	public abstract int[] getReturnValuesForRolesInPlayout(SimulationResult simulationResult);

}
