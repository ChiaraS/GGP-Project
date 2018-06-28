package org.ggp.base.util.placeholders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.Pair;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;

import com.google.common.collect.BiMap;

/**
 * Placeholder class for the java library that interfaces with FPGA propnet.
 *
 * @author C.Sironi
 *
 */
public class FPGAPropnetLibrary {

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
	private BiMap<FpgaInternalState,Set<GdlSentence>> statesMap;

	public FPGAPropnetLibrary(ImmutablePropNet propnet/*? Place here whatever input is needed to initialize the library*/) {
		// Initialize FPGA-propnet

		// Create the bi-directional maps movesMap and statesMap to translate FPGA states and moves
		// to GDL and vice-versa.
	}

	public GdlSentence getGDLMove(FpgaInternalMove fpgaMove) {
		return this.movesMap.get(fpgaMove);
	}

	public FpgaInternalMove getFpgaMove(GdlSentence gdlMove) {
		return this.movesMap.inverse().get(gdlMove);
	}

	public Set<GdlSentence> getGDLState(FpgaInternalState fpgaState) {
		return this.statesMap.get(fpgaState);
	}

	public FpgaInternalState getFpgaState(Set<GdlSentence> gdlState) {
		return this.statesMap.inverse().get(gdlState);
	}

	/**
	 *
	 * @return the initial state.
	 */
	public FpgaInternalState getRootState(){
		// TODO
		return new FpgaInternalState();
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
	 */
	public Pair<List<List<FpgaInternalMove>>,List<Pair<FpgaInternalState,List<FpgaInternalMove>>>> getNextStates(FpgaInternalState state){
		// TODO
		return Pair.from(null);
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
		// TODO
		return new ArrayList<Double>();
	}

}
