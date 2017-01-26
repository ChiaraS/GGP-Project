package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class SequentialTunerBeforeSimulation extends TunerBeforeSimulation {

	public SequentialTunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void beforeSimulationActions() {

		/*
		if(this.simCount == 0){
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

		this.simCount = (this.simCount + 1)%this.batchSize;
		*/

	}

}
