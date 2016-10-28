package org.ggp.base.util.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableMap;


/**
 * Provides the base class for all state machine implementations.
 */
public abstract class StateMachine {
    // ============================================
    //          Stubs for implementations
    // ============================================
    //  The following methods are required for a valid
    // state machine implementation.
	/**
	 * Initializes the StateMachine to describe the given game rules.
	 * <p>
	 * This method should only be called once, and it should be called before any
	 * other methods on the StateMachine.
	 *
	 * @param descriptiom GDL description of the game that this state machine should reason on.
	 * @param timeout the time by when this state machine must finish initialization (feel free to
	 * ignore it in your state machine implementation of the initialize() method if your state
	 * machine doesn't have a timeout managing mechanism. E.g. the GGP Base prover doesn't have one,
	 * but other state machines need it to avoid getting blocked forever in the initialization phase,
	 * like the propnet state machine that might take forever to build the propnet).
	 * Moreover, if you don't care about how long the state machine will take to initialize (e.g. if
	 * you are running a test and not playing a real time game) you can just give as input for the
	 * timeout the maximum value (Long.MAX_VALUE). Note that in this case some state machines that
	 * risk getting stuck forever might take a very long time before stopping initialization (i.e.
	 * until Long.MAX_VALUE time is reached).
	 *
	 * @throws StateMachineInitializationException when the initialization of the state machine fails,
	 * so that whoever is using the state machine can take corrective actions, e.g. switch
	 * to the use of another state machine or inform the game manager that the player is
	 * not able to play anymore. If this method throws this exception the state machine should NOT be
	 * used! Moreover, before discarding the state machine, make sure to shut it down (i.e. call the
	 * shutdown() method on it).
	 */
    public abstract void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException;

    public void initialize(List<Gdl> description) throws StateMachineInitializationException{
    	this.initialize(description, Long.MAX_VALUE);
    }

    /**
     * Returns the goal value for the given role in the given state. Goal values
     * are always between 0 and 100.
     *
     * @throws GoalDefinitionException if there is no goal value or more than one
     * goal value for the given role in the given state. If this occurs when this
     * is called on a terminal state, this indicates an error in either the game
     * description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the goal value
     * for the given role because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    public int getGoal(ExplicitMachineState state, ExplicitRole role) throws GoalDefinitionException, StateMachineException{

    	List<Integer> goals = this.getOneRoleGoals(state, role);

		if(goals.size() > 1){
			GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(state, role);
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(goals.isEmpty()){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(state, role);
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
    public abstract List<Integer> getOneRoleGoals(ExplicitMachineState state, ExplicitRole role) throws StateMachineException;


    /**
     * Returns true if and only if the given state is a terminal state (i.e. the
     * game is over).
     * @throws StateMachineException if it was not possible to determine if the current
     * state is terminal because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    public abstract boolean isTerminal(ExplicitMachineState state) throws StateMachineException;

    /**
     * Returns a list of the roles in the game, in the same order as they
     * were defined in the game description.
     * <p>
     * The result will be the same as calling {@link ExplicitRole#computeRoles(List)}
     * on the game rules used to initialize this state machine.
     */
    public abstract List<ExplicitRole> getRoles();
    /**
     * Returns the initial state of the game.
     */
    public abstract ExplicitMachineState getInitialState();

    /**
     * Returns a list containing every move that is legal for the given role in the
     * given state.
     *
     * @throws MoveDefinitionException if the role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the legal moves
     * for the given role because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    // TODO: There are philosophical reasons for this to return Set<Move> rather than List<Move>.
    public abstract List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException;

    /**
     * Returns the next state of the game given the current state and a joint move
     * list containing one move per role.
     *
     * @param moves A list containing one move per role. The moves should be
     * listed in the same order as roles are listed by {@link #getRoles()}.
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the next
     * state for the given state and the given joint moves because of an error
     * that occurred in the state machine and couldn't be handled.
     */
    public abstract ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException;

    /**
     * This method must allow to turn off the state machine (maybe temporarily), i.e.
     * perform all needed actions to leave the state machine in a consistent state but
     * not active anymore and not performing any operation (some sort of standby).
     * Most of the state machines don't need to do anything as they can be considered always
     * in standby, waiting for queries to be asked. However there might be a state machine that
     * performs some internal tasks or runs internal commands (e.g. the YapStateMachine is
     * always running an instance of the external Yap Prolog program).
     * This method should be called, for example, at the end of a match (stopped or aborted),
     * so that the state machine can take care of everything (if anything) that needs to be
     * done before the state machine won't be used anymore (e.g. the YapStateMahcine needs to
     * shutdown YAP prolog). Moreover this method should be called every time we want the state
     * machine to stop running (for example if the thread using the state machine quits/is
     * interrupted, it must put the state machine in standby or it might never really interrupt
     * because the state machine will still be running tasks/external commands)
     *
     * Note that this method must NOT leave the state machine in an inconsistent state. If we want
     * to use the state machine again we must be able to do so by just querying it.
     *
     * For now this method makes sense only for the YapProverStateMachine, that internally executes
     * an external instance of the Yap prolog program.
     */
    public abstract void shutdown();

