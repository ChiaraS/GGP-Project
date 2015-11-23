package org.ggp.base.util.statemachine.cache;

import java.util.ArrayList;
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
	private final TtlCache<ExternalPropnetMachineState, PropnetEntry> internalStateTtlCache;

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
		this.internalStateTtlCache = new TtlCache<ExternalPropnetMachineState, PropnetEntry>(1);
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		backingStateMachine.initialize(description, timeout);
	}

	private PropnetEntry getPropnetEntry(ExternalPropnetMachineState state){
		if (!internalStateTtlCache.containsKey(state)){
			this.internalStateTtlCache.put(state, new PropnetEntry());
		}

		return this.internalStateTtlCache.get(state);
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException, StateMachineException{
		return this.getGoal(this.backingStateMachine.stateToExternalState(state), this.backingStateMachine.roleToExternalRole(role));
	}

	public int getGoal(ExternalPropnetMachineState state, ExternalPropnetRole role) throws GoalDefinitionException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.goals.containsKey(role)){
				entry.goals.put(role, this.backingStateMachine.getGoal(state, role));
			}

			return entry.goals.get(role);
		}
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{
		List<Move> moves = new ArrayList<Move>();
		for(ExternalPropnetMove m : this.getLegalMoves(this.backingStateMachine.stateToExternalState(state), this.backingStateMachine.roleToExternalRole(role))){
			moves.add(this.backingStateMachine.externalMoveToMove(m));
		}
		return moves;
	}

	public List<ExternalPropnetMove> getLegalMoves(ExternalPropnetMachineState state, ExternalPropnetRole role) throws MoveDefinitionException, StateMachineException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.moves.containsKey(role)){
				entry.moves.put(role, ImmutableList.copyOf(this.backingStateMachine.getLegalMoves(state, role)));
			}

			return entry.moves.get(role);
		}
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException{
		return this.backingStateMachine.externalStateToState(this.getNextState(this.backingStateMachine.stateToExternalState(state), this.backingStateMachine.moveToExternalMove(moves)));
	}

	public ExternalPropnetMachineState getNextState(ExternalPropnetMachineState state, List<ExternalPropnetMove> moves) throws TransitionDefinitionException, StateMachineException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.nexts.containsKey(moves)){
				entry.nexts.put(moves, this.backingStateMachine.getNextState(state, moves));
			}

			return entry.nexts.get(moves);
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException{
		return this.isTerminal(this.backingStateMachine.stateToExternalState(state));
	}

	public boolean isTerminal(ExternalPropnetMachineState state) throws StateMachineException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (entry.terminal == null){
				entry.terminal = this.backingStateMachine.isTerminal(state);
			}

			return entry.terminal;
		}
	}

	@Override
	public void doPerMoveWork(){
		prune();
	}

	public void prune(){
		this.internalStateTtlCache.prune();
	}

	@Override
	public List<Role> getRoles() {
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public MachineState getInitialState() {
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getInitialState();
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
            return "PnCache(" + this.backingStateMachine.getName() + ")";
        }
        return "PnCache(null)";
    }
}