package org.ggp.base.server.event;

import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


public final class ServerNewMatchEvent extends Event
{

	private final List<ExplicitRole> roles;
	private final ExplicitMachineState initialState;

	public ServerNewMatchEvent(List<ExplicitRole> roles, ExplicitMachineState initialState)
	{
		this.roles = roles;
		this.initialState = initialState;
	}

	public List<ExplicitRole> getRoles()
	{
		return roles;
	}

	public ExplicitMachineState getInitialState()
	{
		return initialState;
	}

}
