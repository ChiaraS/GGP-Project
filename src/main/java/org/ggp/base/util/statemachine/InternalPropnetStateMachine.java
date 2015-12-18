package org.ggp.base.util.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

/**
 * Provides the base class for all state machine implementations that are based on the version
 * of the propnet that provides an alternative internal representation of the machine states,
 * the moves and the roles.
 */
public abstract class InternalPropnetStateMachine extends StateMachine{

	/** Standard state machine methods rewritten using internal representation for states, moves and roles. **/

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	public abstract boolean isTerminal(InternalPropnetMachineState state);

	/**
	 * Computes the goal for a role in the given state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	public abstract int getGoal(InternalPropnetMachineState state, InternalPropnetRole role) throws GoalDefinitionException;

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	public abstract InternalPropnetMachineState getInternalInitialState();

	/**
	 * Returns the internal representation of roles.
	 *
	 * @return the internal representation of roles.
	 */
	public abstract InternalPropnetRole[] getInternalRoles();

	/**
	 * Computes the legal moves for role in state.
	 */
	public abstract List<InternalPropnetMove> getInternalLegalMoves(InternalPropnetMachineState state, InternalPropnetRole role)throws MoveDefinitionException;

	/**
	 * Computes the next state given a state and the list of moves.
	 */
	public abstract InternalPropnetMachineState getInternalNextState(InternalPropnetMachineState state, List<InternalPropnetMove> moves);

	/***************** Extra methods to replace the ones offered by the StateMahcine *****************/

    /**
     * Returns the goal values for each role in the given state. The goal values
     * are listed in the same order the roles are listed in the game rules, which
     * is the same order in which they're returned by {@link #getRoles()}.
     *
     * @throws GoalDefinitionException if there is no goal value or more than one
     * goal value for any one role in the given state. If this occurs when this
     * is called on a terminal state, this indicates an error in either the game
     * description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the list
     * with the goals for all the roles in the given state because of an error
     * that occurred in the state machine and couldn't be handled.
     */
    public int[] getGoals(InternalPropnetMachineState state) throws GoalDefinitionException{
    	InternalPropnetRole[] theRoles = this.getInternalRoles();
    	int[] theGoals = new int[theRoles.length];
        for (int i = 0; i < theRoles.length; i++) {
            theGoals[i] = getGoal(state, theRoles[i]);
        }
        return theGoals;
    }

    /**
     * Returns a terminal state derived from repeatedly making random joint moves
     * until reaching the end of the game.
     *
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made to reach a terminal state.
     *
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to completely perform a
     * playout of the game because of an error that occurred in the state machine and
     * couldn't be handled.
     */
	public InternalPropnetMachineState performDepthCharge(InternalPropnetMachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException {
        int nDepth = 0;
        while(!isTerminal(state)) {
            nDepth++;
            state = getInternalNextState(state, getRandomJointMove(state));
        }
        if(theDepth != null)
            theDepth[0] = nDepth;
        return state;
    }

    /**
     * Returns a state derived from repeatedly making random joint moves
     * until reaching the end of the game or the maximum given depth.
     *
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made to reach the returned state.
     * @param maxDepth the maximum depth of the tree that this method must explore.
     *
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to completely perform a
     * playout of the game because of an error that occurred in the state machine and
     * couldn't be handled.
     */
	public InternalPropnetMachineState performLimitedDepthCharge(InternalPropnetMachineState state, final int[] theDepth, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException {
        int nDepth = 0;

        while(nDepth < maxDepth && !isTerminal(state)) {
            nDepth++;
            state = getInternalNextState(state, getRandomJointMove(state));
        }
        if(theDepth != null)
            theDepth[0] = nDepth;
        return state;
    }

    /**
     * Like performDepthCharge() method, but this one checks after visiting each node
     * if it has been interrupted. If so it makes sure that the array theDepth contains
     * the currently reached depth and returns null as terminal state.
     * Moreover, when any other exception is thrown while visiting the nodes, the number
     * of nodes visited so far (nDepth) is returned and the exception is not re-thrown,
     * since this method is only used to check the amount of nodes that the state machine
     * can visit in a certain amount of time. Also in this case null will be returned as
     * terminal state.
     *
     * @param state the state from where to start the simulation.
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made until the current visited state.
     *
     */
	public InternalPropnetMachineState interruptiblePerformDepthCharge(InternalPropnetMachineState state, final int[] theDepth) /*throws TransitionDefinitionException, MoveDefinitionException, StateMachineException*/ {
        int nDepth = 0;
        try {
	        while(!isTerminal(state)) {

	            state = getInternalNextState(state, getRandomJointMove(state));

	            nDepth++;

				ConcurrencyUtils.checkForInterruption();
			}
        } catch (InterruptedException | MoveDefinitionException e) {
			// This method can return a consistent result even if it has not completed execution
			// so the InterruptedException is not re-thrown
			if(theDepth != null)
	            theDepth[0] = nDepth;
	        return null;
		}
        if(theDepth != null)
            theDepth[0] = nDepth;
        return state;
    }

    /**
     * Returns a random joint move from among all the possible joint moves in
     * the given state.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the random
     * joint move in the given state because of an error that occurred in the state
     * machine and couldn't be handled.
     */
	public List<InternalPropnetMove> getRandomJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{
        List<InternalPropnetMove> random = new ArrayList<InternalPropnetMove>();
        for(InternalPropnetRole role : this.getInternalRoles()) {
            random.add(getRandomMove(state, role));
        }

        return random;
    }

    /**
     * Returns a random move from among the possible legal moves for the
     * given role in the given state.
     *
     * @throws MoveDefinitionException if the role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute a
     * random move for the given role in the given state because of an
     * error that occurred in the state machine and couldn't be handled.
     */
	public InternalPropnetMove getRandomMove(InternalPropnetMachineState state, InternalPropnetRole role) throws MoveDefinitionException{
        List<InternalPropnetMove> legals = getInternalLegalMoves(state, role);
        return legals.get(new Random().nextInt(legals.size()));
    }
}