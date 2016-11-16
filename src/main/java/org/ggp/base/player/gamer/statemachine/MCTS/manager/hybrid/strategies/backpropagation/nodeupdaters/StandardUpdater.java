package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMCTSNode;
import org.ggp.base.util.statemachine.structure.MachineState;

public class StandardUpdater extends NodeUpdater {

	public StandardUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.BackpropagationStrategy#update(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode, org.ggp.base.util.statemachine.structure.MachineState, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult)
	 */
	@Override
	public void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult){
		if(currentNode instanceof DecoupledMCTSNode && jointMove instanceof SequDecMCTSJointMove){
			this.decUpdate((DecoupledMCTSNode)currentNode, currentState, (SequDecMCTSJointMove)jointMove, simulationResult);
		}else if(currentNode instanceof SequentialMCTSNode && jointMove instanceof SequDecMCTSJointMove){
			this.seqUpdate((SequentialMCTSNode)currentNode, currentState, (SequDecMCTSJointMove)jointMove, simulationResult);
		}/*else if(currentNode instanceof SlowSeqentialMCTSNode && jointMove instanceof SlowSequentialMCTSJointMove){
			this.sseqUpdate((SlowSeqentialMCTSNode)currentNode, currentState, (SlowSequentialMCTSJointMove)jointMove, simulationResult);
		}*/else{
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
	private void decUpdate(DecoupledMCTSNode currentNode, MachineState currentState, SequDecMCTSJointMove jointMove, SimulationResult simulationResult) {

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
	private void seqUpdate(SequentialMCTSNode currentNode, MachineState currentState, SequDecMCTSJointMove jointMove, SimulationResult simulationResult) {

		currentNode.incrementTotVisits();

		int currentRoleIndex = this.gameDependentParameters.getMyRoleIndex();

		SequentialMCTSMoveStats currentStatsToUpdate = currentNode.getMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];

		while(currentRoleIndex != ((this.gameDependentParameters.getMyRoleIndex()-1+this.gameDependentParameters.getNumRoles())%this.gameDependentParameters.getNumRoles())){
			currentStatsToUpdate.incrementVisits();
			currentStatsToUpdate.incrementScoreSum(simulationResult.getTerminalGoals()[currentRoleIndex]);
			currentRoleIndex = (currentRoleIndex+1)%this.gameDependentParameters.getNumRoles();
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
			currentRoleIndex = this.gameDependentParameters.getMyRoleIndex();

			currentStatsToUpdate = currentNode.getMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];

			while(currentRoleIndex != ((this.gameDependentParameters.getMyRoleIndex()-1+this.gameDependentParameters.getNumRoles())%this.gameDependentParameters.getNumRoles())){
				currentStatsToUpdate.decreaseUnvisitedSubLeaves();
				currentRoleIndex = (currentRoleIndex+1)%this.gameDependentParameters.getNumRoles();
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
	/*
	private void sseqUpdate(SlowSeqentialMCTSNode currentNode, MachineState currentState, SlowSequentialMCTSJointMove jointMove, SimulationResult simulationResult) {

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
	*/

	@Override
	public void processPlayoutResult(MCTSNode leafNode, MachineState leafState,	SimulationResult simulationResult) {
		// TODO Auto-generated method stub
	}

}
