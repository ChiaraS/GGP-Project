package org.ggp.base.util.statemachine.implementation.firstYapProlog;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableList;

public class FirstYapStateMachine extends StateMachine{

	private ExplicitMachineState initialState;
	private FirstYapEngine yapEngine;
	private ImmutableList<ExplicitRole> roles;

	/*
	 * The backing StateMachine to handle the InterProlog-Prolog crashes
	 */
	private final StateMachine backingStateMachine;

	/*
	 *  If 'TRUE', use threads to prevent InterProlog crashes
	 *  Else, use the dangerous method
	 */
	private static final boolean THREAD = true;
	/*
	 * If 'TRUE', use the YapEngine-Prolog methods
	 * Else, use the basic StateMachine methods
	 */
	//private static final boolean USEP = false;
	/*
	 * If 'TRUE', use the Internal Prolog Database
	 * Else, use the 'assert/retract' methods
	 */
	private static final boolean IDB = false;




	public FirstYapStateMachine(StateMachine backingStateMachine)
	{
		this.backingStateMachine = backingStateMachine;
	}

	public StateMachine getBackingStateMachine()
	{
		return backingStateMachine;
	}



	//////////
	/*
	 * Stop the YapEngine
	 * Used when the match ends normally
	 */
	public void stop()
	{
		yapEngine.stop();
	}
	/*
	 * Abort the YapEngine
	 * Used when the match ends abruptly
	 */
	public void abort()
	{
		yapEngine.stop();
	}
	//////////



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
	 */
	@Override
	public void initialize(List<Gdl> description, long timeout)
	{
		yapEngine = new FirstYapEngine(description, THREAD, IDB, backingStateMachine);
		initialState = computeInitialState();
		roles = computeRoles();
	}

	private ExplicitMachineState computeInitialState()
	{
		return yapEngine.computeInitialStateGdl();
	}
	private ImmutableList<ExplicitRole> computeRoles()
	{
		return ImmutableList.copyOf(yapEngine.computeRoles());
	}



	/**
	 * Returns the goal value for the given role in the given state. Goal values
	 * are always between 0 and 100.
	 *
	 * TODO: ATTENTION! This method only returns one goal value, if there are more
	 *       it's not detected, should be fixed if you want to properly use this
	 *       state machine
	 *
	 * @throws GoalDefinitionException if there is no goal value or more than one
	 * goal value for the given role in the given state. If this occurs when this
	 * is called on a terminal state, this indicates an error in either the game
	 * description or the StateMachine implementation.
	 * @throws StateMachineException
	 */
	@Override
	public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException{

		List<Integer> goal = new ArrayList<Integer>();
		goal.add(new Integer(yapEngine.getGoal(state, role)));
		return goal;
	}



	/**
	 * Returns true if and only if the given state is a terminal state (i.e. the
	 * game is over).
	 * @throws StateMachineException
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException
	{
		return yapEngine.isTerminal(state);
	}



	/**
	 * Returns a list of the roles in the game, in the same order as they
	 * were defined in the game description.
	 * <p>
	 * The result will be the same as calling {@link ExplicitRole#computeRoles(List)}
	 * on the game rules used to initialize this state machine.
	 */
	@Override
	public List<ExplicitRole> getExplicitRoles()
	{
		return this.roles;
	}



	/**
	 * Returns the initial state of the game.
	 */
	@Override
	public ExplicitMachineState getExplicitInitialState()
	{
		return this.initialState;
	}



	/**
	 * Returns a list containing every move that is legal for the given role in the
	 * given state.
	 *
	 * @throws MoveDefinitionException if the role has no legal moves. This indicates
	 * an error in either the game description or the StateMachine implementation.
	 * @throws StateMachineException
	 */
	// TODO: There are philosophical reasons for this to return Set<Move> rather than List<Move>.
	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException
	{
		return yapEngine.getLegalMoves(state, role);
	}



