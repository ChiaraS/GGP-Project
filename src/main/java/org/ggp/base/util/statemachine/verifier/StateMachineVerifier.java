package org.ggp.base.util.statemachine.verifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;


public class StateMachineVerifier {

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");
		ThreadContext.put("GENERAL", System.currentTimeMillis() + "StateMachineVerifier");

		LOGGER = LogManager.getRootLogger();
	}

    public static boolean checkMachineConsistency(StateMachine theReference, StateMachine theSubject, long timeToSpend) {
        long startTime = System.currentTimeMillis();

        LOGGER.info("Performing automatic consistency testing on " + theSubject.getClass().getName() + " using " + theReference.getClass().getName() + " as a reference.");

        List<StateMachine> theMachines = new ArrayList<StateMachine>();
        theMachines.add(theReference);
        theMachines.add(theSubject);

        LOGGER.info("Consistency checking...");

        //GamerLogger.emitToConsole("Consistency checking: [");
        int nRound = 0;
        while(true) {
            nRound++;

            //GamerLogger.emitToConsole(".");
            MachineState[] theCurrentStates = new MachineState[theMachines.size()];
            for(int i = 0; i < theMachines.size(); i++) {
                try {
                    theCurrentStates[i] = theMachines.get(i).getInitialState();
                } catch(Exception e) {
                	LOGGER.error("Machine #" + i + " failed to generate an initial state!", e);
                    return false;
                }
            }

            while(!theMachines.get(0).isTerminal(theCurrentStates[0])) {
                if(System.currentTimeMillis() > startTime + timeToSpend)
                    break;

                // Do per-state consistency checks
                for(int i = 1; i < theMachines.size(); i++) {
                    for(Role theRole : theMachines.get(0).getRoles()) {
                        try {
                            if(!(theMachines.get(i).getLegalMoves(theCurrentStates[i], theRole).size() == theMachines.get(0).getLegalMoves(theCurrentStates[0], theRole).size())) {
                            	LOGGER.info("Inconsistency between machine #" + i + " and ProverStateMachine over state " + theCurrentStates[0] + " vs " + theCurrentStates[i].getContents());
                                LOGGER.info("Machine #" + 0 + " has move count = " + theMachines.get(0).getLegalMoves(theCurrentStates[0], theRole).size() + " for player " + theRole);
                                LOGGER.info("Machine #" + i + " has move count = " + theMachines.get(i).getLegalMoves(theCurrentStates[i], theRole).size() + " for player " + theRole);
                                return false;
                            }
                        } catch(Exception e) {
                            LOGGER.error("Caught exception while getting legal moves.", e);
                        }
                    }
                }

                try {
                    //Proceed on to the next state.
                    List<Move> theJointMove = theMachines.get(0).getRandomJointMove(theCurrentStates[0]);

                    for(int i = 0; i < theMachines.size(); i++) {
                        try {
                            theCurrentStates[i] = theMachines.get(i).getNextState(theCurrentStates[i], theJointMove);
                        } catch(Exception e) {
                        	LOGGER.error("Caught exception while getting next state.", e);
                        }
                    }
                } catch(Exception e) {
                	LOGGER.error("Caught exception while getting random joint move.", e);
                }
            }

            if(System.currentTimeMillis() > startTime + timeToSpend)
                break;

            // Do final consistency checks
            for(int i = 1; i < theMachines.size(); i++) {
                if(!theMachines.get(i).isTerminal(theCurrentStates[i])) {
                    LOGGER.info("Inconsistency between machine #" + i + " and ProverStateMachine over terminal-ness of state " + theCurrentStates[0] + " vs " + theCurrentStates[i]);
                    return false;
                }
                for(Role theRole : theMachines.get(0).getRoles()) {
                    try {
                        theMachines.get(0).getGoal(theCurrentStates[0], theRole);
                    } catch(Exception e) {
                        continue;
                    }

                    try {
                        if(theMachines.get(i).getGoal(theCurrentStates[i], theRole) != theMachines.get(0).getGoal(theCurrentStates[0], theRole)) {
                            LOGGER.info("Inconsistency between machine #" + i + " and ProverStateMachine over goal value for " + theRole + " of state " + theCurrentStates[0] + ": " + theMachines.get(i).getGoal(theCurrentStates[i], theRole) + " vs " + theMachines.get(0).getGoal(theCurrentStates[0], theRole));
                            return false;
                        }
                    } catch(Exception e) {
                        LOGGER.error("Inconsistency between machine #" + i + " and ProverStateMachine over goal-ness of state " + theCurrentStates[0] + " vs " + theCurrentStates[i]);
                        return false;
                    }
                }
            }
        }
        //GamerLogger.emitToConsole("]\n");

        LOGGER.info("Completed automatic consistency testing on " + theSubject.getClass().getName() + ", w/ " + nRound + " rounds: all tests pass!");
        return true;
    }
}