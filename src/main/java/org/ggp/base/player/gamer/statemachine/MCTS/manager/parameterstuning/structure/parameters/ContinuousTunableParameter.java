package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters;

import java.util.Random;

import org.ggp.base.util.Interval;
import org.ggp.base.util.logging.GamerLogger;

public class ContinuousTunableParameter extends TunableParameter {

	private Interval possibleValuesInterval;

	public ContinuousTunableParameter(Random random, String name, double fixedValue, int tuningOrderIndex, boolean randomizePerCall, Interval possibleValuesInterval) {

		super(random, name, fixedValue, tuningOrderIndex, randomizePerCall);
		this.possibleValuesInterval = possibleValuesInterval;
	}


	/*
	public void setInterval(int lowerBound, int upperBound, boolean isLeftClosed, boolean isRightClosed ) {
		// We are tuning only the parameter of myRole
		this.possibleValuesInterval = new Interval(lowerBound, upperBound, isLeftClosed, isRightClosed);
	}*/

	public Interval getPossibleValuesInterval() {
	    return this.possibleValuesInterval;
    }

	public void setMyRoleNewValue(int myRoleIndex, double newValue) {
		// We are tuning only the parameter of myRole
		if(this.possibleValuesInterval.contains(newValue)) {
			this.currentValues[myRoleIndex] = newValue;
		}else {
			GamerLogger.logError("ParametersTuner", "ContinuousTunableParameter - Trying to set for the parameter " + this.getName() +
					" for my role the value " + newValue + " that is not included in the interval " + this.possibleValuesInterval.toString() + ".");
			throw new RuntimeException("ContinuousTunableParameter - Trying to set for the parameter " + this.getName() +
					" for my role the value " + newValue + " that is not included in the interval " + this.possibleValuesInterval.toString() + ".");
		}
	}

	public void setAllRolesNewValues(double[] newValues) {

		if(newValues.length != this.currentValues.length) {
			GamerLogger.logError("ParametersTuner", "ContinuousTunableParameter - Trying to set " + newValues.length +
					" new values for the parameter " + this.getName() + " when there are " + this.currentValues.length +
					" values expected for the roles instead.");
			throw new RuntimeException("ContinuousTunableParameter - Trying to set " + newValues.length +
					" new values for the parameter " + this.getName() + " when there are " + this.currentValues.length +
					" values expected for the roles instead.");
		}

		// We are tuning for all roles
		for(int i = 0; i < newValues.length; i++){
			if(this.possibleValuesInterval.contains(newValues[i])) {
				this.currentValues[i] = newValues[i];
			}else {
				GamerLogger.logError("ParametersTuner", "ContinuousTunableParameter - Trying to set for the parameter " + this.getName() +
						" for role " + i + " the value " + newValues[i] + " that is not included in the interval " + this.possibleValuesInterval.toString() + ".");
				throw new RuntimeException("ContinuousTunableParameter - Trying to set for the parameter " + this.getName() +
						" for role " + i + " the value " + newValues[i] + " that is not included in the interval " + this.possibleValuesInterval.toString() + ".");
			}
		}
	}

	@Override
	public String getParameters(String indentation) {

		String superParams = super.getParameters(indentation);

		String params = "";

		if(this.possibleValuesInterval != null){
			params += indentation + "POSSIBLE_VALUES_INTERVAL = " + this.possibleValuesInterval;
		}else{
			params += indentation + "POSSIBLE_VALUES = null";
		}

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}


	@Override
	public double getValuePerRole(int roleIndex){
		if(this.randomizePerCall) {
			return this.getRandomValuePerRole();
		}else {
			return this.currentValues[roleIndex];
		}
	}

	private double getRandomValuePerRole() {
		// TODO: implement so that it returns a random value in the interval
		return 0;
	}

}
