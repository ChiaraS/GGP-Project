package org.ggp.base.player.gamer.statemachine;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.exception.MetaGamingException;
import org.ggp.base.player.gamer.exception.MoveSelectionException;
import org.ggp.base.player.gamer.exception.StoppingException;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


/**
 * The base class for Gamers that rely on representing games as state machines.
 * Almost every player should subclass this class, since it provides the common
 * methods for interpreting the match history as transitions in a state machine,
 * and for keeping an up-to-date view of the current state of the game.
 *
 * See @SimpleSearchLightGamer, @HumanGamer, and @RandomGamer for examples.
 *
 * @author evancox
 * @author Sam
 */
public abstract class StateMachineGamer extends Gamer
{
    // =====================================================================
    // First, the abstract methods which need to be overriden by subclasses.
    // These determine what state machine is used, what the gamer does during
    // metagaming, and how the gamer selects moves.

    /**
     * Defines which state machine this gamer will use.
     * @return
     */
    public abstract AbstractStateMachine getInitialStateMachine();

    /**
     * Defines the metagaming action taken by a player during the START_CLOCK
     * @param timeout time in milliseconds since the era when this function must return
     * @throws TransitionDefinitionException
     * @throws MoveDefinitionException
     * @throws GoalDefinitionException
     * @throws StateMachineException
     */
    public abstract void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException;

    /**
     * Defines the algorithm that the player uses to select their move.
     * @param timeout time in milliseconds since the era when this function must return
     * @return Move - the move selected by the player
     * @throws TransitionDefinitionException
     * @throws MoveDefinitionException
     * @throws GoalDefinitionException
     * @throws StateMachineException
     */
    public abstract ExplicitMove stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException;

    /**
     * Defines any actions that the player takes upon the game cleanly ending.
     */
    public abstract void stateMachineStop();

    /**
     * Defines any actions that the player takes upon the game abruptly ending.
     */
    public abstract void stateMachineAbort();

    // =====================================================================
    // Next, methods which can be used by subclasses to get information about
    // the current state of the game, and tweak the state machine on the fly.

	/**
	 * Returns the current state of the game.
	 */
    /*
	public final ExplicitMachineState getCurrentState()
	{
		return currentState;
	}*/

	/**
	 * Returns the current state of the game in its internal format.
	 */
	public final MachineState getCurrentState()
	{
		return this.internalCurrentState;
	}

	/**
	 * Returns the role that this gamer is playing as in the game.
	 */
	public final Role getRole()
	{
		return this.role;
	}

	/**
	 * Returns the state machine.  This is used for calculating the next state and other operations, such as computing
	 * the legal moves for all players, whether states are terminal, and the goal values of terminal states.
	 */
	public final AbstractStateMachine getStateMachine()
	{
		return this.stateMachine;
	}

    /**
     * Cleans up the role, currentState and stateMachine. This should only be
     * used when a match is over, and even then only when you really need to
     * free up resources that the state machine has tied up. Currently, it is
     * only used in the Proxy, for players designed to run 24/7.
     */
    protected final void cleanupAfterMatch() {
    	this.role = null;
        //currentState = null;
        this.internalCurrentState = null;
        this.stateMachine = null;
        setMatch(null);
        setRoleName(null);
    }

