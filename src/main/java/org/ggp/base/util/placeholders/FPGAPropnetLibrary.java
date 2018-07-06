package org.ggp.base.util.placeholders;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components.ImmutableProposition;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import csironi.ggp.course.utils.MyPair;

/**
 * Placeholder class for the java library that interfaces with FPGA propnet.
 *
 * This class simulates the library by actually using the software propnet to reason on the game.
 * It just mocks the library so we can test the gamer's code that uses it.
 * It's also a bit inefficient because it keeps translating states, moves and roles from Compact to Fpga format.
 *
 * @author C.Sironi
 *
 */
// CHANGES:
// - Changed GDLSentence to GDLTerm
// - Changed Pair to MyPair
public class FPGAPropnetLibrary {

	private SeparateInternalPropnetStateMachine theMachine;

	// Mapping the FPGA internal structure for moves and states with the GDL structure for moves and states.
	// FpgaInternalMove and FpgaInternalState are just place-holders for whatever format you are using
	// with the FPGA-propnet and you can change them accordingly.

	/**
	 * Maps an FpgaInternalMove to the GdlSentence in the corresponding input proposition in the ImmutablePropNet.
	 * To get the GdlSentence:
	 * 	ImmutableProposition inputProposition;
	 * 	inputProposition.getName();
	 */
	private BiMap<FpgaInternalMove,GdlSentence> movesMap;

