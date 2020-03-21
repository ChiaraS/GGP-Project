package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;


/**
 * This class represents one episode (i.e. one simulation) using a list of all the joint moves
 * performed during the simulation and a list of the intermediate goals obtained by performing
 * such joint moves (note that if not necessary for the algorithm the intermediate goals are
 * not memorized but only final goals are memorized, and also all the joint moves if not necessary
 * are not memorized. HOWEVER, whenever intermediate goals and all the joint moves are memorized
 * then both lists have the same length, i.e. there is a 1-to-1 correspondence between joint move
 * and intermediate goals).
 *
 * NOTE that the order of the two list goes from last to first visited move/obtained goal in the
 * simulation.
 *
 * @author c.sironi
 *
 */
public class SimulationResult{

	/**
	 * The length of the playout.
	 *
	 * NOTE that this doesn't necessarily always correspond to the length of the list of allJointMoves or the
	 * list of intermediateGoals because such lists might also contain goals and moves obtained/performed during
	 * the selection phase.
	 *
	 * ALSO NOTE that this class doesn't in any way ensure that the playoutLength corresponds to the length of
	 * the list of allJointMoves or the list of intermediateGoals even when this class is initialized after the
	 * end of the playout.
	 */
	private double playoutLength;

	/**
	 *  The joint moves that form the path of this simulation.
	 *
	 * NOTE that since some implementations of MCTS do not need to remember all the taken actions,
	 * this list might be empty.
	 *
	 * NOTE that when both the joint moves and the intermediate goals are memorized for each simulation then
	 * the length of the list of goals will be longer than the list of all joint moves by one element (i.e. we
	 * save the goals of each state in the simulation, from root to last state, but in the last state we have
	 * no joint move to memorize).
	 */
	private List<List<Move>> allJointMoves;

	/**
	 * The siblings of each joint move in the path of this simulation.
	 *
	 * NOTE that since some implementations of MCTS do not need to remember all the taken actions,
	 * this list might be empty.
	 *
	 * NOTE that when both the siblings of joint actions and the intermediate goals are memorized for each simulation then
	 * the length of the list of goals will be longer than the list of all sbling moves by one element (i.e. we
	 * save the goals of each state in the simulation, from root to last state, but in the last state we have
	 * no sibling moves to memorize).
	 */
	private List<List<List<Move>>> allLegalMovesOfAllRoles;

	/**
	 * The goals of each role in each state reached by performing the corresponding joint moves
	 * (i.e. list of intermediate rewards for each role).
	 *
	 * NOTE that since some implementations of MCTS do not need intermediate goals, this list might
	 * only have a single element, that is the goals in the last state of the simulation.
	 *
	 * ALSO NOTE if this list contains more than one element then the goals in the last state of the
	 * simulation will be memorized as the first element of this list.
	 *
	 *
	 */
	private List<double[]> intermediateGoals;

	/**
	 * Constructor that initializes the playout length to 0, the two lists as empty lists and the terminal goals as null.
	 */
	public SimulationResult() {

		this(0, new ArrayList<List<Move>>(), new ArrayList<double[]>(), new ArrayList<List<List<Move>>>());

	}

	/**
	 * Constructor that initializes the playout length to 0, the two lists as empty lists and the terminal goals to the given values.
	 *
	 * @param terminalGoals
	 */
	public SimulationResult(double[] terminalGoals) {

		this(0, terminalGoals);

	}

	/**
	 * Constructor that initializes the two lists as empty lists and the playout length and the terminal goals to the given values.
	 *
	 * @param terminalGoals
	 */
	public SimulationResult(double playoutLength, double[] terminalGoals) {

		this(playoutLength, new ArrayList<List<Move>>(), new ArrayList<double[]>(), new ArrayList<List<List<Move>>>());

		this.intermediateGoals.add(terminalGoals);

	}

	public SimulationResult(double playoutLength, List<double[]> intermediateGoals) {

		this(playoutLength, new ArrayList<List<Move>>(), intermediateGoals, new ArrayList<List<List<Move>>>());

	}