    /**
     * Switches stateMachine to newStateMachine, playing through the match
     * history to the current state so that currentState is expressed using
     * a MachineState generated by the new state machine.
     *
     * This is not done in a thread-safe fashion with respect to the rest of
     * the gamer, so be careful when using this method.
     *
     * @param newStateMachine the new state machine
     */
    protected final void switchStateMachine(AbstractStateMachine newStateMachine) {
        try {
            //ExplicitMachineState newCurrentState = newStateMachine.convertToExplicitMachineState(newStateMachine.getInitialState());
            ExplicitRole newExplicitRole = newStateMachine.getRoleFromConstant(getRoleName());
            Role newRole = newStateMachine.convertToInternalRole(newExplicitRole);
            MachineState newInternalCurrentState = newStateMachine.getInitialState();

            // Attempt to run through the game history in the new machine
            /*List<List<GdlTerm>> theMoveHistory = getMatch().getMoveHistory();
            for(List<GdlTerm> nextMove : theMoveHistory) {
                List<ExplicitMove> theJointMove = new ArrayList<ExplicitMove>();
                for(GdlTerm theSentence : nextMove)
                    theJointMove.add(newStateMachine.getMoveFromTerm(theSentence));
                newCurrentState = newStateMachine.getNextStateDestructively(newCurrentState, theJointMove);
                newInternalCurrentState = newStateMachine.getNextState(newInternalCurrentState, newStateMachine.convertToInternalMoves(moves));
            }*/

            List<List<GdlTerm>> theMoveHistory = getMatch().getMoveHistory();
            for(List<GdlTerm> nextMove : theMoveHistory) {
                List<Move> theJointMove = new ArrayList<Move>();
                for(int roleIndex = 0; roleIndex < nextMove.size(); roleIndex++) {
                    theJointMove.add(newStateMachine.convertToInternalMove(newStateMachine.getMoveFromTerm(nextMove.get(roleIndex)), newStateMachine.getExplicitRoles().get(roleIndex)));
                }
                //newCurrentState = newStateMachine.getNextStateDestructively(newCurrentState, theJointMove);
                newInternalCurrentState = newStateMachine.getNextState(newInternalCurrentState, theJointMove);
            }

            // Finally, switch over if everything went well.
            this.role = newRole;
            this.internalCurrentState = newInternalCurrentState;
            this.stateMachine = newStateMachine;
        } catch (Exception e) {
            GamerLogger.log("GamePlayer", "Caught an exception while switching state machine!");
            GamerLogger.logStackTrace("GamePlayer", e);
        }
    }

    /**
     * A function that can be used when deserializing gamers, to bring a
     * state machine gamer back to the internal state that it has when it
     * arrives at a particular game state.
     * @throws StateMachineException
     */
	public final void resetStateFromMatch() throws StateMachineInitializationException {
        // The use of the tmp variable is needed to avoid substituting the current state machine
		// with a new one before being certain that the new one will manage to initialize.
		// If the new state machine fails initialization, the old state machine will still be available.
		AbstractStateMachine tmp = getInitialStateMachine();
		// We don't have a timeout for this operation so we give to the state machine the maximum amount of time for initialization
        tmp.initialize(getMatch().getGame().getRules(), Long.MAX_VALUE);
        this.stateMachine = tmp;
        ExplicitMachineState currentState = this.stateMachine.getMachineStateFromSentenceList(getMatch().getMostRecentState());
        // NOTE: this method can be used only if the state machine correctly implements the translation of
        // ExplicitMachineState to the internal format. Here we cannot use the most recent state in the
        // internal format because two machines that use the same internal format might still represent the
        // same estate in a different way. (e.g. the propnet represents moves as indices in the list of input
        // proposition, but the order of the moves in this list is different for every new propnet creation)
        this.internalCurrentState = this.stateMachine.convertToInternalMachineState(currentState);
        this.role = this.stateMachine.convertToInternalRole(this.stateMachine.getRoleFromConstant(getRoleName()));
	}

    // =====================================================================
    // Finally, methods which are overridden with proper state-machine-based
	// semantics. These basically wrap a state-machine-based view of the world
	// around the ordinary metaGame() and selectMove() functions, calling the
	// new stateMachineMetaGame() and stateMachineSelectMove() functions after
	// doing the state-machine-related book-keeping.

	/**
	 * A wrapper function for stateMachineMetaGame. When the match begins, this
	 * initializes the state machine and role using the match description, and
	 * then calls stateMachineMetaGame.
	 */
	@Override
	public final void metaGame(long timeout) throws MetaGamingException
	{
		this.metagamingTimeout = timeout;

		try
		{
			this.stateMachine = getInitialStateMachine();
			this.stateMachine.initialize(getMatch().getGame().getRules(), timeout);
			//currentState = stateMachine.getExplicitInitialState();
			this.internalCurrentState = this.stateMachine.getInitialState();
			role =  this.stateMachine.convertToInternalRole(this.stateMachine.getRoleFromConstant(getRoleName()));
			getMatch().appendState(this.internalCurrentState);
			getMatch().appendState(this.stateMachine.convertToExplicitMachineState(this.internalCurrentState).getContents());

			stateMachineMetaGame(timeout);
		}
		catch (Exception e)
		{
			GamerLogger.logStackTrace("GamePlayer", e);
			throw new MetaGamingException(e);
		}
	}

