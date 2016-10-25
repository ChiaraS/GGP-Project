package ggpbasebenchmark;

import java.util.List;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public class IterativeDeepeningSearch extends SearchAlgorithm {

	private int upperDepthLimit;

	public IterativeDeepeningSearch(StateMachine stateMachine, int playclock, int depthLimit) {
		super(stateMachine, playclock);
		this.upperDepthLimit = depthLimit;
	}

	@Override
	public void doSearch(ProverMachineState state) {
		if(upperDepthLimit == Integer.MAX_VALUE) {
			System.out.println("iterativeDeepening " + getPlayclock());
		} else {
			System.out.println("iterativeDeepeningFixed " + upperDepthLimit);
		}
		int depthLimit = 0;
		boolean finished = false;
		while (!finished && !timeout() && depthLimit<=upperDepthLimit) {
			System.out.println("depth limit: " + depthLimit);
			try {
				finished = dfs(state, depthLimit);
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
	private boolean dfs(ProverMachineState state, int depth) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException {
		if (timeout()) return false;
		if (stateMachine.isTerminal(state)) {
	        evaluateGoals(state);
			if (timeout()) return false;
			return true;
	    }
		if (depth <= 0) {
			return false;
		}
		List<List<ProverMove>> jointMoves = stateMachine.getLegalJointMoves(state);
		nbLegals++;
		boolean finished = true;
		for (List<ProverMove> jointMove : jointMoves) {
			if (timeout()) return false;
			ProverMachineState nextState = stateMachine.getNextState(state, jointMove);
			++nbUpdates;
			finished = dfs(nextState, depth-1) && finished; // order matters here!
		}
		return finished;
	}

}