	public SimulationResult(double playoutLength, double[] terminalGoals, List<List<Move>> allJointMoves) {

		this(playoutLength, allJointMoves, new ArrayList<double[]>(), new ArrayList<List<List<Move>>>());

		this.intermediateGoals.add(terminalGoals);

	}

	public SimulationResult(double playoutLength, double[] terminalGoals, List<List<Move>> allJointMoves, List<List<List<Move>>> allMovesInAllStates) {

		this(playoutLength, allJointMoves, new ArrayList<double[]>(), allMovesInAllStates);

		this.intermediateGoals.add(terminalGoals);

	}

	public SimulationResult(double playoutLength, List<List<Move>> allJointMoves, List<double[]> intermediateGoals) {

		this(playoutLength, allJointMoves, intermediateGoals, new ArrayList<List<List<Move>>>());

	}


	public SimulationResult(double playoutLength, List<List<Move>> allJointMoves, List<double[]> intermediateGoals, List<List<List<Move>>> allLegalMovesOfAllRoles) {

		this.playoutLength = playoutLength;

		if(allJointMoves == null){
			allJointMoves = new ArrayList<List<Move>>();
		}

		if(intermediateGoals == null){
			intermediateGoals = new ArrayList<double[]>();
		}

		this.allJointMoves = allJointMoves;

		this.intermediateGoals = intermediateGoals;

		this.allLegalMovesOfAllRoles = allLegalMovesOfAllRoles;

	}

	public double getPlayoutLength(){
		return this.playoutLength;
	}

	public List<List<List<Move>>> getAllLegalMovesOfAllRoles(){

		return this.allLegalMovesOfAllRoles;

	}

	public List<double[]> getIntermediateGoals(){

		return this.intermediateGoals;

	}

	public List<List<Move>> getAllJointMoves(){

		return this.allJointMoves;

	}

	/**
	 *
	 * @return only the terminal goals of the whole simulation.
	 */
	public double[] getTerminalGoals(){

		return this.intermediateGoals.get(0);
	}

	public void addJointMove(List<Move> jointMove){

		//if(this.allJointMoves == null){
		//	GamerLogger.logError("MctsManager", "Simulation result not initialized to memorize all the joint moves. Probably a wrong combination of strategies has been set!");
		//	throw new RuntimeException("Simulation result not initialized to memorize all the joint moves.");
		//}

		this.allJointMoves.add(jointMove);

	}

	public void addLegalMovesOfAllRoles(List<List<Move>> legalMovesOfAllRoles){

		//if(this.allJointMoves == null){
		//	GamerLogger.logError("MctsManager", "Simulation result not initialized to memorize all the joint moves. Probably a wrong combination of strategies has been set!");
		//	throw new RuntimeException("Simulation result not initialized to memorize all the joint moves.");
		//}

		this.allLegalMovesOfAllRoles.add(legalMovesOfAllRoles);

	}

	public void addGoals(double[] goals){

		//if(this.intermediateGoals == null){
		//	GamerLogger.logError("MctsManager", "Simulation result not initialized to memorize all the intermediate goals. Probably a wrong combination of strategies has been set!");
		//	throw new RuntimeException("Simulation result not initialized to memorize all the intermediate goals.");
		//}

		this.intermediateGoals.add(goals);

	}

	/**
	 * For each role r, flips the terminal score s_{r} to 100-s_{r}.
	 * Note that this method flips only the terminal scores, even if we are keeping intermediate scores.
	 * If using intermediate scores, attention must be paid to the effect that a probability of flipping
	 * scores greater than 0 might have on the rest of the algorithm.
	 */
	public void flipTerminalScores() {
		if(this.intermediateGoals.size() > 0) {
			for(int roleIndex = 0; roleIndex < this.intermediateGoals.get(0).length; roleIndex++) {
				this.intermediateGoals.get(0)[roleIndex] = 100 - this.intermediateGoals.get(0)[roleIndex];
			}
		//}else if (this.intermediateGoals.size() > 1){
		//	GamerLogger.logError("MctsManager", "Trying to flip scores for a SimulationResult that has intermediate scores that will stay unflipped.");
		//	throw new RuntimeException("MctsManager - Trying to flip scores for a SimulationResult that has intermediate scores that will stay unflipped.");
		}else {
			GamerLogger.logError("MctsManager", "Trying to flip scores for a SimulationResult that has no goals.");
			throw new RuntimeException("MctsManager - Trying to flip scores for a SimulationResult that has no goals.");
		}
	}

