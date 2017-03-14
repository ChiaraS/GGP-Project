package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders.ParametersOrder;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class SimultaneousTunerBeforeSimulation extends TunerBeforeSimulation {

	/**
	 * This ParametersOrder is used to order the parameters right after the creation of a new player
	 * and before such player starts playing any game.
	 */
	private ParametersOrder initialParametersOrder;

	public SimultaneousTunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		try {
			this.initialParametersOrder = (ParametersOrder) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PARAMETERS_ORDER.getConcreteClasses(),
					gamerSettings.getPropertyValue("BeforeSimulationStrategy.initialParametersOrderType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ParametersOrder " + gamerSettings.getPropertyValue("ParameterTuner.initialParametersOrderType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}
	}

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
