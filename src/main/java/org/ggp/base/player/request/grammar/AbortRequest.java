package org.ggp.base.player.request.grammar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.event.GamerAbortedMatchEvent;
import org.ggp.base.player.gamer.event.GamerUnrecognizedMatchEvent;
import org.ggp.base.player.gamer.exception.AbortingException;

public final class AbortRequest extends Request
{

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{
		LOGGER = LogManager.getRootLogger();
	}


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
			LOGGER.warn(new StructuredDataMessage("AbortRequest", "Got ABORT message not intended for current game: ignoring.", "GamePlayer"));
			gamer.notifyObservers(new GamerUnrecognizedMatchEvent(matchId));
			return "busy";
		}

		// Mark the match as aborted and notify observers
		gamer.getMatch().markAborted();
		gamer.notifyObservers(new GamerAbortedMatchEvent());
		try {
			gamer.abort();
		} catch (AbortingException e) {
			LOGGER.error(new StructuredDataMessage("AbortRequest", "Error while aborting match.", "GamePlayer"), e);
		}

		LOGGER.info(new StructuredDataMessage("AbortRequest", "Match " + matchId + "aborted. Stopping file logging for match " + matchId + ".", "GamePlayer"));
	    ThreadContext.remove("MATCH_ID");
	    LOGGER.info(new StructuredDataMessage("AbortRequest", "Match " + matchId + "aborted. Player available to play a match.", "GamePlayer"));

		// Once the match has ended, set 'roleName' and 'match'
		// to NULL to indicate that we're ready to begin a new match.
		gamer.setRoleName(null);
	    gamer.setMatch(null);

		return "aborted";
	}

	@Override
	public String toString()
	{
		return "abort";
	}
}