package org.ggp.base.util.statemachine.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
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

/**
 * ATTENTION! THIS CLASS IS NOT THREAD SAFE
 *
 * @author C.Sironi
 *
 */
public final class NoSyncRefactoredSeparateInternalPropnetCachedStateMachine extends InternalPropnetStateMachine{

	private final InternalPropnetStateMachine backingStateMachine;
	private final RefactoredTtlCache<CompactMachineState, PropnetMachineStateEntry> internalStateTtlCache;

	private final class PropnetMachineStateEntry{
		public Map<CompactRole, List<Integer>> goals;
		public Map<CompactRole, List<CompactMove>> moves;
		public Map<List<CompactMove>, CompactMachineState> nexts;
		public Boolean terminal;

		public PropnetMachineStateEntry(){
			goals = new HashMap<CompactRole, List<Integer>>();
			moves = new HashMap<CompactRole, List<CompactMove>>();
			nexts = new HashMap<List<CompactMove>, CompactMachineState>();
			terminal = null;
		}
	}

	public NoSyncRefactoredSeparateInternalPropnetCachedStateMachine(InternalPropnetStateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		this.internalStateTtlCache = new RefactoredTtlCache<CompactMachineState, PropnetMachineStateEntry>(1);

		GamerLogger.log(FORMAT.CSV_FORMAT, "CacheStats", "Cache size before pruning;Cache size after pruning;Pruning time;");
	}

	private PropnetMachineStateEntry getPropnetEntry(CompactMachineState state){

		//System.out.println("");

		//System.out.println("PN: Looking for entry in the cache!");

		PropnetMachineStateEntry entry = this.internalStateTtlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.

			//System.out.println("PN: Entry not found!");

			entry = new PropnetMachineStateEntry();
			this.internalStateTtlCache.put(state, entry);
		}//else{
			//System.out.println("PN: Entry found!");
		//}

		return entry;
	}

	@Override
	public List<Integer> getOneRoleGoals(ExplicitMachineState state, ExplicitRole role){

		//System.out.println("PN: Wrong call of cache (goals)!");

		return this.getOneRoleGoals(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role));
	}

	@Override
	public List<Integer> getOneRoleGoals(CompactMachineState state, CompactRole role){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for goals in the cache!");

		List<Integer> goals = entry.goals.get(role);

		if (goals == null){

			//System.out.println("PN: Goals not found!");

			goals = this.backingStateMachine.getOneRoleGoals(state, role);
			entry.goals.put(role, goals);
		}//else{
			//System.out.println("PN: Goals found!");
		//}

		return goals;

	}

	@Override
	public List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException{

		//System.out.println("PN: Wrong call of cache (legal moves)!");

		List<ExplicitMove> moves = new ArrayList<ExplicitMove>();
		for(CompactMove m : this.getInternalLegalMoves(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role))){
			moves.add(this.backingStateMachine.internalMoveToMove(m));
		}
		return moves;
	}

	@Override
	public List<CompactMove> getInternalLegalMoves(CompactMachineState state, CompactRole role) throws MoveDefinitionException{
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for legal moves in the cache!");

		List<CompactMove> moves = entry.moves.get(role);

		if (moves == null){

			//System.out.println("PN: Legal moves not found!");

			moves = ImmutableList.copyOf(this.backingStateMachine.getInternalLegalMoves(state, role));
			entry.moves.put(role, moves);
		}//else{
			//System.out.println("PN: Legal moves found!");
		//}

		return moves;

	}

	@Override
	public ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException{

		//System.out.println("PN: Wrong call of cache (next state)!");

		return this.backingStateMachine.internalStateToState(this.getInternalNextState(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.movesToInternalMoves(moves)));
	}

	@Override
	public CompactMachineState getInternalNextState(CompactMachineState state, List<CompactMove> moves){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for next state in the cache!");

		CompactMachineState nextState = entry.nexts.get(moves);

		if (nextState == null){

			//System.out.println("PN: Next state not found!");

			nextState = this.backingStateMachine.getInternalNextState(state, moves);
			entry.nexts.put(moves, nextState);
		}//else{
			//System.out.println("PN: Next state found!");
		//}

		return nextState;
	}

	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException{

		//System.out.println("PN: Wrong call of cache (terminality)!");

		return this.isTerminal(this.backingStateMachine.stateToInternalState(state));
	}

	@Override
	public boolean isTerminal(CompactMachineState state){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for terminality in the cache!");

		if (entry.terminal == null){

			//System.out.println("PN: Terminality not found!");

			entry.terminal = this.backingStateMachine.isTerminal(state);
		}//else{
			//System.out.println("PN: Terminality found!");
		//}

		return entry.terminal;
	}

	@Override
	public void doPerMoveWork(){
		prune();
	}

	public void prune(){

		int sizeBefore = this.internalStateTtlCache.size();

		long timeBefore = System.currentTimeMillis();

		this.internalStateTtlCache.prune();

		GamerLogger.log(FORMAT.CSV_FORMAT, "CacheStats", sizeBefore + ";" + this.internalStateTtlCache.size() + ";" + (System.currentTimeMillis()-timeBefore) + ";");
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		this.backingStateMachine.initialize(description, timeout);
	}

	@Override
	public List<ExplicitRole> getRoles() {
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public ExplicitMachineState getInitialState() {
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getInitialState();
	}

	@Override
	public CompactMachineState getInternalInitialState() {
		return this.backingStateMachine.getInternalInitialState();
	}

	@Override
	public CompactRole[] getInternalRoles() {
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
            return this.getClass().getSimpleName() + "(" + this.backingStateMachine.getName() + ")";
        }
        return this.getClass().getSimpleName() + "(null)";
    }

	@Override
	public CompactMachineState stateToInternalState(ExplicitMachineState state) {
		return this.backingStateMachine.stateToInternalState(state);
	}

	@Override
	public ExplicitMachineState internalStateToState(CompactMachineState state) {
		return this.backingStateMachine.internalStateToState(state);
	}

	@Override
	public ExplicitRole internalRoleToRole(CompactRole role) {
		return this.backingStateMachine.internalRoleToRole(role);
	}

	@Override
	public CompactRole roleToInternalRole(ExplicitRole role) {
		return this.backingStateMachine.roleToInternalRole(role);
	}

	@Override
	public ExplicitMove internalMoveToMove(CompactMove move) {
		return this.backingStateMachine.internalMoveToMove(move);
	}

	@Override
	public CompactMove moveToInternalMove(ExplicitMove move) {
		return this.backingStateMachine.moveToInternalMove(move);
	}

	@Override
	public List<CompactMove> movesToInternalMoves(List<ExplicitMove> moves) {
		return this.backingStateMachine.movesToInternalMoves(moves);
	}

}