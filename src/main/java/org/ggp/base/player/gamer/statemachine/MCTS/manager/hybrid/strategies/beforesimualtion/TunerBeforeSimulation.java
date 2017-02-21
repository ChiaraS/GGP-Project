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

public abstract class TunerBeforeSimulation extends BeforeSimulationStrategy {

	/**
	 * Each selected configuration of parameters will be evaluated with a batch of simulations and not
	 * only one simulation. This expresses the size of such batch (i.e. after evaluating batchSize times
	 * a configuration of parameters, a new one is selected with the combinatorial tuner).
	 */
	protected int batchSize;

	/**
	 * Counts the number of simulations performed in the current interval.
	 * When this number reaches batchSize then the parameters are updated with a new
	 * configuration that will be evaluated next.
	 */
	protected int simCountForBatch;

	protected ParametersTuner parametersTuner;

	/**
	 * List of the parameters that we are tuning.
	 */
	protected List<TunableParameter> tunableParameters;

	public TunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.batchSize = gamerSettings.getIntPropertyValue("BeforeSimulationStrategy.batchSize");

		this.simCountForBatch = 0;

		try {
			this.parametersTuner = (ParametersTuner) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PARAMETERS_TUNERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("BeforeSimulationStrategy.parameterTunerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ParameterTuner " + gamerSettings.getPropertyValue("BeforeSimulationStrategy.parameterTunerType") + ".");
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

		if(this.tunableParameters == null || this.tunableParameters.size() == 0){
			GamerLogger.logError("SearchManagerCreation", "TunerBeforeSimulation - Initialization with null or empty list of tunable parameters!");
			throw new RuntimeException("ParametersTuner - Initialization with null or empty list of tunable parameters!");
		}

	}

	@Override
	public void clearComponent(){

		this.simCountForBatch = 0;

		// It's not the job of this class to clear the tunable component because the component
		// is for sure either another strategy or part of another strategy. A class must be
		// responsible of clearing only the objects that it was responsible for creating.
		this.parametersTuner.clearComponent();
	}

	@Override
	public void setUpComponent(){

		this.parametersTuner.setUpComponent();

		this.simCountForBatch = 0;

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

		params += indentation + "sim_count_for_batch = " + this.simCountForBatch;

		return params;

	}

}
