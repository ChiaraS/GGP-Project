package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.TreeNodeFactory;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class SequentialTreeNodeFactory implements TreeNodeFactory {

	private AbstractStateMachine theMachine;

	/**
	 * The index in the default list of roles of the role that is actually performing the search.
	 * Needed by the SUCT version of MCTS.
	 */
	private int myRoleIndex;

	public SequentialTreeNodeFactory(AbstractStateMachine theMachine, int myRoleIndex) {
		this.theMachine = theMachine;
		this.myRoleIndex = myRoleIndex;
	}

	@Override
	public MCTSNode createNewNode(MachineState state) {
		//System.out.println("Creating new node.");

		int goals[] = null;
		boolean terminal = false;
		List<List<Move>> allLegalMoves = null;

		SequentialMCTSMoveStats[] suctMovesStats = null;
		int unvisitedLeavesCount = 0;

		try {
			terminal = this.theMachine.isTerminal(state);
		} catch (StateMachineException e) {
			GamerLogger.logError("MCTSManager", "Failed to compute state terminality when creating new tree node. Considering node terminal.");
			GamerLogger.logStackTrace("MCTSManager", e);
			terminal = true;
		}

		// Terminal state:
		if(terminal){

			goals = this.theMachine.getSafeGoalsAvgForAllRoles(state);

		}else{// Non-terminal state:

			try {
				allLegalMoves = this.theMachine.getAllLegalMovesForAllRoles(state);

				suctMovesStats = this.createSUCTMCTSMoves(allLegalMoves);
				unvisitedLeavesCount = suctMovesStats[0].getUnvisitedSubleaves() * suctMovesStats.length;
				//this.printSUCTMovesTree(suctMovesStats, "");
			} catch (MoveDefinitionException | StateMachineException e) {
				// Error when computing moves.
				GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SUCT state to the tree.");
				GamerLogger.logStackTrace("MCTSManager", e);
				// If for at least one player the legal moves cannot be computed we consider this node
				// "pseudo-terminal" (i.e. the corresponding state is not terminal but we cannot explore
				// any of the next states, so we treat it as terminal during the MCT search). This means
				// that we will need the goal values in this node and they will not change for all the rest
				// of the search, so we compute them and memorize them.

				// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
				// We use the state machine method that will return default goal values for the player for which goal
				// values cannot be computed in this state.

				//System.out.println("Null moves in non terminal state!");

				allLegalMoves = null;
				goals = this.theMachine.getSafeGoalsAvgForAllRoles(state);
				terminal = true;
				unvisitedLeavesCount = 0;
			}
			// If the legal moves can be computed for every player, there is no need to compute the goals.
		}

		return new SequentialMCTSNode(allLegalMoves, suctMovesStats, goals, terminal, unvisitedLeavesCount);

	}

	private SequentialMCTSMoveStats[] createSUCTMCTSMoves(List<List<Move>> allLegalMoves){

		List<Role> roles = this.theMachine.getRoles();

		// Get legal moves for all players.
		/*try {
			for(int i = 0; i < roles.length; i++){
				legalMoves.add(this.theMachine.getInternalLegalMoves(state, roles[i]));
			}
		}catch (MoveDefinitionException e) {
			// If for at least one player the legal moves cannot be computed, we return null.
			GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SUCT state to the tree.");
			GamerLogger.logStackTrace("MCTSManager", e);

			return null;
		} */

		// For all the moves of my role (i.e. the role actually performing the search)
		// create the SUCT move containing the move statistics.

		List<Move> myLegalMoves = allLegalMoves.get(this.myRoleIndex);
		SequentialMCTSMoveStats[] moves = new SequentialMCTSMoveStats[myLegalMoves.size()];
		for(int i = 0; i < myLegalMoves.size(); i++){
			moves[i] = new SequentialMCTSMoveStats(createSUCTMCTSMoves((this.myRoleIndex+1)%(roles.size()), roles.size(), allLegalMoves));
		}

		return moves;
	}

	private SequentialMCTSMoveStats[] createSUCTMCTSMoves(int roleIndex, int numRoles, List<List<Move>> allLegalMoves){

		if(roleIndex == this.myRoleIndex){
			return null;
		}

		List<Move> roleLegalMoves = allLegalMoves.get(roleIndex);
		SequentialMCTSMoveStats[] moves = new SequentialMCTSMoveStats[roleLegalMoves.size()];
		for(int i = 0; i < roleLegalMoves.size(); i++){
			moves[i] = new SequentialMCTSMoveStats(createSUCTMCTSMoves((roleIndex+1)%(numRoles), numRoles, allLegalMoves));
		}

		return moves;
	}

}
