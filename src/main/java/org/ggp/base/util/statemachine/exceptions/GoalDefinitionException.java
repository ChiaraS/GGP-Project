package org.ggp.base.util.statemachine.exceptions;

import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

@SuppressWarnings("serial")
public final class GoalDefinitionException extends Exception
{

	private final ProverRole role;
	private final ProverMachineState state;

	public GoalDefinitionException(ProverMachineState state, ProverRole role)
	{
		this.state = state;
		this.role = role;
	}

	public GoalDefinitionException(ProverMachineState state, ProverRole role, Throwable cause)
	{
		super(cause);
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
		return "Goal is poorly defined for " + role + " in " + state;
	}

}
