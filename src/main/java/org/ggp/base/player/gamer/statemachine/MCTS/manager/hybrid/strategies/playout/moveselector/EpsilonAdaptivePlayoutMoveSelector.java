package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaInfo;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class EpsilonAdaptivePlayoutMoveSelector extends AdaptivePlayoutMoveSelector {

	/**
	 * With probability ppaEpsilon select a random action, otherwise select the action with
	 * highest weight
	 */
	private double ppaEpsilon;

	private double ppaFpu;

	public EpsilonAdaptivePlayoutMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.ppaEpsilon = gamerSettings.getDoublePropertyValue("MoveSelector.ppaEpsilon");

		this.ppaFpu = gamerSettings.getDoublePropertyValue("MoveSelector.ppaFpu");

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

		if(this.random.nextDouble() < this.ppaEpsilon){
    		// Choose random action with probability epsilon
			return legalMoves.get(this.random.nextInt(legalMoves.size()));
    	}else{
    		// Choose move with highest average score
    		return this.getBestMove(roleIndex, legalMoves);
    	}

	}

	/**
	 * WARNING! Be very careful! MAST, as opposed to UCT, doesn't normalize value in [0, 1] to select
	 * the best move. It would be irrelevant to normalize since the choices of MAST are not influenced
	 * by the range of move values.
	 *
	 * @param moves
	 * @return
	 */
	private Move getBestMove(int roleIndex, List<Move> moves) {

		List<Move> chosenMoves = new ArrayList<Move>();
		PpaInfo moveInfo;
		double maxWeight = -1;
		double currentWeight;

		Move move;
		// For each legal move check the weight
		for(int moveIndex = 0; moveIndex < moves.size(); moveIndex++){

			move = moves.get(moveIndex);
			moveInfo = this.ppaWeights.getPpaInfoForEpsilonGreedySelection(roleIndex, move);

			if(moveInfo != null){
				currentWeight = moveInfo.getWeight();
			}else{
				currentWeight = this.ppaFpu;
			}

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentWeight > maxWeight){
				maxWeight = currentWeight;
				chosenMoves.clear();
				chosenMoves.add(move);
			}else if(currentWeight == maxWeight){
				chosenMoves.add(move);
			}
		}

		return chosenMoves.get(this.random.nextInt(chosenMoves.size()));

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = super.getComponentParameters(indentation);

		if(params != null){
			return params + indentation + "PPA_EPSILON = " + this.ppaEpsilon +
					indentation + "PPA_FPU = " + this.ppaFpu;
		}else{
			return indentation + "PPA_EPSILON = " + this.ppaEpsilon +
					indentation + "PPA_FPU = " + this.ppaFpu;
		}

	}

}
