package org.ggp.base.util.statemachine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachineInterface;
import org.ggp.base.util.statemachine.abstractsm.ExplicitAndCompactStateMachineInterface;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.utils.MyPair;

/**
 * Provides the base class for all state machine implementations that are based on the version
 * of the propnet that provides an alternative internal representation of the machine states,
 * the moves and the roles.
 */
public abstract class InternalPropnetStateMachine extends StateMachine implements CompactStateMachineInterface, ExplicitAndCompactStateMachineInterface{

	public InternalPropnetStateMachine(Random random) {
		super(random);
	}

	/** Standard state machine methods rewritten using internal representation for states, moves and roles. **/

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	@Override
	public abstract boolean isTerminal(CompactMachineState state);

    /**
     * Returns the goal value(s) for the given role in the given state. Goal values
     * are always between 0 and 100.
     *
     * @throws StateMachineException if it was not possible to compute the goal value
     * for the given role because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    @Override
	public abstract List<Double> getAllGoalsForOneRole(CompactMachineState state, CompactRole role);


	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	@Override
	public abstract CompactMachineState getCompactInitialState();

	/**
	 * Returns the internal representation of roles.
	 *
	 * @return the internal representation of roles.
	 */
	@Override
	public abstract List<CompactRole> getCompactRoles();

	/**
	 * Computes the legal moves for role in state.
	 */
	@Override
	public abstract List<CompactMove> getCompactLegalMoves(CompactMachineState state, CompactRole role)throws MoveDefinitionException;

	/**
	 * Computes the next state given a state and the list of moves.
	 */
	@Override
	public abstract CompactMachineState getCompactNextState(CompactMachineState state, List<CompactMove> moves);


	/************************************** Translation methods **************************************/

	@Override
	public abstract ExplicitMachineState convertToExplicitMachineState(CompactMachineState state);

	@Override
	public abstract ExplicitMove convertToExplicitMove(CompactMove move);

	@Override
	public abstract ExplicitRole convertToExplicitRole(CompactRole role);


	@Override
	public abstract CompactMachineState convertToCompactMachineState(ExplicitMachineState state);

	@Override
	public abstract CompactMove convertToCompactMove(ExplicitMove move);

	@Override
	public abstract CompactRole convertToCompactRole(ExplicitRole role);

	/**
	 * Useful when we need to translate a joint move. Faster than translating the moves one by one.
	 *
	 * @param move
	 * @param roleIndex
	 * @return
	 */
	public abstract List<CompactMove> movesToInternalMoves(List<ExplicitMove> moves);


	/***************** Extra methods to replace the ones offered by the StateMachine *****************/

    /**
     * Returns the goal values for each role in the given state. The goal values
     * are listed in the same order the roles are listed in the game rules, which
     * is the same order in which they're returned by {@link #getExplicitRoles()}.
     *
     * @throws GoalDefinitionException if there is no goal value or more than one
     * goal value for any one role in the given state. If this occurs when this
     * is called on a terminal state, this indicates an error in either the game
     * description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the list
     * with the goals for all the roles in the given state because of an error
     * that occurred in the state machine and couldn't be handled.
     */
    public List<Double> getGoals(CompactMachineState state) throws GoalDefinitionException{
    	List<CompactRole> theRoles = this.getCompactRoles();
    	List<Double> theGoals = new ArrayList<Double>(theRoles.size());
        for(int i = 0; i < theRoles.size(); i++) {
            theGoals.add(getGoal(state, theRoles.get(i)));
        }
        return theGoals;
    }

