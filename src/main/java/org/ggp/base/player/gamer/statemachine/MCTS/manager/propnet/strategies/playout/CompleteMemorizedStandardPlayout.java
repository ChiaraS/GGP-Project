package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.JointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class CompleteMemorizedStandardPlayout extends StandardPlayout {

	public CompleteMemorizedStandardPlayout(
			InternalPropnetStateMachine theMachine,
			JointMoveSelector jointMoveSelector) {

		super(theMachine, jointMoveSelector);

	}

	@Override
	public SimulationResult playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth) {
		//InternalPropnetMachineState lastState;

		// NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
		// then it's the responsibility of the MCTS manager to compute the simulation result (e.g. collect the
		// jointMoves and/or the goals), thus we return null.
		// ALSO NOTE that at the moment the MCTS manager already doesn't call the play-out if the state is terminal
		// or if the depth limit has been reached, so this check will never be true, but it's here just to be safe.
		if(this.theMachine.isTerminal(state) || maxDepth == 0){

			if(playoutVisitedNodes != null)
	        	playoutVisitedNodes[0] = 0;

			return null;
		}

        int nDepth = 0;

        List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

        List<int[]> allGoals = new ArrayList<int[]>();

        List<InternalPropnetMove> jointMove;

        do{

        	jointMove = null;
			try {
				jointMove = this.jointMoveSelector.getJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}

			allJointMoves.add(jointMove);

			state = this.theMachine.getInternalNextState(state, jointMove);

			allGoals.add(this.theMachine.getSafeGoalsAvg(state));

			nDepth++;

        }while(nDepth < maxDepth && !this.theMachine.isTerminal(state));

        if(playoutVisitedNodes != null)
        	playoutVisitedNodes[0] = nDepth;

        Collections.reverse(allJointMoves);

        Collections.reverse(allGoals);

        return new SimulationResult(allGoals.get(0), allJointMoves, allGoals);

	}

}
