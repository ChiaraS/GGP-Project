package org.ggp.base.server.event;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;


public final class ServerNewGameStateEvent extends Event
{
	private final ExplicitMachineState state;

	public ServerNewGameStateEvent(ExplicitMachineState state)
	{
		this.state = state;
	}

	public ExplicitMachineState getState()
	{
		return state;
	}
}