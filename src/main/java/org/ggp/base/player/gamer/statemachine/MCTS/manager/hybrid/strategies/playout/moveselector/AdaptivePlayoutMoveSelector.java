package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaWeights;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class AdaptivePlayoutMoveSelector extends MoveSelector {

	private PpaWeights ppaWeights;

	// Used to multiply the exponent of e when computing the exponential of the weight.
	private double temperature;

	public AdaptivePlayoutMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.temperature = gamerSettings.getDoublePropertyValue("MoveSelector.temperature");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.ppaWeights = sharedReferencesCollector.getPpaWeights();

	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

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

		// Pick the move according to the distribution computed with the weights.
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

		probabilities = new ArrayList<Pair<Integer,Double>>();

		Map<Move,Double> weightsForRole = this.weightsPerMove.get(roleIndex);

		double[] exponentialPerMove = new double[moves.size()];

		double probabilitySum = 0;
		for(int i = 0; i < moves.size(); i++){

			Double weightForMove = weightsForRole.get(moves.get(i));
			if(weightForMove != null){

				//System.out.println();
				//System.out.println(weightForMove);
				//System.out.println();


				exponentialPerMove[i] = Math.exp(this.temperature*weightForMove);
				probabilitySum += exponentialPerMove[i];
			}else{
				exponentialPerMove[i] = Math.exp(this.temperature*0.0);
				probabilitySum += exponentialPerMove[i];
			}

		}

		if(probabilitySum == 0){ // If all weights are 0, return random move
			return moves.get(this.getRandom().nextInt(moves.size()));
		}

		for(int i = 0; i < moves.size(); i++){

			//System.out.println(exponentialPerMove[i]);
			//System.out.println(probabilitySum);
			//System.out.println();

			probabilities.add(new Pair<Integer,Double>(i,exponentialPerMove[i]/probabilitySum));
		}




		try{
		distribution = new EnumeratedDistribution<Integer>(probabilities);
		}catch(Exception e){
			e.printStackTrace();
			String toLog = "";
			for(int index = 0; index < this.weightsPerMove.size(); index++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(index)) + ";\n");
				for(Entry<Move, Double> moveWeight : this.weightsPerMove.get(roleIndex).entrySet()){
					double weight = moveWeight.getValue();
					toLog += ("MOVE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitMove(moveWeight.getKey()) +
					";WEIGHT=;" + weight + ";\n");
				}
			}
			System.out.println(toLog);
			distribution = null;
		}

		return moves.get(distribution.sample());

	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "TEMPERATURE = " + this.temperature;

		if(this.ppaWeights != null){
			params += indentation + "ppa_weights = " + this.ppaWeights.getMinimalInfo();
		}else{
			params += indentation + "ppa_weights = null";
		}

		return params;
	}

}
