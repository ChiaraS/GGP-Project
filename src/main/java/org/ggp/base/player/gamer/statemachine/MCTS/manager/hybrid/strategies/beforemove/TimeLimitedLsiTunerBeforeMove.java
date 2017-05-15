package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforemove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.TimeLimitedLsiParametersTuner;
import org.ggp.base.util.logging.GamerLogger;

public class TimeLimitedLsiTunerBeforeMove extends BeforeMoveStrategy {

	private TimeLimitedLsiParametersTuner timeLimitedLsiParametersTuner;

	/**
	 * Safety margin (ms) to be subtracted to the timeout for each game step to increase the probability that
	 * the tuner will finish tuning in time, finding the best combination for the game step.
	 */
	private long tuningSafetyMargin;

	public TimeLimitedLsiTunerBeforeMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tuningSafetyMargin = gamerSettings.getLongPropertyValue("BeforeMoveStrategy.tuningSafetyMargin");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		ParametersTuner parametersTuner = sharedReferencesCollector.getParametersTuner();

		if(parametersTuner instanceof TimeLimitedLsiParametersTuner){
			this.timeLimitedLsiParametersTuner = (TimeLimitedLsiParametersTuner) parametersTuner;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TimeLimitedLsiTunerBeforeMove. Expected tuner of type TimeLimitedLsiParametersTuner, but found tuner of type " + parametersTuner.getClass().getSimpleName() + ".");
			throw new RuntimeException("Error when instantiating TimeLimitedLsiTunerBeforeMove. Expected tuner of type TimeLimitedLsiParametersTuner, but found tuner of type " + parametersTuner.getClass().getSimpleName() + ".");
		}

	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public void beforeMoveActions(long timeout) {
		this.timeLimitedLsiParametersTuner.startTuningForNewStep(timeout-this.tuningSafetyMargin);
	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "TUNING_SAFETY_MARGIN = " + this.tuningSafetyMargin +
				indentation + "PARAMETERS_TUNER = " + this.timeLimitedLsiParametersTuner.getClass().getSimpleName();

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