	/**
	 * Computes the goal for a role in the given state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	public double getGoal(CompactMachineState state, CompactRole role) throws GoalDefinitionException{
		List<Double> goals = this.getAllGoalsForOneRole(state, role);

		if(goals.size() > 1){
			GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(this.convertToExplicitMachineState(state), this.convertToExplicitRole(role));
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(goals.size() == 0){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(this.convertToExplicitMachineState(state), this.convertToExplicitRole(role));
		}

		// Return the single goal for the given role in the given state.
		return goals.get(0);
	}

    /**
     * Returns a list containing a list for each role with all the goal values for
     * that role in the given state. The lists of goal values are listed in the
     * same order the roles are listed in the game rules, which is the same order
     * in which they're returned by {@link #getExplicitRoles()}. If a list is empty it means
     * that the role has no goals in the given state.
     *
     * @throws StateMachineException if it was not possible to compute the list
     * with the goals for all the roles in the given state because of an error
     * that occurred in the state machine and couldn't be handled.
     */
    public List<List<Double>> getAllRolesGoals(CompactMachineState state) throws StateMachineException {
    	List<CompactRole> theRoles = this.getCompactRoles();
    	List<List<Double>> theGoals = new ArrayList<List<Double>>(theRoles.size());

    	for(CompactRole r : this.getCompactRoles()) {
            theGoals.add(this.getAllGoalsForOneRole(state, r));
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
	public CompactMachineState performDepthCharge(CompactMachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException {
        int nDepth = 0;
        while(!isTerminal(state)) {
            nDepth++;
            state = getCompactNextState(state, getRandomJointMove(state));
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
	public CompactMachineState performSafeLimitedDepthCharge(CompactMachineState state, final int[] theDepth, int maxDepth){
        int nDepth = 0;

        while(nDepth < maxDepth && !isTerminal(state)) {

        	List<CompactMove> jointMove = null;
			try {
				jointMove = getRandomJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("StateMachine", "Exception getting a joint move while performing safe limited depth charges.");
				GamerLogger.logStackTrace("StateMachine", e);
				break;
			}
			state = getCompactNextState(state, jointMove);
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
	public CompactMachineState interruptiblePerformDepthCharge(CompactMachineState state, final int[] theDepth) /*throws TransitionDefinitionException, MoveDefinitionException, StateMachineException*/ {
        int nDepth = 0;
        try {
	        while(!isTerminal(state)) {

	            state = getCompactNextState(state, getRandomJointMove(state));

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
	public List<CompactMove> getRandomJointMove(CompactMachineState state) throws MoveDefinitionException{
        List<CompactMove> random = new ArrayList<CompactMove>();
        for(CompactRole role : this.getCompactRoles()) {
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
    public List<CompactMove> getRandomJointMove(CompactMachineState state, CompactRole role, CompactMove move) throws MoveDefinitionException, StateMachineException
    {
        List<CompactMove> random = new ArrayList<CompactMove>();
        for (CompactRole r : getCompactRoles()) {
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
	public CompactMove getRandomMove(CompactMachineState state, CompactRole role) throws MoveDefinitionException{
        List<CompactMove> legals = getCompactLegalMoves(state, role);
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
     * rules, which is the same order in which they're returned by {@link #getExplicitRoles()}.
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
    public double[] getSafeGoalsAvg(CompactMachineState state){
    	List<CompactRole> theRoles = this.getCompactRoles();
    	double[] theGoals = new double[theRoles.size()];
    	int avg;
    	List<Double> roleGoals = null;

    	for (int i = 0; i < theRoles.size(); i++) {

        	roleGoals = this.getAllGoalsForOneRole(state, theRoles.get(i));

        	if(roleGoals != null && !roleGoals.isEmpty()){

        		avg = 0;

        		for(Double goal : roleGoals){
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
     * rules, which is the same order in which they're returned by {@link #getExplicitRoles()}.
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
    public double[] getSafeGoals(CompactMachineState state){
    	List<CompactRole> theRoles = this.getCompactRoles();
    	double[] theGoals = new double[theRoles.size()];
        for (int i = 0; i < theRoles.size(); i++) {
            try {
				theGoals[i] = getGoal(state, theRoles.get(i));
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
     * rules, which is the same order in which they're returned by {@link #getExplicitRoles()}.
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
    public double[] getSafeGoalsTie(CompactMachineState state){
    	List<CompactRole> theRoles = this.getCompactRoles();
    	int failures = 0;
    	double[] theGoals = new double[theRoles.size()];
        for (int i = 0; i < theRoles.size(); i++) {
            try {
				theGoals[i] = getGoal(state, theRoles.get(i));
			} catch (GoalDefinitionException e){
				GamerLogger.logError("StateMachine", "Failed to compute a goal value when computing safe goals with tie default.");
				GamerLogger.logStackTrace("StateMachine", e);
				theGoals[i] = 0; // TODO: should this be 50???
				failures++;
			}
        }

        // If computation of goal failed for all players...
        if(failures == theRoles.size()){
        	// Distinguish the single-player case from the multi-player case.
        	if(theRoles.size() == 1){
        		theGoals[0] = 50;
        	}else{
        		double defaultGoal = 100.0 / ((double)theRoles.size());
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
     * This method returns a list. Each element in the list is a list of legal moves
     * for a role in the state. These lists are ordered per role with the standard role
     * order (i.e. the same as the one of the list with roles).
     *
     * @param state
     * @return
     * @throws MoveDefinitionException
     */
    public List<List<CompactMove>> getAllLegalMoves(CompactMachineState state) throws MoveDefinitionException{
    	if(this.isTerminal(state)){
    		return null;
    	}else{
    		List<List<CompactMove>> legalMoves = new ArrayList<List<CompactMove>>();

    		// Get legal moves for all players.
    		for(int i = 0; i < this.getCompactRoles().size(); i++){
    			legalMoves.add(this.getCompactLegalMoves(state, this.getCompactRoles().get(i)));
    		}
    		return legalMoves;

    	}
    }

    /**
     * Returns a list containing every joint move possible in the given state.
     * A joint move consists of one move for each role, with the moves in the
     * same ordering that their roles have in {@link #getExplicitRoles()}.
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
    public List<List<CompactMove>> getLegalJointMoves(CompactMachineState state) throws MoveDefinitionException, StateMachineException
    {
        List<List<CompactMove>> legals = new ArrayList<List<CompactMove>>();
        for (CompactRole role : getCompactRoles()) {
            legals.add(getCompactLegalMoves(state, role));
        }

        List<List<CompactMove>> crossProduct = new ArrayList<List<CompactMove>>();
        crossProductInternalLegalMoves(legals, crossProduct, new LinkedList<CompactMove>());

        return crossProduct;
    }

    protected void crossProductInternalLegalMoves(List<List<CompactMove>> legals, List<List<CompactMove>> crossProduct, LinkedList<CompactMove> partial)
    {
        if (partial.size() == legals.size()) {
            crossProduct.add(new ArrayList<CompactMove>(partial));
        } else {
            for (CompactMove move : legals.get(partial.size())) {
                partial.addLast(move);
                crossProductInternalLegalMoves(legals, crossProduct, partial);
                partial.removeLast();
            }
        }
    }

    public void getAverageDiscountedScoresFromRepeatedDepthCharges(final CompactMachineState state, final double[] avgScores, final double[] avgDepth, final double discountFactor, final int repetitions) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException {
    	avgDepth[0] = 0;
    	for (int j = 0; j < avgScores.length; j++) {
    		avgScores[j] = 0;
    	}
    	final int[] depth = new int[1];
    	for (int i = 0; i < repetitions; i++) {
    		CompactMachineState stateForCharge = state.clone();
    		stateForCharge = performDepthCharge(stateForCharge, depth);
    		avgDepth[0] += depth[0];
    		final double accumulatedDiscountFactor = Math.pow(discountFactor, depth[0]);
    		for (int j = 0; j < avgScores.length; j++) {
    			avgScores[j] += getGoal(stateForCharge, this.getCompactRoles().get(j)) * accumulatedDiscountFactor;
    		}
    	}
    	avgDepth[0] /= repetitions;
    	for (int j = 0; j < avgScores.length; j++) {
    		avgScores[j] /= repetitions;
    	}
    }


	@Override
	public MyPair<double[], Double> fastPlayouts(CompactMachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException {
		double[] avgScores = new double[this.getCompactRoles().size()];
		double[] avgDepth = new double[1];

		this.getAverageDiscountedScoresFromRepeatedDepthCharges(state, avgScores, avgDepth, 1.0, numSimulationsPerPlayout);

		return new MyPair<double[], Double>(avgScores, new Double(avgDepth[0]));
	}

	/**
	 * For now all state machines implementing CompactStateMachineInterface will construct a joint move
     * by selecting a random move for each role.
	 * @throws MoveDefinitionException
	 */
	/*
	@Override
	public List<CompactMove> getJointMove(List<List<CompactMove>> legalMovesPerRole, CompactMachineState state) throws MoveDefinitionException {
		List<CompactMove> compactJointMove = new ArrayList<CompactMove>();

		List<CompactRole> roles = this.getCompactRoles();

		if(legalMovesPerRole == null || legalMovesPerRole.isEmpty()) {
			for(int i = 0; i < roles.size(); i++) {
				compactJointMove.add(this.getMoveForRole(null, state, roles.get(i)));
			}
		}else if(legalMovesPerRole.size() == roles.size()) {
			for(int i = 0; i < roles.size(); i++) {
				compactJointMove.add(this.getMoveForRole(legalMovesPerRole.get(i), state, roles.get(i)));
			}
		}else {
			GamerLogger.logError("StateMachine", "Requesting joint move in state " + state +
					" giving legal moves for " + legalMovesPerRole.size() + " roles when the game has " + roles.size() + " roles.");
			throw new RuntimeException("StateMachine - Requesting joint move in state " + state +
					" giving legal moves for " + legalMovesPerRole.size() + " roles when the game has " + roles.size() + " roles.");
		}

		return compactJointMove;
	}
	*/

	/**
	 * For now all state machines implementing CompactStateMachineInterface will select a random move for each role.
	 * @throws MoveDefinitionException
	 */
	@Override
	public CompactMove getMoveForRole(List<CompactMove> legalMoves, CompactMachineState state, CompactRole role) throws MoveDefinitionException {

		if(legalMoves == null) {
			legalMoves = this.getCompactLegalMoves(state, role);
		}else if(legalMoves.size() < 1) {
			GamerLogger.logError("StateMachine", "Requesting move for role " + this.convertToExplicitRole(role) +
					" in state " + this.convertToExplicitMachineState(state) + " giving an empty list of legal moves.");
			throw new RuntimeException("StateMachine - Requesting move for role " + this.convertToExplicitRole(role) +
					" in state " + this.convertToExplicitMachineState(state) + " giving an empty list of legal moves.");
		}

		return legalMoves.get(this.random.nextInt(legalMoves.size()));
	}
}