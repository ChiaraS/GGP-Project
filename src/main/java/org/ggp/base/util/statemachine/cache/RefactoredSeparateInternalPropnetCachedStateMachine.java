package org.ggp.base.util.statemachine.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableList;

public final class RefactoredSeparateInternalPropnetCachedStateMachine extends InternalPropnetStateMachine{

	private final InternalPropnetStateMachine backingStateMachine;
	private final RefactoredTtlCache<CompactMachineState, PropnetMachineStateEntry> internalStateTtlCache;

	private final class PropnetMachineStateEntry{
		public Map<CompactRole, List<Double>> goals;
		public Map<CompactRole, List<CompactMove>> moves;
		public Map<List<CompactMove>, CompactMachineState> nexts;
		public Boolean terminal;

		public PropnetMachineStateEntry(){
			goals = new HashMap<CompactRole, List<Double>>();
			moves = new HashMap<CompactRole, List<CompactMove>>();
			nexts = new HashMap<List<CompactMove>, CompactMachineState>();
			terminal = null;
		}
	}

	public RefactoredSeparateInternalPropnetCachedStateMachine(Random random, InternalPropnetStateMachine backingStateMachine){
		super(random);
		this.backingStateMachine = backingStateMachine;
		this.internalStateTtlCache = new RefactoredTtlCache<CompactMachineState, PropnetMachineStateEntry>(1);
	}

	private PropnetMachineStateEntry getPropnetEntry(CompactMachineState state){

		PropnetMachineStateEntry entry = internalStateTtlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
			entry = new PropnetMachineStateEntry();
			internalStateTtlCache.put(state, entry);
		}

		return entry;
	}

	@Override
	public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException{
		return this.getAllGoalsForOneRole(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToCompactRole(role));
	}

	@Override
	public List<Double> getAllGoalsForOneRole(CompactMachineState state, CompactRole role) {
		PropnetMachineStateEntry entry = getPropnetEntry(state);
		synchronized (entry){
			List<Double> goal = entry.goals.get(role);

			if (goal == null){
				goal = this.backingStateMachine.getAllGoalsForOneRole(state, role);
				entry.goals.put(role, goal);
			}

			return goal;
		}
	}

	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException{
		List<ExplicitMove> moves = new ArrayList<ExplicitMove>();
		for(CompactMove m : this.getCompactLegalMoves(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToCompactRole(role))){
			moves.add(this.backingStateMachine.convertToExplicitMove(m));
		}
		return moves;
	}

	@Override
	public List<CompactMove> getCompactLegalMoves(CompactMachineState state, CompactRole role) throws MoveDefinitionException{
		PropnetMachineStateEntry entry = getPropnetEntry(state);
		synchronized (entry){

			List<CompactMove> moves = entry.moves.get(role);

			if (moves == null){
				moves = ImmutableList.copyOf(this.backingStateMachine.getCompactLegalMoves(state, role));
				entry.moves.put(role, moves);
			}

			return moves;
		}
	}

	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException{
		return this.backingStateMachine.convertToExplicitMachineState(this.getCompactNextState(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToInternalJointMoves(moves)));
	}

	@Override
	public CompactMachineState getCompactNextState(CompactMachineState state, List<CompactMove> moves){
		PropnetMachineStateEntry entry = getPropnetEntry(state);
		synchronized (entry){

			CompactMachineState nextState = entry.nexts.get(moves);

			if (nextState == null){

				nextState = this.backingStateMachine.getCompactNextState(state, moves);
				entry.nexts.put(moves, nextState);
			}

			return nextState;
		}
	}

	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException{
		return this.isTerminal(this.backingStateMachine.convertToCompactMachineState(state));
	}

	@Override
	public boolean isTerminal(CompactMachineState state){
		PropnetMachineStateEntry entry = getPropnetEntry(state);
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
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		backingStateMachine.initialize(description, timeout);
	}

	@Override
	public List<ExplicitRole> getExplicitRoles() {
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getExplicitRoles();
	}

	@Override
	public ExplicitMachineState getExplicitInitialState() {
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getExplicitInitialState();
	}

	@Override
	public CompactMachineState getCompactInitialState() {
		return this.backingStateMachine.getCompactInitialState();
	}

	@Override
	public List<CompactRole> getCompactRoles() {
		return this.backingStateMachine.getCompactRoles();
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

	@Override
	public CompactMachineState convertToCompactMachineState(ExplicitMachineState state) {
		return this.backingStateMachine.convertToCompactMachineState(state);
	}

	@Override
	public ExplicitMachineState convertToExplicitMachineState(CompactMachineState state) {
		return this.backingStateMachine.convertToExplicitMachineState(state);
	}

	@Override
	public ExplicitRole convertToExplicitRole(CompactRole role) {
		return this.backingStateMachine.convertToExplicitRole(role);
	}

	@Override
	public CompactRole convertToCompactRole(ExplicitRole role) {
		return this.backingStateMachine.convertToCompactRole(role);
	}

	@Override
	public ExplicitMove convertToExplicitMove(CompactMove move) {
		return this.backingStateMachine.convertToExplicitMove(move);
	}

	@Override
	public CompactMove convertToCompactMove(ExplicitMove move, ExplicitRole role) {
		return this.backingStateMachine.convertToCompactMove(move, role);
	}

	@Override
	public List<CompactMove> convertToInternalJointMoves(List<ExplicitMove> moves) {
		return this.backingStateMachine.convertToInternalJointMoves(moves);
	}

}