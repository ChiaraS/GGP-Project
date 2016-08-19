package org.ggp.base.util.statemachine.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

import com.google.common.collect.ImmutableList;

/**
 * ATTENTION! THIS CLASS IS NOT THREAD SAFE
 *
 * @author C.Sironi
 *
 */
public final class NoSyncRefactoredSeparateInternalPropnetCachedStateMachine extends InternalPropnetStateMachine{

	private final InternalPropnetStateMachine backingStateMachine;
	private final RefactoredTtlCache<InternalPropnetMachineState, PropnetMachineStateEntry> internalStateTtlCache;

	private final class PropnetMachineStateEntry{
		public Map<InternalPropnetRole, List<Integer>> goals;
		public Map<InternalPropnetRole, List<InternalPropnetMove>> moves;
		public Map<List<InternalPropnetMove>, InternalPropnetMachineState> nexts;
		public Boolean terminal;

		public PropnetMachineStateEntry(){
			goals = new HashMap<InternalPropnetRole, List<Integer>>();
			moves = new HashMap<InternalPropnetRole, List<InternalPropnetMove>>();
			nexts = new HashMap<List<InternalPropnetMove>, InternalPropnetMachineState>();
			terminal = null;
		}
	}

	public NoSyncRefactoredSeparateInternalPropnetCachedStateMachine(InternalPropnetStateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		this.internalStateTtlCache = new RefactoredTtlCache<InternalPropnetMachineState, PropnetMachineStateEntry>(1);
	}

	private PropnetMachineStateEntry getPropnetEntry(InternalPropnetMachineState state){

		PropnetMachineStateEntry entry = this.internalStateTtlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
			entry = new PropnetMachineStateEntry();
			this.internalStateTtlCache.put(state, entry);
		}

		return entry;
	}

	@Override
	public List<Integer> getOneRoleGoals(MachineState state, Role role){
		return this.getOneRoleGoals(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role));
	}

	@Override
	public List<Integer> getOneRoleGoals(InternalPropnetMachineState state, InternalPropnetRole role){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		List<Integer> goals = entry.goals.get(role);

		if (goals == null){
			goals = this.backingStateMachine.getOneRoleGoals(state, role);
			entry.goals.put(role, goals);
		}

		return goals;

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
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		List<InternalPropnetMove> moves = entry.moves.get(role);

		if (moves == null){
			moves = ImmutableList.copyOf(this.backingStateMachine.getInternalLegalMoves(state, role));
			entry.moves.put(role, moves);
		}

		return moves;

	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException{
		return this.backingStateMachine.internalStateToState(this.getInternalNextState(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.movesToInternalMoves(moves)));
	}

	@Override
	public InternalPropnetMachineState getInternalNextState(InternalPropnetMachineState state, List<InternalPropnetMove> moves){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		InternalPropnetMachineState nextState = entry.nexts.get(moves);

		if (nextState == null){

			nextState = this.backingStateMachine.getInternalNextState(state, moves);
			entry.nexts.put(moves, nextState);
		}

		return nextState;
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException{
		return this.isTerminal(this.backingStateMachine.stateToInternalState(state));
	}

	@Override
	public boolean isTerminal(InternalPropnetMachineState state){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		if (entry.terminal == null){
			entry.terminal = this.backingStateMachine.isTerminal(state);
		}

		return entry.terminal;
	}

	@Override
	public void doPerMoveWork(){
		prune();
	}

	public void prune(){
		this.internalStateTtlCache.prune();
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		this.backingStateMachine.initialize(description, timeout);
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
            return "NoSyncPnCache(" + this.backingStateMachine.getName() + ")";
        }
        return "NoSyncPnCache(null)";
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

}