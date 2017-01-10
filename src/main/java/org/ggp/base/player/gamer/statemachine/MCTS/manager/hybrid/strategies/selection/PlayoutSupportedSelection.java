package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SequDecMctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This selection uses the given selection strategy to select a move, but for nodes that have less
 * than a given number of simulations 't', it falls back to the playout strategy currently used by
 * the MCTS manager.
 *
 * @author C.Sironi
 *
 */
public class PlayoutSupportedSelection extends SelectionStrategy {

	private SelectionStrategy selectionStrategy;

	private PlayoutStrategy playoutStrategy;

	/**
	 * Minimum number of visits that a node must have to use the selection strategy to pick a move.
	 * If the number of visits is inferior to this threshold, the playout strategy will be used instead.
	 */
	private int t;

	public PlayoutSupportedSelection(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		String propertyValue = gamerSettings.getPropertyValue("SelectionStrategy.subSelectionStrategyType");
		try {
			this.selectionStrategy = (SelectionStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(ProjectSearcher.SELECTION_STRATEGIES.getConcreteClasses(),
					propertyValue).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating (Sub)SelectionStrategy " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.t = gamerSettings.getIntPropertyValue("SelectionStrategy.t");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.playoutStrategy = sharedReferencesCollector.getPlayoutStrategy();

	}

	@Override
	public void clearComponent() {
		this.selectionStrategy.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.selectionStrategy.setUpComponent();
	}

	@Override
	public MctsJointMove select(MctsNode currentNode, MachineState state) {
		if(currentNode.getTotVisits() >= t){
			return this.selectionStrategy.select(currentNode, state);
		}else{
			List<Move> jointMove = this.playoutStrategy.getJointMove(state);

			// The playout returns a plain move. We must find the indices of the move in the tree node.
			if(currentNode instanceof DecoupledMctsNode){
				return this.decCreateMove((DecoupledMctsNode) currentNode, jointMove);
			}else if(currentNode instanceof SequentialMctsNode){
				return this.seqCreateMove((SequentialMctsNode) currentNode, jointMove);
			}else{
				throw new RuntimeException("PlayoutSupportedSelection-select(): detected a node of a non-recognizable sub-type of class MctsNode.");
			}
		}
	}

	private MctsJointMove decCreateMove(DecoupledMctsNode currentNode, List<Move> jointMove){

		DecoupledMctsMoveStats[][] moves = currentNode.getMoves();

		// Also here we can assume that the moves will be non-null since the code takes care
		// of only passing to this method the nodes that have all the information needed for
		// selection.

		int[] movesIndices = new int[moves.length];

		// For each role look for the index of the selected move.
		for(int i = 0; i < moves.length; i++){

			// For each legal move check if it is the selected one.
			for(int j = 0; j < moves[i].length; j++){

				if(jointMove.get(i).equals(moves[i][j].getTheMove())){
					movesIndices[i] = j;
				}
			}

		}

		return new SequDecMctsJointMove(jointMove, movesIndices);

	}

	private MctsJointMove seqCreateMove(SequentialMctsNode currentNode, List<Move> jointMove){

		int[] movesIndices = new int[this.gameDependentParameters.getNumRoles()];

		// Get all the moves for the roles
		List<List<Move>> allMoves = currentNode.getAllLegalMoves();

		// For each role look for the index of the selected move.
		for(int i = 0; i < allMoves.size(); i++){

			// For each legal move check if it is the selected one.
			for(int j = 0; j < allMoves.get(i).size(); j++){

				if(jointMove.get(i).equals(allMoves.get(i).get(j))){
					movesIndices[i] = j;
				}
			}

		}

		return new SequDecMctsJointMove(jointMove, movesIndices);

	}


	@Override
	public String getComponentParameters(String indentation) {

		return indentation + "T = " + this.t +
				indentation + "SUB_SELECTION = " + this.selectionStrategy.printComponent(indentation + "  ") +
				indentation + "SUB_PLAYOUT = " + this.playoutStrategy.getClass().getName();

	}

}