	/**
	 * This method looks at the terminal scores and returns the corresponding wins. To compute the wins
	 * 1 point is split equally among all the agents that have the highest score. If it's a single player
	 * game the only role gets the fraction of 1 point proportional to its score: (score/100)*1
	 * Examples:
	 * Scores		Wins
	 * [100]		[1]
	 * [80]			[0.8]
	 * [50]			[0.5]
	 * [100 0]		[1 0]
	 * [30 70]		[0 1]
	 * [30 30 30]	[0.33 0.33 0.33]
	 * [70 70 50]	[0.5 0.5 0]
	 *
	 * @return
	 */
	public double[] getTerminalWins() {

		if(this.intermediateGoals.size() > 0) {

			double[] wins = new double[this.intermediateGoals.get(0).length];

			if(this.intermediateGoals.get(0).length == 1) {
				wins[0] = this.intermediateGoals.get(0)[0]/100.0;
			}else {
				List<Integer> bestIndices = new ArrayList<Integer>();
				double max = -1;
				for(int roleIndex = 0; roleIndex < this.intermediateGoals.get(0).length; roleIndex++) {
					if(this.intermediateGoals.get(0)[roleIndex] > max) {
						max = this.intermediateGoals.get(0)[roleIndex];
						bestIndices.clear();
						bestIndices.add(roleIndex);
					}else if(this.intermediateGoals.get(0)[roleIndex] == max){
						bestIndices.add(roleIndex);
					}
				}
				if(bestIndices.size() == 0) {
					GamerLogger.logError("MctsManager", "Found no best score when computing wins for a SimulationResult.");
					throw new RuntimeException("MctsManager - Found no best score when computing wins for a SimulationResult.");
				}
				// Wins is already initialized to all 0s, so we just change the wins for the bestIndices
				double splitPoint = 1.0/((double)bestIndices.size());
				for(Integer roleIndex : bestIndices) {
					wins[roleIndex] = splitPoint;
				}
			}
			return wins;
		}else {
			GamerLogger.logError("MctsManager", "Trying to compute wins for a SimulationResult that has no goals.");
			throw new RuntimeException("MctsManager - Trying to compute wins for a SimulationResult that has no goals.");
		}
	}

	/**
	 * This method is similar to getTerminalWins() because it converts goals to wins. However, instead of returning
	 * the wins it checks if there is only one winner. If so, it returns the index of the winner, otherwise it returns -1.
	 * @return
	 */
	public int getSingleWinner(){

		if(this.intermediateGoals.size() > 0) {

			double[] wins = new double[this.intermediateGoals.get(0).length];

			if(this.intermediateGoals.get(0).length == 1) {
				double win = this.intermediateGoals.get(0)[0]/100.0;
				if(win == 1){
					return 0;
				}else{
					return -1;
				}
			}else {

				List<Integer> bestIndices = new ArrayList<Integer>();
				double max = -1;
				for(int roleIndex = 0; roleIndex < this.intermediateGoals.get(0).length; roleIndex++) {
					if(this.intermediateGoals.get(0)[roleIndex] > max) {
						max = this.intermediateGoals.get(0)[roleIndex];
						bestIndices.clear();
						bestIndices.add(roleIndex);
					}else if(this.intermediateGoals.get(0)[roleIndex] == max){
						bestIndices.add(roleIndex);
					}
				}
				if(bestIndices.size() == 0) {
					GamerLogger.logError("MctsManager", "Found no best score when computing winning role for a SimulationResult.");
					throw new RuntimeException("MctsManager - Found no best score when computing winning role for a SimulationResult.");
				}
				if(bestIndices.size() == 1){
					return bestIndices.get(0);
				}else{
					return -1;
				}
			}
		}else{
			GamerLogger.logError("MctsManager", "Trying to compute wins for a SimulationResult that has no goals.");
			throw new RuntimeException("MctsManager - Trying to compute wins for a SimulationResult that has no goals.");
		}

	}

}
