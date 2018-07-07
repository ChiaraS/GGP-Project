package org.ggp.base.server.threads;

import org.ggp.base.server.GameServer;
import org.ggp.base.server.request.RequestBuilder;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


public final class StartRequestThread extends RequestThread
{
	public StartRequestThread(GameServer gameServer, Match match, ExplicitRole role, String host, int port, String playerName)
	{
		super(gameServer, role, host, port, playerName, match.getStartClock() * 1000, RequestBuilder.getStartRequest(match.getMatchId(), role, match.getGame().getRules(), match.getStartClock(), match.getPlayClock(), match.getGdlScrambler()));
	}

	public StartRequestThread(GameServer gameServer, Match match, ExplicitRole role, String host, int port, String playerName, boolean unlimitedTime)
	{
		super(gameServer, role, host, port, playerName, unlimitedTime ? -1 : match.getStartClock() * 1000, RequestBuilder.getStartRequest(match.getMatchId(), role, match.getGame().getRules(), match.getStartClock(), match.getPlayClock(), match.getGdlScrambler()));
	}

	@Override
	protected void handleResponse(String response) {
		;
	}
}