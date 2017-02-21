package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.biascomputers;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.logging.GamerLogger;

public class ProgressiveBias extends BiasComputer {

	/**
	 * Weight of the bias.
	 */
	private double w;

	/**
	 * Values that controls how fast the bias decreases when the penalty value increases.
	 */
	private double decreaseFactor;

	public ProgressiveBias(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.w = gamerSettings.getDoublePropertyValue("BiasComputer" + id + ".w");
		this.decreaseFactor = gamerSettings.getDoublePropertyValue("BiasComputer" + id + ".decreaseFactor");

	}


	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
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
	public double computeMoveBias(double moveNormalizedAvgValue, double moveVisits, double penalty){

		// For now we assume that penalty has to be non-negative. A negative value means that there
		// is something wrong in the code or in the settings for the gamer.
		if(penalty < 0){
			GamerLogger.logError("MctsManager", "Cannot compute Progressive Bias with negative penalty value.");
			throw new RuntimeException("Cannot compute Progressive Bias with negative penalty value.");
		}

		return ((1/(1+Math.pow(penalty, this.decreaseFactor))) * (this.w/((1-moveNormalizedAvgValue)*moveVisits+1)));
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "W = " + this.w +
				indentation + "DECREASE_FACTOR = " + this.decreaseFactor;
	}

}
