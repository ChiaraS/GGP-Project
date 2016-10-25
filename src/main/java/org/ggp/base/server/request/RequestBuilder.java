package org.ggp.base.server.request;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.scrambler.GdlScrambler;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;


public final class RequestBuilder
{
	public static String getPlayRequest(String matchId, List<ProverMove> moves, GdlScrambler scrambler)
	{
		if (moves == null) {
			return "( PLAY " + matchId + " NIL )";
		} else {
			StringBuilder sb = new StringBuilder();

			sb.append("( PLAY " + matchId + " (");
			for (ProverMove move : moves)
			{
				sb.append(scrambler.scramble(move.getContents()) + " ");
			}
			sb.append(") )");

			return sb.toString();
		}
	}

	public static String getStartRequest(String matchId, ProverRole role, List<Gdl> description, int startClock, int playClock, GdlScrambler scrambler)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("( START " + matchId + " " + scrambler.scramble(role.getName()) + " (");
		for (Gdl gdl : description)
		{
			sb.append(scrambler.scramble(gdl) + " ");
		}
		sb.append(") " + startClock + " " + playClock + ")");

		return sb.toString();
	}

	public static String getPreviewRequest(List<Gdl> description, int previewClock, GdlScrambler scrambler)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("( PREVIEW (");
		for (Gdl gdl : description)
		{
			sb.append(scrambler.scramble(gdl) + " ");
		}
		sb.append(") " + previewClock + " )");

		return sb.toString();
	}

	public static String getStopRequest(String matchId, List<ProverMove> moves, GdlScrambler scrambler)
	{
		if (moves == null) {
			return "( STOP " + matchId + " NIL )";
		} else {
			StringBuilder sb = new StringBuilder();

			sb.append("( STOP " + matchId + " (");
			for (ProverMove move : moves)
			{
				sb.append(scrambler.scramble(move.getContents()) + " ");
			}
			sb.append(") )");

			return sb.toString();
		}
	}

	public static String getAbortRequest(String matchId)
	{
		return "( ABORT " + matchId + " )";
	}

	public static String getInfoRequest()
	{
		return "( INFO )";
	}
}