package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.DoubleTunableParameter;

public class GraveBetaComputer extends BetaComputer {

	private DoubleTunableParameter bias;

	public GraveBetaComputer(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		// Get default value for Bias (this is the value used for the roles for which we are not tuning the parameter)
		int fixedBias = gamerSettings.getIntPropertyValue("BetaComputer.fixedBias");

		if(gamerSettings.getBooleanPropertyValue("BetaComputer.tuneBias")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			this.bias = new DoubleTunableParameter(fixedBias, gamerSettings.getDoublePropertyMultiValue("BetaComputer.valuesForBias"));

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.bias);

		}else{
			this.bias = new DoubleTunableParameter(fixedBias);
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.bias.clearParameter();
	}

	@Override
	public void setUpComponent(){
		this.bias.setUpParameter(this.gameDependentParameters.getNumRoles());
	}

	@Override
	public double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats,
			int nodeVisits, int roleIndex) {

		if(theAmafMoveStats == null){
			return -1.0;
		}

		double amafVisits = theAmafMoveStats.getVisits();
		double moveVisits = theMoveStats.getVisits();

		return (amafVisits / (amafVisits + moveVisits + (this.bias.getValuePerRole(roleIndex) * amafVisits * moveVisits)));
	}

	@Override
	public String getComponentParameters(String indentation) {

		return indentation + "BIAS = " + this.bias.getParameters(indentation + "  ");

	}

}
