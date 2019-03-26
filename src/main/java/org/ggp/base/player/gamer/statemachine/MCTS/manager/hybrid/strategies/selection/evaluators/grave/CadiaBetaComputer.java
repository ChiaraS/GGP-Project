package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;

public class CadiaBetaComputer extends BetaComputer{

	/**
	 * Equivalence parameter: number of node visits needed to consider as equal
	 * the UCT value and the AMAF value of a move. It memorizes a value for each
	 * role in case we want different values for each role.
	 */
	private TunableParameter k;

	public CadiaBetaComputer(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.k = this.createTunableParameter("BetaComputer", "K", gamerSettings, sharedReferencesCollector);

		/*
		// Get default value for K (this is the value used for the roles for which we are not tuning the parameter)
		int fixedK = gamerSettings.getIntPropertyValue("BetaComputer.fixedK");

		if(gamerSettings.getBooleanPropertyValue("BetaComputer.tuneK")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces.
			// We also need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters.
			// Moreover, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			int[] possibleValues = gamerSettings.getIntPropertyMultiValue("BetaComputer.valuesForK");
			double[] possibleValuesPenalty = null;
			if(gamerSettings.specifiesProperty("BetaComputer.possibleValuesPenaltyForK")){
				possibleValuesPenalty =  gamerSettings.getDoublePropertyMultiValue("BetaComputer.possibleValuesPenaltyForK");
			}
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty("BetaComputer.tuningOrderIndexK")){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue("BetaComputer.tuningOrderIndexK");
			}

			this.k = new IntTunableParameter("K", fixedK, possibleValues, possibleValuesPenalty, tuningOrderIndex);

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.k);

		}else{
			this.k = new IntTunableParameter("K", fixedK);
		}*/

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.k.clearParameter();
	}

	@Override
	public void setUpComponent(){
		this.k.setUpParameter(this.gameDependentParameters.getNumRoles());
	}

	@Override
	public double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats,
			int nodeVisits, int roleIndex) {

		double kValuePerRole = this.k.getValuePerRole(roleIndex);

		//System.out.println("Role=" + roleIndex + "K=" + kValuePerRole);

		if(kValuePerRole == 0){
			return 0.0;
			// ATTENTION: here we consider 10^10 to be a value for K that's high enough to be equivalent to +infinity, so
			// whenever K >= 10^10 we return the value 1 for beta. This is a good approximation if the maximum number of
			// visits a node can reach is at least about 3 orders of magnitude smaller than 10^10 (i.e. 10^7). Increase this
			// value if you estimate that a node can reach an amount of visits higher than 10^7.
			// This trick is needed to run CMA-ES tuning so we can consider for K the interval of possible value as [0, 10^10]
			// instead of [0, Double.MAX_VALUE]. In this way, when we perform a rescaling that maps [0,1] to [0, 10^10] there
			// won't be issues when the value to be rescaled is >1. Such value will be mapped in a value greater that the right
			// extreme of the interval (i.e. 10^10) but still representable as a double. This wouldn't be possible if the right
			// extreme was Double.MAX_VALUE. This trick allows us to have no troubles with rescaling and at the same time obtain
			// the behavior of the GRAVE strategy that we will have when K=+infinity (because if the number of visits of a node
			// cannot exceed 10^7, the value of beta will be very close to 1 for any value greater than 10^10, as it is exaclty
			// 1 for K=+infinity).
		}else if(kValuePerRole >= Math.pow(10.0, 10.0)){
			return 1.0;
		}

		double numerator = kValuePerRole;
		double denominator = ((3*nodeVisits) + kValuePerRole);
		return Math.sqrt(numerator / denominator);
	}

	@Override
	public String getComponentParameters(String indentation) {

		return indentation + "K = " + this.k.getParameters(indentation + "  ");

	}

	/*
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

		double[] possibleValues = new double[this.valuesForK.length];

		for(int i = 0; i <this.valuesForK.length; i++){
			possibleValues[i] = this.valuesForK[i];
		}
		return possibleValues;
	}

	@Override
	public void setNewValuesFromIndices(int[] newValuesIndices) {
		// We are tuning only the parameter of myRole
		if(newValuesIndices.length == 1){

			this.k[this.gameDependentParameters.getMyRoleIndex()] = this.valuesForK[newValuesIndices[0]];

		}else{ // We are tuning all parameters
			for(int i = 0; i < this.k.length; i++){
				this.k[i] = this.valuesForK[newValuesIndices[i]];
			}
		}

	}
	*/

}
