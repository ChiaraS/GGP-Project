package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.MoveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SeqDecMctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MoveValueSelection extends SelectionStrategy {

	private double valueOffset;

	/**
	 * NOTE: to obtain the desired selection type set the MoveEvaluator as follows:
	 * UCT SELECTION = UCTEvaluator
	 * GRAVE SELECTION = GraveEvaluator (and use the subclass GraveSelection)
	 */
	protected MoveEvaluator moveEvaluator;

	public MoveValueSelection(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.valueOffset = gamerSettings.getDoublePropertyValue("SelectionStrategy.valueOffset");

		try {
			this.moveEvaluator = (MoveEvaluator) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.MOVE_EVALUATORS.getConcreteClasses(),
					gamerSettings.getPropertyValue("SelectionStrategy.moveEvaluatorType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating MoveEvaluator " + gamerSettings.getPropertyValue("SelectionStrategy.moveEvaluatorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.moveEvaluator.setReferences(sharedReferencesCollector);
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
	public MctsJointMove select(MctsNode currentNode, MachineState state) {
		if(currentNode instanceof DecoupledMctsNode){
			return this.decSelect((DecoupledMctsNode)currentNode);
		}else if(currentNode instanceof SequentialMctsNode){
			return this.seqSelect((SequentialMctsNode)currentNode);
		}else{
			throw new RuntimeException("MoveValueSelection-select(): detected a node of a non-recognizable sub-type of class MctsNode.");
		}
	}

	@Override
	public MctsMove selectPerRole(MctsNode currentNode, MachineState state, int roleIndex) {
		if(currentNode instanceof DecoupledMctsNode){
			return this.decSelectPerRole((DecoupledMctsNode)currentNode, roleIndex);
		}else /*if(currentNode instanceof SequentialMctsNode){
			return this.seqSelect((SequentialMctsNode)currentNode);
		}else*/{
			throw new RuntimeException("MoveValueSelection-selectPerRole(): detected a node of a sub-type of class MctsNode that is not supported by this selection strategy.");
		}
	}

	@Override
	public void preSelectionActions(MctsNode currentNode) {
		// No need to do anything
	}

	private MctsJointMove decSelect(DecoupledMctsNode currentNode) {

		List<Move> selectedJointMove = new ArrayList<Move>(this.gameDependentParameters.getNumRoles());
		int[] movesIndices = new int[this.gameDependentParameters.getNumRoles()];
		MctsMove move;

		// For each role check the statistics and pick a move.
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){

			move = this.decSelectPerRole(currentNode, i);
			selectedJointMove.add(move.getMove());
			movesIndices[i] = move.getMovesIndex();

		}

		return new SeqDecMctsJointMove(selectedJointMove, movesIndices);
	}

	private MctsMove decSelectPerRole(DecoupledMctsNode currentNode, int roleIndex){

		// No need to make sure that currentNode is terminal if the code is correct,
		// because the node that is passed as input is always non-terminal.

		DecoupledMctsMoveStats[][] moves = currentNode.getMoves();

		// Also here we can assume that the moves will be non-null since the code takes care
		// of only passing to this method the nodes that have all the information needed for
		// selection.

		// Compute move value for all moves.
		double maxMoveValue = -Double.MAX_VALUE;
		double[] moveValues = new double[moves[roleIndex].length];

		// For each legal move check the moveValue.
		for(int j = 0; j < moves[roleIndex].length; j++){

			// Compute the move value.
			moveValues[j] = this.moveEvaluator.computeMoveValue(currentNode, moves[roleIndex][j].getTheMove(), roleIndex, moves[roleIndex][j], currentNode.getTotVisits()[roleIndex]);

			// If it's higher than the current maximum one, replace the max value.
			if(moveValues[j] > maxMoveValue){
				maxMoveValue = moveValues[j];
			}
		}

		// Now that we have the maximum move value we can look for all moves that have their value
		// in the interval [maxMoveValue-valueOffset, maxMoveValue].
		List<Integer> selectedMovesIndices = new ArrayList<Integer>();

		//System.out.print("MoveValues = [ ");

		for(int j = 0; j < moveValues.length; j++){

			//System.out.print(moveValues[j] + " ");

			if(moveValues[j] >= (maxMoveValue-this.valueOffset)){
				selectedMovesIndices.add(new Integer(j));
			}
		}

		//System.out.println("]");

		// Extra check (should never be true).
		if(selectedMovesIndices.isEmpty()){
			throw new RuntimeException("Decoupled selection: detected no moves with value higher than -1.");
		}

		int moveIndex = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
		Move move = moves[roleIndex][moveIndex].getTheMove();

		return new MctsMove(move, moveIndex);

	}


	private MctsJointMove seqSelect(SequentialMctsNode currentNode){

		List<Move> jointMove = new ArrayList<Move>(this.gameDependentParameters.getNumRoles());
		int[] movesIndices = new int[this.gameDependentParameters.getNumRoles()];

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			jointMove.add(null);
		}

		// Get the index of myRole.
		int roleIndex = this.gameDependentParameters.getMyRoleIndex();

		// Get the moves for myRole.
		SequentialMctsMoveStats[] movesStats = currentNode.getMovesStats();

		double maxMoveValue;
		double[] moveValues;
		int parentVisits = currentNode.getTotVisits()[roleIndex];

		while(movesStats != null){

			// Compute the value for all moves.
			maxMoveValue = -1;
			moveValues = new double[movesStats.length];

			for(int i = 0; i < movesStats.length; i++){
				// Compute the move value.
				moveValues[i] = this.moveEvaluator.computeMoveValue(currentNode, currentNode.getAllLegalMoves().get(roleIndex).get(i), roleIndex, movesStats[i], parentVisits);

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

			// Get the number of visits of the selected move
			parentVisits = movesStats[movesIndices[roleIndex]].getVisits();

			// Get the move statistics of the next role, given the selected move.
			movesStats = movesStats[movesIndices[roleIndex]].getNextRoleMovesStats();

			// Compute the index for the next role
			roleIndex = (roleIndex+1)%this.gameDependentParameters.getNumRoles();

		}

		return new SeqDecMctsJointMove(jointMove, movesIndices);

	}

	@Override
	public String getComponentParameters(String indentation){

		return indentation + "VALUE_OFFSET = " + this.valueOffset + indentation + "MOVE_EVALUATOR = " + this.moveEvaluator.printComponent(indentation + "  ");
	}

}
