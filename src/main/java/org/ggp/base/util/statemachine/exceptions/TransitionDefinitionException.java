package org.ggp.base.util.statemachine.exceptions;

import java.util.List;

import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;


@SuppressWarnings("serial")
public final class TransitionDefinitionException extends Exception
{

	private final List<ExplicitMove> moves;
	private final ExplicitMachineState state;

	public TransitionDefinitionException(ExplicitMachineState state, List<ExplicitMove> moves)
	{
		this.state = state;
		this.moves = moves;
	}

	public List<ExplicitMove> getMoves()
	{
		return moves;
	}

	public ExplicitMachineState getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "Transition is poorly defined for " + moves + " in " + state;
	}

}
