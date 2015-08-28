/**
 *
 */
package org.ggp.base.util.statemachine.implementation.yapProlog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import com.google.common.collect.ImmutableList;

/**
 * @author Dubs
 *
 */
public class YapStateMachine extends StateMachine {

	private MachineState initialState;
	private YapEngine yapEngine;
	private ImmutableList<Role> roles;

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
	private static final boolean USEP = true;
	/*
	 * If 'TRUE', use the Internal Prolog Database
	 * Else, use the 'assert/retract' methods
	 */
	private static final boolean IDB = false;




	public YapStateMachine(StateMachine backingStateMachine)
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



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 */
	@Override
	public void initialize(List<Gdl> description)
	{
		yapEngine = new YapEngine(description, THREAD, IDB, backingStateMachine);
		initialState = computeInitialState();
		roles = computeRoles();
	}


	private MachineState computeInitialState()
	{
		return yapEngine.computeInitialStateGdl();
	}


	private ImmutableList<Role> computeRoles()
	{
		return ImmutableList.copyOf(yapEngine.computeRoles());
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException
	{
		return yapEngine.getGoal(state, role);
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(MachineState state)
	{
		return yapEngine.isTerminal(state);
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<Role> getRoles()
	{
		return this.roles;
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public MachineState getInitialState()
	{
		return this.initialState;
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	// TODO: There are philosophical reasons for this to return Set<Move> rather than List<Move>. ???
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException
	{
		return yapEngine.getLegalMoves(state, role);
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException
	{
		return yapEngine.getNextState(state, moves);
	}





	/*
	 * Implementation of the non-abstract StateMachine methods
	 * 		By modifying them, I can improve the gamers I think
	 * 			e.g. the SampleMonteCarloYapGamer
	 */


	private MachineState nextState;


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
     */
    @Override
	public MachineState performDepthCharge(MachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
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



    /** Override this to provide memory-saving destructive-next-state functionality.
     * <p>
     * CONTRACT: After calling this method, "state" should not be accessed.
     */
    @Override
	public MachineState getNextStateDestructively(MachineState state, List<Move> moves) throws TransitionDefinitionException {
    	nextState = getNextState(state, moves);
    	state = null;
    	moves = null;
    	return nextState;
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
     */
    @Override
	public MachineState getRandomNextState(MachineState state, Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException
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



    /**
     * Returns a random joint move from among all the possible joint moves in
     * the given state.
     */
    @Override
	public List<Move> getRandomJointMove(MachineState state) throws MoveDefinitionException
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
    }



    /**
	 * Returns a random joint move from among all the possible joint moves in
	 * the given state in which the given role makes the given move.
	 */
	@Override
	public List<Move> getRandomJointMove(MachineState state, Role role, Move move) throws MoveDefinitionException
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



    /**
     * Returns a random move from among the possible legal moves for the
     * given role in the given state.
     */
    @Override
	public Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException
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

}
