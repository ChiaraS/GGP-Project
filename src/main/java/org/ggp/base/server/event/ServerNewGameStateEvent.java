package org.ggp.base.server.event;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;


public final class ServerNewGameStateEvent extends Event
{
	private final ProverMachineState state;

	public ServerNewGameStateEvent(ProverMachineState state)
	{
		this.state = state;
	}

	public ProverMachineState getState()
	{
		return state;
	}
}