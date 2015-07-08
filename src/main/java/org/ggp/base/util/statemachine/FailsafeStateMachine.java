package org.ggp.base.util.statemachine;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;


/**
 * The FailsafeStateMachine is a wrapper around a particular state machine.
 * It will catch errors/exceptions being thrown from that state machine, and
 * fall back to a regular prover if the state machine fails. It's not totally
 * clear that this is helpful, but it's an additional layer of bullet-proofing
 * in case anything goes wrong.
 *
 * @author Sam Schreiber
 */
public class FailsafeStateMachine extends StateMachine
{

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{
		LOGGER = LogManager.getRootLogger();
	}

    private StateMachine theBackingMachine = null;
    private List<Gdl> gameDescription;

    public FailsafeStateMachine (StateMachine theInitialMachine) {
        theBackingMachine = theInitialMachine;
    }

    @Override
    public String getName() {
        if(theBackingMachine != null) {
            return "Failsafe(" + theBackingMachine.getName() + ")";
        }
        return "Failsafe(null)";
    }

    @Override
    public synchronized void initialize(List<Gdl> description) {
        this.gameDescription = description;

        if(attemptLoadingInitialMachine())
            return;

        LOGGER.warn(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Failed to load initial state machine. Falling back...", "StateMachine"));
        if(attemptLoadingProverMachine())
            return;

        LOGGER.error(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Catastrophic failure to load *any* state machine. Cannot recover.", "StateMachine"));
        LOGGER.error(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Cannot recover from current state. Shutting down.", "StateMachine"));
        theBackingMachine = null;
    }

    private void failGracefully(Exception e1, Error e2) {
        if(e1 != null) LOGGER.error(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Caught exception.", "StateMachine"), e1);
        if(e2 != null) LOGGER.error(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Caught error.", "StateMachine"), e2);
        LOGGER.info(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Graceful failure mode kicking in.", "StateMachine"));

        if(theBackingMachine.getClass() != ProverStateMachine.class) {
        	LOGGER.warn(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Online failure for " + theBackingMachine.getClass() + ". Attempting to restart with a standard prover.", "StateMachine"));
            if(attemptLoadingProverMachine())
                return;
        }

        theBackingMachine = null;
        LOGGER.error(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MACHINE] Online failure for regular prover. Cannot recover.", "StateMachine"));
    }

    private boolean attemptLoadingInitialMachine() {
        try {
            theBackingMachine.initialize(gameDescription);
            LOGGER.info(new StructuredDataMessage("FailsafeStateMachine","[FAILSAFE MACHINE] Successfully activated initial state machine for use!", "StateMahcine"));
            return true;
        } catch(Exception e1) {
        } catch(ThreadDeath d) {
            throw d;
        } catch(Error e2) {
        }
        return false;
    }

    private boolean attemptLoadingProverMachine() {
        try {
            StateMachine theStateMachine = new ProverStateMachine();
            theStateMachine.initialize(gameDescription);
            theBackingMachine = theStateMachine;
            LOGGER.info(new StructuredDataMessage("FailsafeStateMachine", "[FAILSAFE MAHCINE] Successfully loaded traditional prover.", "StateMachine"));
            return true;
        } catch(Exception e1) {
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e2) {
        }
        return false;
    }

    @Override
    public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
        if(theBackingMachine == null)
            return 0;

        try {
            return theBackingMachine.getGoal(state, role);
        } catch(GoalDefinitionException ge) {
            throw ge;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getGoal(state, role);
    }

    @Override
    public MachineState getInitialState() {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getInitialState();
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getInitialState();
    }

    @Override
    public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getLegalMoves(state, role);
        } catch(MoveDefinitionException me) {
            throw me;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(ThreadDeath d) {
            throw d;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getLegalMoves(state, role);
    }

    @Override
    public Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getRandomMove(state, role);
        } catch(MoveDefinitionException me) {
            throw me;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(ThreadDeath d) {
            throw d;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getRandomMove(state, role);
    }

    @Override
    public MachineState getMachineStateFromSentenceList(Set<GdlSentence> sentenceList) {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getMachineStateFromSentenceList(sentenceList);
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getMachineStateFromSentenceList(sentenceList);
    }

    @Override
    public Move getMoveFromTerm(GdlTerm term) {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getMoveFromTerm(term);
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getMoveFromTerm(term);
    }

    @Override
    public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getNextState(state, moves);
        } catch(TransitionDefinitionException te) {
            throw te;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getNextState(state, moves);
    }

    @Override
    public MachineState getNextStateDestructively(MachineState state, List<Move> moves) throws TransitionDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getNextStateDestructively(state, moves);
        } catch(TransitionDefinitionException te) {
            throw te;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getNextStateDestructively(state, moves);
    }

    @Override
    public Role getRoleFromConstant(GdlConstant constant) {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getRoleFromConstant(constant);
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getRoleFromConstant(constant);
    }

    @Override
    public List<Role> getRoles() {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getRoles();
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getRoles();
    }

    @Override
    public boolean isTerminal(MachineState state) {
        if(theBackingMachine == null)
            return false;

        try {
            return theBackingMachine.isTerminal(state);
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return isTerminal(state);
    }

    @Override
    public MachineState performDepthCharge(MachineState state, int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.performDepthCharge(state, theDepth);
        } catch (TransitionDefinitionException te) {
        	throw te;
        } catch (MoveDefinitionException me) {
        	throw me;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return performDepthCharge(state, theDepth);
    }

    @Override
    public void getAverageDiscountedScoresFromRepeatedDepthCharges(MachineState state, double[] avgScores, double[] avgDepth, double discountFactor, int repetitions) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        if(theBackingMachine == null)
            return;

        try {
            theBackingMachine.getAverageDiscountedScoresFromRepeatedDepthCharges(state, avgScores, avgDepth, discountFactor, repetitions);
            return;
        } catch (TransitionDefinitionException te) {
        	throw te;
        } catch (MoveDefinitionException me) {
        	throw me;
        } catch (GoalDefinitionException ge) {
        	throw ge;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        getAverageDiscountedScoresFromRepeatedDepthCharges(state, avgScores, avgDepth, discountFactor, repetitions);
    }

    @Override
    public void updateRoot(MachineState theState) {
        if(theBackingMachine == null)
            return;

        try {
            theBackingMachine.updateRoot(theState);
            return;
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        updateRoot(theState);
    }

    public StateMachine getBackingMachine() {
        return theBackingMachine;
    }
}