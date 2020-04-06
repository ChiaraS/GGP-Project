package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class GibbsMastMoveSelector extends MoveSelector {

	private List<Map<Move, MoveStats>> mastStatistics;

	/**
	 * Initial value of a move that has never been visited. This value is
	 * the one that is then plugged into the Gibbs formula for the move.
	 * Note that this must be in the range [0,100] because MAST doesn't
	 * bother normalizing the move values (it's not necessary).
	 */
	private double gibbsFpu;

	/**
	 * NOTE that the temperature for Gibbs at the moment is different from the temperature used
	 * by the AdaptivePlayoutMoveSelector. In this class MAST scores vary between 0 and 100,
	 * while for AdaptivePlayoutMoveSelector the eights are usually smaller and vary from negative
	 * to positive values (around [-10, 10] in general).
	 */
	private double gibbsTemperature;

	public GibbsMastMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);


		this.gibbsFpu = gamerSettings.getDoublePropertyValue("MoveSelector.gibbsFpu");

		this.gibbsTemperature = gamerSettings.getDoublePropertyValue("MoveSelector.gibbsTemperature");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastStatistics = sharedReferencesCollector.getMastStatistics();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	/**
	 * This method returns a joint move according to the MAST strategy.
	 * For each role it gets the list of all its legal moves in the state and picks the one with highest MAST expected score.
	 * @throws StateMachineException
	 */
/*	@Override
	public List<Move> getJointMove(MctsNode node, MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			jointMove.add(this.getMoveForRole(node, state, i));
		}

		//System.out.println(Arrays.toString(jointMove.toArray()));

		return jointMove;

	}
*/
	/**
	 * This method returns a move according to the MAST strategy.
	 * For the given role it gets the list of all its legal moves in the state
	 * and picks the one with highest MAST expected score.
	 *
	 * @throws MoveDefinitionException, StateMachineException
	 */
	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {

		List<Move> legalMoves;

		if(node != null && node instanceof DecoupledMctsNode) {
			legalMoves = ((DecoupledMctsNode)node).getLegalMovesForRole(roleIndex);
		}else {
			Role role = this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex);
			legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(state, role);
		}

		// Pick the move according to the Gibbs measure.
		return this.getMastMoveFromDistribution(roleIndex, legalMoves);
	}

	/**
	 * WARNING! Be very careful! MAST, as opposed to UCT, doesn't normalize value in [0, 1] to select
	 * the best move. It would be irrelevant to normalize since the choices of MAST are not influenced
	 * by the range of move values.
	 *
	 * @param moves
	 * @return
	 */
	private Move getMastMoveFromDistribution(int roleIndex, List<Move> moves) {

		if(moves.size() == 1){
			return moves.get(0);
		}

		EnumeratedDistribution<Integer> distribution;
		List<Pair<Integer,Double>> probabilities;

		probabilities = this.getPlayoutProbabilities(roleIndex, moves);

		if(probabilities == null){ // If all probabilities are null (should never happen), return random move
			return moves.get(this.getRandom().nextInt(moves.size()));
		}

		try{
			distribution = new EnumeratedDistribution<Integer>(probabilities);
		}catch(Exception e){
			GamerLogger.logError("MoveSelector", "AdaptivePlayoutMoveSelector - Found non-positive sum of exponentials when adapting the playout policy!");
			throw new RuntimeException(e);
		}

		return moves.get(distribution.sample());

	}

	private List<Pair<Integer,Double>> getPlayoutProbabilities(int roleIndex, List<Move> roleMoves){

		//System.out.println("select");
		//this.printPpaWeights();
		//System.out.println();

		List<Pair<Integer,Double>> probabilities;

		probabilities = new ArrayList<Pair<Integer,Double>>();

		double[] legalMovesForWinnerExponential = new double[roleMoves.size()];

		MoveStats currentStats;
		double mastScore;

		// Iterate over all legal moves to compute the sum of the exponential of their scores
		double exponentialSum = 0;
		for(int j = 0; j < roleMoves.size(); j++){
			currentStats = this.mastStatistics.get(roleIndex).get(roleMoves.get(j));
			if(currentStats == null || currentStats.getVisits() <= 0){
				mastScore = this.gibbsFpu;
			}else{
				mastScore = currentStats.getScoreSum()/currentStats.getVisits();
			}
			legalMovesForWinnerExponential[j] = Math.exp(mastScore/this.gibbsTemperature);
			exponentialSum += legalMovesForWinnerExponential[j];
		}

		if(exponentialSum == 0){ // If the sum is 0 (should never happen) return null.
			return null;
		}

		// Iterate over all the exponentials and create the probability distribution
		for(int j = 0; j < legalMovesForWinnerExponential.length; j++){

			probabilities.add(new Pair<Integer,Double>(j,legalMovesForWinnerExponential[j]/exponentialSum));

		}

		//this.printPpaWeights();
		//System.out.println();
		//System.out.println("--------------------------------");
		//System.out.println();


		return probabilities;

	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "GIBBS_FPU = " + this.gibbsFpu +
				indentation + "GIBBS_TEMPERATURE = " + this.gibbsTemperature;

		if(this.mastStatistics != null){
			String mastStatisticsString = "[ ";

			for(Map<Move, MoveStats> roleMastStats : this.mastStatistics){
				mastStatisticsString += roleMastStats.size() + " entries, ";
			}

			mastStatisticsString += "]";

			params += indentation + "mast_statistics = " + mastStatisticsString;
		}else{
			params += indentation + "mast_statistics = null";
		}

		return params;

	}

}
