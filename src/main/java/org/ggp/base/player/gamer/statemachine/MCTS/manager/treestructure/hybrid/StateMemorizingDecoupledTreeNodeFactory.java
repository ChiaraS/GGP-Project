package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.FPGAMCTS.manager.treestructure.FpgaMctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class StateMemorizingDecoupledTreeNodeFactory extends TreeNodeFactory {

	public StateMemorizingDecoupledTreeNodeFactory(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
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
	public MctsNode createNewNode(MachineState state) {

		//System.out.println("Creating new node.");

		int goals[] = null;
		boolean terminal = false;

		DecoupledMctsMoveStats[][] ductMovesStats = null;

		Map<List<Move>,MachineState> nextStates = null;

		try {
			terminal = this.gameDependentParameters.getTheMachine().isTerminal(state);
		} catch (StateMachineException e) {
			GamerLogger.logError("MctsManager", "Failed to compute state terminality when creating new tree node. Considering node terminal.");
			GamerLogger.logStackTrace("MctsManager", e);
			terminal = true;
		}

		// Terminal state:
		if(terminal){

			goals = this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state);

		}else{// Non-terminal state:

			nextStates = this.getNextStatesMap(state);
			ductMovesStats = this.createDuctMctsMoves(state);

			// Error when computing moves.
			// If for at least one player the legal moves or the next states cannot be computed
			// (and thus the moves or the next states are returned as a null value), we consider
			// this node "pseudo-terminal" (i.e. the corresponding state is not terminal but we
			// cannot explore any of the next states, so we treat it as terminal during the MCT
			// search). This means that we will need the goal values in this node and they will
			// not change for all the rest of the search, so we compute them and memorize them.
			// NOTE (the following is all hypothetical, because I don't know if it can ever
			// happen that the FPGA-propnet cannot compute moves or next states. It can happen with
			// the software propnet): if the FPGA-propnet cannot compute moves and/or next states
			// then the goals for the pseudo-terminal state are computed with the same FPGA propnet,
			// which computes them performing the playouts. So if the FPGA-propnet cannot compute
			// moves and/or next states I suppose it also cannot perform playouts. This means that
			// the goals returned here depends on how the FPGA propnet deals with this situation.
			if(ductMovesStats == null || nextStates == null){
				// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
				// We use the state machine method that will return default goal values for the player for which goal
				// values cannot be computed in this state.
				goals = this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state);
				terminal = true;
			}
			// If the legal moves can be computed for every player, there is no need to compute the goals.
		}

		return createActualNewNode(nextStates, ductMovesStats, goals, terminal);
	}

	protected MctsNode createActualNewNode(Map<List<Move>,MachineState> nextStates, DecoupledMctsMoveStats[][] ductMovesStats,
			int[] goals, boolean terminal){
		return new FpgaMctsNode(nextStates, ductMovesStats, goals, terminal, this.gameDependentParameters.getNumRoles());
	}

	/**
	 * This method gets the map with all joint moves and corresponding next states.
	 *
	 * @param state the state for which to create the moves statistics.
	 * @return the moves statistics, if the moves can be computed, null otherwise.
	 */
	protected DecoupledMctsMoveStats[][] createDuctMctsMoves(MachineState state){

		List<Role> roles = this.gameDependentParameters.getTheMachine().getRoles();

		//System.out.println("NUMROLES = " + roles.size());

		DecoupledMctsMoveStats[][] moves = new DecoupledMctsMoveStats[this.gameDependentParameters.getNumRoles()][];

		try{
			List<Move> legalMoves;

			for(int i = 0; i < roles.size(); i++){

				legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(state, roles.get(i));

				moves[i] = new DecoupledMctsMoveStats[legalMoves.size()];
				for(int j = 0; j < legalMoves.size(); j++){
					moves[i][j] = new DecoupledMctsMoveStats(legalMoves.get(j));
				}
			}
		}catch(MoveDefinitionException | StateMachineException e){
			// If for at least one player the legal moves cannot be computed, we return null.
			GamerLogger.logError("MctsManager", "Failed to retrieve the legal moves while adding non-terminal DUCT state to the tree.");
			GamerLogger.logStackTrace("MctsManager", e);

			moves = null;
		}

		return moves;
	}

	/**
	 * This method creates the moves statistics to be put in the MC Tree node of the given state
	 * for the DUCT version of the MCTS player.
	 *
	 * @param state the state for which to create the moves statistics.
	 * @return the moves statistics, if the moves can be computed, null otherwise.
	 */
	protected Map<List<Move>,MachineState> getNextStatesMap(MachineState state){

		Map<List<Move>,MachineState> nextStates;

		try{
			nextStates = this.gameDependentParameters.getTheMachine().getAllJointMovesAndNextStates(state);
		}catch(TransitionDefinitionException | MoveDefinitionException | StateMachineException e){
			// If for at least one player the legal moves cannot be computed, we return null.
			GamerLogger.logError("MctsManager", "Failed to retrieve all joint moves with corresponding next states while adding non-terminal DUCT state to the tree.");
			GamerLogger.logStackTrace("MctsManager", e);

			nextStates = null;
		}

		return nextStates;
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
