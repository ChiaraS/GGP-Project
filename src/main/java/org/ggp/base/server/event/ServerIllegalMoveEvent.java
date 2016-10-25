package org.ggp.base.server.event;

import java.io.Serializable;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;


@SuppressWarnings("serial")
public final class ServerIllegalMoveEvent extends Event implements Serializable
{

	private final ProverMove move;
	private final ProverRole role;

	public ServerIllegalMoveEvent(ProverRole role, ProverMove move)
	{
		this.role = role;
		this.move = move;
	}

	public ProverMove getMove()
	{
		return move;
	}

	public ProverRole getRole()
	{
		return role;
	}

}
