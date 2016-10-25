package org.ggp.base.player.gamer.statemachine.human.event;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;


public final class HumanNewMovesEvent extends Event
{

	private final List<ProverMove> moves;
	private final ProverMove selection;

	public HumanNewMovesEvent(List<ProverMove> moves, ProverMove selection)
	{
	    Collections.sort(moves, new Comparator<ProverMove>(){@Override
		public int compare(ProverMove o1, ProverMove o2) {return o1.toString().compareTo(o2.toString());}});
		this.moves = moves;
		this.selection = selection;
	}

	public List<ProverMove> getMoves()
	{
		return moves;
	}

	public ProverMove getSelection()
	{
		return selection;
	}

}
