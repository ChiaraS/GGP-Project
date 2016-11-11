package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;

public class GRAVEBetaComputer extends BetaComputer {

	private double[] bias;

	private double initialBias;

	public GRAVEBetaComputer(GameDependentParameters gameDependentParameters, double initialBias) {

		super(gameDependentParameters);

		this.bias = null;
		this.initialBias = initialBias;

	}

	public void clearComponent(){
		this.bias = null;
	}

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
	public String getBetaComputerParameters() {

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
