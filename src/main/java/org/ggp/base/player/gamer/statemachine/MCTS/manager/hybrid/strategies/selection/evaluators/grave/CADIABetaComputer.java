package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class CADIABetaComputer extends BetaComputer{

	/**
	 * Equivalence parameter: number of node visits needed to consider as equal
	 * the UCT value and the AMAF value of a move. It's an array because it's
	 * possible to use a different value for each role.
	 */
	private int[] k;

	public CADIABetaComputer(int initialK, int numRoles, int myRoleIndex) {

		super(myRoleIndex);

		this.k = new int[numRoles];

		for(int i = 0; i < numRoles; i++){
			this.k[i] = initialK;
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
	public String getBetaComputerParameters() {

		String roleParams = "[ ";

		for(int i = 0; i <this.k.length; i++){

			roleParams += this.k[i] + " ";

		}

		roleParams += "]";

		return "EQUIVALENCE_PARAMETERS = " + roleParams;

	}

	@Override
	public void setNewValues(double[] newValues){

		// We are tuning only the constant of myRole
		if(newValues.length == 1){

			// TODO: fix this not-so-nice casting
			this.k[this.myRoleIndex] = (int) newValues[0];

			//System.out.println("C = " + this.c[this.myRoleIndex]);

		}else{ // We are tuning all constants
			for(int i = 0; i <this.k.length; i++){

				// TODO: fix this not-so-nice casting
				this.k[i] = (int) newValues[i];
			}
		}

		//System.out.println(k);
	}

}
