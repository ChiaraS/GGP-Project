package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
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

public class GibbsNstMoveSelector extends NstMoveSelector {

	/**
	 * NOTE that the temperature for Gibbs at the moment is different from the temperature used
	 * by the AdaptivePlayoutMoveSelector. In this class NST scores vary between 0 and 100,
	 * while for AdaptivePlayoutMoveSelector the eights are usually smaller and vary from negative
	 * to positive values (around [-10, 10] in general).
	 */
	private double nstTemperature;

	public GibbsNstMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.nstTemperature = gamerSettings.getDoublePropertyValue("MoveSelector.nstTemperature");

	}

	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex)
			throws MoveDefinitionException, StateMachineException {

		List<Move> legalMoves;

		if(node != null && node instanceof DecoupledMctsNode) {
			legalMoves = ((DecoupledMctsNode)node).getLegalMovesForRole(roleIndex);
		}else {
			Role role = this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex);
			legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(state, role);
		}

		//String toLog = "";
		//for(int index = 0; index < this.weightsPerMove.size(); index++){
		//	toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(index)) + ";\n");
		//	for(Entry<Move, Double> moveWeight : this.weightsPerMove.get(index).entrySet()){
		//		double weight = moveWeight.getValue();
		//		toLog += ("MOVE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitMove(moveWeight.getKey()) +
		//		";WEIGHT=;" + weight + ";\n");
		//	}
		//}
		//System.out.println(toLog);

		// Pick the move according to the distribution computed with the NST values.
		return this.getMoveFromDistribution(roleIndex, legalMoves);
	}

	/**
	 * @param moves
	 * @return
	 */
	private Move getMoveFromDistribution(int roleIndex, List<Move> moves) {

		if(moves.size() == 1){
			return moves.get(0);
		}

		EnumeratedDistribution<Integer> distribution;
		List<Pair<Integer,Double>> probabilities;

		probabilities = this.getPlayoutProbabilities(roleIndex, moves);

		if(probabilities == null){ // If all weights are 0 and thus probabilities are null, return random move
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

		// Iterate over all legal moves to compute the sum of the exponential of their scores
		double exponentialSum = 0;
		for(int j = 0; j < roleMoves.size(); j++){
			legalMovesForWinnerExponential[j] = Math.exp(this.computeNstValue(roleIndex, roleMoves.get(j))/this.nstTemperature);
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

		String params = super.getComponentParameters(indentation);

		if(params != null){
			return params + indentation + "NST_TEMPERATURE = " + this.nstTemperature;
		}else{
			return indentation + "NST_TEMPERATURE = " + this.nstTemperature;
		}

	}


}
