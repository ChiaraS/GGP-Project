package org.ggp.base.player.gamer.statemachine.human.event;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;


public final class HumanNewMovesEvent extends Event
{

	private final List<ExplicitMove> moves;
	private final ExplicitMove selection;

	public HumanNewMovesEvent(List<ExplicitMove> moves, ExplicitMove selection)
	{
	    Collections.sort(moves, new Comparator<ExplicitMove>(){@Override
		public int compare(ExplicitMove o1, ExplicitMove o2) {return o1.toString().compareTo(o2.toString());}});
		this.moves = moves;
		this.selection = selection;
	}

	public List<ExplicitMove> getMoves()
	{
		return moves;
	}

	public ExplicitMove getSelection()
	{
		return selection;
	}

}
