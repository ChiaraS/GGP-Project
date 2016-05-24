package ggpbasebenchmark;

import java.util.List;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class InternalReprIterativeDeepeningSearch extends SearchAlgorithm {

	private int upperDepthLimit;

	public InternalReprIterativeDeepeningSearch(InternalPropnetStateMachine stateMachine,
			int playclock) {
		super(stateMachine, playclock);
	}

	@Override
	public void doSearch(MachineState state) {
		if(upperDepthLimit == Integer.MAX_VALUE) {
			System.out.println("iterativeDeepening " + getPlayclock());
		} else {
			System.out.println("iterativeDeepeningFixed " + upperDepthLimit);
		}

		// Translate current state to internal state
		InternalPropnetMachineState internalState = ((InternalPropnetStateMachine)this.stateMachine).stateToInternalState(state);

		int depthLimit = 0;
		boolean finished = false;
		while (!finished && !timeout() && depthLimit<=upperDepthLimit) {
			System.out.println("depth limit: " + depthLimit);
			try {
				finished = dfs(internalState, depthLimit);
			} catch (Exception e) {
				e.printStackTrace();
			}
			++depthLimit;
		}
	}

	/**
	 * do a depth-first expansion of the game tree starting in state up depth steps deep
	 * @param state
	 * @param depth
	 * @return true if all leaf nodes in the expanded part of the tree are terminal states and no timeout occurred, false otherwise
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws StateMachineException
	 */
	private boolean dfs(InternalPropnetMachineState internalState, int depth) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException {
		if (timeout()) return false;

		InternalPropnetStateMachine internalReprStateMachine = (InternalPropnetStateMachine) this.stateMachine;

		if (internalReprStateMachine.isTerminal(internalState)) {
	        evaluateGoals(internalState);
			if (timeout()) return false;
			return true;
	    }
		if (depth <= 0) {
			return false;
		}
		List<List<InternalPropnetMove>> internalJointMoves = internalReprStateMachine.getLegalJointMoves(internalState);
		nbLegals++;
		boolean finished = true;
		for (List<InternalPropnetMove> internalJointMove : internalJointMoves) {
			if (timeout()) return false;
			InternalPropnetMachineState nextState = internalReprStateMachine.getInternalNextState(internalState, internalJointMove);
			++nbUpdates;
			finished = dfs(nextState, depth-1) && finished; // order matters here!
		}
		return finished;
	}

}
