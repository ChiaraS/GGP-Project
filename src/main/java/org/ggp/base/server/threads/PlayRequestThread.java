package org.ggp.base.server.threads;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.ggp.base.server.GameServer;
import org.ggp.base.server.event.ServerIllegalMoveEvent;
import org.ggp.base.server.request.RequestBuilder;
import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;


public class PlayRequestThread extends RequestThread
{
	private final GameServer gameServer;
	private final List<ProverMove> legalMoves;
	private final Match match;
	private final ProverRole role;

	private ProverMove move;

	public PlayRequestThread(GameServer gameServer, Match match, List<ProverMove> previousMoves, List<ProverMove> legalMoves, ProverRole role, String host, int port, String playerName, boolean unlimitedTime)
	{
		super(gameServer, role, host, port, playerName, unlimitedTime ? -1 : (match.getPlayClock() * 1000 + 1000), RequestBuilder.getPlayRequest(match.getMatchId(), previousMoves, match.getGdlScrambler()));
		this.gameServer = gameServer;
		this.legalMoves = legalMoves;
		this.match = match;
		this.role = role;

		move = legalMoves.get(new Random().nextInt(legalMoves.size()));
	}

	public ProverMove getMove()
	{
		return move;
	}

	@Override
	protected void handleResponse(String response) {
		try {
			ProverMove candidateMove = gameServer.getStateMachine().getMoveFromTerm(GdlFactory.createTerm(match.getGdlScrambler().unscramble(response).toString()));
			if (new HashSet<ProverMove>(legalMoves).contains(candidateMove)) {
				move = candidateMove;
			} else {
				gameServer.notifyObservers(new ServerIllegalMoveEvent(role, candidateMove));
			}
		} catch (GdlFormatException e) {
			gameServer.notifyObservers(new ServerIllegalMoveEvent(role, null));
		} catch (SymbolFormatException e) {
			gameServer.notifyObservers(new ServerIllegalMoveEvent(role, null));
		}
	}
}