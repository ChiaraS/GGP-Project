package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

public class StandardMultiPopEvoParametersTuner extends	MultiPopEvoParametersTuner {

	/**
	 * Problem representation for each role being tuned.
	 */
	private EvoProblemRepresentation[] roleProblems;

	public StandardMultiPopEvoParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		this.roleProblems = null;
	}

	@Override
	public void createRoleProblems(int numRolesToTune){
		// Create the initial population for each role
		this.roleProblems = new EvoProblemRepresentation[numRolesToTune];
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			roleProblems[roleProblemIndex] = new EvoProblemRepresentation(this.evolutionManager.getInitialPopulation());
		}
	}

	@Override
	public void setRoleProblemsToNull() {
		this.roleProblems = null;
	}

	@Override
	public EvoProblemRepresentation[] getRoleProblems() {
		return this.roleProblems;
	}

	@Override
	public void computeAndSetBestCombinations() {

		Move theParametersCombination;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			theParametersCombination = this.roleProblems[roleProblemIndex].getPopulation()[this.bestCombinationSelector.selectMove(this.roleProblems[roleProblemIndex].getPopulation(), null,
					new double[this.roleProblems[roleProblemIndex].getPopulation().length], this.roleProblems[roleProblemIndex].getTotalUpdates())].getTheMove();

			if(theParametersCombination instanceof CombinatorialCompactMove){
				this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			}else{
				GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			}
		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations));

		this.parametersManager.setParametersValues(this.selectedCombinations);

	}


	@Override
	public void updateRoleProblems(List<Integer> individualsIndices, int[] neededRewards) {

		CompleteMoveStats toUpdate;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			this.roleProblems[roleProblemIndex].incrementTotalUpdates();
			toUpdate = this.roleProblems[roleProblemIndex].getPopulation()[individualsIndices.get(roleProblemIndex)];
			toUpdate.incrementScoreSum(neededRewards[roleProblemIndex]);
			toUpdate.incrementVisits();
		}

	}

}
