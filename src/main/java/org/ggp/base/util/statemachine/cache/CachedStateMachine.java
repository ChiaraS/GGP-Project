package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableList;

public final class CachedStateMachine extends StateMachine
{
	private final StateMachine backingStateMachine;
	private final TtlCache<ExplicitMachineState, Entry> ttlCache;

	private final class Entry
	{
		public Map<ExplicitRole, List<Integer>> goals;
		public Map<ExplicitRole, List<ExplicitMove>> moves;
		public Map<List<ExplicitMove>, ExplicitMachineState> nexts;
		public Boolean terminal;

		public Entry()
		{
			goals = new HashMap<ExplicitRole, List<Integer>>();
			moves = new HashMap<ExplicitRole, List<ExplicitMove>>();
			nexts = new HashMap<List<ExplicitMove>, ExplicitMachineState>();
			terminal = null;
		}
	}

	public CachedStateMachine(Random random, StateMachine backingStateMachine)
	{
		super(random);

		this.backingStateMachine = backingStateMachine;
		ttlCache = new TtlCache<ExplicitMachineState, Entry>(1);
	}

	private Entry getEntry(ExplicitMachineState state)
	{
		if (!ttlCache.containsKey(state))
		{
			ttlCache.put(state, new Entry());
		}

		return ttlCache.get(state);
	}

	@Override
	public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role)
			throws StateMachineException {

		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (!entry.goals.containsKey(role))
			{
				entry.goals.put(role, backingStateMachine.getAllGoalsForOneRole(state, role));
			}

			return entry.goals.get(role);
		}
	}

	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException
	{
		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (!entry.moves.containsKey(role))
			{
				entry.moves.put(role, ImmutableList.copyOf(backingStateMachine.getExplicitLegalMoves(state, role)));
			}

			return entry.moves.get(role);
		}
	}

	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException
	{
		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (!entry.nexts.containsKey(moves))
			{
				entry.nexts.put(moves, backingStateMachine.getExplicitNextState(state, moves));
			}

			return entry.nexts.get(moves);
		}
	}

	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException
	{
		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (entry.terminal == null)
			{
				entry.terminal = backingStateMachine.isTerminal(state);
			}

			return entry.terminal;
		}
	}

	@Override
	public void doPerMoveWork()
	{
		prune();
	}

	public void prune()
	{
		ttlCache.prune();
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		backingStateMachine.initialize(description, timeout);
	}

	@Override
	public List<ExplicitRole> getExplicitRoles() {
		// TODO(schreib): Should this be cached as well?
		return backingStateMachine.getExplicitRoles();
	}

	@Override
	public ExplicitMachineState getExplicitInitialState() {
		// TODO(schreib): Should this be cached as well?
		return backingStateMachine.getExplicitInitialState();
	}

	@Override
	public void shutdown() {
		if(this.backingStateMachine != null){
			this.backingStateMachine.shutdown();
		}
	}

	@Override
    public String getName() {
        if(this.backingStateMachine != null) {
            return this.getClass().getSimpleName() + "(" + this.backingStateMachine.getName() + ")";
        }
        return this.getClass().getSimpleName() + "(null)";
    }
}