package csironi.ggp.course.algorithms;

import java.util.ArrayList;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class LimitedBFSearch {

	private ProverStateMachine theMachine;

	public LimitedBFSearch(ProverStateMachine theMachine) {
		this.theMachine = theMachine;
	}

	public void search(MachineState state, int maxDepth){
		int currentDepth = 0;

		ArrayList<MachineState> frontier = new ArrayList<MachineState>();

		ArrayList<MachineState> nextFrontier = new ArrayList<MachineState>();

		frontier.add(state);

		while(!(frontier.isEmpty())){

			for(MachineState s: frontier){
				if(this.theMachine.isTerminal(s)){
					try {
						this.theMachine.getGoals(s);
					} catch (GoalDefinitionException | StateMachineException e) {
						LOGGER.error("[BFS] [STEP "+ currentDepth + "] Exception when computing goals.", e);
					}
				}else if(currentDepth <= maxDepth){
					// Create all joint moves
					ArrayList<ArrayList<Move>> theJointMoves = new ArrayList<ArrayList<Move>>();

					// For every joint move add next state to the next frontier

				}
			}

			currentDepth++;

			frontier = nextFrontier;
			nextFrontier = new ArrayList<MachineState>();

		}

	}

}
