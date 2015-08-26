/**
 *
 */
package org.ggp.base.util.statemachine.implementation.hybrid;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.StateMachineInitializer;

/**
 * This class implements a state machine that tries to initialize a given state machine in a certain
 * amount of time and if it fails, falls back to another state machine (more specifically the prover state
 * machine, that seems to always initialize correctly and in time).
 *
 * Note that the specified initialization time for this class includes also the time needed to initialize
 * the prover state machine in case the initialization of the given state machine takes too long.
 * This means that in 'maxInitializationTime' milliseconds this state machine must try to initialize the
 * given state machine and, when needed also initialize the prover state machine.
 *
 * @author C.Sironi
 *
 */
public class InitializationSafeStateMachine extends StateMachine {

	/**
	 * The actual state machine that this state machine will use to reason on the game.
	 */
    private StateMachine theMainMachine = null;
    /**
     * Maximum time (in milliseconds) that this state machine has available to initialize the given state
     * machine and, when necessary, initialize the prover state machine.
     */
    private long maxInitializationTime;
    /**
     * The game description that has to be used for initialization.
     */
    private List<Gdl> gameDescription;

	/**
	 * Constructor that sets the main state machine used by this state machine with the given initial
	 * state machine and the maximum time that this state machine can use for initialization.
	 */
	public InitializationSafeStateMachine(StateMachine theInitialMachine, long maxInitializationTime) {
		 this.theMainMachine = theInitialMachine;
		 this.maxInitializationTime = maxInitializationTime;
	}


	@Override
    public String getName() {
        if(theMainMachine != null) {
            return "InitializationSafe(" + theMainMachine.getName() + ")";
        }
        return "InitializationSafe(null)";
    }

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 */
	@Override
	public void initialize(List<Gdl> description) throws StateMachineInitializationException{

        this.gameDescription = description;

        if(attemptInitializingInitialMachine())
            return;

        GamerLogger.logError("StateMachine", "[ConstructionSafe] Failed to initialize initial state machine. Falling back to prover.");

        if(attemptInitializingProverMachine())
            return;

        GamerLogger.logError("StateMachine", "[ConstructionSafe] catastrophic failure to initialize *any* state machine. Cannot recover.");
        GamerLogger.logError("StateMachine", "Failsafe Machine: cannot recover from current state. Shutting down.");
        theMainMachine = null;
        throw new StateMachineInitializationException();
	}

	private boolean attemptInitializingInitialMachine() {

		StateMachineInitializer initializer = new StateMachineInitializer(this.theMainMachine, this.gameDescription);

		initializer.start();






        try {
            theMainMachine.initialize(gameDescription);
            GamerLogger.log("StateMachine", "Failsafe Machine: successfully activated initial state machine for use!");
            return true;
        } catch(Exception e1) {
        } catch(ThreadDeath d) {
            throw d;
        } catch(Error e2) {
        }
        return false;
    }

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int getGoal(MachineState state, Role role)
			throws GoalDefinitionException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<Role> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public MachineState getInitialState() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
			throws MoveDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
			throws TransitionDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

}