	/**
	 * Returns the next state of the game given the current state and a joint move
	 * list containing one move per role.
	 *
	 * @param moves A list containing one move per role. The moves should be
	 * listed in the same order as roles are listed by {@link #getExplicitRoles()}.
	 * @throws TransitionDefinitionException indicates an error in either the
	 * game description or the StateMachine implementation.
	 * @throws StateMachineException
	 */
	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException
	{
		return yapEngine.getNextState(state, moves);
	}





	/*
	 * Implementation of the non-abstract StateMachine methods
	 * 		By modifying them, I can improve the gamers I think
	 * 			e.g. the SampleMonteCarloYapGamer
	 */


	//private MachineState nextState;


	/*
	 * Used in SampleMonteCarloYapGamer like this :
	 * MachineState finalState = theMachine.performDepthCharge(theMachine.getRandomNextState(theState, getRole(), myMove), depth);
	 * 		where :
	 * 			theState := getCurrentState()
	 * 			myMove := moves.get(i)
	 * 			moves := theMachine.getLegalMoves(getCurrentState(), getRole());
	 */
	/**
     * Returns a terminal state derived from repeatedly making random joint moves
     * until reaching the end of the game.
     *
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made to reach a terminal state.
	 * @throws StateMachineException
     */
	/*
    @Override
	public MachineState performDepthCharge(MachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException {
    	if(USEP)
    	{
    		return yapEngine.performDepthCharge(state, theDepth);
    	}
    	else
    	{
    		int nDepth = 0;
            while(!isTerminal(state)) {
                nDepth++;
                state = getNextStateDestructively(state, getRandomJointMove(state));
            }
            if(theDepth != null)
                theDepth[0] = nDepth;
            return state;
    	}
    }
    */



    /** Override this to provide memory-saving destructive-next-state functionality.
     * <p>
     * CONTRACT: After calling this method, "state" should not be accessed.
     * @throws StateMachineException
     */
    /*
    @Override
	public MachineState getNextStateDestructively(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException {
    	nextState = getNextState(state, moves);
    	state = null;
    	moves = null;
    	return nextState;
    }*/



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
     * @throws StateMachineException
     */
    /*
    @Override
	public MachineState getRandomNextState(MachineState state, Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException
    {
    	if(USEP)
    	{
    		return yapEngine.getRandomNextState(state, role, move);
    	}
    	else
    	{
    		List<Move> random = getRandomJointMove(state, role, move);
            return getNextState(state, random);
    	}
    }
    */



    /**
     * Returns a random joint move from among all the possible joint moves in
     * the given state.
     * @throws StateMachineException
     */
    /*
    @Override
	public List<Move> getRandomJointMove(MachineState state) throws MoveDefinitionException, StateMachineException
    {
    	if(USEP)
    	{
    		return yapEngine.getRandomJointMove(state);
    	}
    	else
    	{
    		List<Move> random = new ArrayList<Move>();
            for (Role role : getRoles()) {
                random.add(getRandomMove(state, role));
            }
            return random;
    	}
    }*/



    /**
	 * Returns a random joint move from among all the possible joint moves in
	 * the given state in which the given role makes the given move.
     * @throws StateMachineException
	 */
    /*
	@Override
	public List<Move> getRandomJointMove(MachineState state, Role role, Move move) throws MoveDefinitionException, StateMachineException
	{
		if(USEP)
    	{
    		return yapEngine.getRandomJointMove(state, role, move);
    	}
    	else
    	{
    		List<Move> random = new ArrayList<Move>();
    		for (Role r : getRoles()) {
    			if (r.equals(role)) {
    				random.add(move);
    			} else {
    				random.add(getRandomMove(state, r));
    			}
    		}
    		return random;
    	}
	}
	*/



    /**
     * Returns a random move from among the possible legal moves for the
     * given role in the given state.
     * @throws StateMachineException
     */
    /*
    @Override
	public Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException, StateMachineException
    {
    	if(USEP)
    	{
    		return yapEngine.getRandomMove(state, role);
    	}
    	else
    	{
    		List<Move> legals = getLegalMoves(state, role);
    		return legals.get(new Random().nextInt(legals.size()));
    	}
    }
    */


	@Override
	public void shutdown() {
		yapEngine.stop();

	}


}

