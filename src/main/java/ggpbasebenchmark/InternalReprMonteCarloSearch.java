package ggpbasebenchmark;

import java.util.List;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class InternalReprMonteCarloSearch extends SearchAlgorithm {

	public InternalReprMonteCarloSearch(InternalPropnetStateMachine stateMachine,
			int playclock) {
		super(stateMachine, playclock);
	}

	@Override
	public void doSearch(MachineState state) {
		System.out.println("monteCarloSearch " + getPlayclock());

		// Translate current state to internal state
		InternalPropnetMachineState internalState = ((InternalPropnetStateMachine)this.stateMachine).stateToInternalState(state);

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

	private void randomSimulation(InternalPropnetMachineState unChangableState) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException {
		InternalPropnetMachineState internalState = unChangableState.clone();

		InternalPropnetStateMachine internalReprStateMachine = (InternalPropnetStateMachine) this.stateMachine;

		boolean terminal = internalReprStateMachine.isTerminal(internalState);
		while (!terminal) {
			if (timeout()) return;
			List<InternalPropnetMove> internalJointMove = internalReprStateMachine.getRandomJointMove(internalState);
			++nbLegals;
			if (timeout()) return;
			internalState = internalReprStateMachine.getInternalNextState(internalState, internalJointMove);
			++nbUpdates;
			terminal = internalReprStateMachine.isTerminal(internalState);
		}
        evaluateGoals(internalState);
	}

}
