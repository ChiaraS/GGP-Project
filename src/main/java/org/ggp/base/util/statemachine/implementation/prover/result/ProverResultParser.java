package org.ggp.base.util.statemachine.implementation.prover.result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


public final class ProverResultParser
{

	private final static GdlConstant TRUE = GdlPool.getConstant("true");

	public List<ExplicitMove> toMoves(Set<GdlSentence> results)
	{
		List<ExplicitMove> moves = new ArrayList<ExplicitMove>();
		for (GdlSentence result : results)
		{
			moves.add(new ExplicitMove(result.get(1)));
		}

		return moves;
	}

	public List<ExplicitRole> toRoles(List<GdlSentence> results)
	{
		List<ExplicitRole> roles = new ArrayList<ExplicitRole>();
		for (GdlSentence result : results)
		{
			GdlConstant name = (GdlConstant) result.get(0);
			roles.add(new ExplicitRole(name));
		}

		return roles;
	}

	public ExplicitMachineState toState(Set<GdlSentence> results)
	{
		Set<GdlSentence> trues = new HashSet<GdlSentence>();
		for (GdlSentence result : results)
		{
			trues.add(GdlPool.getRelation(TRUE, new GdlTerm[] { result.get(0) }));
		}
		return new ExplicitMachineState(trues);
	}
}