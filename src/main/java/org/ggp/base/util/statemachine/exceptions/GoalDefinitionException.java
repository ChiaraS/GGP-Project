package org.ggp.base.util.statemachine.exceptions;

import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

@SuppressWarnings("serial")
public final class GoalDefinitionException extends Exception
{

	private final ExplicitRole role;
	private final ExplicitMachineState state;

	public GoalDefinitionException(ExplicitMachineState state, ExplicitRole role)
	{
		this.state = state;
		this.role = role;
	}

	public GoalDefinitionException(ExplicitMachineState state, ExplicitRole role, Throwable cause)
	{
		super(cause);
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
		return "Goal is poorly defined for " + role + " in " + state;
	}

}
