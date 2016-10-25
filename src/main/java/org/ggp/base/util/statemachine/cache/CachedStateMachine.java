package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

import com.google.common.collect.ImmutableList;

public final class CachedStateMachine extends StateMachine
{
	private final StateMachine backingStateMachine;
	private final TtlCache<ProverMachineState, Entry> ttlCache;

	private final class Entry
	{
		public Map<ProverRole, List<Integer>> goals;
		public Map<ProverRole, List<ProverMove>> moves;
		public Map<List<ProverMove>, ProverMachineState> nexts;
		public Boolean terminal;

		public Entry()
		{
			goals = new HashMap<ProverRole, List<Integer>>();
			moves = new HashMap<ProverRole, List<ProverMove>>();
			nexts = new HashMap<List<ProverMove>, ProverMachineState>();
			terminal = null;
		}
	}

	public CachedStateMachine(StateMachine backingStateMachine)
	{
		this.backingStateMachine = backingStateMachine;
		ttlCache = new TtlCache<ProverMachineState, Entry>(1);
	}

	private Entry getEntry(ProverMachineState state)
	{
		if (!ttlCache.containsKey(state))
		{
			ttlCache.put(state, new Entry());
		}

		return ttlCache.get(state);
	}

	@Override
	public List<Integer> getOneRoleGoals(ProverMachineState state, ProverRole role)
			throws StateMachineException {

		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (!entry.goals.containsKey(role))
			{
				entry.goals.put(role, backingStateMachine.getOneRoleGoals(state, role));
			}

			return entry.goals.get(role);
		}
	}

	@Override
	public List<ProverMove> getLegalMoves(ProverMachineState state, ProverRole role) throws MoveDefinitionException, StateMachineException
	{
		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (!entry.moves.containsKey(role))
			{
				entry.moves.put(role, ImmutableList.copyOf(backingStateMachine.getLegalMoves(state, role)));
			}

			return entry.moves.get(role);
		}
	}

	@Override
	public ProverMachineState getNextState(ProverMachineState state, List<ProverMove> moves) throws TransitionDefinitionException, StateMachineException
	{
		Entry entry = getEntry(state);
		synchronized (entry)
		{
			if (!entry.nexts.containsKey(moves))
			{
				entry.nexts.put(moves, backingStateMachine.getNextState(state, moves));
			}

			return entry.nexts.get(moves);
		}
	}

	@Override
	public boolean isTerminal(ProverMachineState state) throws StateMachineException
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
	public List<ProverRole> getRoles() {
		// TODO(schreib): Should this be cached as well?
		return backingStateMachine.getRoles();
	}

	@Override
	public ProverMachineState getInitialState() {
		// TODO(schreib): Should this be cached as well?
		return backingStateMachine.getInitialState();
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