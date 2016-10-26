package org.ggp.base.util.statemachine.implementation.prover;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.prover.Prover;
import org.ggp.base.util.prover.aima.AimaProver;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;
import org.ggp.base.util.statemachine.implementation.prover.result.ProverResultParser;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableList;


public class ProverStateMachine extends StateMachine
{
	private ExplicitMachineState initialState;
	private Prover prover;
	private ImmutableList<ExplicitRole> roles;

	/**
	 * Initialize must be called before using the StateMachine
	 */
	public ProverStateMachine()
	{

	}

	@Override
	public void initialize(List<Gdl> description, long timeout)
	{
		prover = new AimaProver(description);
		roles = ImmutableList.copyOf(ExplicitRole.computeRoles(description));
		initialState = computeInitialState();
	}

	private ExplicitMachineState computeInitialState()
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getInitQuery(), new HashSet<GdlSentence>());
		return new ProverResultParser().toState(results);
	}

	/*
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getGoalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() != 1)
		{
		    GamerLogger.logError("StateMachine", "[Prover] Got goal results of size: " + results.size() + " when expecting size one.");
			throw new GoalDefinitionException(state, role);
		}

		try
		{
			GdlRelation relation = (GdlRelation) results.iterator().next();
			GdlConstant constant = (GdlConstant) relation.get(1);

			return Integer.parseInt(constant.toString());
		}
		catch (Exception e)
		{
			throw new GoalDefinitionException(state, role);
		}
	}
	*/

	@Override
	public List<Integer> getOneRoleGoals(ExplicitMachineState state, ExplicitRole role)	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getGoalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() != 1) {
		    GamerLogger.logError("StateMachine", "[Prover] Got goal results of size: " + results.size() + " when expecting size one.");
			//throw new GoalDefinitionException(state, role);
		}

		List<Integer> goalValues = new ArrayList<Integer>();

		for(GdlSentence sentence : results){

			GdlRelation relation = (GdlRelation) sentence;
			GdlConstant constant = (GdlConstant) relation.get(1);

			try	{
				int value = Integer.parseInt(constant.toString());
				goalValues.add(value);
			}catch (Exception e){
				GamerLogger.logError("StateMachine", "[Prover] Got goal results that is not a number: " + constant.toString() + ".");
				GamerLogger.logStackTrace("StateMachine", e);
				//throw new GoalDefinitionException(state, role);
			}
		}

		return goalValues;
	}

	@Override
	public ExplicitMachineState getInitialState()
	{
		return initialState;
	}

	@Override
	public List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getLegalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() == 0)
		{
			GamerLogger.logError("StateMachine", "Got no legal moves when expecting at least one.");
			throw new MoveDefinitionException(state, role);
		}

		return new ProverResultParser().toMoves(results);
	}

	@Override
	public ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getNextQuery(), ProverQueryBuilder.getContext(state, getRoles(), moves));

		for (GdlSentence sentence : results)
		{
			if (!sentence.isGround())
			{
				throw new TransitionDefinitionException(state, moves);
			}
		}

		return new ProverResultParser().toState(results);
	}

	@Override
	public List<ExplicitRole> getRoles()
	{
		return roles;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state)
	{
		return prover.prove(ProverQueryBuilder.getTerminalQuery(), ProverQueryBuilder.getContext(state));
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#shutdown()
	 */
	@Override
	public void shutdown() {
		// Does nothing cause for the prover state machine nothing needs to be
		// done after finishing using the state machine.
	}
}