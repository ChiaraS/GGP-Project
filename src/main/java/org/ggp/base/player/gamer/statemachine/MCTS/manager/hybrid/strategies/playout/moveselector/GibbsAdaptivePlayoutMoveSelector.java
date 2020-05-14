package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

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

public class GibbsAdaptivePlayoutMoveSelector extends AdaptivePlayoutMoveSelector {

	/**
	 * Used to multiply the exponent of e when computing the exponential of the weight.
	 * Note that this is different from the one used by the Gibbs formula in MAST, because
	 * here it rescales weights that are usually between -10 and 10, while in MAST it rescales
	 * moves scores in [0,100]. Moreover, here it is used as a multiplier, in MAST as a divisor.
	 */
	private double ppaTemperature;

	public GibbsAdaptivePlayoutMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.ppaTemperature = gamerSettings.getDoublePropertyValue("MoveSelector.ppaTemperature");

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

		probabilities = this.ppaWeights.getPlayoutProbabilities(roleIndex, moves, this.ppaTemperature);

		if(probabilities == null){ // If all weights are 0 and thus probabilities are null, return random move
			return moves.get(this.getRandom().nextInt(moves.size()));
		}

		try{
			distribution = new EnumeratedDistribution<Integer>(probabilities);
		}catch(Exception e){
			for(Pair<Integer,Double> p : probabilities){
				System.out.println("[ " + p.getFirst() + ", " + p.getSecond() + "]");
			}
			this.ppaWeights.printPpaWeights();
			GamerLogger.logError("MoveSelector", "AdaptivePlayoutMoveSelector - Error creating probability distribution when selecting a move in the playout!");
			throw new RuntimeException(e);
		}

		return moves.get(distribution.sample());

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = super.getComponentParameters(indentation);

		if(params != null){
			return params + indentation + "PPA_TEMPERATURE = " + this.ppaTemperature;
		}else{
			return indentation + "PPA_TEMPERATURE = " + this.ppaTemperature;
		}

	}

}
