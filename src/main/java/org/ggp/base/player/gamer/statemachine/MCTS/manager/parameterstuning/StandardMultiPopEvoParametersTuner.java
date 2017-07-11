package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;

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
