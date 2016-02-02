package org.ggp.base.util.statemachine.implementation.internalPropnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.cache.TtlCache;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMove;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetRole;

import com.google.common.collect.ImmutableList;

public final class SeparateInternalPropnetCachedStateMachine extends InternalPropnetStateMachine
{
	private final SeparateInternalPropnetStateMachine backingStateMachine;
	private final TtlCache<InternalPropnetMachineState, PropnetEntry> internalStateTtlCache;

	private final class PropnetEntry{

		public Map<InternalPropnetRole, Integer> goals;
		public Map<InternalPropnetRole, List<InternalPropnetMove>> moves;
		public Map<List<InternalPropnetMove>, InternalPropnetMachineState> nexts;
		public Boolean terminal;

		public PropnetEntry()
		{
			goals = new HashMap<InternalPropnetRole, Integer>();
			moves = new HashMap<InternalPropnetRole, List<InternalPropnetMove>>();
			nexts = new HashMap<List<InternalPropnetMove>, InternalPropnetMachineState>();
			terminal = null;
		}
	}

	public SeparateInternalPropnetCachedStateMachine(SeparateInternalPropnetStateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		this.internalStateTtlCache = new TtlCache<InternalPropnetMachineState, PropnetEntry>(1);
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		backingStateMachine.initialize(description, timeout);
	}

	private PropnetEntry getPropnetEntry(InternalPropnetMachineState state){
		if (!internalStateTtlCache.containsKey(state)){
			this.internalStateTtlCache.put(state, new PropnetEntry());
		}

		return this.internalStateTtlCache.get(state);
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException, StateMachineException{
		return this.getGoal(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role));
	}

	@Override
	public int getGoal(InternalPropnetMachineState state, InternalPropnetRole role) throws GoalDefinitionException{
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
		for(InternalPropnetMove m : this.getInternalLegalMoves(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role))){
			moves.add(this.backingStateMachine.internalMoveToMove(m));
		}
		return moves;
	}

	@Override
	public List<InternalPropnetMove> getInternalLegalMoves(InternalPropnetMachineState state, InternalPropnetRole role) throws MoveDefinitionException{
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.moves.containsKey(role)){
				entry.moves.put(role, ImmutableList.copyOf(this.backingStateMachine.getInternalLegalMoves(state, role)));
			}

			return entry.moves.get(role);
		}
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException{
		return this.backingStateMachine.internalStateToState(this.getInternalNextState(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.movesToInternalMoves(moves)));
	}

	@Override
	public InternalPropnetMachineState getInternalNextState(InternalPropnetMachineState state, List<InternalPropnetMove> moves){
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.nexts.containsKey(moves)){
				entry.nexts.put(moves, this.backingStateMachine.getInternalNextState(state, moves));
			}

			return entry.nexts.get(moves);
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException{
		return this.isTerminal(this.backingStateMachine.stateToInternalState(state));
	}

	@Override
	public boolean isTerminal(InternalPropnetMachineState state){
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
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public MachineState getInitialState() {
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getInitialState();
	}

	@Override
	public InternalPropnetMachineState getInternalInitialState() {
		return this.backingStateMachine.getInternalInitialState();
	}

	@Override
	public InternalPropnetRole[] getInternalRoles() {
		return this.backingStateMachine.getInternalRoles();
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

	@Override
	public InternalPropnetMachineState stateToInternalState(MachineState state) {
		return this.backingStateMachine.stateToInternalState(state);
	}

	@Override
	public MachineState internalStateToState(InternalPropnetMachineState state) {
		return this.backingStateMachine.internalStateToState(state);
	}

	@Override
	public Role internalRoleToRole(InternalPropnetRole role) {
		return this.backingStateMachine.internalRoleToRole(role);
	}

	@Override
	public InternalPropnetRole roleToInternalRole(Role role) {
		return this.backingStateMachine.roleToInternalRole(role);
	}

	@Override
	public Move internalMoveToMove(InternalPropnetMove move) {
		return this.backingStateMachine.internalMoveToMove(move);
	}

	@Override
	public InternalPropnetMove moveToInternalMove(Move move) {
		return this.backingStateMachine.moveToInternalMove(move);
	}

	@Override
	public List<InternalPropnetMove> movesToInternalMoves(List<Move> moves) {
		return this.backingStateMachine.movesToInternalMoves(moves);
	}

	@Override
	public List<Move> internalMovesToMoves(List<InternalPropnetMove> moves) {
		return this.backingStateMachine.internalMovesToMoves(moves);
	}

}