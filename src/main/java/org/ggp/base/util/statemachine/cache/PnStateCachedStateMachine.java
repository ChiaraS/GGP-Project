package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.ExternalPropnetMachineState;
import org.ggp.base.util.statemachine.ExternalPropnetMove;
import org.ggp.base.util.statemachine.ExternalPropnetRole;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateExternalPropnetStateMachine;

import com.google.common.collect.ImmutableList;

public final class PnStateCachedStateMachine extends StateMachine
{
	private final SeparateExternalPropnetStateMachine backingStateMachine;
	private final TtlCache<MachineState, Entry> externalStateTtlCache;
	private final TtlCache<ExternalPropnetMachineState, PropnetEntry> internalStateTtlCache;

	private final class Entry{
		public Map<Role, Integer> goals;
		public Map<Role, List<Move>> moves;
		public Map<List<Move>, MachineState> nexts;
		public Boolean terminal;

		public Entry()
		{
			goals = new HashMap<Role, Integer>();
			moves = new HashMap<Role, List<Move>>();
			nexts = new HashMap<List<Move>, MachineState>();
			terminal = null;
		}
	}

	private final class PropnetEntry{

		public Map<ExternalPropnetRole, Integer> goals;
		public Map<ExternalPropnetRole, List<ExternalPropnetMove>> moves;
		public Map<List<ExternalPropnetMove>, ExternalPropnetMachineState> nexts;
		public Boolean terminal;

		public PropnetEntry()
		{
			goals = new HashMap<ExternalPropnetRole, Integer>();
			moves = new HashMap<ExternalPropnetRole, List<ExternalPropnetMove>>();
			nexts = new HashMap<List<ExternalPropnetMove>, ExternalPropnetMachineState>();
			terminal = null;
		}
	}

	public PnStateCachedStateMachine(SeparateExternalPropnetStateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		externalStateTtlCache = new TtlCache<MachineState, Entry>(1);
		internalStateTtlCache = new TtlCache<ExternalPropnetMachineState, PropnetEntry>(1);
	}

	private Entry getEntry(MachineState state){
		if (!externalStateTtlCache.containsKey(state)){
			externalStateTtlCache.put(state, new Entry());
		}

		return externalStateTtlCache.get(state);
	}

	private PropnetEntry getPropnetEntry(ExternalPropnetMachineState state){
		if (!internalStateTtlCache.containsKey(state)){
			internalStateTtlCache.put(state, new PropnetEntry());
		}

		return internalStateTtlCache.get(state);
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException, StateMachineException{
		Entry entry = getEntry(state);
		synchronized (entry){
			if (!entry.goals.containsKey(role)){
				entry.goals.put(role, backingStateMachine.getGoal(state, role));
			}

			return entry.goals.get(role);
		}
	}

	public int getGoal(ExternalPropnetMachineState state, ExternalPropnetRole role) throws GoalDefinitionException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.goals.containsKey(role)){
				entry.goals.put(role, backingStateMachine.getGoal(state, role));
			}

			return entry.goals.get(role);
		}
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{
		Entry entry = getEntry(state);
		synchronized (entry){
			if (!entry.moves.containsKey(role)){
				entry.moves.put(role, ImmutableList.copyOf(backingStateMachine.getLegalMoves(state, role)));
			}

			return entry.moves.get(role);
		}
	}

	public List<ExternalPropnetMove> getLegalMoves(ExternalPropnetMachineState state, ExternalPropnetRole role) throws MoveDefinitionException, StateMachineException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.moves.containsKey(role)){
				entry.moves.put(role, ImmutableList.copyOf(backingStateMachine.getLegalMoves(state, role)));
			}

			return entry.moves.get(role);
		}
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException{
		Entry entry = getEntry(state);
		synchronized (entry){
			if (!entry.nexts.containsKey(moves)){
				entry.nexts.put(moves, backingStateMachine.getNextState(state, moves));
			}

			return entry.nexts.get(moves);
		}
	}

	public ExternalPropnetMachineState getNextState(ExternalPropnetMachineState state, List<ExternalPropnetMove> moves) throws TransitionDefinitionException, StateMachineException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.nexts.containsKey(moves)){
				entry.nexts.put(moves, backingStateMachine.getNextState(state, moves));
			}

			return entry.nexts.get(moves);
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException{
		Entry entry = getEntry(state);
		synchronized (entry){
			if (entry.terminal == null){
				entry.terminal = backingStateMachine.isTerminal(state);
			}

			return entry.terminal;
		}
	}

	public boolean isTerminal(ExternalPropnetMachineState state) throws StateMachineException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (entry.terminal == null){
				entry.terminal = backingStateMachine.isTerminal(state);
			}

			return entry.terminal;
		}
	}

	@Override
	public void doPerMoveWork(){
		prune();
	}

	public void prune()
	{
		externalStateTtlCache.prune();
		internalStateTtlCache.prune();
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		backingStateMachine.initialize(description, timeout);
	}

	@Override
	public List<Role> getRoles() {
		// TODO(schreib): Should this be cached as well?
		return backingStateMachine.getRoles();
	}

	@Override
	public MachineState getInitialState() {
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
            return "Cache(" + this.backingStateMachine.getName() + ")";
        }
        return "Cache(null)";
    }
}