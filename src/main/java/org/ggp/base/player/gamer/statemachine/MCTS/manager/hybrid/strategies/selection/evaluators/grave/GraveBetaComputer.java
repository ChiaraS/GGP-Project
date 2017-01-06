package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class GraveBetaComputer extends BetaComputer {

	private double[] bias;

	private double initialBias;

	private double[] valuesForBias;

	public GraveBetaComputer(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.bias = null;

		this.initialBias = Double.parseDouble(gamerSettings.getPropertyValue("BetaComputer.initialBias"));

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		String toTuneString = gamerSettings.getPropertyValue("BetaComputer.tune");
		boolean toTune = Boolean.parseBoolean(toTuneString);
		if(toTune){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			String[] values = gamerSettings.getPropertyMultiValue("BetaComputer.valuesForBias");
			this.valuesForBias = new double[values.length];
			for(int i = 0; i < values.length; i++){
				this.valuesForBias[i] = Double.parseDouble(values[i]);
			}
			sharedReferencesCollector.addComponentToTune(this);
		}else{
			this.valuesForBias = null;
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.bias = null;
	}

	@Override
	public void setUpComponent(){

		this.bias = new double[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.bias[i] = initialBias;
		}

	}

	@Override
	public double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats,
			int nodeVisits, int roleIndex) {

		if(theAmafMoveStats == null){
			return -1.0;
		}

		double amafVisits = theAmafMoveStats.getVisits();
		double moveVisits = theMoveStats.getVisits();

		return (amafVisits / (amafVisits + moveVisits + (this.bias[roleIndex] * amafVisits * moveVisits)));
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "INITIAL_BIAS = " + this.initialBias;

		if(this.valuesForBias != null){
			String valuesForBiasString = "[ ";

			for(int i = 0; i < this.valuesForBias.length; i++){

				valuesForBiasString += this.valuesForBias[i] + " ";

			}

			valuesForBiasString += "]";

			params += indentation + "VALUES_FOR_TUNING_BIAS = " + valuesForBiasString;
		}else{
			params += indentation + "VALUES_FOR_TUNING_BIAS = null";
		}

		if(this.bias != null){
			String biasString = "[ ";

			for(int i = 0; i < this.bias.length; i++){

				biasString += this.bias[i] + " ";

			}

			biasString += "]";

			params += indentation + "bias = " + biasString;
		}else{
			params += indentation + "bias = null";
		}

		return params;

	}

	@Override
	public void setNewValues(double[] newValues) {

		// We are tuning only the constant of myRole
		if(newValues.length == 1){

			// TODO: fix this not-so-nice casting
			this.bias[this.gameDependentParameters.getMyRoleIndex()] = newValues[0];

			//System.out.println("C = " + this.c[this.gameDependentParameters.getMyRoleIndex()]);

		}else{ // We are tuning all constants
			for(int i = 0; i <this.bias.length; i++){

				// TODO: fix this not-so-nice casting
				this.bias[i] = (int) newValues[i];
			}
		}

	}

	@Override
	public double[] getPossibleValues() {
		return this.valuesForBias;
	}

	@Override
	public void setNewValuesFromIndices(int[] newValuesIndices) {
		// We are tuning only the parameter of myRole
		if(newValuesIndices.length == 1){

			this.bias[this.gameDependentParameters.getMyRoleIndex()] = this.valuesForBias[newValuesIndices[0]];

		}else{ // We are tuning all parameters
			for(int i = 0; i <this.bias.length; i++){
				this.bias[i] = this.valuesForBias[newValuesIndices[i]];
			}
		}
	}

}
