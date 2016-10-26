package org.ggp.base.apps.benchmark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.Pair;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * EndgameCaseGenerator uses a minimax solver to produce some test cases
 * for the PlayerTester. It runs a depth charge from the initial state of
 * a game down to a terminal state, and then backs off and runs minimax to
 * find the best moves in the resulting state. Since the state is near the
 * end of the game, the resulting minimax solve will usually finish quickly.
 * This produces a nice test case that can be used in the PlayerTester.
 *
 * Please note this will only produce a subset of the collection of tests that
 * are useful in PlayerTester: all of the tests that it produces are for states
 * that are near an ending to the game. Notably, it doesn't test critical moves
 * in the mid-game that heavily impact the outcome, but don't immediately lead
 * to a victory or loss for one player.
 *
 * @author Sam Schreiber
 */
public class EndgameCaseGenerator {
	public static void main(String[] args) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineException, StateMachineInitializationException {
		generateTestCase("connectFour", 0, 5, 6, new ProverStateMachine());
	}

	public static void generateTestCase(String gameKey, int nRole, int nBackoff, int nMaxDepth, StateMachine theMachine) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineException, StateMachineInitializationException {
        // Load the game and create a state machine for it
		Game theGame = GameRepository.getDefaultRepository().getGame(gameKey);
        theMachine.initialize(theGame.getRules(), Long.MAX_VALUE);
        ExplicitRole ourRole = theMachine.getRoles().get(nRole);

        // Once the game is loaded, run depth charges until we find a suitable
        // endgame backoff state that can be used to produce a test case.
        while (true) {
	        // Find a state that's nBackoff steps from a terminal state
	        List<ExplicitMachineState> theStates = new ArrayList<ExplicitMachineState>();
	        ExplicitMachineState theChargeState = theMachine.getInitialState();
	        while (!theMachine.isTerminal(theChargeState)) {
	        	theStates.add(theChargeState);
	        	theChargeState = theMachine.getRandomNextState(theChargeState);
	        }
	        ExplicitMachineState theState = theStates.get(Math.max(theStates.size() - nBackoff, 0));

	        // Solve the game from the backoff state. For moves that return
	        // definite scores, track the best and worst scores. For moves that
	        // don't return definite scores, track them separately.
	        int bestScore = 0;
	        int worstScore = 100;
	        List<Pair<ExplicitMove, Integer>> scoredMoves = new ArrayList<Pair<ExplicitMove, Integer>>();
	        Set<ExplicitMove> unscoredMoves = new HashSet<ExplicitMove>();
	        for (ExplicitMove ourMove : theMachine.getLegalMoves(theState, ourRole)) {
	        	Pair<Integer, Integer> theScore = minimax(theMachine, nRole, ourRole, theMachine.getRandomNextState(theState, ourRole, ourMove), nMaxDepth);
	        	if (theScore.left == theScore.right) {
		        	bestScore = Math.max(bestScore, theScore.left);
		        	worstScore = Math.min(worstScore, theScore.left);
		        	scoredMoves.add(Pair.of(ourMove, theScore.left));
	        	} else {
	        		unscoredMoves.add(ourMove);
	        	}
	        }

	        // If no moves had definite scores, it's a bad test case. Repeat!
	        if (worstScore > bestScore) {
	        	System.out.println("Found a backoff state with no fully scored moves.");
	        	continue;
	        }

	        // If all scored moves are equally good, it's a bad test case. Repeat!
	        if (bestScore == worstScore) {
	        	System.out.println("Found a backoff state in which all scored moves were equal at " + bestScore);
	        	continue;
	        }

	        // Select out the best moves for the "known good" answer. Also
	        // include any unscored moves in this set, since they might be
	        // good choices as well (we just don't know).
	        Set<ExplicitMove> bestMoves = new HashSet<ExplicitMove>();
	        for (Pair<ExplicitMove, Integer> scoredMove : scoredMoves) {
	        	if (scoredMove.right == bestScore) {
	        		bestMoves.add(scoredMove.left);
	        	}
	        }
	        StringBuilder goodMoveStrings = new StringBuilder();
	        for (ExplicitMove bestMove : bestMoves) {
	        	goodMoveStrings.append("\"" + bestMove + "\", ");
	        }
	        for (ExplicitMove unscoredMove : unscoredMoves) {
	        	goodMoveStrings.append("\"" + unscoredMove + "\", ");
	        }

	        // Also represent the current state as a symbol list.
	        StringBuilder theStateSymbols = new StringBuilder("( ");
	        for (GdlSentence aSentence : theState.getContents()) {
	        	theStateSymbols.append(aSentence.getBody().get(0) + " ");
	        }
	        theStateSymbols.append(")");

	        // And finally, output the code for the new test.
	        System.out.println("new PlayerTester.TestCase(\"Endgame\", \"" + gameKey + "\", 0, 15, 5, \"" + theStateSymbols + "\", new String[] {" + goodMoveStrings + "}),");
	        return;
        }
    }

	// This is a traditional minimax solver.
    private static Pair<Integer, Integer> minimax(StateMachine machine, int ourRoleIndex, ExplicitRole ourRole, ExplicitMachineState currentState, int depth) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, StateMachineException {
    	// If we've hit our max depth, return immediately, sending back
    	// no useful information about the final score.
    	if (depth < 0) {
    		return Pair.of(0, 100);
    	}

        // Upon reaching a terminal state, we know its value immediately.
        if (machine.isTerminal(currentState)) {
            int theScore = machine.getGoal(currentState, ourRole);
            return Pair.of(theScore, theScore);
        }

        // Otherwise, perform recursive descent to compute the state's value.
        List<List<ExplicitMove>> legalMoves = machine.getLegalJointMoves(currentState);
        Pair<Integer, Integer> overallScore = Pair.of(0, 0);
        for (ExplicitMove ourMove : machine.getLegalMoves(currentState, ourRole)) {
        	Pair<Integer, Integer> worstMove = Pair.of(100, 100);
            for (List<ExplicitMove> jointMove : legalMoves) {
                if (jointMove.get(ourRoleIndex).equals(ourMove)) {
                    ExplicitMachineState newState = machine.getNextState(currentState, jointMove);
                    Pair<Integer, Integer> score = minimax(machine, ourRoleIndex, ourRole, newState, depth - 1);
                    worstMove = Pair.of(Math.min(worstMove.left, score.left), Math.min(worstMove.right, score.right));
                    if(score.right == 0)
                    	break;
                }
            }
            overallScore = Pair.of(Math.max(overallScore.left, worstMove.left), Math.max(overallScore.right, worstMove.right));
            if(overallScore.left == 100)
            	break;
        }
        return overallScore;
    }
}