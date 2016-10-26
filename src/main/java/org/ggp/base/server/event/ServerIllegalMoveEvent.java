package org.ggp.base.server.event;

import java.io.Serializable;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


@SuppressWarnings("serial")
public final class ServerIllegalMoveEvent extends Event implements Serializable
{

	private final ExplicitMove move;
	private final ExplicitRole role;

	public ServerIllegalMoveEvent(ExplicitRole role, ExplicitMove move)
	{
		this.role = role;
		this.move = move;
	}

	public ExplicitMove getMove()
	{
		return move;
	}

	public ExplicitRole getRole()
	{
		return role;
	}

}
