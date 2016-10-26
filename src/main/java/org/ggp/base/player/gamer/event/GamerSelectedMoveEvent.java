package org.ggp.base.player.gamer.event;

import java.util.List;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public final class GamerSelectedMoveEvent extends Event
{
	private final List<ExplicitMove> moves;
	private final ExplicitMove selection;
	private final long time;

	public GamerSelectedMoveEvent(List<ExplicitMove> moves, ExplicitMove selection, long time) {
		this.moves = moves;
		this.selection = selection;
		this.time = time;
	}

	public List<ExplicitMove> getMoves() {
		return moves;
	}

	public ExplicitMove getSelection() {
		return selection;
	}

	public long getTime() {
		return time;
	}
}
