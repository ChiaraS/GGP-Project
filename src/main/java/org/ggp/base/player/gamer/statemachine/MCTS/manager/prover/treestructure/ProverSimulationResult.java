package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.Move;

public class ProverSimulationResult {

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
	private int playoutLength;

	/**
	 * Goals in the final state of the simulation (note that it's not necessarily the terminal state,
	 * because the simulation could have been interrupted earlier).
	 */
	//private int[] terminalGoals;

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
	private List<int[]> intermediateGoals;

	/**
	 * Constructor that initializes the two lists as empty lists and the terminal goals as null.
	 */
	public ProverSimulationResult() {

		this(0, new ArrayList<List<Move>>(), new ArrayList<int[]>());

	}

	/**
	 * Constructor that initializes the two lists as empty lists and the terminal goals to the given values.
	 *
	 * @param terminalGoals
	 */
	public ProverSimulationResult(int[] terminalGoals) {

		this(0, terminalGoals);

	}

	/**
	 * Constructor that initializes the two lists as empty lists and the playout length and the terminal goals to the given values.
	 *
	 * @param terminalGoals
	 */
	public ProverSimulationResult(int playoutLength, int[] terminalGoals) {

		this(playoutLength, new ArrayList<List<Move>>(), new ArrayList<int[]>());

		this.intermediateGoals.add(terminalGoals);

	}

	public ProverSimulationResult(int playoutLength, List<int[]> intermediateGoals) {

		this(playoutLength, new ArrayList<List<Move>>(), intermediateGoals);

	}

	public ProverSimulationResult(int playoutLength, int[] terminalGoals, List<List<Move>> allJointMoves) {

		this(playoutLength, allJointMoves, new ArrayList<int[]>());

		this.intermediateGoals.add(terminalGoals);

	}

	public ProverSimulationResult(int playoutLength, List<List<Move>> allJointMoves, List<int[]> intermediateGoals) {

		this.playoutLength = playoutLength;

		if(allJointMoves == null){
			allJointMoves = new ArrayList<List<Move>>();
		}

		if(intermediateGoals == null){
			intermediateGoals = new ArrayList<int[]>();
		}

		this.allJointMoves = allJointMoves;

		this.intermediateGoals = intermediateGoals;

	}

	public int getPlayoutLength(){
		return this.playoutLength;
	}

	public List<List<Move>> getAllJointMoves(){

		return this.allJointMoves;

	}

	public List<int[]> getAllGoals(){

		return this.intermediateGoals;

	}

	/**
	 *
	 * @return only the terminal goals of the whole simulation.
	 */
	public int[] getTerminalGoals(){

		return this.intermediateGoals.get(0);
	}

	public void addJointMove(List<Move> jointMove){

		this.allJointMoves.add(jointMove);

	}

	public void addGoals(int[] goals){

		this.intermediateGoals.add(goals);

	}

}
