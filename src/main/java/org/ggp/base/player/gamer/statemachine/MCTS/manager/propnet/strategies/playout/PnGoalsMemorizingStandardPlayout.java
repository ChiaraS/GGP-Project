package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnGoalsMemorizingStandardPlayout extends PnStandardPlayout {

	public PnGoalsMemorizingStandardPlayout(
			InternalPropnetStateMachine theMachine,
			PnJointMoveSelector jointMoveSelector) {
		super(theMachine, jointMoveSelector);
	}

	@Override
	public PnSimulationResult playout(CompactMachineState state, int maxDepth) {
		//InternalPropnetMachineState lastState;

		// NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
		// we just return the final goals of the state. At the moment the MCTS manager already doesn't call the
        // play-out if the state is terminal or if the depth limit has been reached, so this check will never be
        // true, but it's here just to be safe.
		if(this.theMachine.isTerminal(state) || maxDepth == 0){

			//if(playoutVisitedNodes != null)
	        //	playoutVisitedNodes[0] = 0;

			GamerLogger.logError("MCTSManager", "Playout strategy shouldn't be called on a terminal node. The MCTSManager must take care of computing the simulation result in this case.");
			//throw new RuntimeException("Playout strategy called on a terminal node.");

			return new PnSimulationResult(0, this.theMachine.getSafeGoalsAvg(state));

		}

        int nDepth = 0;

        List<int[]> allGoals = new ArrayList<int[]>();

        List<CompactMove> jointMove;

        allGoals.add(this.theMachine.getSafeGoalsAvg(state));

        do{

        	jointMove = null;
			try {
				jointMove = this.jointMoveSelector.getJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}

			state = this.theMachine.getCompactNextState(state, jointMove);

			allGoals.add(this.theMachine.getSafeGoalsAvg(state));

			nDepth++;

        }while(nDepth < maxDepth && !this.theMachine.isTerminal(state));

        //if(playoutVisitedNodes != null)
        //	playoutVisitedNodes[0] = nDepth;

        Collections.reverse(allGoals);

        return new PnSimulationResult(nDepth, allGoals);

	}

}
