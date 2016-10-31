package ggpbasebenchmark;

import java.util.List;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

public class InternalReprMonteCarloSearch extends SearchAlgorithm {

	public InternalReprMonteCarloSearch(InternalPropnetStateMachine stateMachine,
			int playclock) {
		super(stateMachine, playclock);
	}

	@Override
	public void doSearch(ExplicitMachineState state) {
		System.out.println("monteCarloSearch " + getPlayclock());

		// Translate current state to internal state
		CompactMachineState internalState = ((InternalPropnetStateMachine)this.stateMachine).convertToCompactMachineState(state);

		long simulationCount = 0;
		while (!timeout()) {
			try {
				randomSimulation(internalState);
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

	private void randomSimulation(CompactMachineState unChangableState) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException {
		CompactMachineState internalState = unChangableState.clone();

		InternalPropnetStateMachine internalReprStateMachine = (InternalPropnetStateMachine) this.stateMachine;

		boolean terminal = internalReprStateMachine.isTerminal(internalState);
		while (!terminal) {
			if (timeout()) return;
			List<CompactMove> internalJointMove = internalReprStateMachine.getRandomJointMove(internalState);
			++nbLegals;
			if (timeout()) return;
			internalState = internalReprStateMachine.getCompactNextState(internalState, internalJointMove);
			++nbUpdates;
			terminal = internalReprStateMachine.isTerminal(internalState);
		}
        evaluateGoals(internalState);
	}

}
