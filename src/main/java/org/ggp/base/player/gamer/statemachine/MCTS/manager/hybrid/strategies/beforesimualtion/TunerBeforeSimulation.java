package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.SimLimitedLsiParametersTuner;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class TunerBeforeSimulation extends BeforeSimulationStrategy {

	/**
	 * Number of simulations that are available to tune parameters.
	 * Once this number has been reached, the tuning will stop and we will
	 * commit to a single combination of parameters.
	 */
	protected int simBudget;

	/**
	 * Each selected configuration of parameters will be evaluated with a batch of simulations and not
	 * only one simulation. This expresses the size of such batch (i.e. after evaluating batchSize times
	 * a configuration of parameters, a new one is selected with the combinatorial tuner).
	 */
	protected int batchSize;

	/**
	 * Counts the number of simulations performed so far
	 * Used to know when batchSize samples have been taken for the current configuration of parameters
	 * and thus a new one should be retrieved to test, and to know when it's time to stop tuning and
	 * commit to a certain combination of parameters.
	 */
	protected int simCount;

	protected ParametersTuner parametersTuner;

	/**
	 * List of the parameters that we are tuning.
	 */
	//protected List<TunableParameter> tunableParameters;

	public TunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		if(gamerSettings.specifiesProperty("BeforeSimulationStrategy.simBudget")){
			this.simBudget = gamerSettings.getIntPropertyValue("BeforeSimulationStrategy.simBudget");
		}else{
			this.simBudget = Integer.MAX_VALUE;
		}

		if(gamerSettings.specifiesProperty("BeforeSimulationStrategy.batchSize")){
			this.batchSize = gamerSettings.getIntPropertyValue("BeforeSimulationStrategy.batchSize");
		}else{
			this.batchSize = 1;
		}

		this.simCount = 0;

		try {
			this.parametersTuner = (ParametersTuner) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PARAMETERS_TUNERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("BeforeSimulationStrategy.parametersTunerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ParameterTuner " + gamerSettings.getPropertyValue("BeforeSimulationStrategy.parametersTunerType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		sharedReferencesCollector.setParametersTuner(parametersTuner);

		// Here the parameter tuner has no parameters manager.
		// It will be initialized when setting references to the tunable components.

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.parametersTuner.setReferences(sharedReferencesCollector);

		/*
		this.tunableParameters = sharedReferencesCollector.getTheParametersToTune();

		if(this.tunableParameters == null || this.tunableParameters.size() == 0){
			GamerLogger.logError("SearchManagerCreation", "TunerBeforeSimulation - Initialization with null or empty list of tunable parameters!");
			throw new RuntimeException("ParametersTuner - Initialization with null or empty list of tunable parameters!");
		}*/

	}

	@Override
	public void clearComponent(){

		this.simCount = 0;

		// It's not the job of this class to clear the tunable component because the component
		// is for sure part of another strategy. A class must be
		// responsible of clearing only the objects that it was responsible for creating.
		this.parametersTuner.clearComponent();
	}

	@Override
	public void setUpComponent(){

		this.parametersTuner.setUpComponent();

		this.simCount = 0;

	}

	@Override
	public void beforeSimulationActions() {

		if(this.parametersTuner.isTuning()){

			if(this.parametersTuner.isMemorizingBestCombo() || this.simCount == this.simBudget){
				// We are memorizing the best combo from the previous game or we reached the num of
				// simulations available for tuning so we commit to a single configuration of parameters.
				this.parametersTuner.setBestCombinations();

				/*
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
				}*/

				//this.parametersTuner.stopTuning();

			}else if(this.simCount < this.simBudget && this.simCount%this.batchSize == 0){
				// We still have simulations left to tune parameters and we finished performing the batch of simulations
				// for the current configuration of parameters.
				this.parametersTuner.setNextCombinations();

			}

			this.simCount++;
		}else if(this.parametersTuner instanceof SimLimitedLsiParametersTuner) {
			((SimLimitedLsiParametersTuner)this.parametersTuner).increaseSampledCombos();
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "SIM_BUDGET = " + this.simBudget +
				indentation + "BATCH_SIZE = " + this.batchSize +
				indentation + "PARAMETERS_TUNER = " + this.parametersTuner.printComponent(indentation + "  ") +
				indentation + "sim_count = " + this.simCount;

		/*
		if(this.tunableParameters != null){

			String tunableParametersString = "[ ";

			for(TunableParameter p : this.tunableParameters){

				tunableParametersString += indentation + "  TUNABLE_PARAMETER = " + p.getParameters(indentation + "    ");

			}

			tunableParametersString += "\n]";

			params += indentation + "TUNABLE_PARAMETERS = " + tunableParametersString;
		}else{
			params += indentation + "TUNABLE_PARAMETERS = null";
		}*/

		return params;

	}

}
