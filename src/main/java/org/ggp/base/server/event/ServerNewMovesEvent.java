package org.ggp.base.server.event;

import java.io.Serializable;
import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;


@SuppressWarnings("serial")
public final class ServerNewMovesEvent extends Event implements Serializable
{

	private final List<ExplicitMove> moves;

	public ServerNewMovesEvent(List<ExplicitMove> moves)
	{
		this.moves = moves;
	}

	public List<ExplicitMove> getMoves()
	{
		return moves;
	}

}
