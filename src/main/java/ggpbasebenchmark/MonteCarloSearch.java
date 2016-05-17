package ggpbasebenchmark;

import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MonteCarloSearch extends SearchAlgorithm {

	public MonteCarloSearch(StateMachine stateMachine, int playclock) {
		super(stateMachine, playclock);
	}

	@Override
	public void doSearch(MachineState state) {
		System.out.println("monteCarloSearch " + getPlayclock());
		long simulationCount = 0;
		while (!timeout()) {
			try {
				randomSimulation(state);
			} catch (Exception e) {
				e.printStackTrace();
			}
			++simulationCount;
//			if (simulationCount % 100 == 0) {
//				System.out.println("#simulations: " + simulationCount);
//			}
		}
		System.out.println("#simulations: " + simulationCount);
	}

	private void randomSimulation(MachineState unChangableState) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException {
		MachineState state = unChangableState.clone();
		boolean terminal = stateMachine.isTerminal(state);
		while (!terminal) {
			if (timeout()) return;
			List<Move> jointMove = stateMachine.getRandomJointMove(state);
			++nbLegals;
			if (timeout()) return;
			state = stateMachine.getNextStateDestructively(state, jointMove);
			++nbUpdates;
			terminal = stateMachine.isTerminal(state);
		}
        evaluateGoals(state);
	}

}