    // The following methods are included in the abstract StateMachine base so
    // implementations which use alternative Role/Move/State representations
    // can look up/compute what some Gdl corresponds to in their representation.
    // They are implemented for convenience, using the default ways of generating
    // these objects, but they can be overridden to support machine-specific objects.
    public ExplicitMachineState getMachineStateFromSentenceList(Set<GdlSentence> sentenceList) {
        return new ExplicitMachineState(sentenceList);
    }
    public ExplicitRole getRoleFromConstant(GdlConstant constant) {
        return new ExplicitRole(constant);
    }
    public ExplicitMove getMoveFromTerm(GdlTerm term) {
        return new ExplicitMove(term);
    }

    // ============================================
    //          Stubs for advanced methods
    // ============================================
    //
    //   The following methods have functioning stubs,
    // which can be overridden with full-fledged versions
    // as needed by state machines. Clients should assume
    // the contracts for these methods hold, regardless
    // of the state machine implementation they pick.

    /** Override this to perform some extra work (like trimming a cache) once per move.
     * <p>
     * CONTRACT: Should be called once per move.
     */
    public void doPerMoveWork() {}

    /** Override this to provide memory-saving destructive-next-state functionality.
     * <p>
     * CONTRACT: After calling this method, "state" should not be accessed.
     *
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the next
     * state destructively for the given state and the given joint moves because
     * of an error that occurred in the state machine and couldn't be handled.
     */
    public ExplicitMachineState getNextStateDestructively(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException {
        return getNextState(state, moves);
    }

    /** Override this to allow the state machine to be conditioned on a particular current state.
     * This means that the state machine will only handle portions of the game tree at and below
     * the given state; it no longer needs to properly handle earlier portions of the game tree.
     * This constraint can be used to optimize certain state machine implementations.
     * <p>
     * CONTRACT: After calling this method, the state machine never deals with a state that
     *           is not "theState" or one of its descendants in the game tree.
     */
    public void updateRoot(ExplicitMachineState theState) {
        ;
    }

    // ============================================
    //   Implementations of convenience methods
    // ============================================

    public String getName() {
        return this.getClass().getSimpleName();
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
    public List<List<ExplicitMove>> getLegalJointMoves(ExplicitMachineState state) throws MoveDefinitionException, StateMachineException
    {
        List<List<ExplicitMove>> legals = new ArrayList<List<ExplicitMove>>();
        for (ExplicitRole role : getRoles()) {
            legals.add(getLegalMoves(state, role));
        }

        List<List<ExplicitMove>> crossProduct = new ArrayList<List<ExplicitMove>>();
        crossProductLegalMoves(legals, crossProduct, new LinkedList<ExplicitMove>());

        return crossProduct;
    }

    /**
     * Returns a list of every joint move possible in the given state in which
     * the given role makes the given move. This will be a subset of the list
     * of joint moves given by {@link #getLegalJointMoves(ExplicitMachineState)}.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the legal
     * joint moves for the given state where the given role performs the given
     * move because of an error that occurred in the state machine and couldn't
     * be handled.
     */
    public List<List<ExplicitMove>> getLegalJointMoves(ExplicitMachineState state, ExplicitRole role, ExplicitMove move) throws MoveDefinitionException, StateMachineException
    {
        List<List<ExplicitMove>> legals = new ArrayList<List<ExplicitMove>>();
        for (ExplicitRole r : getRoles()) {
            if (r.equals(role)) {
                List<ExplicitMove> m = new ArrayList<ExplicitMove>();
                m.add(move);
                legals.add(m);
            } else {
                legals.add(getLegalMoves(state, r));
            }
        }

        List<List<ExplicitMove>> crossProduct = new ArrayList<List<ExplicitMove>>();
        crossProductLegalMoves(legals, crossProduct, new LinkedList<ExplicitMove>());

        return crossProduct;
    }

    /**
     * Returns a list containing every possible next state of the game after
     * the given state. The list will contain one entry for every possible
     * joint move that could be played; as such, a single machine state could
     * be included multiple times.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the list
     * of all the possible next states that can be reached from the given state
     * because of an error that occurred in the state machine and couldn't be
     * handled.
     */
    public List<ExplicitMachineState> getNextStates(ExplicitMachineState state) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException
    {
        List<ExplicitMachineState> nextStates = new ArrayList<ExplicitMachineState>();
        for (List<ExplicitMove> move : getLegalJointMoves(state)) {
            nextStates.add(getNextState(state, move));
        }

        return nextStates;
    }

    /**
     * Returns a map from each move that is legal for the given role in
     * the given state to the list of possible resulting states if that
     * move is chosen.
     * <p>
     * If the given role is the only role with more than one legal move,
     * then each list of states in the map will only contain one state.
     *
     * @throws MoveDefinitionException if the role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the
     * map with all the possible next states per role move because of an
     * error that occurred in the state machine and couldn't be handled.
     */
    public Map<ExplicitMove, List<ExplicitMachineState>> getNextStates(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException
    {
        Map<ExplicitMove, List<ExplicitMachineState>> nextStates = new HashMap<ExplicitMove, List<ExplicitMachineState>>();
        Map<ExplicitRole, Integer> roleIndices = getRoleIndices();
        for (List<ExplicitMove> moves : getLegalJointMoves(state)) {
            ExplicitMove move = moves.get(roleIndices.get(role));
            if (!nextStates.containsKey(move)) {
                nextStates.put(move, new ArrayList<ExplicitMachineState>());
            }
            nextStates.get(move).add(getNextState(state, moves));
        }

        return nextStates;
    }

    protected void crossProductLegalMoves(List<List<ExplicitMove>> legals, List<List<ExplicitMove>> crossProduct, LinkedList<ExplicitMove> partial)
    {
        if (partial.size() == legals.size()) {
            crossProduct.add(new ArrayList<ExplicitMove>(partial));
        } else {
            for (ExplicitMove move : legals.get(partial.size())) {
                partial.addLast(move);
                crossProductLegalMoves(legals, crossProduct, partial);
                partial.removeLast();
            }
        }
    }

    private Map<ExplicitRole,Integer> roleIndices = null;
    /**
     * Returns a mapping from a role to the index of that role, as in
     * the list returned by {@link #getRoles()}. This may be a faster
     * way to check the index of a role than calling {@link List#indexOf(Object)}
     * on that list.
     */
    public Map<ExplicitRole, Integer> getRoleIndices()
    {
        if (roleIndices == null) {
        	ImmutableMap.Builder<ExplicitRole, Integer> roleIndicesBuilder = ImmutableMap.builder();
            List<ExplicitRole> roles = getRoles();
            for (int i = 0; i < roles.size(); i++) {
                roleIndicesBuilder.put(roles.get(i), i);
            }
            roleIndices = roleIndicesBuilder.build();
        }

        return roleIndices;
    }

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
    public List<Integer> getGoals(ExplicitMachineState state) throws GoalDefinitionException, StateMachineException {
        List<Integer> theGoals = new ArrayList<Integer>(getRoles().size());
        for(ExplicitRole r : getRoles()) {
            theGoals.add(getGoal(state, r));
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
    public List<List<Integer>> getAllRolesGoals(ExplicitMachineState state) throws GoalDefinitionException, StateMachineException {
        List<List<Integer>> theGoals = new ArrayList<List<Integer>>();
        for (ExplicitRole r : getRoles()) {
        	theGoals.add(getOneRoleGoals(state, r));
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
    public int[] getSafeGoals(ExplicitMachineState state){
    	List<ExplicitRole> theRoles = this.getRoles();
    	int[] theGoals = new int[theRoles.size()];
        for (int i = 0; i < theRoles.size(); i++) {
            try {
				theGoals[i] = getGoal(state, theRoles.get(i));
			} catch (GoalDefinitionException | StateMachineException e){
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
     * The goal values are listed in the same order the roles are listed in the game
     * rules, which is the same order in which they're returned by {@link #getRoles()}.
     *
     * This method is safe, meaning that it won't throw any GoalDefinitionException,
     * but it will set a zero value for the goals when they cannot be computed or an
     * average value when there is more than one goal per role.
     *
     * Note: method meant to be used for terminal states, where an error computing a
     * goal must be penalized (i.e. we don't want to end the game in a terminal state
     * we don't know anything about for our player).
     *
     * @param state the state for which to compute the goals.
	 * @throws StateMachineException
     */
    public int[] getSafeGoalsAvg(ExplicitMachineState state){
    	List<ExplicitRole> theRoles = this.getRoles();
    	int[] theGoals = new int[theRoles.size()];
    	int avg;
    	List<Integer> roleGoals = null;

    	for(int i = 0; i < theRoles.size(); i++) {

        	try{
        		roleGoals = this.getOneRoleGoals(state, theRoles.get(i));

        		if(roleGoals != null && !roleGoals.isEmpty()){

        			avg = 0;

        			for(Integer goal : roleGoals){
        				avg += goal;
        			}

        			theGoals[i] = (int) Math.round(((double)avg)/((double)roleGoals.size()));
        		}
        	}catch(StateMachineException e){
        		GamerLogger.logError("StateMachine", "Failed to compute a goal value when computing safe goals.");
				GamerLogger.logStackTrace("StateMachine", e);
				theGoals[i] = 0;
        	}
        }
        return theGoals;
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
    public List<ExplicitMove> getRandomJointMove(ExplicitMachineState state) throws MoveDefinitionException, StateMachineException
    {
        List<ExplicitMove> random = new ArrayList<ExplicitMove>();
        for (ExplicitRole role : getRoles()) {
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
    public List<ExplicitMove> getRandomJointMove(ExplicitMachineState state, ExplicitRole role, ExplicitMove move) throws MoveDefinitionException, StateMachineException
    {
        List<ExplicitMove> random = new ArrayList<ExplicitMove>();
        for (ExplicitRole r : getRoles()) {
            if (r.equals(role)) {
                random.add(move);
            } else {
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
    public ExplicitMove getRandomMove(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException
    {
        List<ExplicitMove> legals = getLegalMoves(state, role);
        return legals.get(new Random().nextInt(legals.size()));
    }

    /**
     * Returns a state chosen at random from the possible next states of the
     * game.
     * <p>
     * The distribution among states is based on the possible joint moves.
     * This is not necessarily uniform among the possible states themselves,
     * as multiple joint moves may result in the same state.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute a random next
     * state from the given state because of an error that occurred in the state
     * machine and couldn't be handled.
     */
    public ExplicitMachineState getRandomNextState(ExplicitMachineState state) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException
    {
        List<ExplicitMove> random = getRandomJointMove(state);
        return getNextState(state, random);
    }

    /**
     * Returns a random next state of the game from the possible next states
     * resulting from the given role playing the given move.
     * <p>
     * The distribution among states is based on the possible joint moves.
     * This is not necessarily uniform among the possible states themselves,
     * as multiple joint moves may result in the same state.
     * <p>
     * If the given role is the only role with more than one legal move, then
     * there is only one possible next state for this method to return.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the random
     * next state because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    public ExplicitMachineState getRandomNextState(ExplicitMachineState state, ExplicitRole role, ExplicitMove move) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException
    {
        List<ExplicitMove> random = getRandomJointMove(state, role, move);
        return getNextState(state, random);
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
    public ExplicitMachineState performDepthCharge(ExplicitMachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException {
        int nDepth = 0;
        while(!isTerminal(state)) {
            nDepth++;

            //System.out.println("!");
            //System.out.println(getRandomJointMove(state));
            //System.out.println("!");

            state = getNextStateDestructively(state, getRandomJointMove(state));
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
    public ExplicitMachineState interruptiblePerformDepthCharge(ExplicitMachineState state, final int[] theDepth) /*throws TransitionDefinitionException, MoveDefinitionException, StateMachineException*/ {
        int nDepth = 0;
        try {
	        while(!isTerminal(state)) {

	            state = getNextStateDestructively(state, getRandomJointMove(state));

	            nDepth++;

				ConcurrencyUtils.checkForInterruption();
			}
        } catch (InterruptedException | StateMachineException | TransitionDefinitionException | MoveDefinitionException e) {
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

    public void getAverageDiscountedScoresFromRepeatedDepthCharges(final ExplicitMachineState state, final double[] avgScores, final double[] avgDepth, final double discountFactor, final int repetitions) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException {
    	avgDepth[0] = 0;
    	for (int j = 0; j < avgScores.length; j++) {
    		avgScores[j] = 0;
    	}
    	final int[] depth = new int[1];
    	for (int i = 0; i < repetitions; i++) {
    		ExplicitMachineState stateForCharge = state.clone();
    		stateForCharge = performDepthCharge(stateForCharge, depth);
    		avgDepth[0] += depth[0];
    		final double accumulatedDiscountFactor = Math.pow(discountFactor, depth[0]);
    		for (int j = 0; j < avgScores.length; j++) {
    			avgScores[j] += getGoal(stateForCharge, getRoles().get(j)) * accumulatedDiscountFactor;
    		}
    	}
    	avgDepth[0] /= repetitions;
    	for (int j = 0; j < avgScores.length; j++) {
    		avgScores[j] /= repetitions;
    	}
    }
}