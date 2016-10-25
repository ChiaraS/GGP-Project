package org.ggp.base.server.event;

import java.io.Serializable;
import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;


@SuppressWarnings("serial")
public final class ServerNewMovesEvent extends Event implements Serializable
{

	private final List<ProverMove> moves;

	public ServerNewMovesEvent(List<ProverMove> moves)
	{
		this.moves = moves;
	}

	public List<ProverMove> getMoves()
	{
		return moves;
	}

}
