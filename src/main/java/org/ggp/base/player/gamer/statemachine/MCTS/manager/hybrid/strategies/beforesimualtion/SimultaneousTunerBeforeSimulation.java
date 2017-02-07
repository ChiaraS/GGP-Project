package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.Collections;
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

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		// Randomize the order of the parameters (needed by the HierarchicalSingleMabTuner)
		// Other SimultaneousTuners are not affected by the different order
		// TODO: temporary solution. Change the code to allow different ways of picking the order
		// for all the tuners and also in between games.
		Collections.shuffle(this.tunableParameters);

		int[] classesLength = new int[this.tunableParameters.size()];

		int i = 0;
		for(TunableParameter p : this.tunableParameters){
			classesLength[i] = p.getPossibleValuesLength();
			i++;
		}

		this.parametersTuner.setClassesLength(classesLength);

	}

	@Override
	public void beforeSimulationActions() {

		if(this.simCountForBatch == 0){
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

		this.simCountForBatch = (this.simCountForBatch + 1)%this.batchSize;

	}

}
