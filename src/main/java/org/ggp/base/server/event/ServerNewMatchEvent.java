package org.ggp.base.server.event;

import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;


public final class ServerNewMatchEvent extends Event
{

	private final List<ProverRole> roles;
	private final ProverMachineState initialState;

	public ServerNewMatchEvent(List<ProverRole> roles, ProverMachineState initialState)
	{
		this.roles = roles;
		this.initialState = initialState;
	}

	public List<ProverRole> getRoles()
	{
		return roles;
	}

	public ProverMachineState getInitialState()
	{
		return initialState;
	}

}
