package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.PnDecoupledMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.sequential.PnSequentialMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.sequential.SequentialMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.slowsequential.PnSlowSeqentialMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.slowsequential.SlowSequentialMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.slowsequential.SlowSequentialMCTSMoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class StandardBackpropagation implements BackpropagationStrategy {

	/**
	 * The total number of roles in the game.
	 * Needed by the sequential version of MCTS.
	 */
	private int numRoles;

	/**
	 * The role that is actually performing the search.
	 * Needed by the sequential version of MCTS.
	 */
	private InternalPropnetRole myRole;

	public StandardBackpropagation(int numRoles, InternalPropnetRole myRole){
		this.numRoles = numRoles;
		this.myRole = myRole;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy#update(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove, int[])
	 */
	@Override
	public void update(MCTSNode currentNode, MCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult){
		if(currentNode instanceof PnDecoupledMCTSNode && jointMove instanceof SequDecMCTSJointMove){
			this.decUpdate((PnDecoupledMCTSNode)currentNode, (SequDecMCTSJointMove)jointMove, nextState, simulationResult);
		}else if(currentNode instanceof PnSequentialMCTSNode && jointMove instanceof SequDecMCTSJointMove){
			this.seqUpdate((PnSequentialMCTSNode)currentNode, (SequDecMCTSJointMove)jointMove, nextState, simulationResult);
		}else if(currentNode instanceof PnSlowSeqentialMCTSNode && jointMove instanceof SlowSequentialMCTSJointMove){
			this.sseqUpdate((PnSlowSeqentialMCTSNode)currentNode, (SlowSequentialMCTSJointMove)jointMove, nextState, simulationResult);
		}else{
			throw new RuntimeException("StandardBackpropagation-update(): detected wrong combination of types for node (" + currentNode.getClass().getSimpleName() + ") and joint move (" + jointMove.getClass().getSimpleName() + ").");
		}
	}

	/**
	 * Backpropagation, DECOUPLED version.
	 * This method backpropagates the goal values, updating for each role the score
	 * and the visits of the move that was selected to be part of the joint move
	 * independently of what other roles did.
	 *
	 * @param node the node for which to update the moves.
	 * @param jointMove the explored joint move.
	 * @param goals the goals obtained by the simulation, to be used to update the statistics.
	 */
	private void decUpdate(PnDecoupledMCTSNode currentNode, SequDecMCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult) {

		currentNode.incrementTotVisits();

		DecoupledMCTSMoveStats[][] moves = currentNode.getMoves();

		int[] moveIndices = jointMove.getMovesIndices();

		for(int i = 0; i < moves.length; i++){
			// Get the decoupled MCTS Move
			DecoupledMCTSMoveStats theMoveToUpdate = moves[i][moveIndices[i]];
			theMoveToUpdate.incrementScoreSum(simulationResult.getTerminalGoals()[i]);
			if(theMoveToUpdate.getVisits() == 0){
				//System.out.println("!!!!!");
				//System.out.println(node.getUnexploredMovesCount()[i]);
				currentNode.getUnexploredMovesCount()[i]--;
				//System.out.println(node.getUnexploredMovesCount()[i]);
				//System.out.println("!!!!!");

			}
			theMoveToUpdate.incrementVisits();
		}
	}

	/**
	 * Backpropagation, SEQUENTIAL version.
	 *
	 *
	 * This method backpropagates the goal values, updating for each role the score
	 * and the visits of the move that was selected to be part of the joint move.
	 * This method updates only the statistics of a move depending on the moves selected
	 * in the joint move for the roles that precede this move in the moves statistics tree.
	 *
	 * @param node the node for which to update the moves.
	 * @param jointMove the explored joint move.
	 * @param goals the goals obtained by the simulation, to be used to update the statistics.
	 */
	private void seqUpdate(PnSequentialMCTSNode currentNode, SequDecMCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult) {

		currentNode.incrementTotVisits();

		int currentRoleIndex = this.myRole.getIndex();

		SequentialMCTSMoveStats currentStatsToUpdate = currentNode.getMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];

		while(currentRoleIndex != ((this.myRole.getIndex()-1+this.numRoles)%this.numRoles)){
			currentStatsToUpdate.incrementVisits();
			currentStatsToUpdate.incrementScoreSum(simulationResult.getTerminalGoals()[currentRoleIndex]);
			currentRoleIndex = (currentRoleIndex+1)%this.numRoles;
			currentStatsToUpdate = currentStatsToUpdate.getNextRoleMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];
		}

		// When the while-loop exits, the leaf move's stats (or the ones of my role if the game is
		// a single player game) still have to be updated. We do so also checking if it was the
		// first time the joint move was explored (i.e. the unvisitedSubleaves count of this move's
		// stats equals 1 or equally its visits count equals 0).
		currentStatsToUpdate.incrementVisits();
		currentStatsToUpdate.incrementScoreSum(simulationResult.getTerminalGoals()[currentRoleIndex]);

		// If it's the first visit for this leaf, it's also the first visit of the joint move and
		// we must decrement by 1 the unvisitedSubleaves count of all the moves in this joint move.
		if(currentStatsToUpdate.getUnvisitedSubleaves() == 1){
			currentRoleIndex = this.myRole.getIndex();

			currentStatsToUpdate = currentNode.getMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];

			while(currentRoleIndex != ((this.myRole.getIndex()-1+this.numRoles)%this.numRoles)){
				currentStatsToUpdate.decreaseUnvisitedSubLeaves();
				currentRoleIndex = (currentRoleIndex+1)%this.numRoles;
				currentStatsToUpdate = currentStatsToUpdate.getNextRoleMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];
			}

			currentStatsToUpdate.decreaseUnvisitedSubLeaves();

			currentNode.decreaseUnvisitedLeaves();
		}
	}

	/**
	 * Backpropagation, slow SEQUENTIAL version.
	 * This method backpropagates the goal values, updating for each role the score
	 * and the visits of the move that was selected to be part of the joint move.
	 * This method updates only the statistics of a move depending on the moves selected
	 * in the joint move for the roles that precede this move in the moves statistics tree.
	 *
	 * @param node the node for which to update the moves.
	 * @param jointMove the explored joint move.
	 * @param goals the goals obtained by the simulation, to be used to update the statistics.
	 */
	private void sseqUpdate(PnSlowSeqentialMCTSNode currentNode, SlowSequentialMCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult) {

		currentNode.incrementTotVisits();

		// Get the leaf move from where to start updating
		SlowSequentialMCTSMoveStats theMoveToUpdate = jointMove.getLeafMove();

		// Get the index of the role that performs the leaf move.
		int roleIndex = ((this.myRole.getIndex()-1) + this.numRoles)%this.numRoles;

		// Update the score and the visits of the move.
		// For the leaf move, if this is the first visit, remove it from the unvisited leaf moves list.
		theMoveToUpdate.incrementScoreSum(simulationResult.getTerminalGoals()[roleIndex]);
		if(theMoveToUpdate.getVisits() == 0){
			currentNode.getUnvisitedLeaves().remove(theMoveToUpdate);
		}
		theMoveToUpdate.incrementVisits();

		// Get the parent move to be updated.
		theMoveToUpdate = theMoveToUpdate.getPreviousRoleMoveStats();

		// Until we reach the root move (i.e. our move), keep updating.
		while(theMoveToUpdate != null){
			// Compute the index of the role playing the parent move.
			roleIndex = ((roleIndex-1) + this.numRoles)%this.numRoles;

			// Update the score and the visits of the move.
			theMoveToUpdate.incrementScoreSum(simulationResult.getTerminalGoals()[roleIndex]);
			theMoveToUpdate.incrementVisits();

			// Get the parent move, that will be the chosen move for the previous role.
			theMoveToUpdate = theMoveToUpdate.getPreviousRoleMoveStats();
		}
	}

	@Override
	public String getStrategyParameters() {
		return null;
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

	@Override
	public void processPlayoutResult(MCTSNode leafNode,	SimulationResult simulationResult) {
		// TODO Auto-generated method stub
	}

}
