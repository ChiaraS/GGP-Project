package org.ggp.base.util.statemachine.implementation.prover.result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;


public final class ProverResultParser
{

	private final static GdlConstant TRUE = GdlPool.getConstant("true");

	public List<ProverMove> toMoves(Set<GdlSentence> results)
	{
		List<ProverMove> moves = new ArrayList<ProverMove>();
		for (GdlSentence result : results)
		{
			moves.add(new ProverMove(result.get(1)));
		}

		return moves;
	}

	public List<ProverRole> toRoles(List<GdlSentence> results)
	{
		List<ProverRole> roles = new ArrayList<ProverRole>();
		for (GdlSentence result : results)
		{
			GdlConstant name = (GdlConstant) result.get(0);
			roles.add(new ProverRole(name));
		}

		return roles;
	}

	public ProverMachineState toState(Set<GdlSentence> results)
	{
		Set<GdlSentence> trues = new HashSet<GdlSentence>();
		for (GdlSentence result : results)
		{
			trues.add(GdlPool.getRelation(TRUE, new GdlTerm[] { result.get(0) }));
		}
		return new ProverMachineState(trues);
	}
}