package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SeqDecMctsJointMove;
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
	private TunableParameter t;

	public PlayoutSupportedSelection(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		String propertyValue = gamerSettings.getPropertyValue("SelectionStrategy.subSelectionStrategyType");
		try {
			this.selectionStrategy = (SelectionStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.SELECTION_STRATEGIES.getConcreteClasses(),
					propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating (Sub)SelectionStrategy " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.t = this.createTunableParameter("SelectionStrategy", "T", gamerSettings, sharedReferencesCollector);

		/*
		// Get default value for T (this is the value used for the roles for which we are not tuning the parameter)
		int fixedT = gamerSettings.getIntPropertyValue("SelectionStrategy.fixedT");

		if(gamerSettings.getBooleanPropertyValue("SelectionStrategy.tuneT")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// SelectionStrategy.valuesForT=v1;v2;...;vn
			// The values are listed separated by ; with no spaces.
			// We also need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters.
			// Moreover, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			int[] possibleValues = gamerSettings.getIntPropertyMultiValue("SelectionStrategy.valuesForT");
			double[] possibleValuesPenalty = null;
			if(gamerSettings.specifiesProperty("SelectionStrategy.possibleValuesPenaltyForT")){
				possibleValuesPenalty =  gamerSettings.getDoublePropertyMultiValue("SelectionStrategy.possibleValuesPenaltyForT");
			}
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty("SelectionStrategy.tuningOrderIndexT")){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue("SelectionStrategy.tuningOrderIndexT");
			}

			this.t = new IntTunableParameter("T", fixedT, possibleValues, possibleValuesPenalty, tuningOrderIndex);

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.t);

		}else{
			this.t = new IntTunableParameter("T", fixedT);
		}
		*/

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.playoutStrategy = sharedReferencesCollector.getPlayoutStrategy();

	}

	@Override
	public void clearComponent() {
		this.selectionStrategy.clearComponent();
		this.t.clearParameter();
	}

	@Override
	public void setUpComponent() {
		this.selectionStrategy.setUpComponent();
		this.t.setUpParameter(this.gameDependentParameters.getNumRoles());
	}

	@Override
	public MctsJointMove select(MctsNode currentNode, MachineState state) {

		List<Move> selectedMove = new ArrayList<Move>(this.gameDependentParameters.getNumRoles());
		int[] movesIndices = new int[this.gameDependentParameters.getNumRoles()];
		MctsMove move;

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){

			move = this.selectPerRole(currentNode, state, i);

			selectedMove.add(move.getMove());

			movesIndices[i] = move.getMovesIndex();

		}

		return new SeqDecMctsJointMove(selectedMove, movesIndices);
	}

	@Override
	public MctsMove selectPerRole(MctsNode currentNode, MachineState state, int roleIndex) {
		if(currentNode.getTotVisits()[roleIndex] >= this.t.getValuePerRole(roleIndex)){
			return this.selectionStrategy.selectPerRole(currentNode, state, roleIndex);
		}else{
			Move move = this.playoutStrategy.getMoveForRole(state, roleIndex);

			// The playout returns a plain move. We must find the indices of the move in the tree node.
			if(currentNode instanceof DecoupledMctsNode){
				return this.decCreateMove((DecoupledMctsNode) currentNode, move, roleIndex);
			}else if(currentNode instanceof SequentialMctsNode){
				return this.seqCreateMove((SequentialMctsNode) currentNode, move, roleIndex);
			}else{
				throw new RuntimeException("PlayoutSupportedSelection-select(): detected a node of a non-recognizable sub-type of class MctsNode.");
			}
		}
	}

	@Override
	public void preSelectionActions(MctsNode currentNode) {
		this.selectionStrategy.preSelectionActions(currentNode);
	}

	private MctsMove decCreateMove(DecoupledMctsNode currentNode, Move move, int roleIndex){

		DecoupledMctsMoveStats[] moves = currentNode.getMoves()[roleIndex];

		// Also here we can assume that the moves will be non-null since the code takes care
		// of only passing to this method the nodes that have all the information needed for
		// selection.

		int moveIndex = -1;

		// For each legal move check if it is the selected one.
		for(int i = 0; i < moves.length; i++){

			if(move.equals(moves[i].getTheMove())){
					moveIndex = i;
					break;
			}

		}

		return new MctsMove(move, moveIndex);

	}

	private MctsMove seqCreateMove(SequentialMctsNode currentNode, Move move, int roleIndex){

		int moveIndex = -1;

		// Get all the moves for the roles
		List<Move> moves = currentNode.getAllLegalMoves().get(roleIndex);

		// For each legal move check if it is the selected one.
		for(int i = 0; i < moves.size(); i++){

			if(move.equals(moves.get(i))){
				moveIndex = i;
				break;
			}

		}

		return new MctsMove(move, moveIndex);

	}


	@Override
	public String getComponentParameters(String indentation) {

		return indentation + "T = " + this.t.getParameters(indentation + "  ") +
				indentation + "SUB_SELECTION = " + this.selectionStrategy.printComponent(indentation + "  ") +
				indentation + "SUB_PLAYOUT = " + this.playoutStrategy.getClass().getSimpleName();

	}

}
