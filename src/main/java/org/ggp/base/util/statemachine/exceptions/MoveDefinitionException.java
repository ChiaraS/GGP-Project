package org.ggp.base.util.statemachine.exceptions;

import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

@SuppressWarnings("serial")
public final class MoveDefinitionException extends Exception
{

	private final ExplicitRole role;
	private final ExplicitMachineState state;

	public MoveDefinitionException(ExplicitMachineState state, ExplicitRole role)
	{
		this.state = state;
		this.role = role;
	}

	public ExplicitRole getRole()
	{
		return role;
	}

	public ExplicitMachineState getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "There are no legal moves defined for " + role + " in " + state;
	}

}
