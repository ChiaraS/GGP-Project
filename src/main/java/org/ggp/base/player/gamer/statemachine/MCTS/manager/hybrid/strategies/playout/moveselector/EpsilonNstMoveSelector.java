package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class EpsilonNstMoveSelector extends NstMoveSelector {

	/**
	 * With probability nstEpsilon select a random action, otherwise select the action with
	 * highest weight
	 */
	private double nstEpsilon;

	public EpsilonNstMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.nstEpsilon = gamerSettings.getDoublePropertyValue("MoveSelector.nstEpsilon");

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

		//System.out.println("Selecting for role " + this.gameDependentParameters.getTheMachine().convertToExplicitRole(new CompactRole(roleIndex)));

		if(legalMoves.size() == 1){
			Move m = legalMoves.get(0);
			//System.out.println("Selected " + this.gameDependentParameters.getTheMachine().convertToExplicitMove(m));
			return m;
		}

		if(this.random.nextDouble() < this.nstEpsilon){
			//System.out.println("Random");
    		// Choose random action with probability epsilon
			Move m = legalMoves.get(this.random.nextInt(legalMoves.size()));
			//System.out.println("Selected " + this.gameDependentParameters.getTheMachine().convertToExplicitMove(m));
			return m;
    	}else{
    		//System.out.println("Best");
    		// Choose move with highest average score
    		Move m = this.getBestMove(roleIndex, legalMoves);
    		//System.out.println("Selected " + this.gameDependentParameters.getTheMachine().convertToExplicitMove(m));
    		return m;
    	}

	}

	/**
	 * WARNING! Be very careful! NST, as opposed to UCT, doesn't normalize value in [0, 1] to select
	 * the best move. It would be irrelevant to normalize since the choices of NST are not influenced
	 * by the range of move values.
	 *
	 * @param moves
	 * @return
	 */
	private Move getBestMove(int roleIndex, List<Move> moves) {

		List<Move> chosenMoves = new ArrayList<Move>();
		double maxValue = -Double.MAX_VALUE;
		double currentValue;

		Move move;
		// For each legal move, compute the NST value by averaging the values
		// of the n-grams of length 1 to maxNGramLength
		for(int moveIndex = 0; moveIndex < moves.size(); moveIndex++){

			move = moves.get(moveIndex);

			currentValue = this.computeNstValue(roleIndex, move);

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentValue > maxValue){
				maxValue = currentValue;
				chosenMoves.clear();
				chosenMoves.add(move);
			}else if(currentValue == maxValue){
				chosenMoves.add(move);
			}
		}

		return chosenMoves.get(this.random.nextInt(chosenMoves.size()));

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = super.getComponentParameters(indentation);

		if(params != null){
			return params + indentation + "NST_EPSILON = " + this.nstEpsilon;
		}else{
			return indentation + "NST_EPSILON = " + this.nstEpsilon;
		}

	}


}
