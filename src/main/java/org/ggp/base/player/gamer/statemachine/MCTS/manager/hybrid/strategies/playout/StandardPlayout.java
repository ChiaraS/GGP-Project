package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.JointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class StandardPlayout extends PlayoutStrategy {

	protected JointMoveSelector jointMoveSelector;

	public StandardPlayout(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, JointMoveSelector jointMoveSelector) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.jointMoveSelector = jointMoveSelector;
	}

	@Override
	public void clearComponent() {
		this.jointMoveSelector.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.jointMoveSelector.setUpComponent();
	}

	@Override
	public SimulationResult playout(MachineState state, int maxDepth) {

		// NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
		// we just return the final goals of the state. At the moment the MCTS manager already doesn't call the
        // play-out if the state is terminal or if the depth limit has been reached, so this check will never be
        // true, but it's here just to be safe.
        boolean terminal = true;

        try {
			terminal = this.gameDependentParameters.getTheMachine().isTerminal(state);
		} catch (StateMachineException e) {
			GamerLogger.logError("MCTSManager", "Exception computing state terminality while performing a playout.");
			GamerLogger.logStackTrace("MCTSManager", e);
			terminal = true;
		}

		if(terminal || maxDepth == 0){

			GamerLogger.logError("MCTSManager", "Playout strategy shouldn't be called on a terminal node. The MCTSManager must take care of computing the simulation result in this case.");

			return new SimulationResult(0, this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state));

		}

        int nDepth = 0;

        List<Move> jointMove;

        do{ // NOTE: if any of the try blocks fails on the first iteration this method will return a result with only the terminal goals of the starting state of the playout, depth 0 and empty moves list

        	jointMove = null;
			try {
				jointMove = this.jointMoveSelector.getJointMove(state);
			} catch (MoveDefinitionException | StateMachineException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}
			try {
				state = this.gameDependentParameters.getTheMachine().getNextState(state, jointMove);
			} catch (TransitionDefinitionException | StateMachineException e) {
				GamerLogger.logError("MCTSManager", "Exception getting the next state while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}

			nDepth++;

            try {
				terminal = this.gameDependentParameters.getTheMachine().isTerminal(state);
			} catch (StateMachineException e) {
				GamerLogger.logError("MCTSManager", "Exception computing state terminality while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				terminal = true;
				break;
			}

        }while(nDepth < maxDepth && !terminal);

        return new SimulationResult(nDepth, this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state));

	}

	@Override
	public String getComponentParameters() {
		return this.jointMoveSelector.printComponent();
	}

}
