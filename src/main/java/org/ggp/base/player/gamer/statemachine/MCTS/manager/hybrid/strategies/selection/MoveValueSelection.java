package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.MoveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public abstract class MoveValueSelection extends SelectionStrategy {

	private double valueOffset;

	protected MoveEvaluator moveEvaluator;

	public MoveValueSelection(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, double valueOffset, MoveEvaluator moveEvaluator) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.valueOffset = valueOffset;
		this.moveEvaluator = moveEvaluator;
	}

	@Override
	public void clearComponent(){
		this.moveEvaluator.clearComponent();
	}

	@Override
	public void setUpComponent(){
		this.moveEvaluator.setUpComponent();
	}

	@Override
	public MCTSJointMove select(MCTSNode currentNode) {
		if(currentNode instanceof DecoupledMCTSNode){
			return this.decSelect((DecoupledMCTSNode)currentNode);
		}else if(currentNode instanceof SequentialMCTSNode){
			return this.seqSelect((SequentialMCTSNode)currentNode);
		}else{
			throw new RuntimeException("MoveValueSelection-select(): detected a node of a non-recognizable sub-type of class MCTSNode.");
		}
	}

	private MCTSJointMove decSelect(DecoupledMCTSNode currentNode) {

		// No need to make sure that currentNode is terminal if the code is correct,
		// because the node that is passed as input is always non-terminal.

		DecoupledMCTSMoveStats[][] moves = currentNode.getMoves();

		// Also here we can assume that the moves will be non-null since the code takes care
		// of only passing to this method the nodes that have all the information needed for
		// selection.

		List<Move> selectedJointMove = new ArrayList<Move>();
		int[] movesIndices = new int[moves.length];

		double maxMoveValue;
		double[] moveValues;

		// For each role check the statistics and pick a move.
		for(int i = 0; i < moves.length; i++){

			// Compute move value for all moves.
			maxMoveValue = -Double.MAX_VALUE;
			moveValues = new double[moves[i].length];

			// For each legal move check the moveValue.
			for(int j = 0; j < moves[i].length; j++){

				// Compute the move value.
				moveValues[j] = this.moveEvaluator.computeMoveValue(currentNode, moves[i][j].getTheMove(), i, moves[i][j]);

				// If it's higher than the current maximum one, replace the max value.
				if(moveValues[j] > maxMoveValue){
					maxMoveValue = moveValues[j];
				}
			}

			// Now that we have the maximum move value we can look for all moves that have their value
			// in the interval [maxMoveValue-valueOffset, maxMoveValue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int j = 0; j < moveValues.length; j++){
				if(moveValues[j] >= (maxMoveValue-this.valueOffset)){
					selectedMovesIndices.add(new Integer(j));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("Decoupled selection: detected no moves with value higher than -1.");
			}

			movesIndices[i] = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			selectedJointMove.add(moves[i][movesIndices[i]].getTheMove());
		}

		return new SequDecMCTSJointMove(selectedJointMove, movesIndices);
	}


	private MCTSJointMove seqSelect(SequentialMCTSNode currentNode){

		List<Move> jointMove = new ArrayList<Move>(this.gameDependentParameters.getNumRoles());
		int[] movesIndices = new int[this.gameDependentParameters.getNumRoles()];

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			jointMove.add(null);
		}

		// Get the index of myRole.
		int roleIndex = this.gameDependentParameters.getMyRoleIndex();

		// Get the moves for myRole.
		SequentialMCTSMoveStats[] movesStats = currentNode.getMovesStats();

		double maxMoveValue;
		double[] moveValues;
		//int nodeVisits = currentNode.getTotVisits();

		while(movesStats != null){

			// Compute the value for all moves.
			maxMoveValue = -1;
			moveValues = new double[movesStats.length];

			for(int i = 0; i < movesStats.length; i++){
				// Compute the move value.
				moveValues[i] = this.moveEvaluator.computeMoveValue(currentNode, currentNode.getAllLegalMoves().get(roleIndex).get(i), roleIndex, movesStats[i]);

				// If it's higher than the current maximum one, replace the max value
				if(moveValues[i] > maxMoveValue){
					maxMoveValue = moveValues[i];
				}
			}

			// Now that we have the maximum move value we can look for all the moves that have their value
			// in the interval [maxMoveValue-valueOffset, maxMoveValue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int i = 0; i < moveValues.length; i++){
				if(moveValues[i] >= (maxMoveValue-this.valueOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("Sequential selection: detected no moves with value higher than -1.");
			}

			// Add one of the selected moves to the joint move.
			movesIndices[roleIndex] = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			jointMove.set(roleIndex, currentNode.getAllLegalMoves().get(roleIndex).get(movesIndices[roleIndex]));

			// Get the move statistics of the next role, given the selected move.
			movesStats = movesStats[movesIndices[roleIndex]].getNextRoleMovesStats();

			// Compute the index for the next role
			roleIndex = (roleIndex+1)%this.gameDependentParameters.getNumRoles();

		}

		return new SequDecMCTSJointMove(jointMove, movesIndices);

	}

	@Override
	public String getComponentParameters(){

		return "VALUE_OFFSET = " + this.valueOffset + ", " + this.moveEvaluator.printComponent();
	}

}
