package org.ggp.base.util.placeholders;

import java.util.Vector;

import org.ggp.base.util.Pair;

/**
 * Placeholder class for the java library that interfaces with FPGA propnet.
 *
 * @author C.Sironi
 *
 */
public class FPGAPropnetInterface {

	/**
	 *
	 * @return the initial state.
	 */
	public FpgaInternalState getRootState(){
		return new FpgaInternalState();
	}

	/**
	 * Given a state, this function returns all states reachable from this state. Each next state is
	 * paired with the list of moves (one for each role in the game) that lead to the next state.
	 *
	 * @param state
	 * @return all states reachable from the given state, each of which is paired with the list of moves
	 * (one for each role in the game) that lead to the next state.
	 */
	public Vector<Pair<FpgaInternalState,Vector<FpgaInternalMove>>> getNextStates(FpgaInternalState state){
		return new Vector<Pair<FpgaInternalState,Vector<FpgaInternalMove>>>();
	}

	/**
	 * This method performs the given number of simulations starting from the given state
	 * and returns the score obtained by each role.
	 *
	 * @param state
	 * @param simulationsNumber
	 * @return
	 */
	public Vector<Double> getScores(FpgaInternalState state, int simulationsNumber){
		return new Vector<Double>();
	}

}
