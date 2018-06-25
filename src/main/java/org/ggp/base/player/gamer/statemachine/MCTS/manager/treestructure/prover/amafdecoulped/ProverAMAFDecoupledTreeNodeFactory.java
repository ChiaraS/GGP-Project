package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.amafdecoulped;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledTreeNodeFactory;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

public class ProverAMAFDecoupledTreeNodeFactory extends ProverDecoupledTreeNodeFactory{

	public ProverAMAFDecoupledTreeNodeFactory(StateMachine theMachine) {
		super(theMachine);
	}

	@Override
	public MctsNode createNewNode(ExplicitMachineState state) {

		//System.out.println("Creating new node.");

		double goals[] = null;
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
			goals = this.theMachine.getSafeGoalsAvg(state);
			terminal = true;

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
				goals = this.theMachine.getSafeGoalsAvg(state);
				terminal = true;
			}
			// If the legal moves can be computed for every player, there is no need to compute the goals.
		}

		return new ProverAMAFDecoupledMCTSNode(ductMovesStats, goals, terminal, this.theMachine.getExplicitRoles().size());
	}

}
