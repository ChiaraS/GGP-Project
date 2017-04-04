package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.Individual;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.logging.GamerLogger;

public class EvoBeforeSimulation extends BeforeSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private boolean tuneAllRoles;

	private TunableParameter tunableParameter;

	public EvoBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		double evoC = gamerSettings.getDoublePropertyValue("BeforeSimulationStrategy.evoC");
		double evoValueOffset = gamerSettings.getDoublePropertyValue("BeforeSimulationStrategy.evoValueOffset");
		boolean useNormalization = gamerSettings.getBooleanPropertyValue("BeforeSimulationStrategy.useNormalization");

		this.evolutionManager = new SingleParameterEvolutionManager(random, evoC, evoValueOffset, useNormalization);

		sharedReferencesCollector.setSingleParameterEvolutionManager(evolutionManager);

		this.tuneAllRoles = gamerSettings.getBooleanPropertyValue("BeforeSimulationStrategy.tuneAllRoles");

		// Here the evolution manager has no populations!
		// They will be initialized by this class everytime a new game is being played
		// and we know how many roles are in the game.

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		List<TunableParameter> tunableParameters = sharedReferencesCollector.getTheParametersToTune();

		if(tunableParameters != null && tunableParameters.size() == 1){
			this.tunableParameter = tunableParameters.get(0);
		}else{
			GamerLogger.logError("SearchManagerCreation", "There is no single ComponentToTune! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("There is no single ComponentToTune!");
		}

	}

	@Override
	public void clearComponent(){
		// It's not the job of this class to clear the tunable component because the component
		// is for sure either another strategy or part of another strategy. A class must be
		// responsible of clearing only the objects taht it was responsible for creating.
		this.evolutionManager.clear();
	}

	@Override
	public void setUpComponent(){

		int numPossibleValues = this.tunableParameter.getNumPossibleValues();

		//int[] individualsValues = new int[numPossibleValues];

		Individual[][] populations;

		int numPopulations;

		if(this.tuneAllRoles){
			numPopulations = this.gameDependentParameters.getNumRoles();
		}else{
			numPopulations = 1;
		}

		populations = new Individual[numPopulations][];

		for(int i = 0; i < populations.length; i++){

			populations[i] = new Individual[numPossibleValues];

			for(int j = 0; j < populations[i].length; j++){
				populations[i][j] = new Individual();
			}
		}

		this.evolutionManager.setUp(populations);
	}

	@Override
	public void beforeSimulationActions() {

		int[] indices = this.evolutionManager.selectNextIndividualsIndices();

		if(indices.length == 1){
			this.tunableParameter.setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), indices[0]);
		}else{
			this.tunableParameter.setAllRolesNewValues(indices);
		}

	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "TUNE_ALL_ROLES = " + this.tuneAllRoles + indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.printEvolutionManager(indentation + "  ") + indentation + "TUNABLE_PARAMETER = " + this.tunableParameter.getParameters(indentation + "  ");
	}

}
