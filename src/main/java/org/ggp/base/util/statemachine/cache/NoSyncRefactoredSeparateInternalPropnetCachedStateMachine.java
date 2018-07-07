package org.ggp.base.util.statemachine.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

	public NoSyncRefactoredSeparateInternalPropnetCachedStateMachine(Random random, InternalPropnetStateMachine backingStateMachine){

		super(random);

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
	public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role){

		//System.out.println("PN: Wrong call of cache (goals)!");

		return this.getAllGoalsForOneRole(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToCompactRole(role));
	}

	@Override
	public List<Double> getAllGoalsForOneRole(CompactMachineState state, CompactRole role){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for goals in the cache!");

		List<Double> goals = entry.goals.get(role);

		if (goals == null){

			//System.out.println("PN: Goals not found!");

			goals = this.backingStateMachine.getAllGoalsForOneRole(state, role);
			entry.goals.put(role, goals);
		}//else{
			//System.out.println("PN: Goals found!");
		//}

		return goals;

	}

	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException{

		//System.out.println("PN: Wrong call of cache (legal moves)!");

		List<ExplicitMove> moves = new ArrayList<ExplicitMove>();
		for(CompactMove m : this.getCompactLegalMoves(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToCompactRole(role))){
			moves.add(this.backingStateMachine.convertToExplicitMove(m));
		}
		return moves;
	}

	@Override
	public List<CompactMove> getCompactLegalMoves(CompactMachineState state, CompactRole role) throws MoveDefinitionException{
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for legal moves in the cache!");

		List<CompactMove> moves = entry.moves.get(role);

		if (moves == null){

			//System.out.println("PN: Legal moves not found!");

			moves = ImmutableList.copyOf(this.backingStateMachine.getCompactLegalMoves(state, role));
			entry.moves.put(role, moves);
		}//else{
			//System.out.println("PN: Legal moves found!");
		//}

		return moves;

	}

	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException{

		//System.out.println("PN: Wrong call of cache (next state)!");

		return this.backingStateMachine.convertToExplicitMachineState(this.getCompactNextState(this.backingStateMachine.convertToCompactMachineState(state), this.backingStateMachine.convertToInternalJointMoves(moves)));
	}

	@Override
	public CompactMachineState getCompactNextState(CompactMachineState state, List<CompactMove> moves){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for next state in the cache!");

		CompactMachineState nextState = entry.nexts.get(moves);

		if (nextState == null){

			//System.out.println("PN: Next state not found!");

			nextState = this.backingStateMachine.getCompactNextState(state, moves);
			entry.nexts.put(moves, nextState);
		}//else{
			//System.out.println("PN: Next state found!");
		//}

		return nextState;
	}

	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException{

		//System.out.println("PN: Wrong call of cache (terminality)!");

		return this.isTerminal(this.backingStateMachine.convertToCompactMachineState(state));
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