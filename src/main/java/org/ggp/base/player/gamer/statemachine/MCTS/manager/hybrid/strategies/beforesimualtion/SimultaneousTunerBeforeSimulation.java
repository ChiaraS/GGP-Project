package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;

public class SimultaneousTunerBeforeSimulation extends TunerBeforeSimulation {



	public SimultaneousTunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);


	}

	/*
	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.initialParametersOrder.imposeOrder(this.tunableParameters);

		String[] classesNames = new String[this.tunableParameters.size()];
		int[] classesLength = new int[this.tunableParameters.size()];
		String[][] classesValues = new String[this.tunableParameters.size()][];

		// The penalty for the parameter values must be either specified for all values of all parameters or for none of them.
		// This means that if it's specified for all values of the first parameter we expect it to be specified for all values
		// of other parameters, too.
		boolean usingPenalty = this.tunableParameters.get(0).getPossibleValuesPenalty() != null && this.tunableParameters.get(0).getPossibleValuesPenalty().length > 0;

		double[][] unitMovesPenalty = null;
		if(usingPenalty){
			unitMovesPenalty = new double[this.tunableParameters.size()][];
		}

		int i = 0;
		for(TunableParameter p : this.tunableParameters){
			classesNames[i] = p.getName();
			classesLength[i] = p.getPossibleValuesLength();
			classesValues[i] = p.getPossibleValues();
			if(classesLength[i] == 0){
				GamerLogger.logError("SearchManagerCreation", "SimultaneousTunerBeforeSimulation - Initialization with empty list of possible values for a parameter set to be tuned!");
				throw new RuntimeException("SimultaneousTunerBeforeSimulation - Initialization with empty list of possible values for a parameter set to be tuned!");
			}
			// Get the penalty values (if expected)
			// If expected but not specified or if not expected but specified, throw exception
			if(usingPenalty^(this.tunableParameters.get(i).getPossibleValuesPenalty() != null && this.tunableParameters.get(i).getPossibleValuesPenalty().length > 0)){
				GamerLogger.logError("SearchManagerCreation", "SimultaneousTunerBeforeSimulation - Parameters values penalty is specified only for some tunable parameters!");
				throw new RuntimeException("SimultaneousTunerBeforeSimulation - Parameters values penalty is specified only for some tunable parameters!");
			}else if(usingPenalty){ // Get penalty
				unitMovesPenalty[i] = this.tunableParameters.get(i).getPossibleValuesPenalty();

				// The penalty array must have same length of the possible values array
				if(unitMovesPenalty[i].length != classesLength[i]){
					GamerLogger.logError("SearchManagerCreation", "SimultaneousTunerBeforeSimulation - Parameters values penalty is specified only for some of the possible values of one of the tunable parameters!");
					throw new RuntimeException("SimultaneousTunerBeforeSimulation - Parameters values penalty is specified only for some of the possible values of one of the tunable parameter!");
				}
			}
			i++;
		}

		this.parametersTuner.setClassesAndPenalty(classesNames, classesLength, classesValues, unitMovesPenalty);

	}*/

	@Override
	public void beforeSimulationActions() {

		// We reached the num of simulations available for tuning so we commit to a single configuration of parameters.
		if(this.simCount == this.simBudget){

			int[][] bestCombinations = this.parametersTuner.getBestCombinations();

			int i = 0;

			// If we are tuning only for my role...
			if(bestCombinations.length == 1){
				for(TunableParameter p : this.tunableParameters){
					p.setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), bestCombinations[0][i]);
					i++;
				}
			}else{ //If we are tuning for all roles...

				int[] newValuesIndices;

				for(TunableParameter p : this.tunableParameters){

					//System.out.print(c.getClass().getSimpleName() + ": [ ");

					newValuesIndices = new int[bestCombinations.length]; // nextCombinations.length equals the number of roles for which we are tuning

					for(int j = 0; j < newValuesIndices.length; j++){
						newValuesIndices[j] = bestCombinations[j][i];
						//System.out.print(newValuesIndices[j] + " ");
					}

					//System.out.println("]");

					p.setAllRolesNewValues(newValuesIndices);

					i++;
				}
			}

			this.parametersTuner.stopTuning();

		}else if(this.simCount < this.simBudget && this.simCount%this.batchSize == 0){
			// We still have simulations left to tune parameters and we finished performing the batch of simulations
			// for the current configuration of parameters.
			int[][] nextCombinations = this.parametersTuner.selectNextCombinations();

			int i = 0;

			// If we are tuning only for my role...
			if(nextCombinations.length == 1){
				for(TunableParameter p : this.tunableParameters){
					p.setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), nextCombinations[0][i]);
					i++;
				}
			}else{ //If we are tuning for all roles...

				int[] newValuesIndices;

				for(TunableParameter p : this.tunableParameters){

					//System.out.print(c.getClass().getSimpleName() + ": [ ");

					newValuesIndices = new int[nextCombinations.length]; // nextCombinations.length equals the number of roles for which we are tuning

					for(int j = 0; j < newValuesIndices.length; j++){
						newValuesIndices[j] = nextCombinations[j][i];
						//System.out.print(newValuesIndices[j] + " ");
					}

					//System.out.println("]");

					p.setAllRolesNewValues(newValuesIndices);

					i++;
				}
			}
		}

		this.simCount++;

	}

}
