package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.Individual;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class EvoBeforeSimulation extends BeforeSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	private boolean tuneAllRoles;

	private OnlineTunableComponent tunableComponent;

	public EvoBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector, SingleParameterEvolutionManager evolutionManager, double[] individualsValues, boolean tuneAllRoles, OnlineTunableComponent tunableComponent) {

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		double evoC = Double.parseDouble(gamerConfiguration.getPropertyValue("BeforeSimulationStrategy.evoC"));
		double evoValueOffset = Double.parseDouble(gamerConfiguration.getPropertyValue("BeforeSimulationStrategy.evoValueOffset"));
		boolean useNormalization = Boolean.parseBoolean(gamerConfiguration.getPropertyValue("BeforeSimulationStrategy.useNormalization"));

		this.evolutionManager = new SingleParameterEvolutionManager(random, evoC, evoValueOffset, useNormalization);

		sharedReferencesCollector.setSingleParameterEvolutionManager(evolutionManager);

		this.tuneAllRoles = Boolean.parseBoolean(gamerConfiguration.getPropertyValue("BeforeSimulationStrategy.tuneAllRoles"));

		// Here the evolution manager has no populations!
		// They will be initialized by this class everytime a new game is being played
		// and we know how many roles are in the game.

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.tunableComponent = sharedReferencesCollector.getTheComponentToTune();
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

		double[] individualsValues = this.tunableComponent.getPossibleValues();

		Individual[][] populations;

		int numPopulations;

		if(this.tuneAllRoles){
			numPopulations = this.gameDependentParameters.getNumRoles();
		}else{
			numPopulations = 1;
		}

		populations = new Individual[numPopulations][];

		for(int i = 0; i < populations.length; i++){

			populations[i] = new Individual[individualsValues.length];

			for(int j = 0; j < populations[i].length; j++){
				populations[i][j] = new Individual(individualsValues[j]);
			}
		}

		this.evolutionManager.setUp(populations);
	}

	@Override
	public void beforeSimulationActions() {

		this.tunableComponent.setNewValues(this.evolutionManager.selectNextIndividuals());

	}

	@Override
	public String getComponentParameters() {

		return this.tunableComponent.printOnlineTunableComponent() + ", " + this.evolutionManager.printEvolutionManager();
	}

}
