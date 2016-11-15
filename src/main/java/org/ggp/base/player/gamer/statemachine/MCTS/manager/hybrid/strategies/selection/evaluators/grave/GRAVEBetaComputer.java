package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class GRAVEBetaComputer extends BetaComputer {

	private double[] bias;

	private double initialBias;

	public GRAVEBetaComputer(GameDependentParameters gameDependentParameters, Random random,
			Properties properties, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.bias = null;

		this.initialBias = Double.parseDouble(properties.getProperty("BetaComputer.initialBias"));

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		String toTuneString = properties.getProperty("BetaComputer.tune");
		if(toTuneString != null){
			boolean toTune = Boolean.parseBoolean(toTuneString);
			if(toTune){
				sharedReferencesCollector.setTheComponentToTune(this);
			}
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
	public String getComponentParameters() {

		String roleParams = "[ ";

		for(int i = 0; i <this.bias.length; i++){

			roleParams += this.bias[i] + " ";

		}

		roleParams += "]";

		return "BIASES = " + roleParams;

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

}
