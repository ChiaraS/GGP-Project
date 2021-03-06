package org.ggp.base.server.event;

import java.io.Serializable;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


@SuppressWarnings("serial")
public final class ServerTimeoutEvent extends Event implements Serializable
{

	private final ExplicitRole role;

	public ServerTimeoutEvent(ExplicitRole role)
	{
		this.role = role;
	}

	public ExplicitRole getRole()
	{
		return role;
	}

}
