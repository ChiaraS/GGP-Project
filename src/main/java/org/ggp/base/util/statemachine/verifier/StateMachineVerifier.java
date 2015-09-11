package org.ggp.base.util.statemachine.verifier;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;


public class StateMachineVerifier {

	/**
	 * SOMETHING NOT TO DO! But this parameter is used to get the number of rounds played by the last executed test
	 * for logging purposes. Absolutely not therad safe! Nor a god programming practice.
	 */
	public static int lastRounds;
	/**
	 * Same as before, this parameter tells if the test failed because of an exception of the tested state machine.
	 */
	public static String exception;

    public static boolean checkMachineConsistency(StateMachine theReference, StateMachine theSubject, long timeToSpend) {
        long startTime = System.currentTimeMillis();

        GamerLogger.log("Verifier", "Performing automatic consistency testing on " + theSubject.getClass().getName() + " using " + theReference.getClass().getName() + " as a reference.");

        List<StateMachine> theMachines = new ArrayList<StateMachine>();
        theMachines.add(theReference);
        theMachines.add(theSubject);

        System.out.print("Consistency checking: [");
        lastRounds = 0;
        exception = "-";
        while(true) {
            lastRounds++;

            System.out.print(".");
            MachineState[] theCurrentStates = new MachineState[theMachines.size()];
            for(int i = 0; i < theMachines.size(); i++) {
                    theCurrentStates[i] = theMachines.get(i).getInitialState();
            }

            //AGGIUNTA
            /*for(int i=0; i < theCurrentStates.length; i++){
            	if(theCurrentStates[i] == null){
            		System.out.println("Ma perchééééée'?");
            	}else{
            		System.out.println("INIT S");
            		System.out.println(theCurrentStates[i]);
            	}
            }*/
            //FINE AGGIUNTA
            //AGGIUNTA
            //int step = 0;
            //FINE AGGIUNTA


            try {
				while(!theMachines.get(0).isTerminal(theCurrentStates[0])) {

					//AGGIUNTA
					//step++;
					//System.out.println("STEP " + step);
					//FINE AGGIUNTA


				    if(System.currentTimeMillis() > startTime + timeToSpend)
				        break;

				    // Do per-state consistency checks
				    for(int i = 1; i < theMachines.size(); i++) {
				        for(Role theRole : theMachines.get(0).getRoles()) {
				            try {
				            	//AGGIUNTA
				            	/*System.out.println(i);
				        		System.out.println(theRole);
				            	if(theCurrentStates[i] == null){
				            		System.out.println("Ma perchééééée'?");

				            	}else{
				            		System.out.println("INIT Sttt");
				            		System.out.println(theCurrentStates[i]);
				            	}*/
				            	//FINE AGGIUNTA

				            	List<Move> referenceMoves = theMachines.get(0).getLegalMoves(theCurrentStates[0], theRole);
				            	List<Move> subjectMoves = null;

				            	try{
				            		subjectMoves = theMachines.get(i).getLegalMoves(theCurrentStates[i], theRole);
				            	}catch(StateMachineException sme){
				            		GamerLogger.log("Verifier", "Machine #" + i + " failed computation of legal moves for state " + theCurrentStates[i] + " and role " + theRole + ".");
				            		GamerLogger.logStackTrace("Verifier", sme);
				            		exception = "STATE MACHINE";
				            		return false;
				            	}catch(MoveDefinitionException mde){
				            		GamerLogger.log("Verifier", "Machine #" + i + " failed computation legal moves for state " + theCurrentStates[i] + " and role " + theRole + ".");
				            		GamerLogger.logStackTrace("Verifier", mde);
				            		exception = "MOVE DEFINITION";
				            		return false;
				            	}

				                if(!(subjectMoves.size() == referenceMoves.size())) {
				                    GamerLogger.log("Verifier", "Inconsistency between machine #" + i + " and ProverStateMachine over state " + theCurrentStates[0] + " vs " + theCurrentStates[i].getContents());
				                    GamerLogger.log("Verifier", "Machine #" + 0 + " has move count = " + referenceMoves.size() + " for player " + theRole);
				                    GamerLogger.log("Verifier", "Machine #" + i + " has move count = " + subjectMoves.size() + " for player " + theRole);
				                    GamerLogger.log("Verifier", "Machine #" + 0 + " has legal moves = " + referenceMoves);
				                    GamerLogger.log("Verifier", "Machine #" + i + " has legal moves = " + subjectMoves);
				                    return false;
				                }
				            } catch(Exception e) {
				                GamerLogger.logStackTrace("Verifier", e);
				            }
				        }
				    }

				    try {
				        //Proceed on to the next state.
				        List<Move> theJointMove = theMachines.get(0).getRandomJointMove(theCurrentStates[0]);

				        for(int i = 0; i < theMachines.size(); i++) {
				            try {

				            	//AGGIUNTA
				            	//System.out.println("STATE BEFORE: " + theCurrentStates[i]);
				            	//FINE AGGIUNTA

				                theCurrentStates[i] = theMachines.get(i).getNextState(theCurrentStates[i], theJointMove);

				              //AGGIUNTA
				            	//System.out.println("STATE AFTER: " + theCurrentStates[i]);
				            	//FINE AGGIUNTA


				            } catch(StateMachineException sme) {
				            	//AGGIUNTA
				            	//System.out.println("ECCEZIONE " + e.getMessage());
				            	//FINE AGGIUNTA
				            	GamerLogger.log("Verifier", "Machine #" + i + " failed computation of next state for state " + theCurrentStates[i] + " and joint move " + theJointMove + ".");
				                GamerLogger.logStackTrace("Verifier", sme);
				                exception = "STATE MACHINE";
				                return false;
				            }catch(TransitionDefinitionException tse){
				            	GamerLogger.log("Verifier", "Machine #" + i + " failed computation of next state for state " + theCurrentStates[i] + " and joint move " + theJointMove + ".");
				                GamerLogger.logStackTrace("Verifier", tse);
				                exception = "TRANSITION DEFINITION";
				                return false;
				            }
				        }
				    } catch(Exception e) {
				        GamerLogger.logStackTrace("Verifier", e);
				    }
				}
			} catch (StateMachineException sme) {
				GamerLogger.logStackTrace("Verifier", sme);
			}

            if(System.currentTimeMillis() > startTime + timeToSpend)
                break;

            // Do final consistency checks
            for(int i = 1; i < theMachines.size(); i++) {
                try {
					if(!theMachines.get(i).isTerminal(theCurrentStates[i])) {
					    GamerLogger.log("Verifier", "Inconsistency between machine #" + i + " and ProverStateMachine over terminal-ness of state " + theCurrentStates[0] + " vs " + theCurrentStates[i]);
					    return false;
					}
				} catch (StateMachineException sme) {
					GamerLogger.log("Verifier", "Machine #" + i + " failed the check for terminality of state " + theCurrentStates[i]);
					GamerLogger.logStackTrace("Verifier", sme);
					exception = "STATE MACHINE";
					return false;
				}
                for(Role theRole : theMachines.get(0).getRoles()) {

                	int referenceGoal = -1;
                	int subjectGoal = -2;

                    try {
                        referenceGoal = theMachines.get(0).getGoal(theCurrentStates[0], theRole);
                    } catch(Exception e) {
                    	GamerLogger.log("Verifier", "Reference state machine threw exception when computing a goal.");
                    	GamerLogger.logStackTrace("Verifier", e);
                        continue;
                    }

                    try {
                        subjectGoal = theMachines.get(i).getGoal(theCurrentStates[i], theRole);
                    } catch(StateMachineException sme) {
    					GamerLogger.log("Verifier", "Machine #" + i + " failed the computation of the goal for role " + theRole + " in state " + theCurrentStates[i]);
    					GamerLogger.logStackTrace("Verifier", sme);
    					exception = "STATE MACHINE";
    					return false;
                    } catch(GoalDefinitionException gde) {
    					GamerLogger.log("Verifier", "Machine #" + i + " failed the computation of the goal for role " + theRole + " in state " + theCurrentStates[i]);
    					GamerLogger.logStackTrace("Verifier", gde);
    					exception = "GOAL DEFINITION";
    					return false;
                    }

                    if(subjectGoal != referenceGoal) {
                    	GamerLogger.log("Verifier", "Inconsistency between machine #" + i + " and ProverStateMachine over goal value for " + theRole + " of state " + theCurrentStates[0] + ": " + subjectGoal + " vs " + referenceGoal);
                        return false;
                    }

                }
            }
        }
        System.out.println("]");

        GamerLogger.log("Verifier", "Completed automatic consistency testing on " + theSubject.getClass().getName() + ", w/ " + lastRounds + " rounds: all tests pass!");
        return true;
    }
}