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

public final class SeparateInternalPropnetCachedStateMachine extends InternalPropnetStateMachine
{

	//private int allQueries;
	//private int notCachedQueries;



	private final InternalPropnetStateMachine backingStateMachine;
	private final TtlCache<CompactMachineState, PropnetEntry> internalStateTtlCache;

	private final class PropnetEntry{

		public Map<CompactRole, List<Integer>> goals;
		public Map<CompactRole, List<CompactMove>> moves;
		public Map<List<CompactMove>, CompactMachineState> nexts;
		public Boolean terminal;

		public PropnetEntry()
		{
			goals = new HashMap<CompactRole, List<Integer>>();
			moves = new HashMap<CompactRole, List<CompactMove>>();
			nexts = new HashMap<List<CompactMove>, CompactMachineState>();
			terminal = null;
		}
	}

	public SeparateInternalPropnetCachedStateMachine(Random random, InternalPropnetStateMachine backingStateMachine){
		super(random);
		this.backingStateMachine = backingStateMachine;
		this.internalStateTtlCache = new TtlCache<CompactMachineState, PropnetEntry>(1);

		//this.allQueries = 0;
		//this.notCachedQueries = 0;
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		backingStateMachine.initialize(description, timeout);
	}

	private PropnetEntry getPropnetEntry(CompactMachineState state){
		if (!internalStateTtlCache.containsKey(state)){
			this.internalStateTtlCache.put(state, new PropnetEntry());
		}

		return this.internalStateTtlCache.get(state);
	}

	@Override
	public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException{
		return this.getAllGoalsForOneRole(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToCompactRole(role));
	}

	@Override
	public List<Integer> getAllGoalsForOneRole(CompactMachineState state, CompactRole role) {
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.goals.containsKey(role)){
				entry.goals.put(role, this.backingStateMachine.getAllGoalsForOneRole(state, role));
				//!
				//this.notCachedQueries++;
			}

			//!
			//this.allQueries++;

			return entry.goals.get(role);
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
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.moves.containsKey(role)){
				entry.moves.put(role, ImmutableList.copyOf(this.backingStateMachine.getCompactLegalMoves(state, role)));

				//!
				//this.notCachedQueries++;
			}

			//!
			//this.allQueries++;

			return entry.moves.get(role);
		}
	}

	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException{
		return this.backingStateMachine.convertToExplicitMachineState(this.getCompactNextState(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.movesToInternalMoves(moves)));
	}

	@Override
	public CompactMachineState getCompactNextState(CompactMachineState state, List<CompactMove> moves){
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (!entry.nexts.containsKey(moves)){
				entry.nexts.put(moves, this.backingStateMachine.getCompactNextState(state, moves));

				//!
				//this.notCachedQueries++;
			}

			//!
			//this.allQueries++;

			return entry.nexts.get(moves);
		}
	}

	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException{
		return this.isTerminal(this.backingStateMachine.convertToCompactMachineState(state));
	}

	@Override
	public boolean isTerminal(CompactMachineState state){
		PropnetEntry entry = getPropnetEntry(state);
		synchronized (entry){
			if (entry.terminal == null){
				entry.terminal = this.backingStateMachine.isTerminal(state);

				//!
				//this.notCachedQueries++;
			}

			//!
			//this.allQueries++;

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
	public CompactMove convertToCompactMove(ExplicitMove move) {
		return this.backingStateMachine.convertToCompactMove(move);
	}

	@Override
	public List<CompactMove> movesToInternalMoves(List<ExplicitMove> moves) {
		return this.backingStateMachine.movesToInternalMoves(moves);
	}

}