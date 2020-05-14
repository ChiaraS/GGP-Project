package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import java.util.ArrayList;
import java.util.Arrays;
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
	 * The joint moves that form the path of this simulation.
	 *
	 * ATTENTION!!! They are kept in reversed order, from last to first performed one.
	 * Whenever they are needed, retrieve them with the getAllJointMoves() method, that
	 * will create a copy of the list and return them in the correct order, from first
	 * to last.
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
	 * the length of the list of goals will be longer than the list of all sibling moves by one element (i.e. we
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

		List<List<List<Move>>> orderedAllLegalMovesOfAllRoles = new ArrayList<List<List<Move>>>(this.allLegalMovesOfAllRoles.size());

		for(int i = this.allLegalMovesOfAllRoles.size()-1; i >= 0; i--){
			orderedAllLegalMovesOfAllRoles.add(this.allLegalMovesOfAllRoles.get(i));
		}

		return orderedAllLegalMovesOfAllRoles;

	}

	public List<double[]> getIntermediateGoals(){

		return this.intermediateGoals;

	}

	/**
	 * NOTE: this class keeps the joint moves in reverse order, thus from last to first played move.
	 * Therefore, to return them in the correct order, it creates a copy of the current list, and
	 * reverses it before returning it.
	 *
	 * @return
	 */
	public List<List<Move>> getAllJointMoves(){

		List<List<Move>> orderedJointMoves = new ArrayList<List<Move>>(this.allJointMoves.size());

		for(int i = this.allJointMoves.size()-1; i >= 0; i--){
			orderedJointMoves.add(this.allJointMoves.get(i));
		}

		return orderedJointMoves;

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
	 *
	 * @return only the terminal goals of the whole simulation.
	 */
	public double[] getTerminalGoalsIn0_100(){

		return this.intermediateGoals.get(0);
	}

	/**
	 * Same as getTerminalWinsIn0_1(), but wins are rescaled in [0, 100] instead of [0, 1]
	 *
	 * @return
	 */
	public double[] getTerminalWinsIn0_100() {

		if(this.intermediateGoals.size() > 0) {

			double[] wins = new double[this.intermediateGoals.get(0).length];

			if(this.intermediateGoals.get(0).length == 1) {
				wins[0] = this.intermediateGoals.get(0)[0];
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
					GamerLogger.logError("MctsManager", "Found no best score when computing rescaled wins for a SimulationResult.");
					throw new RuntimeException("MctsManager - Found no best score when computing rescaled wins for a SimulationResult.");
				}
				// Wins is already initialized to all 0s, so we just change the wins for the bestIndices
				double split100Points = 100.0/((double)bestIndices.size());
				for(Integer roleIndex : bestIndices) {
					wins[roleIndex] = split100Points;
				}
			}
			return wins;
		}else {
			GamerLogger.logError("MctsManager", "Trying to compute rescaled wins for a SimulationResult that has no goals.");
			throw new RuntimeException("MctsManager - Trying to compute rescaled wins for a SimulationResult that has no goals.");
		}
	}

	/**
	 *
	 * @return only the terminal goals of the whole simulation, rescaled between 0 and 1.
	 */
	public double[] getTerminalGoalsIn0_1(){

		double[] g = this.intermediateGoals.get(0);
		for(int i = 0; i < g.length; i++){
			g[i] /= 100.0;
		}
		return g;
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
	public double[] getTerminalWinsIn0_1() {

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
	 *
	 * @return only the terminal goals of the whole simulation, rescaled between
	 * the given leftExtreme and rightExtreme.
	 */
	public double[] getRescaledTerminalGoals(double leftExtreme, double rightExtreme){

		double[] g = new double[this.intermediateGoals.get(0).length];
		for(int i = 0; i < g.length; i++){
			g[i] = this.rescaleValue(this.intermediateGoals.get(0)[i], 0.0, 100.0,
					leftExtreme, rightExtreme);
		}

		return g;
	}

	/**
	 * This method computes the wins looking at the terminal scores and
	 * returns them rescaled between the given leftExtreme and rightExtreme.
	 *
	 * @return
	 */
	public double[] getRescaledTerminalWins(double leftExtreme, double rightExtreme) {

		double[] w = this.getTerminalWinsIn0_100();
		for(int i = 0; i < w.length; i++){
			w[i] = this.rescaleValue(w[i], 0.0, 100.0,
					leftExtreme, rightExtreme);
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets a 1, while all other players get a 0. A player wins the simulation if it
	 * is the only one to get the highest score. For single player games, a player wins
	 * a simulation if it gets a score higher than 50.
	 */
	public double[] getSingleWin() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = 0;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = 1.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get a 1,
	 * while all other players get a 0. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5.
	 */
	public double[] getAllWins() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = 0;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = 1.0;
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets a 1, while all other players get a -1. A player wins the simulation if it
	 * is the only one to get the highest score. For single player games, a player wins
	 * a simulation if it gets a score higher than 50.
	 */
	public double[] getSingleWinAndLosses() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = -1;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = 1.0;
				}else{
					w[i] = -1.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get a 1,
	 * while all other players get a -1. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5.
	 */
	public double[] getAllWinsAndLosses() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = -1;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = 1.0;
				}else{
					w[i] = -1.0;
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets an update proportional to its score, while all other players get a 0.
	 * A player wins the simulation if it is the only one to get the highest score.
	 * For single player games, a player wins a simulation if it gets a score higher
	 * than 50.
	 * For example:
	 * scores [0 20 20  80]
	 * wins   [0  0  0   1]
	 * output [0  0  0 0.8]
	 */
	public double[] getProportionalSingleWin() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = 0;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = score[i]/100.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get an update
	 * proportional to their score while all other players get a 0. The players
	 * that win the simulation are the ones that get the highest score. For single
	 * player games, a player wins a simulation if it gets a score higher than 0.5.
	 * For example:
	 * scores [0 20  80  80]
	 * wins   [0  0   1   1]
	 * output [0  0 0.8 0.8]
	 */
	public double[] getProportionalAllWins() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = 0;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = score[i]/100.0;
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets an update proportional to its score, while all other players get a decrement
	 * proportional to their score. A player wins the simulation if it is the only one
	 * to get the highest score. For single player games, a player wins a simulation if
	 * it gets a score higher than 50. The decrement for a losing player is computed as
	 * (1-score/100).
	 * For example:
	 * scores [ 0   20   20  80]
	 * wins   [-1   -1   -1   1]
	 * output [-1 -0.8 -0.8 0.8]
	 */
	public double[] getProportionalSingleWinAndLosses() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = -(1.0-(score[0]/100.0));
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = score[i]/100.0;
				}else{
					w[i] = -(1.0-(score[i]/100.0));
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get an upate
	 * proportional to their score, while all other players get a decrement
	 * proportional to their scores. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5. The
	 * decrement for a losing player is computed as (1-score/100).
	 */
	public double[] getProportionalAllWinsAndLosses() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = -(1.0-score[0]/100.0);
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = score[i]/100.0;
				}else{
					w[i] = -(1.0-score[i]/100.0);
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets a 1, while all other players get a -1. A player wins the simulation if it
	 * is the only one to get the highest score. For single player games, a player wins
	 * a simulation if it gets a score higher than 50.
	 */
	public double[] getLossesWithSingleWin() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 0;
			}else{
				w[0] = -1;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = 0;
				}else{
					w[i] = -1.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get a 1,
	 * while all other players get a -1. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5.
	 */
	public double[] getLossesWithAllWins() {

		double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 0;
			}else{
				w[0] = -1;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = 0.0;
				}else{
					w[i] = -1.0;
				}
			}
		}

		return w;

	}

	/**
	 * This method is similar to getTerminalWins() because it converts goals to wins. However, instead of returning
	 * the wins it checks if there is only one winner. If so, it returns the index of the winner, otherwise it returns -1.
	 * For multi-player games the winner is the one that gets the highest score, for single-player games the only role
	 * is a winner only if its score is 100 (i.e. the maximum).
	 * @return
	 */
	public int getSingleWinner(){

		if(this.intermediateGoals.size() > 0) {

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

	private double rescaleValue(double value, double left, double right, double newLeft, double newRight){

		if(right <= left || newRight <= newLeft){
			GamerLogger.logError("MctsManager", "Trying to rescale value in inconsistent intervals: left=" +
					left + ", right=" + right + ", newLeft=" + newLeft + ", newRight=" + newRight + ".");
			throw new RuntimeException("MctsManager - Trying to rescale value in inconsistent intervals: left=" +
					left + ", right=" + right + ", newLeft=" + newLeft + ", newRight=" + newRight + ".");
		}

		return ((newRight-newLeft)*(value-left)/(right-left))+newLeft;
	}

	public static void main(String[] args){

		double[] aa = {0, 20, 20, 80};
		double[] bb = {0, 20, 80, 80};
		double[] cc = {0, 100};
		double[] dd = {50, 50};

		double[] a = SimulationResult.getTerminalWinsIn0_100(aa);
		double[] b = SimulationResult.getTerminalWinsIn0_100(bb);
		double[] c = SimulationResult.getTerminalWinsIn0_100(cc);
		double[] d = SimulationResult.getTerminalWinsIn0_100(dd);

		System.out.println(Arrays.toString(a));
		System.out.println(Arrays.toString(b));
		System.out.println(Arrays.toString(c));
		System.out.println(Arrays.toString(d));

		System.out.println();

		System.out.println("Single Win");
		System.out.println(Arrays.toString(SimulationResult.getSingleWin(a)));
		System.out.println(Arrays.toString(SimulationResult.getSingleWin(b)));
		System.out.println(Arrays.toString(SimulationResult.getSingleWin(c)));
		System.out.println(Arrays.toString(SimulationResult.getSingleWin(d)));

		System.out.println();

		System.out.println("All Wins");
		System.out.println(Arrays.toString(SimulationResult.getAllWins(a)));
		System.out.println(Arrays.toString(SimulationResult.getAllWins(b)));
		System.out.println(Arrays.toString(SimulationResult.getAllWins(c)));
		System.out.println(Arrays.toString(SimulationResult.getAllWins(d)));

		System.out.println();

		System.out.println("Single Win and Losses");
		System.out.println(Arrays.toString(SimulationResult.getSingleWinAndLosses(a)));
		System.out.println(Arrays.toString(SimulationResult.getSingleWinAndLosses(b)));
		System.out.println(Arrays.toString(SimulationResult.getSingleWinAndLosses(c)));
		System.out.println(Arrays.toString(SimulationResult.getSingleWinAndLosses(d)));

		System.out.println();

		System.out.println("All Wins and Losses");
		System.out.println(Arrays.toString(SimulationResult.getAllWinsAndLosses(a)));
		System.out.println(Arrays.toString(SimulationResult.getAllWinsAndLosses(b)));
		System.out.println(Arrays.toString(SimulationResult.getAllWinsAndLosses(c)));
		System.out.println(Arrays.toString(SimulationResult.getAllWinsAndLosses(d)));

		System.out.println();

		System.out.println("Proportional Single Win");
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWin(a,aa)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWin(b,bb)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWin(c,cc)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWin(d,dd)));

		System.out.println();

		System.out.println("Proportional All Wins");
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWins(a,aa)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWins(b,bb)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWins(c,cc)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWins(d,dd)));

		System.out.println();

		System.out.println("Proportional Single Win and Losses");
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWinAndLosses(a,aa)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWinAndLosses(b,bb)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWinAndLosses(c,cc)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalSingleWinAndLosses(d,dd)));

		System.out.println();

		System.out.println("Proportional All Wins and Losses");
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWinsAndLosses(a,aa)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWinsAndLosses(b,bb)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWinsAndLosses(c,cc)));
		System.out.println(Arrays.toString(SimulationResult.getProportionalAllWinsAndLosses(d,dd)));

		System.out.println();

		System.out.println("Losses With Single Win");
		System.out.println(Arrays.toString(SimulationResult.getLossesWithSingleWin(a)));
		System.out.println(Arrays.toString(SimulationResult.getLossesWithSingleWin(b)));
		System.out.println(Arrays.toString(SimulationResult.getLossesWithSingleWin(c)));
		System.out.println(Arrays.toString(SimulationResult.getLossesWithSingleWin(d)));

		System.out.println();

		System.out.println("Losses With All Wins");
		System.out.println(Arrays.toString(SimulationResult.getLossesWithAllWins(a)));
		System.out.println(Arrays.toString(SimulationResult.getLossesWithAllWins(b)));
		System.out.println(Arrays.toString(SimulationResult.getLossesWithAllWins(c)));
		System.out.println(Arrays.toString(SimulationResult.getLossesWithAllWins(d)));


	}

	public static double[] getSingleWin(double[] wins) {


		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = 0;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = 1.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get a 1,
	 * while all other players get a 0. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5.
	 */
	public static double[] getAllWins(double[] wins) {


		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = 0;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = 1.0;
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets a 1, while all other players get a -1. A player wins the simulation if it
	 * is the only one to get the highest score. For single player games, a player wins
	 * a simulation if it gets a score higher than 50.
	 */
	public static double[] getSingleWinAndLosses(double[] wins) {

		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = -1;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = 1.0;
				}else{
					w[i] = -1.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get a 1,
	 * while all other players get a -1. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5.
	 */
	public static double[] getAllWinsAndLosses(double[] wins) {


		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 1;
			}else{
				w[0] = -1;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = 1.0;
				}else{
					w[i] = -1.0;
				}
			}
		}

		return w;

	}


	public static double[] getProportionalSingleWin(double[] wins, double[] score) {

		//double[] wins = this.getTerminalWinsIn0_100();
		//double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = 0;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = score[i]/100.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get an update
	 * proportional to their score while all other players get a 0. The players
	 * that win the simulation are the ones that get the highest score. For single
	 * player games, a player wins a simulation if it gets a score higher than 0.5.
	 * For example:
	 * scores [0 20  80  80]
	 * wins   [0  0   1   1]
	 * output [0  0 0.8 0.8]
	 */
	public static double[] getProportionalAllWins(double[] wins, double[] score) {

		//double[] wins = this.getTerminalWinsIn0_100();
		//double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = 0;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = score[i]/100.0;
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets an update proportional to its score, while all other players get a decrement
	 * proportional to their score. A player wins the simulation if it is the only one
	 * to get the highest score. For single player games, a player wins a simulation if
	 * it gets a score higher than 50. The decrement for a losing player is computed as
	 * (1-score/100).
	 * For example:
	 * scores [ 0   20   20  80]
	 * wins   [-1   -1   -1   1]
	 * output [-1 -0.8 -0.8 0.8]
	 */
	public static double[] getProportionalSingleWinAndLosses(double[] wins, double[] score) {

		//double[] wins = this.getTerminalWinsIn0_100();
		//double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = -(1.0-score[0]/100.0);
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = score[i]/100.0;
				}else{
					w[i] = -(1.0-score[i]/100.0);
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get an upate
	 * proportional to their score, while all other players get a decrement
	 * proportional to their scores. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5. The
	 * decrement for a losing player is computed as (1-score/100).
	 */
	public static double[] getProportionalAllWinsAndLosses(double[] wins, double[] score) {

		//double[] wins = this.getTerminalWinsIn0_100();
		//double[] score = this.getTerminalGoalsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = score[0]/100.0;
			}else{
				w[0] = -(1.0-score[0]/100.0);
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = score[i]/100.0;
				}else{
					w[i] = -(1.0-score[i]/100.0);
				}
			}
		}

		return w;

	}

	/**
	 * @return an array where the player that won the simulation (if only one exists)
	 * gets a 1, while all other players get a -1. A player wins the simulation if it
	 * is the only one to get the highest score. For single player games, a player wins
	 * a simulation if it gets a score higher than 50.
	 */
	public static double[] getLossesWithSingleWin(double[] wins) {

		//double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 0;
			}else{
				w[0] = -1;
			}
		}else{
			int numWinners = 0;
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					numWinners++;
					w[i] = 0;
				}else{
					w[i] = -1.0;
				}
			}
			if(numWinners > 1){
				for(int i = 0; i < w.length; i++){
					w[i] = 0;
				}
			}

		}

		return w;

	}

	/**
	 * @return an array where the players that won the simulation get a 1,
	 * while all other players get a -1. The players that win the simulation
	 * are the ones that get the highest score. For single player games,
	 * a player wins a simulation if it gets a score higher than 0.5.
	 */
	public static double[] getLossesWithAllWins(double[] wins) {

		//double[] wins = this.getTerminalWinsIn0_100();
		double[] w = new double[wins.length];
		// Single player
		if(wins.length == 1){
			if(wins[0] >= 50){
				w[0] = 0;
			}else{
				w[0] = -1;
			}
		}else{
			for(int i = 0; i < wins.length; i++){
				if(wins[i] != 0){
					w[i] = 0.0;
				}else{
					w[i] = -1.0;
				}
			}
		}

		return w;

	}

	public static double[] getTerminalWinsIn0_100(double[] scores) {

		if(scores.length > 0) {

			double[] wins = new double[scores.length];

			if(scores.length == 1) {
				wins[0] = scores[0];
			}else {
				List<Integer> bestIndices = new ArrayList<Integer>();
				double max = -1;
				for(int roleIndex = 0; roleIndex < scores.length; roleIndex++) {
					if(scores[roleIndex] > max) {
						max = scores[roleIndex];
						bestIndices.clear();
						bestIndices.add(roleIndex);
					}else if(scores[roleIndex] == max){
						bestIndices.add(roleIndex);
					}
				}
				if(bestIndices.size() == 0) {
					GamerLogger.logError("MctsManager", "Found no best score when computing rescaled wins for a SimulationResult.");
					throw new RuntimeException("MctsManager - Found no best score when computing rescaled wins for a SimulationResult.");
				}
				// Wins is already initialized to all 0s, so we just change the wins for the bestIndices
				double split100Points = 100.0/((double)bestIndices.size());
				for(Integer roleIndex : bestIndices) {
					wins[roleIndex] = split100Points;
				}
			}
			return wins;
		}else {
			GamerLogger.logError("MctsManager", "Trying to compute rescaled wins for a SimulationResult that has no goals.");
			throw new RuntimeException("MctsManager - Trying to compute rescaled wins for a SimulationResult that has no goals.");
		}
	}

}