	/**
	 * Maps an FpgaInternalState to the set of GdlSentence in the base proposition of the ImmutablePropNet
	 * that are true in the state.
	 * To get the Set<GdlSentence>:
	 * 	for each true base proposition in the FpgaInternalState:
	 * 		Set<GdlSentence> gdlState;
	 * 		ImmutableProposition trueBase;
	 * 		gdlState.add(trueBase.getName());
	 */
	//private BiMap<FpgaInternalState,Set<GdlSentence>> statesMap;

	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		this.theMachine.initialize(description, timeout);
	}

	public FPGAPropnetLibrary(SeparateInternalPropnetStateMachine theMachine/*? Place here whatever input is needed to initialize the library*/) {
		// Initialize FPGA-propnet

		this.theMachine = theMachine;

		// Create the bi-directional maps movesMap and statesMap to translate FPGA states and moves
		// to GDL and vice-versa.

		ImmutableProposition[] inputPropositions = this.theMachine.getPropNet().getInputPropositions();
		this.movesMap = HashBiMap.<FpgaInternalMove,GdlSentence>create(inputPropositions.length);

		for(int i = 0; i < inputPropositions.length; i++) {
			this.movesMap.put(new FpgaInternalMove(new CompactMove(i)), inputPropositions[i].getName());
		}


	}

	public GdlSentence getGDLMove(FpgaInternalMove fpgaMove) {
		return this.movesMap.get(fpgaMove);
	}

	// Changed GDLSentence to GDLTerm
	public FpgaInternalMove getFpgaMove(GdlSentence gdlMove) {
		return this.movesMap.inverse().get(gdlMove);
	}

	/*public Set<GdlSentence> getGDLState(FpgaInternalState fpgaState) {
		return this.statesMap.get(fpgaState);
	}*/

	/*public FpgaInternalState getFpgaState(Set<GdlSentence> gdlState) {
		return this.statesMap.inverse().get(gdlState);
	}*/

	/**
	 *
	 * @return the initial state.
	 */
	public FpgaInternalState getRootState(){
		if(this.theMachine.getCompactInitialState() == null) {
			System.out.println("Null root");
		}
		return new FpgaInternalState(this.theMachine.getCompactInitialState());
	}

	/**
	 * Given a state, this function returns all states reachable from this state. Each next state is
	 * paired with the list of moves (one for each role in the game) that lead to the next state.
	 *
	 * Whenever I call this method I assume that the moves for the roles are returned always with the
	 * same role order.
	 *
	 * @param state
	 * @return a list of lists of legal moves, one for each role, and all states reachable from the
	 * given state, each of which is paired with the list of moves (one for each role in the game)
	 * that lead to the next state.
	 *
	 * ? What does this method return if the state is terminal?
	 * For now assuming it returns null.
	 */
	public MyPair<List<List<FpgaInternalMove>>,List<MyPair<FpgaInternalState,List<FpgaInternalMove>>>> getNextStates(FpgaInternalState state){
		if(this.theMachine.isTerminal(state.getCompactMachineState())) {
			return null;
		}
		// Retrieve all legal moves
		List<List<CompactMove>> movesForAllRoles;
		try {
			movesForAllRoles = this.theMachine.getAllLegalMoves(state.getCompactMachineState());
		} catch (MoveDefinitionException e) {
			GamerLogger.logError("StateMachine", "[FakeFPGALibrary] Error when getting all legal moves!");
			throw new RuntimeException("StateMachine - [FakeFPGALibrary] Error when getting all legal moves!");
		}
		List<List<FpgaInternalMove>> internalMovesForAllRoles = new ArrayList<List<FpgaInternalMove>>();
		for(List<CompactMove> movesForOneRole : movesForAllRoles) {
			List<FpgaInternalMove> internalMovesForOneRole = new ArrayList<FpgaInternalMove>();
			for(CompactMove move : movesForOneRole) {
				internalMovesForOneRole.add(new FpgaInternalMove(move));
			}
			internalMovesForAllRoles.add(internalMovesForOneRole);
		}
		// Retrieve all joint moves and next states
		List<List<CompactMove>> allJointMoves;
		try {
			allJointMoves = this.theMachine.getLegalJointMoves(state.getCompactMachineState());
		} catch (MoveDefinitionException | StateMachineException e) {
			GamerLogger.logError("StateMachine", "[FakeFPGALibrary] Error when getting all legal joint moves!");
			throw new RuntimeException("StateMachine - [FakeFPGALibrary] Error when getting all legal joint moves!");
		}
		List<MyPair<FpgaInternalState,List<FpgaInternalMove>>> internalJointMovesAndNextStates = new ArrayList<MyPair<FpgaInternalState,List<FpgaInternalMove>>>();
		CompactMachineState nextState;
		if(allJointMoves == null) {
			System.out.println("Null joint moves");
		}
		for(List<CompactMove> jointMove : allJointMoves) {
			nextState = this.theMachine.getCompactNextState(state.getCompactMachineState(), jointMove);
			List<FpgaInternalMove> internalJointMove = new ArrayList<FpgaInternalMove>();
			for(CompactMove move : jointMove) {
				internalJointMove.add(new FpgaInternalMove(move));
			}
			internalJointMovesAndNextStates.add(new MyPair<FpgaInternalState,List<FpgaInternalMove>>(new FpgaInternalState(nextState), internalJointMove));
		}

		return new MyPair<List<List<FpgaInternalMove>>,List<MyPair<FpgaInternalState,List<FpgaInternalMove>>>>(internalMovesForAllRoles, internalJointMovesAndNextStates);
	}

	/**
	 * This method performs the given number of simulations starting from the given state
	 * and returns the score obtained by each role.
	 *
	 * Whenever I call this method I assume that the scores for the roles are returned always with the
	 * same role order.
	 *
	 * @param state
	 * @param simulationsNumber
	 * @return
	 */
	public List<Double> getScores(FpgaInternalState state, int simulationsNumber){
		MyPair<double[], Double> result;
		try {
			result = this.theMachine.fastPlayouts(state.getCompactMachineState(), simulationsNumber, Integer.MAX_VALUE);
		} catch (TransitionDefinitionException | MoveDefinitionException | StateMachineException
				| GoalDefinitionException e) {
			GamerLogger.logError("StateMachine", "[FakeFPGALibrary] Error when performing playouts!");
			throw new RuntimeException("StateMachine - [FakeFPGALibrary] Error when performing playouts!");
		}

		List<Double> scores = new ArrayList<Double>();
		for(double d : result.getFirst()) {
			scores.add(new Double(d));
		}

		return scores;
	}

}
