package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class TunerBeforeSimulation extends BeforeSimulationStrategy {

	/**
	 * Each selected configuration of parameters will be evaluated with a batch of simulations and not
	 * only one simulation. This expresses the size of such batch (i.e. after evaluating batchSize times
	 * a configuration of parameters, a new one is selected with the combinatorial tuner).
	 */
	private int batchSize;

	/**
	 * Counts the number of simulations performed in the current interval.
	 * When this number reaches batchSize then the parameters are updated with a new
	 * configuration that will be evaluated next.
	 */
	private int simCount;

	private ParametersTuner parametersTuner;

	/**
	 * List of the parameters that we are tuning.
	 */
	private List<TunableParameter> tunableParameters;

	public TunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.batchSize = gamerSettings.getIntPropertyValue("BeforeSimulationStrategy.batchSize");

		this.simCount = 0;

		try {
			this.parametersTuner = (ParametersTuner) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PARAMETER_TUNERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("BeforeSimulationStrategy.parameterTunerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ParameterTuner " + gamerSettings.getPropertyValue("ParameterTuner.parameterTunerType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		sharedReferencesCollector.setParametersTuner(parametersTuner);

		// Here the parameter tuner has no classes lengths.
		// They will be initialized when setting references to the tunable components.

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.parametersTuner.setReferences(sharedReferencesCollector);

		this.tunableParameters = sharedReferencesCollector.getTheParametersToTune();

		int[] classesLength = new int[this.tunableParameters.size()];

		int i = 0;
		for(TunableParameter p : this.tunableParameters){
			classesLength[i] = p.getPossibleValuesLength();
			i++;
		}

		this.parametersTuner.setClassesLength(classesLength);

	}

	@Override
	public void clearComponent(){

		this.simCount = 0;

		// It's not the job of this class to clear the tunable component because the component
		// is for sure either another strategy or part of another strategy. A class must be
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

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "BATCH_SIZE = " + this.batchSize +
				indentation + "PARAMETER_TUNER = " + this.parametersTuner.printComponent(indentation + "  ");

		if(this.tunableParameters != null){

			String tunableParametersString = "[ ";

			for(TunableParameter p : this.tunableParameters){

				tunableParametersString += indentation + "  TUNABLE_PARAMETER = " + p.getParameters(indentation + "    ");

			}

			tunableParametersString += "\n]";

			params += indentation + "TUNABLE_PARAMETERS = " + tunableParametersString;
		}else{
			params += indentation + "TUNABLE_PARAMETERS = null";
		}

		params += indentation + "SIM_COUNT = " + this.simCount;

		return params;

	}

}
