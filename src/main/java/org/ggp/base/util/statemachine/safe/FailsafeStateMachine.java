package org.ggp.base.util.statemachine.safe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


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
    private StateMachine theBackingMachine = null;
    private List<Gdl> gameDescription;

    public FailsafeStateMachine (Random random, StateMachine theInitialMachine) {

    	super(random);

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
    public synchronized void initialize(List<Gdl> description, long timeout) {

    	// NOTE: it has been chosen to try and initialize the initial state machine until timeout is reached,
    	// then try and initialize the other one if the initial one fails. However other techniques could be
    	// used (e.g. give half of the time to the first state machine and if it doesn't initialize in time
    	// try with the next).

        this.gameDescription = description;

        if(attemptLoadingInitialMachine(timeout))
            return;

        GamerLogger.logError("StateMachine", "Failsafe Machine: failed to load initial state machine. Falling back...");

        // Giving the timeout to the ProverStateMachine is irrelevant since it will ignore it,
        // but remember to change this if you change the backup machine.
        if(attemptLoadingProverMachine())
            return;

        GamerLogger.logError("StateMachine", "Failsafe Machine: catastrophic failure to load *any* state machine. Cannot recover.");
        GamerLogger.logError("StateMachine", "Failsafe Machine: cannot recover from current state. Shutting down.");
        theBackingMachine = null;
    }

    private void failGracefully(Exception e1, Error e2) {
        if(e1 != null) GamerLogger.logStackTrace("StateMachine", e1);
        if(e2 != null) GamerLogger.logStackTrace("StateMachine", e2);
        GamerLogger.logError("StateMachine", "Failsafe Machine: graceful failure mode kicking in.");

        if(theBackingMachine.getClass() != ProverStateMachine.class) {
            GamerLogger.logError("StateMachine", "Failsafe Machine: online failure for " + theBackingMachine.getClass() + ". Attempting to restart with a standard prover.");
            if(attemptLoadingProverMachine())
                return;
        }

        theBackingMachine = null;
        GamerLogger.logError("StateMachine", "Failsafe Machine: online failure for regular prover. Cannot recover.");
    }

    private boolean attemptLoadingInitialMachine(long timeout) {
        try {
            theBackingMachine.initialize(gameDescription, timeout);
            GamerLogger.log("StateMachine", "Failsafe Machine: successfully activated initial state machine for use!");
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
            StateMachine theStateMachine = new ProverStateMachine(this.random);
            theStateMachine.initialize(gameDescription, Long.MAX_VALUE);
            theBackingMachine = theStateMachine;
            GamerLogger.log("StateMachine", "Failsafe Machine: successfully loaded traditional prover.");
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
    public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) {
        if(theBackingMachine == null){
            List<Integer> goals = new ArrayList<Integer>();
            goals.add(new Integer(0));
        }

        try {
            return theBackingMachine.getAllGoalsForOneRole(state, role);
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getAllGoalsForOneRole(state, role);
    }

    @Override
    public ExplicitMachineState getExplicitInitialState() {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getExplicitInitialState();
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getExplicitInitialState();
    }

    @Override
    public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getExplicitLegalMoves(state, role);
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

        return getExplicitLegalMoves(state, role);
    }

    @Override
    public ExplicitMove getRandomMove(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException {
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
    public ExplicitMachineState getMachineStateFromSentenceList(Set<GdlSentence> sentenceList) {
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
    public ExplicitMove getMoveFromTerm(GdlTerm term) {
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
    public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getExplicitNextState(state, moves);
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

        return getExplicitNextState(state, moves);
    }

    @Override
    public ExplicitMachineState getNextStateDestructively(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException {
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
    public ExplicitRole getRoleFromConstant(GdlConstant constant) {
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
    public List<ExplicitRole> getExplicitRoles() {
        if(theBackingMachine == null)
            return null;

        try {
            return theBackingMachine.getExplicitRoles();
        } catch(Exception e) {
            failGracefully(e, null);
        } catch(ThreadDeath d) {
            throw d;
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(Error e) {
            failGracefully(null, e);
        }

        return getExplicitRoles();
    }

    @Override
    public boolean isTerminal(ExplicitMachineState state) {
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
    public ExplicitMachineState performDepthCharge(ExplicitMachineState state, int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
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
    public void getAverageDiscountedScoresFromRepeatedDepthCharges(ExplicitMachineState state, double[] avgScores, double[] avgDepth, double discountFactor, int repetitions) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
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
    public void updateRoot(ExplicitMachineState theState) {
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

	@Override
	public void shutdown() {
		if(this.theBackingMachine != null){
			this.theBackingMachine.shutdown();
		}

	}
}