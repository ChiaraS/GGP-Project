package org.ggp.base.util.statemachine.exceptions;

import java.util.List;

import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;


@SuppressWarnings("serial")
public final class TransitionDefinitionException extends Exception
{

	private final List<ProverMove> moves;
	private final ProverMachineState state;

	public TransitionDefinitionException(ProverMachineState state, List<ProverMove> moves)
	{
		this.state = state;
		this.moves = moves;
	}

	public List<ProverMove> getMoves()
	{
		return moves;
	}

	public ProverMachineState getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "Transition is poorly defined for " + moves + " in " + state;
	}

}
