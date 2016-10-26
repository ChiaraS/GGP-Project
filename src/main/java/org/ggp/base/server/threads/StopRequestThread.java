package org.ggp.base.server.threads;

import java.util.List;

import org.ggp.base.server.GameServer;
import org.ggp.base.server.request.RequestBuilder;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


public final class StopRequestThread extends RequestThread
{
	public StopRequestThread(GameServer gameServer, Match match, List<ExplicitMove> previousMoves, ExplicitRole role, String host, int port, String playerName)
	{
		super(gameServer, role, host, port, playerName, match.getPlayClock() * 1000, RequestBuilder.getStopRequest(match.getMatchId(), previousMoves, match.getGdlScrambler()));
	}

	@Override
	protected void handleResponse(String response) {
		;
	}
}