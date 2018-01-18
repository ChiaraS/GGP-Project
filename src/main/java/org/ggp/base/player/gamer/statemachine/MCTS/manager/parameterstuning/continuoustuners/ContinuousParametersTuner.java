package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.continuoustuners;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousParametersManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ParametersManager;

public abstract class ContinuousParametersTuner extends ParametersTuner {

	protected ContinuousParametersManager continuousParametersManager;

	public ContinuousParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.continuousParametersManager = new ContinuousParametersManager(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		sharedReferencesCollector.setContinuousParametersManager(this.continuousParametersManager);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		super.setReferences(sharedReferencesCollector);
		this.continuousParametersManager.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
		this.continuousParametersManager.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
		this.continuousParametersManager.setUpComponent();
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "CONTINUOUS_PARAMETERS_MANAGER = " + this.continuousParametersManager.printComponent(indentation + "  ");

		String superParams = super.getComponentParameters(indentation);

		if(superParams != null){
			return  params + superParams;
		}else{
			return params;
		}

	}

	protected String getLogOfCombinations(double[][] combinations){

		String globalParamsOrder = this.getGlobalParamsOrder();
		String toLog = "";

		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.gameDependentParameters.getNumRoles(); roleProblemIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex)) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
				if(combinations != null && combinations[roleProblemIndex] != null){
					for(int paramIndex = 0; paramIndex < this.continuousParametersManager.getNumTunableParameters(); paramIndex++){
						toLog += combinations[roleProblemIndex][paramIndex] + " ";
					}
				}else{
					for(int paramIndex = 0; paramIndex < this.continuousParametersManager.getNumTunableParameters(); paramIndex++){
						toLog += null + " ";
					}
				}
				toLog += "];\n";
			}
		}else{ // Tuning only my role
			toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex())) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
			if(combinations != null && combinations[0] != null){
				for(int paramIndex = 0; paramIndex < this.continuousParametersManager.getNumTunableParameters(); paramIndex++){
					toLog += combinations[0][paramIndex] + " ";
				}
			}else{
				for(int paramIndex = 0; paramIndex < this.continuousParametersManager.getNumTunableParameters(); paramIndex++){
					toLog += null + " ";
				}
			}
			toLog += "];\n";
		}

		return toLog;
	}

	@Override
	public ParametersManager getParametersManager() {
		return this.continuousParametersManager;
	}

}
