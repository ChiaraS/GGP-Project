package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.decoupled;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverTreeNodeFactory;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;

public class ProverDecoupledTreeNodeFactory implements ProverTreeNodeFactory{

	protected StateMachine theMachine;

	public ProverDecoupledTreeNodeFactory(StateMachine theProverMachine) {
		this.theMachine = theProverMachine;
	}

	/**
	 * This method creates a Monte Carlo tree node corresponding to the given state
	 * for the DUCT (Decoupled UCT version of the MCTS algorithm).
	 *
	 * 1. If the state is terminal the corresponding node will memorize the goals
	 * for each player in the state and the fact that the state is terminal.
	 * The legal moves will be null since there are supposed to be none in a
	 * terminal state. If an error occurs in computing the goal for a player, its
	 * goal value will be set to the default value (0 at the moment).
	 *
	 * 2. If the state is not terminal and the legal moves can be computed for each
	 * role with no errors the corresponding node will memorize such moves, their
	 * statistics, null goals and the fact that the node is not terminal.
	 *
	 * 3. If the state is not terminal but at least for one role the legal moves cannot
	 * be computed correctly the corresponding node will be treated as a pseudo-terminal
	 * node. This means that during the rest of the search, whenever this node will be
	 * encountered, it will be treated as terminal even if the corresponding state isn't.
	 * This choice has been made because without complete joint moves it is not possible
	 * to explore any further state from here. Thus the node will memorize the fact that
	 * the node is terminal (EVEN IF THE STATE ISN'T) and the goals for the players in
	 * the state (assigning default values if they cannot be computed), while the moves
	 * will be set to null.
	 *
	 * @param state the state for which to crate the tree node.
	 * @return the tree node corresponding to the state.
	 */
	@Override
	public MCTSNode createNewNode(MachineState state) {

		//System.out.println("Creating new node.");

		int goals[] = null;
		boolean terminal = false;

		ProverDecoupledMCTSMoveStats[][] ductMovesStats = null;

		try {
			terminal = this.theMachine.isTerminal(state);
		} catch (StateMachineException e) {
			GamerLogger.logError("MCTSManager", "Failed to compute state terminality.");
			GamerLogger.logStackTrace("MCTSManager", e);
			terminal = true;
		}

		// Terminal state:
		if(terminal){

			goals = this.theMachine.getSafeGoals(state);

		}else{// Non-terminal state:

				ductMovesStats = this.createDUCTMCTSMoves(state);

				// Error when computing moves.
				// If for at least one player the legal moves cannot be computed (an thus the moves
				// are returned as a null value), we consider this node "pseudo-terminal" (i.e. the
				// corresponding state is not terminal but we cannot explore any of the next states,
				// so we treat it as terminal during the MCT search). This means that we will need
				// the goal values in this node and they will not change for all the rest of the
				// search, so we compute them and memorize them.
				if(ductMovesStats == null){
					// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
					// We use the state machine method that will return default goal values for the player for which goal
					// values cannot be computed in this state.
					goals = this.theMachine.getSafeGoals(state);
					terminal = true;
				}
				// If the legal moves can be computed for every player, there is no need to compute the goals.
			}

			return new ProverDecoupledMCTSNode(ductMovesStats, goals, terminal);
	}

	/**
	 * This method creates the moves statistics to be put in the MC Tree node of the given state
	 * for the DUCT version of the MCTS player.
	 *
	 * @param state the state for which to create the moves statistics.
	 * @return the moves statistics, if the moves can be computed, null otherwise.
	 */
	protected ProverDecoupledMCTSMoveStats[][] createDUCTMCTSMoves(MachineState state){

		List<Role> roles = this.theMachine.getRoles();
		ProverDecoupledMCTSMoveStats[][] moves = new ProverDecoupledMCTSMoveStats[roles.size()][];

		try{
			List<Move> legalMoves;

			for(int i = 0; i < roles.size(); i++){

				legalMoves = this.theMachine.getLegalMoves(state, roles.get(i));

				moves[i] = new ProverDecoupledMCTSMoveStats[legalMoves.size()];
				for(int j = 0; j < legalMoves.size(); j++){
					moves[i][j] = new ProverDecoupledMCTSMoveStats(legalMoves.get(j));
				}
			}
		}catch(MoveDefinitionException | StateMachineException e){
			// If for at least one player the legal moves cannot be computed, we return null.
			GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal DUCT state to the tree.");
			GamerLogger.logStackTrace("MCTSManager", e);

			moves = null;
		}

		return moves;
	}

}
