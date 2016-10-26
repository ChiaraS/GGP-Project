package org.ggp.base.server.threads;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;


public final class RandomPlayRequestThread extends PlayRequestThread
{
	private ExplicitMove move;

	public RandomPlayRequestThread(Match match, List<ExplicitMove> legalMoves)
	{
		super(null, match, null, legalMoves, null, null, 0, null, true);
		move = legalMoves.get(new Random().nextInt(legalMoves.size()));
	}

	@Override
	public ExplicitMove getMove()
	{
		return move;
	}

	@Override
	public void run()
	{
		;
	}
}