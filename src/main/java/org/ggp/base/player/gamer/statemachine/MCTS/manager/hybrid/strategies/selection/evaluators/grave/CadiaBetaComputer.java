package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class CadiaBetaComputer extends BetaComputer{

	/**
	 * Equivalence parameter: number of node visits needed to consider as equal
	 * the UCT value and the AMAF value of a move. It's an array because it's
	 * possible to use a different value for each role.
	 */
	private int[] k;

	private int initialK;

	/**
	 * Array with all the values for K that must be used to tune the parameter.
	 * These values will be used for al roles.
	 */
	private double[] valuesForK;

	public CadiaBetaComputer(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.k = null;

		this.initialK = Integer.parseInt(gamerSettings.getPropertyValue("BetaComputer.initialK"));

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		String toTuneString = gamerSettings.getPropertyValue("BetaComputer.tune");
		if(Boolean.parseBoolean(toTuneString)){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			String[] values = gamerSettings.getPropertyMultiValue("BetaComputer.valuesForK");
			this.valuesForK = new double[values.length];
			for(int i = 0; i < values.length; i++){
				this.valuesForK[i] = Integer.parseInt(values[i]);
			}
			sharedReferencesCollector.setTheComponentToTune(this);
		}else{
			this.valuesForK = null;
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.k = null;
	}

	@Override
	public void setUpComponent(){
		this.k = new int[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.k[i] = this.initialK;
		}
	}

	@Override
	public double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats,
			int nodeVisits, int roleIndex) {

		double numerator = k[roleIndex];
		double denominator = ((3*nodeVisits) + this.k[roleIndex]);
		return Math.sqrt(numerator / denominator);
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "INITIAL_K = " + this.initialK;

		if(this.valuesForK != null){
			String valuesForKString = "[ ";

			for(int i = 0; i < this.valuesForK.length; i++){

				valuesForKString += this.valuesForK[i] + " ";

			}

			valuesForKString += "]";

			params += indentation = "VALUES_FOR_TUNING_K = " + valuesForKString;
		}else{
			params += indentation = "VALUES_FOR_TUNING_K = null";
		}

		if(this.k != null){
			String kString = "[ ";

			for(int i = 0; i < this.k.length; i++){

				kString += this.k[i] + " ";

			}

			kString += "]";

			params += indentation = "k = " + kString;
		}else{
			params += indentation = "k = null";
		}

		return params;

	}

	@Override
	public void setNewValues(double[] newValues){

		// We are tuning only the constant of myRole
		if(newValues.length == 1){

			// TODO: fix this not-so-nice casting
			this.k[this.gameDependentParameters.getMyRoleIndex()] = (int) newValues[0];

			//System.out.println("C = " + this.c[this.gameDependentParameters.getMyRoleIndex()]);

		}else{ // We are tuning all constants
			for(int i = 0; i <this.k.length; i++){

				// TODO: fix this not-so-nice casting
				this.k[i] = (int) newValues[i];
			}
		}

		//System.out.println(k);
	}

	@Override
	public double[] getPossibleValues() {
		return this.valuesForK;
	}

}
