package org.ggp.base.player.request.grammar;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.event.GamerCompletedMatchEvent;
import org.ggp.base.player.gamer.event.GamerUnrecognizedMatchEvent;
import org.ggp.base.player.gamer.exception.StoppingException;
import org.ggp.base.util.gdl.grammar.GdlTerm;

public final class L4J2StopRequest extends L4J2Request
{
	private final Gamer gamer;
	private final String matchId;
	private final List<GdlTerm> moves;

	public L4J2StopRequest(Gamer gamer, String matchId, List<GdlTerm> moves)
	{
		this.gamer = gamer;
		this.matchId = matchId;
		this.moves = moves;
	}

	@Override
	public String getMatchId() {
		return matchId;
	}

	@Override
	public String process(long receptionTime)
	{
        // First, check to ensure that this stop request is for the match
        // we're currently playing. If we're not playing a match, or we're
        // playing a different match, send back "busy".
		if (gamer.getMatch() == null || !gamer.getMatch().getMatchId().equals(matchId))
		{
			LOGGER.error("[GamePlayer] Got stop message not intended for current game: ignoring.");
			gamer.notifyObservers(new GamerUnrecognizedMatchEvent(matchId));
			return "busy";
		}

		LOGGER.info("[GamePlayer] Stopping match.");

		//TODO: Add goal values
		if(moves != null) {
			gamer.getMatch().appendMoves(moves);
		}
		gamer.getMatch().markCompleted(null);
		gamer.notifyObservers(new GamerCompletedMatchEvent());
		try {
			gamer.stop();
		} catch (StoppingException e) {
			LOGGER.error("[GamePlayer] Exception while stopping!", e);
		}

		// Once the match has ended, set 'roleName' and 'match'
		// to NULL to indicate that we're ready to begin a new match.
		gamer.setRoleName(null);
	    gamer.setMatch(null);

		LOGGER.info("[GamePlayer] Stopping file logging for match " + this.matchId + ".");
		LOGGER.info("[GamePlayer] Stopped logging to files at: " + new Date());
		LOGGER.info("[GamePlayer] LOG SEALED");
		ThreadContext.remove("LOG_FILE");

		LOGGER.info("[GamePlayer] Stopped file logging for match " + this.matchId + ".");


		return "done";
	}

	@Override
	public String toString()
	{
		return "stop";
	}
}
