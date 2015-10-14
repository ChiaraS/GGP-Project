package csironi.ggp.course.verifiers;

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

/**
 * This class has a method that verifies the consistency of the answers returned by a state machine
 * (the subject) wrt another given state machine that acts as a reference.
 * This is an extension of the StateMachineVerifier that considers a test passed for the subject
 * state machine only if it returns a consistent answer and doesn't throw any exception every time
 * the reference state machine returns an aswer.
 *
 * @author C.Sironi
 *
 */
public class ExtendedStateMachineVerifier {

	/**
	 * SOMETHING NOT TO DO! But this parameter is used to get the number of rounds played (at least started) by
	 * the last executed test for logging purposes. Absolutely not thread safe! Nor a god programming practice.
	 * Note that if exceptions are thrown, this value won't be realistic. Indeed, if an exception is thrown right
	 * after starting the iteration, the iteration will be skipped to start a new one but still will be counted as
	 * one iteration.
	 */
	public static int lastRounds;
	/**
	 * This parameter represents the number of rounds that have actually been completed till the end (included the
	 * check of the goals).
	 */
	public static int completedRounds;
	/**
	 * Same as before. This parameter tells if the test failed because of an exception of the tested state machine
	 * and which exception it was.
	 */
	public static String exception;
	/**
	 * This parameter tells how many times (if any) one of the reference state machine's methods failed
	 * throwing an exception.
	 */
	public static int otherExceptions;

