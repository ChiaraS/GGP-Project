package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.logging.GamerLogger;

public class PredefinedOrder extends ParametersOrder {

	public PredefinedOrder(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings,
				sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This algorithm takes an unordered array of parameters and orders them according to their
	 * tuning order. It requires all parameters to have a tuning order attribute, the tuning order
	 * must be unique for each parameter and must be a value included in the interval
	 * [0, tunableParameters.size()).
	 */
	@Override
	public void imposeInitialOrderForPlayer(List<TunableParameter> tunableParameters) {

		TunableParameter tmp;
		int tuningOrder;

		for(int i = 0; i < tunableParameters.size(); i++){

			// We must move the parameter only if it is in the wrong position
			if(tunableParameters.get(i).getTuningOrder() != i){

				// Check if the tuning order is outside of the interval [0, tunableParameters.size())
				if(tunableParameters.get(i).getTuningOrder() >= 0 && tunableParameters.get(i).getTuningOrder() < tunableParameters.size()){

					tmp = tunableParameters.set(i, null);

					while(tmp != null){

						tuningOrder = tmp.getTuningOrder();
						tmp = tunableParameters.set(tuningOrder, tmp);

						if(tmp != null && tmp.getTuningOrder() == tuningOrder){
							GamerLogger.logError("SearchManagerCreation", "Error when imposing initial order of tunable parameters for the player. Detected at least two parameters having the same index for the order.");
							throw new RuntimeException("Error when imposing initial order of tunable parameters for the player.");
						}
					}

				}else{
					GamerLogger.logError("SearchManagerCreation", "Error when imposing initial order of tunable parameters for the player. The order specified for a parameter is not in the interval [0, tunableParameters.size()).");
					throw new RuntimeException("Error when imposing initial order of tunable parameters for the player.");
				}
			}
		}

	}

	@Override
	public void setReferences(
			SharedReferencesCollector sharedReferencesCollector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getComponentParameters(String indentation) {
		// TODO Auto-generated method stub
		return null;
	}

}
