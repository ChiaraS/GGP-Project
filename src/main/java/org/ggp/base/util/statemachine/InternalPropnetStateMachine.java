package org.ggp.base.util.statemachine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

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
	public int getGoal(InternalPropnetMachineState state, InternalPropnetRole role) throws GoalDefinitionException{
		List<Integer> goals = this.getOneRoleGoals(state, role);

		if(goals.size() > 1){
			GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(this.internalStateToState(state), this.internalRoleToRole(role));
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(goals.size() == 0){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(this.internalStateToState(state), this.internalRoleToRole(role));
		}

		// Return the single goal for the given role in the given state.
		return goals.get(0);
	}

    /**
     * Returns the goal value(s) for the given role in the given state. Goal values
     * are always between 0 and 100.
     *
     * @throws StateMachineException if it was not possible to compute the goal value
     * for the given role because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    public abstract List<Integer> getOneRoleGoals(InternalPropnetMachineState state, InternalPropnetRole role);


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


	/************************************** Translation methods **************************************/

	public abstract InternalPropnetMachineState stateToInternalState(ProverMachineState state);

	public abstract ProverMachineState internalStateToState(InternalPropnetMachineState state);

	public abstract ProverRole internalRoleToRole(InternalPropnetRole role);

	public abstract InternalPropnetRole roleToInternalRole(ProverRole role);

	public abstract ProverMove internalMoveToMove(InternalPropnetMove move);

	public abstract InternalPropnetMove moveToInternalMove(ProverMove move);

	/**
	 * Useful when we need to translate a joint move. Faster than translating the moves one by one.
	 *
	 * @param move
	 * @param roleIndex
	 * @return
	 */
	public abstract List<InternalPropnetMove> movesToInternalMoves(List<ProverMove> moves);


	/***************** Extra methods to replace the ones offered by the StateMachine *****************/

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
    public List<Integer> getGoals(InternalPropnetMachineState state) throws GoalDefinitionException{
    	InternalPropnetRole[] theRoles = this.getInternalRoles();
    	List<Integer> theGoals = new ArrayList<Integer>(theRoles.length);
        for(int i = 0; i < theRoles.length; i++) {
            theGoals.add(getGoal(state, theRoles[i]));
        }
        return theGoals;
    }

    /**
     * Returns a list containing a list for each role with all the goal values for
     * that role in the given state. The lists of goal values are listed in the
     * same order the roles are listed in the game rules, which is the same order
     * in which they're returned by {@link #getRoles()}. If a list is empty it means
     * that the role has no goals in the given state.
     *
     * @throws StateMachineException if it was not possible to compute the list
     * with the goals for all the roles in the given state because of an error
     * that occurred in the state machine and couldn't be handled.
     */
    public List<List<Integer>> getAllRolesGoals(InternalPropnetMachineState state) throws StateMachineException {
    	InternalPropnetRole[] theRoles = this.getInternalRoles();
    	List<List<Integer>> theGoals = new ArrayList<List<Integer>>(theRoles.length);

    	for(InternalPropnetRole r : this.getInternalRoles()) {
            theGoals.add(this.getOneRoleGoals(state, r));
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
     * Returns a state derived from repeatedly making random joint moves until
     * reaching the end of the game or the maximum given depth or a non-terminal
     * state from which it cannot continue the playout (for example because an
     * exception is thrown when computing legal moves).
     *
     * @param state the state from where to start the playout.
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made to reach the returned state.
     * @param maxDepth the maximum depth of the tree that this method must explore.
     *
     * Note: this method is safe, meaning that it won't throw any checked Exception,
     * but it will always return a state (that might not be terminal).
     */
	public InternalPropnetMachineState performSafeLimitedDepthCharge(InternalPropnetMachineState state, final int[] theDepth, int maxDepth){
        int nDepth = 0;

        while(nDepth < maxDepth && !isTerminal(state)) {

        	List<InternalPropnetMove> jointMove = null;
			try {
				jointMove = getRandomJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("StateMachine", "Exception getting a joint move while performing safe limited depth charges.");
				GamerLogger.logStackTrace("StateMachine", e);
				break;
			}
			state = getInternalNextState(state, jointMove);
            nDepth++;
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
     * Returns a random joint move from among all the possible joint moves in
     * the given state in which the given role makes the given move.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute a random
     * joint move in the given state with the given role performing the given move
     * because of an error that occurred in the state machine and couldn't be handled.
     */
    public List<InternalPropnetMove> getRandomJointMove(InternalPropnetMachineState state, InternalPropnetRole role, InternalPropnetMove move) throws MoveDefinitionException, StateMachineException
    {
        List<InternalPropnetMove> random = new ArrayList<InternalPropnetMove>();
        for (InternalPropnetRole r : getInternalRoles()) {
            if (r.equals(role)) {
                random.add(move);
            }else{
                random.add(getRandomMove(state, r));
            }
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



	/*
	public int[] getLossGoals(InternalPropnetRole myRole){
		int[] goals;
		int numRoles = this.getInternalRoles().length;
		goals = new int[numRoles];
		if(numRoles > 1){
			for(int i = 0; i < goals.length; i++){
				// Attention! Since this rounds the goals to the next integer, it might make a zero-sum game loose
				// the property of being zero-sum. However, this doesn't influence our MCTS implementation since it
				// does not assume that games are always zero-sum.
				goals[i] = (int) Math.round(100.0 / ((double)numRoles-1.0));
			}
		}
		goals[myRole.getIndex()] = 0;

		return goals;
	}*/

	/*
	public int[] getTieGoals(){
		int[] goals;
		int numRoles = this.getInternalRoles().length;
		goals = new int[numRoles];
		for(int i = 0; i < goals.length; i++){
			// Attention! Since this rounds the goals to the next integer, it might make a zero-sum game loose
			// the property of being zero-sum. However, this doesn't influence our MCTS implementation.
			goals[i] = (int) Math.round(100.0 / ((double)numRoles));
		}

		return goals;
	}*/

	/**
     * Returns the goal values for each role in the given state. If and when a goal
     * for a player cannot be computed in the state (either because of an error in
     * the description or because the state is non-terminal and so goals haven't
     * been defined), the corresponding goal value is set to 0 (loss).
     * The goal values are listed in the same order the roles are listed in the game
     * rules, which is the same order in which they're returned by {@link #getRoles()}.
     *
     * This method is safe, meaning that it won't throw any GoalDefinitionException,
     * but it will set a zero value for the goals when they cannot be computed or an
     * average value when there is more than one goal per role.
     *
     * Note: method suited to be used for terminal states, where an error computing a
     * goal must be penalized (i.e. we don't want to end the game in a terminal state
     * we don't know anything about for our player).
     *
     * @param state the state for which to compute the goals.
     */
    public int[] getSafeGoalsAvg(InternalPropnetMachineState state){
    	InternalPropnetRole[] theRoles = this.getInternalRoles();
    	int[] theGoals = new int[theRoles.length];
    	int avg;
    	List<Integer> roleGoals = null;

    	for (int i = 0; i < theRoles.length; i++) {

        	roleGoals = this.getOneRoleGoals(state, theRoles[i]);

        	if(roleGoals != null && !roleGoals.isEmpty()){

        		avg = 0;

        		for(Integer goal : roleGoals){
        			avg += goal;
        		}

        		theGoals[i] = (int) Math.round(((double)avg)/((double)roleGoals.size()));
        	}else{
        		theGoals[i] = 0;
        	}
        }
        return theGoals;
    }

	/**
     * Returns the goal values for each role in the given state. If and when a goal
     * for a player cannot be computed in the state (either because of an error in
     * the description or because the state is non-terminal and so goals haven't
     * been defined), the corresponding goal value is set to 0 (loss).
     * The goal values are listed in the same order the roles are listed in the game
     * rules, which is the same order in which they're returned by {@link #getRoles()}.
     *
     * This method is safe, meaning that it won't throw any GoalDefinitionException,
     * but it will set a zero value for the goals when they cannot be computed.
     *
     * Note: method meant to be used for terminal states, where an error computing a
     * goal must be penalized (i.e. we don't want to end the game in a terminal state
     * we don't know anything about for our player).
     *
     * @param state the state for which to compute the goals.
     */
    public int[] getSafeGoals(InternalPropnetMachineState state){
    	InternalPropnetRole[] theRoles = this.getInternalRoles();
    	int[] theGoals = new int[theRoles.length];
        for (int i = 0; i < theRoles.length; i++) {
            try {
				theGoals[i] = getGoal(state, theRoles[i]);
			} catch (GoalDefinitionException e){
				GamerLogger.logError("StateMachine", "Failed to compute a goal value when computing safe goals.");
				GamerLogger.logStackTrace("StateMachine", e);
				theGoals[i] = 0;
			}
        }
        return theGoals;
    }

	/**
     * Returns the goal values for each role in the given state. If and when a goal
     * for a player cannot be computed in the state (either because of an error in
     * the description or because the state is non-terminal and so goals haven't
     * been defined), the corresponding goal value is set to 0 (loss).
     * If the goals cannot be computed for all players, then each of them will get
     * a tie goal (i.e. 100/#roles when #roles > 1, 50 when there is only one goal).
     * The goal values are listed in the same order the roles are listed in the game
     * rules, which is the same order in which they're returned by {@link #getRoles()}.
     *
     * This method is safe, meaning that it won't throw any GoalDefinitionException,
     * but it will set a zero value for the goals when they cannot be computed.
     *
     * Note: method meant to be used for non-terminal states. An error computing the
     * goal for a role in a non-terminal state doesn't mean that the state is bad for
     * that role. It can be that the goals haven't been defined for that non-terminal
     * state. For a non-terminal state, if some goals can be computed, we assume that
     * the state is "bad" (i.e. a loss) for the players that cannot compute the goal,
     * thus we assign them a 0 goal. However, if no player can compute its goal, we
     * consider the state as a tie.
     *
     * @param state the state for which to compute the goals.
     */
    public int[] getSafeGoalsTie(InternalPropnetMachineState state){
    	InternalPropnetRole[] theRoles = this.getInternalRoles();
    	int failures = 0;
    	int[] theGoals = new int[theRoles.length];
        for (int i = 0; i < theRoles.length; i++) {
            try {
				theGoals[i] = getGoal(state, theRoles[i]);
			} catch (GoalDefinitionException e){
				GamerLogger.logError("StateMachine", "Failed to compute a goal value when computing safe goals with tie default.");
				GamerLogger.logStackTrace("StateMachine", e);
				theGoals[i] = 0; // TODO: should this be 50???
				failures++;
			}
        }

        // If computation of goal failed for all players...
        if(failures == theRoles.length){
        	// Distinguish the single-player case from the multi-player case.
        	if(theRoles.length == 1){
        		theGoals[0] = 50;
        	}else{
        		int defaultGoal = (int) Math.round(100.0 / ((double)theRoles.length));
        		for(int i = 0; i < theGoals.length; i++){
        			// Attention! Since this rounds the goals to the next integer, it might make a zero-sum game loose
        			// the property of being zero-sum. However, this doesn't influence our MCTS implementation.
        			theGoals[i] = defaultGoal;
        		}
        	}
        }
        return theGoals;

    }

    /**
     * This method returns the legal moves for all players in the state.
     * The order of the roles in the list is the standard role order (i.e.
     * the same as the one of the list with roles).
     *
     * @param state
     * @return
     * @throws MoveDefinitionException
     */
    public List<List<InternalPropnetMove>> getAllLegalMoves(InternalPropnetMachineState state) throws MoveDefinitionException{
    	if(this.isTerminal(state)){
    		return null;
    	}else{
    		List<List<InternalPropnetMove>> legalMoves = new ArrayList<List<InternalPropnetMove>>();

    		// Get legal moves for all players.
    		for(int i = 0; i < this.getInternalRoles().length; i++){
    			legalMoves.add(this.getInternalLegalMoves(state, this.getInternalRoles()[i]));
    		}
    		return legalMoves;

    	}
    }

    /**
     * Returns a list containing every joint move possible in the given state.
     * A joint move consists of one move for each role, with the moves in the
     * same ordering that their roles have in {@link #getRoles()}.
     * <p>
     * The list of possible joint moves is the Cartesian product of the lists
     * of legal moves available for each player.
     * <p>
     * If only one player has more than one legal move, then the number of
     * joint moves returned will equal the number of possible moves for that
     * player.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the legal
     * joint moves for the given state because of an error that occurred in the
     * state machine and couldn't be handled.
     */
    public List<List<InternalPropnetMove>> getLegalJointMoves(InternalPropnetMachineState state) throws MoveDefinitionException, StateMachineException
    {
        List<List<InternalPropnetMove>> legals = new ArrayList<List<InternalPropnetMove>>();
        for (InternalPropnetRole role : getInternalRoles()) {
            legals.add(getInternalLegalMoves(state, role));
        }

        List<List<InternalPropnetMove>> crossProduct = new ArrayList<List<InternalPropnetMove>>();
        crossProductInternalLegalMoves(legals, crossProduct, new LinkedList<InternalPropnetMove>());

        return crossProduct;
    }

    protected void crossProductInternalLegalMoves(List<List<InternalPropnetMove>> legals, List<List<InternalPropnetMove>> crossProduct, LinkedList<InternalPropnetMove> partial)
    {
        if (partial.size() == legals.size()) {
            crossProduct.add(new ArrayList<InternalPropnetMove>(partial));
        } else {
            for (InternalPropnetMove move : legals.get(partial.size())) {
                partial.addLast(move);
                crossProductInternalLegalMoves(legals, crossProduct, partial);
                partial.removeLast();
            }
        }
    }
}