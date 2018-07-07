package org.ggp.base.server.event;

import java.io.Serializable;
import java.util.List;

import org.ggp.base.util.observer.Event;


@SuppressWarnings("serial")
public final class ServerCompletedMatchEvent extends Event implements Serializable
{

	private final List<Double> goals;

	public ServerCompletedMatchEvent(List<Double> goals)
	{
		this.goals = goals;
	}

	public List<Double> getGoals()
	{
		return goals;
	}

}
