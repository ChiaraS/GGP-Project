package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters;

import org.ggp.base.util.Interval;

public class ContinuousTunableParameter extends TunableParameter {

	private Interval possibleValuesInterval;

	public ContinuousTunableParameter(String name, double fixedValue, int tuningOrderIndex, Interval possibleValuesInterval) {

		super(name, fixedValue, tuningOrderIndex);

		this.possibleValuesInterval = possibleValuesInterval;

	}

}
