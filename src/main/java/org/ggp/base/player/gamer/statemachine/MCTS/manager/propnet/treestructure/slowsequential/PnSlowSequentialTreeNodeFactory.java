package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.slowsequential;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.TreeNodeFactory;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class PnSlowSequentialTreeNodeFactory implements TreeNodeFactory {

	private InternalPropnetStateMachine theMachine;

	/**
	 * The role that is actually performing the search.
	 * Needed by the SUCT version of MCTS.
	 */
	private InternalPropnetRole myRole;

	public PnSlowSequentialTreeNodeFactory(InternalPropnetStateMachine theMachine, InternalPropnetRole myRole) {
		this.theMachine = theMachine;
		this.myRole = myRole;
	}

	@Override
	public MCTSNode createNewNode(InternalPropnetMachineState state) {

		int goals[] = null;
		boolean terminal = false;
		List<List<InternalPropnetMove>> allLegalMoves = null;

		SlowSequentialMCTSMoveStats[] ssuctMovesStats = null;
		List<SlowSequentialMCTSMoveStats> unvisitedLeaves = null;

		// Terminal state:
		if(this.theMachine.isTerminal(state)){

			goals = this.theMachine.getSafeGoals(state);
			terminal = true;

		}else{// Non-terminal state:

			try {
				allLegalMoves = this.theMachine.getAllLegalMoves(state);
				// The method that creates the SlowSUCTMCTSMoves assumes that the unvisitedLeaves parameter
				// passed as input will be initialized to an empty list. The method will then fill it
				// with all the SUCTMoves that are leaves of the tree representing the move statistics.
				// Such leaf moves represent the end of a path that form a joint move and are included
				// in the unvisitedLeaves list if the corresponding joint move hasn't been visited yet
				// during the search.
				unvisitedLeaves = new ArrayList<SlowSequentialMCTSMoveStats>();
				ssuctMovesStats = this.createSlowSUCTMCTSMoves(allLegalMoves, unvisitedLeaves);
				//this.printMovesTree(ssuctMovesStats, "");
			} catch (MoveDefinitionException e) {
				// Error when computing moves.
				GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SlowSUCT state to the tree.");
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
				goals = this.theMachine.getSafeGoals(state);
				terminal = true;
				unvisitedLeaves = null;
			}

			// If the legal moves can be computed for every player, there is no need to compute the goals.
		}

		return new PnSlowSeqentialMCTSNode(ssuctMovesStats, unvisitedLeaves, goals, terminal);
	}

	private SlowSequentialMCTSMoveStats[] createSlowSUCTMCTSMoves(List<List<InternalPropnetMove>> allLegalMoves, List<SlowSequentialMCTSMoveStats> unvisitedLeaves){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();

		// For all the moves of my role (i.e. the role actually performing the search)
		// create the SUCT move containing the move statistics.
		int myIndex = this.myRole.getIndex();

		List<InternalPropnetMove> myLegalMoves = allLegalMoves.get(myIndex);
		SlowSequentialMCTSMoveStats[] moves = new SlowSequentialMCTSMoveStats[myLegalMoves.size()];
		for(int i = 0; i < myLegalMoves.size(); i++){
			moves[i] = new SlowSequentialMCTSMoveStats(myLegalMoves.get(i), i, createSlowSUCTMCTSMoves((myIndex+1)%(roles.length), roles.length, allLegalMoves, unvisitedLeaves));
			if(moves[i].getNextRoleMovesStats() == null){
				unvisitedLeaves.add(moves[i]);
			}
		}

		return moves;
	}

	private SlowSequentialMCTSMoveStats[] createSlowSUCTMCTSMoves(int roleIndex, int numRoles, List<List<InternalPropnetMove>> legalMoves, List<SlowSequentialMCTSMoveStats> unvisitedLeaves){

		if(roleIndex == this.myRole.getIndex()){
			return null;
		}

		List<InternalPropnetMove> roleLegalMoves = legalMoves.get(roleIndex);
		SlowSequentialMCTSMoveStats[] moves = new SlowSequentialMCTSMoveStats[roleLegalMoves.size()];
		for(int i = 0; i <roleLegalMoves.size(); i++){
			moves[i] = new SlowSequentialMCTSMoveStats(roleLegalMoves.get(i), i, createSlowSUCTMCTSMoves((roleIndex+1)%(numRoles), numRoles, legalMoves, unvisitedLeaves));
			if(moves[i].getNextRoleMovesStats() == null){
				unvisitedLeaves.add(moves[i]);
			}
		}

		return moves;
	}


}
