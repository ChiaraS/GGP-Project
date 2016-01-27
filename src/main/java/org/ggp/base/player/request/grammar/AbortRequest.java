package org.ggp.base.player.request.grammar;

import java.util.Date;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.event.GamerAbortedMatchEvent;
import org.ggp.base.player.gamer.event.GamerUnrecognizedMatchEvent;
import org.ggp.base.player.gamer.exception.AbortingException;

public final class AbortRequest extends Request
{
	private final Gamer gamer;
	private final String matchId;

	public AbortRequest(Gamer gamer, String matchId)
	{
		this.gamer = gamer;
		this.matchId = matchId;
	}

	@Override
	public String getMatchId() {
		return matchId;
	}

	@Override
	public String process(long receptionTime)
	{
        // First, check to ensure that this abort request is for the match
        // we're currently playing. If we're not playing a match, or we're
        // playing a different match, send back "busy".
		if (gamer.getMatch() == null || !gamer.getMatch().getMatchId().equals(matchId))
		{
			LOGGER.error("[GamePlayer] Got abort message not intended for current game: ignoring.");
		    gamer.notifyObservers(new GamerUnrecognizedMatchEvent(matchId));
			return "busy";
		}

		LOGGER.info("[GamePlayer] Aborting match.");

		// Mark the match as aborted and notify observers
		gamer.getMatch().markAborted();
		gamer.notifyObservers(new GamerAbortedMatchEvent());
		try {
			gamer.abort();
		} catch (AbortingException e) {
			LOGGER.error("[GamePlayer] Exception while aborting!", e);
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

		return "aborted";
	}

	@Override
	public String toString()
	{
		return "abort";
	}
}