package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

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
public class SimulationResult {

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
	private int[] terminalGoals;

	/**
	 *  The joint moves that form the path of this simulation.
	 *
	 * NOTE that since some implementations of MCTS do not need to remember all the taken actions,
	 * this list might be empty.
	 * contain only one element, that is the final outcome of the simulation (i.e. the goal of each
	 * role in the final state of the simulation).
	 */
	private List<List<InternalPropnetMove>> allJointMoves;

	/**
	 * The goals of each role in each state reached by performing the corresponding joint moves
	 * (i.e. list of intermediate rewards for each role).
	 *
	 * NOTE that since some implementations of MCTS do not need intermediate goals, this list might
	 * be empty.
	 *
	 * ALSO NOTE that if this list is empty the goals in the last state of the simulation will still
	 * be memorized in the 'intermediateGoals' parameter, and if this list is not empty then the
	 * goals in the last state of the simulation will be memorized as the first element of this list.
	 *
	 * This list can either be empty or have the exact same length as the list of all the joint moves.
	 *
	 */
	private List<int[]> intermediateGoals;

	/**
	 * Constructor that initializes the playout length to 0, the two lists as empty lists and the terminal goals as null.
	 */
	public SimulationResult() {

		this(0, null, new ArrayList<List<InternalPropnetMove>>(), new ArrayList<int[]>());

	}

	/**
	 * Constructor that initializes the playout length to 0, the two lists as empty lists and the terminal goals to the given values.
	 *
	 * @param terminalGoals
	 */
	public SimulationResult(int[] terminalGoals) {

		this(0, terminalGoals, new ArrayList<List<InternalPropnetMove>>(), new ArrayList<int[]>());

	}

	/**
	 * Constructor that initializes the two lists as empty lists and the playout length and the terminal goals to the given values.
	 *
	 * @param terminalGoals
	 */
	public SimulationResult(int playoutLength, int[] terminalGoals) {

		this(playoutLength, terminalGoals, new ArrayList<List<InternalPropnetMove>>(), new ArrayList<int[]>());

	}

	public SimulationResult(int[] terminalGoals, List<List<InternalPropnetMove>> allJointMoves, List<int[]> intermediateGoals) {

		this(0, terminalGoals, allJointMoves, intermediateGoals);

	}

	public SimulationResult(int playoutLength, int[] terminalGoals, List<List<InternalPropnetMove>> allJointMoves, List<int[]> intermediateGoals) {

		this.playoutLength = playoutLength;

		this.terminalGoals = terminalGoals;

		if(allJointMoves == null){
			allJointMoves = new ArrayList<List<InternalPropnetMove>>();
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

	public List<List<InternalPropnetMove>> getAllJointMoves(){

		return this.allJointMoves;

	}

	public List<int[]> getIntermediateGoals(){

		return this.intermediateGoals;

	}

	/**
	 *
	 * @return only the terminal goals of the whole simulation.
	 */
	public int[] getTerminalGoals(){

		return this.terminalGoals;
	}

	public void addJointMove(List<InternalPropnetMove> jointMove){

		//if(this.allJointMoves == null){
		//	GamerLogger.logError("MCTSManager", "Simulation result not initialized to memorize all the joint moves. Probably a wrong combination of strategies has been set!");
		//	throw new RuntimeException("Simulation result not initialized to memorize all the joint moves.");
		//}

		this.allJointMoves.add(jointMove);

	}

	public void addGoals(int[] goals){

		//if(this.intermediateGoals == null){
		//	GamerLogger.logError("MCTSManager", "Simulation result not initialized to memorize all the intermediate goals. Probably a wrong combination of strategies has been set!");
		//	throw new RuntimeException("Simulation result not initialized to memorize all the intermediate goals.");
		//}

		this.intermediateGoals.add(goals);

	}

}