	/**
	 * A wrapper function for stateMachineSelectMove. When we are asked to
	 * select a move, this advances the state machine up to the current state
	 * and then calls stateMachineSelectMove to select a move based on that
	 * current state.
	 */
	@Override
	public final GdlTerm selectMove(long timeout) throws MoveSelectionException
	{
		try
		{
			this.stateMachine.doPerMoveWork();

			List<GdlTerm> lastMoves = getMatch().getMostRecentMoves();
			if (lastMoves != null)
			{
				List<Move> moves = new ArrayList<Move>();
				for (int roleIndex = 0; roleIndex < lastMoves.size(); roleIndex++)
				{
					moves.add(this.stateMachine.convertToInternalMove(this.stateMachine.getMoveFromTerm(lastMoves.get(roleIndex)), this.stateMachine.getExplicitRoles().get(roleIndex)));
				}

				this.internalCurrentState = this.stateMachine.getNextState(this.internalCurrentState, moves);
				getMatch().appendState(this.stateMachine.convertToExplicitMachineState(this.internalCurrentState).getContents());
				getMatch().appendState(this.internalCurrentState);
			}

			return stateMachineSelectMove(timeout).getContents();
		}
		catch (Exception e)
		{
		    GamerLogger.logStackTrace("GamePlayer", e);
			throw new MoveSelectionException(e);
		}
	}

	@Override
	public void stop() throws StoppingException {
		try {
			this.stateMachine.doPerMoveWork();

			List<GdlTerm> lastMoves = getMatch().getMostRecentMoves();
			if (lastMoves != null)
			{
				List<Move> moves = new ArrayList<Move>();
				for (int roleIndex = 0; roleIndex < lastMoves.size(); roleIndex++)
				{
					moves.add(this.stateMachine.convertToInternalMove(this.stateMachine.getMoveFromTerm(lastMoves.get(roleIndex)), this.stateMachine.getExplicitRoles().get(roleIndex)));
				}

				this.internalCurrentState = this.stateMachine.getNextState(this.internalCurrentState, moves);
				getMatch().appendState(this.stateMachine.convertToExplicitMachineState(this.internalCurrentState).getContents());
				getMatch().appendState(this.internalCurrentState);

				List<Double> allGoals = new ArrayList<Double>();

				double[] goals = this.stateMachine.getSafeGoalsAvgForAllRoles(this.internalCurrentState);

				for(int i = 0; i < goals.length; i++){
					allGoals.add(goals[i]);
				}

				//getMatch().markCompleted(stateMachine.getGoals(currentState));
				getMatch().markCompleted(allGoals);

			}
		}catch (Exception e){
			GamerLogger.logStackTrace("GamePlayer", e);
			throw new StoppingException(e);
		}finally{

			stateMachineStop();
			// Stop the state machine (if the state machine implementation needs
			// to be stopped).
			this.stateMachine.shutdown();
		}
	}

	@Override
	public void abort() /*throws AbortingException*/ {
		//try {
			stateMachineAbort();
		//}
		//catch (Exception e)
		//{
		//	GamerLogger.logStackTrace("GamePlayer", e);
		//	throw new AbortingException(e);
		//}finally{
			// Stop the state machine (if the state machine implementation needs
			// to be stopped).
			stateMachine.shutdown();
		//}
	}

    // Internal state about the current state of the state machine.
    private Role role;
    //private ExplicitMachineState currentState;
    private MachineState internalCurrentState;
    private AbstractStateMachine stateMachine;

    /**
	 * C.Sironi: added parameter to memorize the meta-gaming timeout so that it can be used by the
	 * method that sets the state machine (getInitialStateMachine()) before actually calling the
	 * stateMahcineMetaGame() method. In this way the getInitialStateMachine() method can see if it
	 * can build the propnet in time to return the PropNetStateMachine or if it must return the prover.
	 */
	private long metagamingTimeout;

	public long getMetagamingTimeout(){
		return this.metagamingTimeout;
	}

}