package org.ggp.base.util.statemachine.exceptions;

import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

@SuppressWarnings("serial")
public final class MoveDefinitionException extends Exception
{

	private final ProverRole role;
	private final ProverMachineState state;

	public MoveDefinitionException(ProverMachineState state, ProverRole role)
	{
		this.state = state;
		this.role = role;
	}

	public ProverRole getRole()
	{
		return role;
	}

	public ProverMachineState getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "There are no legal moves defined for " + role + " in " + state;
	}

}