	/**
	 * This method performs iterations from the initial state to a terminal state of the game, querying both
	 * state machines and checking if the answers returned by them are consistent with each other.
	 * If the subject state machine returns an inconsistent value, or throws one of its checked exceptions,
	 * the test is considered failed.
	 * If during execution any other exception is thrown (either a runtime exception or a checked exception
	 * of the reference state machine), the test skips the current check and continues from the next thing
	 * that's possible checking (e.g. skip current iteration and go to the next, skip checking legal moves
	 * for current role and go to the next, etc...).
	 *
	 * @param theReference the state machine against which we want to verify our state machine.
	 * @param theSubject the state machine for which we want to verify the consistency.
	 * @param timeToSpend the time (in millisecond) that should be spent to perform the test.
	 * @return true if the test succeeded (i.e. all the answers returned by the subject state machine are
	 * consistent with the ones returned by the reference), false otherwise.
	 */
    public static boolean checkMachineConsistency(StateMachine theReference, StateMachine theSubject, long timeToSpend) {

    	if(theReference == null || theSubject == null){
    		return false;
    	}

        long startTime = System.currentTimeMillis();

        GamerLogger.log("Verifier", "Performing automatic consistency testing on " + theSubject.getClass().getName() + " using " + theReference.getClass().getName() + " as a reference.");

        List<StateMachine> theMachines = new ArrayList<StateMachine>();
        theMachines.add(theReference);
        theMachines.add(theSubject);

        //System.out.print("Consistency checking: [");

        // Reset global variables
        lastRounds = 0;
        completedRounds = 0;
        exception = "-";
        otherExceptions = 0;
        while(true) {
            // If something goes wrong in this try block it means the test cannot continue on this iteration
        	// and the test will skip to the next iteration.
        	try {
        		lastRounds++;

        		//System.out.print(".");

            	MachineState[] theCurrentStates = new MachineState[theMachines.size()];
            	for(int i = 0; i < theMachines.size(); i++) {
            		theCurrentStates[i] = theMachines.get(i).getInitialState();
            	}

				while(!theMachines.get(0).isTerminal(theCurrentStates[0])) {

				    if(System.currentTimeMillis() > startTime + timeToSpend)
				        break;

				    // Do per-state consistency checks
				    for(int i = 1; i < theMachines.size(); i++) {
				        // If check fails for current subject (NOT BECAUSE THE SUBJECT STATE MACHINE FAILS!),
				    	// go check the next.
				    	try{
					    	for(Role theRole : theMachines.get(0).getRoles()) {
					            // If check fails for current role (NOT BECAUSE THE SUBJECT STATE MACHINE FAILS!) go
					    		// check the next.
					        	try{

					            	List<Move> referenceMoves = theMachines.get(0).getLegalMoves(theCurrentStates[0], theRole);
					            	List<Move> subjectMoves = null;

					            	try{
					            		// We can assume that if this instruction throws an exception is because of the
					            		// getLegalMoves method, and thus the subject state machine failed to compute
					            		// legal moves and this test does not succeed.
					            		// Since we checked at the start of this method, we can be sure that theMachines.get(i)
					            		// is not a null pointer exception and since this test fails when the computation of the
					            		// next state fails, we can be sure that theCurrentStates[i] will exist.
					            		subjectMoves = theMachines.get(i).getLegalMoves(theCurrentStates[i], theRole);
					            	}catch(StateMachineException sme){
					            		GamerLogger.log("Verifier", "Machine #" + i + " failed computation of legal moves for state " + theCurrentStates[i] + " and role " + theRole + ".");
					            		GamerLogger.logStackTrace("Verifier", sme);
					            		exception = "STATE MACHINE";
					            		return false;
					            	}catch(MoveDefinitionException mde){
					            		GamerLogger.log("Verifier", "Machine #" + i + " failed computation of legal moves for state " + theCurrentStates[i] + " and role " + theRole + ".");
					            		GamerLogger.logStackTrace("Verifier", mde);
					            		exception = "MOVE DEFINITION";
					            		return false;
					            	}catch(Exception ex){
					            		GamerLogger.log("Verifier", "Machine #" + i + " failed computation of legal moves for state " + theCurrentStates[i] + " and role " + theRole + ".");
					            		GamerLogger.logStackTrace("Verifier", ex);
					            		exception = "EXCEPTION";
					            		return false;
					            	}catch(Error er){
					            		GamerLogger.log("Verifier", "Machine #" + i + " failed computation of legal moves for state " + theCurrentStates[i] + " and role " + theRole + ".");
					            		GamerLogger.logStackTrace("Verifier", er);
					            		exception = "ERROR";
					            		return false;
					            	}

					                if(!(subjectMoves.size() == referenceMoves.size())) {
					                    GamerLogger.log("Verifier", "Inconsistency between machine #" + i + " and ProverStateMachine over state " + theCurrentStates[0] + " vs " + theCurrentStates[i].getContents());
					                    GamerLogger.log("Verifier", "Machine #" + 0 + " has move count = " + referenceMoves.size() + " for role " + theRole);
					                    GamerLogger.log("Verifier", "Machine #" + i + " has move count = " + subjectMoves.size() + " for role " + theRole);
					                    GamerLogger.log("Verifier", "Machine #" + 0 + " has legal moves = " + referenceMoves);
					                    GamerLogger.log("Verifier", "Machine #" + i + " has legal moves = " + subjectMoves);
					                    return false;
					                }
					            }catch(Exception e) {
					            	GamerLogger.log("Verifier", "Failed to check consistency of legal moves of role " + theRole + " in state " + theCurrentStates[0] + ". Skipping to next role (if any)!");
					                GamerLogger.logStackTrace("Verifier", e);
					                otherExceptions++;
					            }catch(Error e) {
					            	GamerLogger.log("Verifier", "Failed to check consistency of legal moves of role " + theRole + " in state " + theCurrentStates[0] + ". Skipping to next role (if any)!");
					                GamerLogger.logStackTrace("Verifier", e);
					                otherExceptions++;
					            }
					    	}
				    	}catch(Exception e){
					    	GamerLogger.log("Verifier", "Failed to check consistency of legal moves for all the roles in state " + theCurrentStates[0] + " for state machine #" + i + ". Skipping to next subject state machine (if any)!");
					        GamerLogger.logStackTrace("Verifier", e);
				            otherExceptions++;
				        }catch(Error e){
					    	GamerLogger.log("Verifier", "Failed to check consistency of legal moves for all the roles in state " + theCurrentStates[0] + " for state machine #" + i + ". Skipping to next subject state machine (if any)!");
					        GamerLogger.logStackTrace("Verifier", e);
				            otherExceptions++;
				        }
				    }


				    // Proceed on to the next state with the reference state machine.
				    // If these instructions throw an exception, proceed to check the next iteration.
				    List<Move> theJointMove = theMachines.get(0).getRandomJointMove(theCurrentStates[0]);
				    theCurrentStates[0] = theMachines.get(0).getNextState(theCurrentStates[0], theJointMove);

				    // Proceed on to the next state with all other subject state machines.
				    for(int i = 1; i < theMachines.size(); i++) {
			            try {
			                theCurrentStates[i] = theMachines.get(i).getNextState(theCurrentStates[i], theJointMove);
			            } catch(StateMachineException sme) {
			            	GamerLogger.log("Verifier", "Machine #" + i + " failed computation of next state for state " + theCurrentStates[i] + " and joint move " + theJointMove + ".");
			                GamerLogger.logStackTrace("Verifier", sme);
			                exception = "STATE MACHINE";
			                return false;
			            }catch(TransitionDefinitionException tse){
			            	GamerLogger.log("Verifier", "Machine #" + i + " failed computation of next state for state " + theCurrentStates[i] + " and joint move " + theJointMove + ".");
			                GamerLogger.logStackTrace("Verifier", tse);
			                exception = "TRANSITION DEFINITION";
			                return false;
			            }catch(Exception ex){
			            	GamerLogger.log("Verifier", "Machine #" + i + " failed computation of next state for state " + theCurrentStates[i] + " and joint move " + theJointMove + ".");
			                GamerLogger.logStackTrace("Verifier", ex);
			                exception = "EXCEPTION";
			                return false;
			            }catch(Error er){
			            	GamerLogger.log("Verifier", "Machine #" + i + " failed computation of next state for state " + theCurrentStates[i] + " and joint move " + theJointMove + ".");
			                GamerLogger.logStackTrace("Verifier", er);
			                exception = "ERROR";
			                return false;
			            }
				    }
				}

				if(System.currentTimeMillis() > startTime + timeToSpend)
					break;

				// Do final consistency checks
				for(int i = 1; i < theMachines.size(); i++){
					try{
						if(!theMachines.get(i).isTerminal(theCurrentStates[i])) {
							GamerLogger.log("Verifier", "Inconsistency between machine #" + i + " and ProverStateMachine over terminal-ness of state " + theCurrentStates[0] + " vs " + theCurrentStates[i]);
							return false;
						}
					}catch(StateMachineException sme){
						GamerLogger.log("Verifier", "Machine #" + i + " failed the check for terminality of state " + theCurrentStates[i]);
						GamerLogger.logStackTrace("Verifier", sme);
						exception = "STATE MACHINE";
						return false;
					}catch(Exception ex){
						GamerLogger.log("Verifier", "Machine #" + i + " failed the check for terminality of state " + theCurrentStates[i]);
						GamerLogger.logStackTrace("Verifier", ex);
						exception = "EXCEPTION";
						return false;
					}catch(Error er){
						GamerLogger.log("Verifier", "Machine #" + i + " failed the check for terminality of state " + theCurrentStates[i]);
						GamerLogger.logStackTrace("Verifier", er);
						exception = "ERROR";
						return false;
					}

	                for(Role theRole : theMachines.get(0).getRoles()){

	                	int referenceGoal = -1;
	                	int subjectGoal = -2;

	                	// If this throws an exception, we can skip to checking the next iteration.
	                    referenceGoal = theMachines.get(0).getGoal(theCurrentStates[0], theRole);

	                    try{
	                        subjectGoal = theMachines.get(i).getGoal(theCurrentStates[i], theRole);
	                    }catch(StateMachineException sme) {
	    					GamerLogger.log("Verifier", "Machine #" + i + " failed the computation of the goal for role " + theRole + " in state " + theCurrentStates[i]);
	    					GamerLogger.logStackTrace("Verifier", sme);
	    					exception = "STATE MACHINE";
	    					return false;
	                    }catch(GoalDefinitionException gde) {
	    					GamerLogger.log("Verifier", "Machine #" + i + " failed the computation of the goal for role " + theRole + " in state " + theCurrentStates[i]);
	    					GamerLogger.logStackTrace("Verifier", gde);
	    					exception = "GOAL DEFINITION";
	    					return false;
	                    }catch(Exception ex) {
	    					GamerLogger.log("Verifier", "Machine #" + i + " failed the computation of the goal for role " + theRole + " in state " + theCurrentStates[i]);
	    					GamerLogger.logStackTrace("Verifier", ex);
	    					exception = "EXCEPTION";
	    					return false;
	                    }catch(Error er) {
	    					GamerLogger.log("Verifier", "Machine #" + i + " failed the computation of the goal for role " + theRole + " in state " + theCurrentStates[i]);
	    					GamerLogger.logStackTrace("Verifier", er);
	    					exception = "ERROR";
	    					return false;
	                    }

	                    if(subjectGoal != referenceGoal) {
	                    	GamerLogger.log("Verifier", "Inconsistency between machine #" + i + " and ProverStateMachine over goal value for " + theRole + " of state " + theCurrentStates[0] + ": " + subjectGoal + " vs " + referenceGoal);
	                        return false;
	                    }

	                }
				}
			// This should catch all the exceptions in the current iteration that won't allow the check to continue
			// in this iteration anymore. Thus, the check will continue in another iteration.
            }catch(Exception e) {
            	GamerLogger.log("Verifier", "Failed to check consistency during current iteration. Skipping to the next!");
		        GamerLogger.logStackTrace("Verifier", e);
	            otherExceptions++;
			}catch(Error e) {
            	GamerLogger.log("Verifier", "Failed to check consistency during current iteration. Skipping to the next!");
		        GamerLogger.logStackTrace("Verifier", e);
	            otherExceptions++;
			}

        	completedRounds++;
        }
        //System.out.println("]");

        GamerLogger.log("Verifier", "Completed automatic consistency testing on " + theSubject.getClass().getName() + ", w/ " + lastRounds + " rounds: all tests pass!");
        return true;
    }
}