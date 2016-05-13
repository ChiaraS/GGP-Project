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
		/*System.out.println();
		System.out.println();

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println();*/


		System.out.println("monteCarloSearch " + getPlayclock());
		long simulationCount = 0;
		while (!timeout()) {
			try {
				//System.out.println("Performing random simulation-start"); //!
				randomSimulation(state);
				//System.out.println("Performing random simulation-end"); //!
			} catch (Exception e) {
				System.out.println("I catched an exception!"); //!
				e.printStackTrace();
			}catch (Error e){ //!
				System.out.println("I catched an error!");
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
		//System.out.println("Cloning"); //!
		MachineState state = unChangableState.clone();
		//System.out.println("Terminality"); //!
		boolean terminal = stateMachine.isTerminal(state);
		//System.out.println("Terminality: " + terminal); //!
		while (!terminal) {
			//System.out.println("In Wile"); //!
			if (timeout()){	return; }
			//System.out.println("JointMove"); //!

			List<Move> jointMove = stateMachine.getRandomJointMove(state);
			System.out.println("Using SM INT"); //!
			//System.out.println("JointMove: " + jointMove); //!
			++nbLegals;
			//System.out.println("Legal increased"); //!
			if (timeout()){	return; }
			//System.out.println("NextState"); //!

			state = stateMachine.getNextStateDestructively(state, jointMove);
			System.out.println("Using SM INT"); //!
			//System.out.println("NextState " + state); //!
			++nbUpdates;
			//System.out.println("Terminality2"); //!

			terminal = stateMachine.isTerminal(state);
			System.out.println("Using SM INT"); //!
			//System.out.println("Terminality2: " + terminal); //!
		}
		//System.out.println("Goals"); //!
        evaluateGoals(state);
        System.out.println("Using SM INT"); //!
	}

}
