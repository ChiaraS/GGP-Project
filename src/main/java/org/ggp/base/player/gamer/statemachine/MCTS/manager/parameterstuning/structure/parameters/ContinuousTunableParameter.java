package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters;

import org.ggp.base.util.Interval;

public class ContinuousTunableParameter extends TunableParameter {

	private Interval possibleValuesInterval;

	public ContinuousTunableParameter(String name, double fixedValue, int tuningOrderIndex, Interval possibleValuesInterval) {

		super(name, fixedValue, tuningOrderIndex);
		this.possibleValuesInterval = possibleValuesInterval;
	}


	public void setInterval(int lowerBound, int upperBound, boolean isLeftClosed, boolean isRightClosed ) {
		// We are tuning only the parameter of myRole
		this.possibleValuesInterval = new Interval(lowerBound, upperBound, isLeftClosed, isRightClosed);
	}

	public Interval getPossibleValuesInterval() {
	    return this.possibleValuesInterval;
    }
}
