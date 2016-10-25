package org.ggp.base.server.threads;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;


public final class RandomPlayRequestThread extends PlayRequestThread
{
	private ProverMove move;

	public RandomPlayRequestThread(Match match, List<ProverMove> legalMoves)
	{
		super(null, match, null, legalMoves, null, null, 0, null, true);
		move = legalMoves.get(new Random().nextInt(legalMoves.size()));
	}

	@Override
	public ProverMove getMove()
	{
		return move;
	}

	@Override
	public void run()
	{
		;
	}
}