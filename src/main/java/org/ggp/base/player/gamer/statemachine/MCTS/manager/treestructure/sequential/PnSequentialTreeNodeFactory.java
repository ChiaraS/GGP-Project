package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.sequential;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.TreeNodeFactory;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class PnSequentialTreeNodeFactory implements TreeNodeFactory {

	private InternalPropnetStateMachine theMachine;

	/**
	 * The role that is actually performing the search.
	 * Needed by the SUCT version of MCTS.
	 */
	private InternalPropnetRole myRole;

	public PnSequentialTreeNodeFactory(InternalPropnetStateMachine theMachine, InternalPropnetRole myRole) {
		this.theMachine = theMachine;
		this.myRole = myRole;
	}

	@Override
	public PnMCTSNode createNewNode(InternalPropnetMachineState state) {
		//System.out.println("Creating new node.");

		int goals[] = null;
		boolean terminal = false;
		List<List<InternalPropnetMove>> allLegalMoves = null;

		SequentialMCTSMoveStats[] suctMovesStats = null;
		int unvisitedLeavesCount = 0;

		// Terminal state:
		if(this.theMachine.isTerminal(state)){

			goals = this.theMachine.getSafeGoals(state);
			terminal = true;

		}else{// Non-terminal state:

			try {
				allLegalMoves = this.theMachine.getAllLegalMoves(state);

				/*
				int r = 0;
				for(List<InternalPropnetMove> legalPerRole : allLegalMoves){
					System.out.println("Legal moves for role " + r);
					System.out.print("[ ");
					for(InternalPropnetMove m : legalPerRole){
						System.out.print("(" + m.getIndex() + ", " + this.theMachine.internalMoveToMove(m) + ") ");
					}
					System.out.println("]");
					r++;
				}
				*/

				suctMovesStats = this.createSUCTMCTSMoves(allLegalMoves);
				unvisitedLeavesCount = suctMovesStats[0].getUnvisitedSubleaves() * suctMovesStats.length;
				//this.printSUCTMovesTree(suctMovesStats, "");
			} catch (MoveDefinitionException e) {
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
				goals = this.theMachine.getSafeGoals(state);
				terminal = true;
				unvisitedLeavesCount = 0;
			}
			// If the legal moves can be computed for every player, there is no need to compute the goals.
		}

		return new PnSequentialMCTSNode(allLegalMoves, suctMovesStats, goals, terminal, unvisitedLeavesCount);

	}

	private SequentialMCTSMoveStats[] createSUCTMCTSMoves(List<List<InternalPropnetMove>> allLegalMoves){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();

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
		int myIndex = this.myRole.getIndex();

		List<InternalPropnetMove> myLegalMoves = allLegalMoves.get(myIndex);
		SequentialMCTSMoveStats[] moves = new SequentialMCTSMoveStats[myLegalMoves.size()];
		for(int i = 0; i < myLegalMoves.size(); i++){
			moves[i] = new SequentialMCTSMoveStats(createSUCTMCTSMoves((myIndex+1)%(roles.length), roles.length, allLegalMoves));
		}

		return moves;
	}

	private SequentialMCTSMoveStats[] createSUCTMCTSMoves(int roleIndex, int numRoles, List<List<InternalPropnetMove>> allLegalMoves){

		if(roleIndex == this.myRole.getIndex()){
			return null;
		}

		List<InternalPropnetMove> roleLegalMoves = allLegalMoves.get(roleIndex);
		SequentialMCTSMoveStats[] moves = new SequentialMCTSMoveStats[roleLegalMoves.size()];
		for(int i = 0; i < roleLegalMoves.size(); i++){
			moves[i] = new SequentialMCTSMoveStats(createSUCTMCTSMoves((roleIndex+1)%(numRoles), numRoles, allLegalMoves));
		}

		return moves;
	}

}
