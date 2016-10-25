package org.ggp.base.server.event;

import java.io.Serializable;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;


@SuppressWarnings("serial")
public final class ServerTimeoutEvent extends Event implements Serializable
{

	private final ProverRole role;

	public ServerTimeoutEvent(ProverRole role)
	{
		this.role = role;
	}

	public ProverRole getRole()
	{
		return role;
	}

}
