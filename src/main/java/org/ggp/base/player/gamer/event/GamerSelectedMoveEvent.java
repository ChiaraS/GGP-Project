package org.ggp.base.player.gamer.event;

import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public final class GamerSelectedMoveEvent extends Event
{
	private final List<ProverMove> moves;
	private final ProverMove selection;
	private final long time;

	public GamerSelectedMoveEvent(List<ProverMove> moves, ProverMove selection, long time) {
		this.moves = moves;
		this.selection = selection;
		this.time = time;
	}

	public List<ProverMove> getMoves() {
		return moves;
	}

	public ProverMove getSelection() {
		return selection;
	}

	public long getTime() {
		return time;
	}
}
