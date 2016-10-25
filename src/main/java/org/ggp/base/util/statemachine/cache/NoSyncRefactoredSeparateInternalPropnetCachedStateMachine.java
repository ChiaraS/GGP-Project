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
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

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

		GamerLogger.log(FORMAT.CSV_FORMAT, "CacheStats", "Cache size before pruning;Cache size after pruning;Pruning time;");
	}

	private PropnetMachineStateEntry getPropnetEntry(InternalPropnetMachineState state){

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
	public List<Integer> getOneRoleGoals(ProverMachineState state, ProverRole role){

		//System.out.println("PN: Wrong call of cache (goals)!");

		return this.getOneRoleGoals(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role));
	}

	@Override
	public List<Integer> getOneRoleGoals(InternalPropnetMachineState state, InternalPropnetRole role){
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
	public List<ProverMove> getLegalMoves(ProverMachineState state, ProverRole role) throws MoveDefinitionException, StateMachineException{

		//System.out.println("PN: Wrong call of cache (legal moves)!");

		List<ProverMove> moves = new ArrayList<ProverMove>();
		for(InternalPropnetMove m : this.getInternalLegalMoves(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.roleToInternalRole(role))){
			moves.add(this.backingStateMachine.internalMoveToMove(m));
		}
		return moves;
	}

	@Override
	public List<InternalPropnetMove> getInternalLegalMoves(InternalPropnetMachineState state, InternalPropnetRole role) throws MoveDefinitionException{
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for legal moves in the cache!");

		List<InternalPropnetMove> moves = entry.moves.get(role);

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
	public ProverMachineState getNextState(ProverMachineState state, List<ProverMove> moves) throws TransitionDefinitionException, StateMachineException{

		//System.out.println("PN: Wrong call of cache (next state)!");

		return this.backingStateMachine.internalStateToState(this.getInternalNextState(this.backingStateMachine.stateToInternalState(state), this.backingStateMachine.movesToInternalMoves(moves)));
	}

	@Override
	public InternalPropnetMachineState getInternalNextState(InternalPropnetMachineState state, List<InternalPropnetMove> moves){
		PropnetMachineStateEntry entry = getPropnetEntry(state);

		//System.out.println("PN: Looking for next state in the cache!");

		InternalPropnetMachineState nextState = entry.nexts.get(moves);

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
	public boolean isTerminal(ProverMachineState state) throws StateMachineException{

		//System.out.println("PN: Wrong call of cache (terminality)!");

		return this.isTerminal(this.backingStateMachine.stateToInternalState(state));
	}

	@Override
	public boolean isTerminal(InternalPropnetMachineState state){
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
	public List<ProverRole> getRoles() {
		// TODO: Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public ProverMachineState getInitialState() {
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
            return this.getClass().getSimpleName() + "(" + this.backingStateMachine.getName() + ")";
        }
        return this.getClass().getSimpleName() + "(null)";
    }

	@Override
	public InternalPropnetMachineState stateToInternalState(ProverMachineState state) {
		return this.backingStateMachine.stateToInternalState(state);
	}

	@Override
	public ProverMachineState internalStateToState(InternalPropnetMachineState state) {
		return this.backingStateMachine.internalStateToState(state);
	}

	@Override
	public ProverRole internalRoleToRole(InternalPropnetRole role) {
		return this.backingStateMachine.internalRoleToRole(role);
	}

	@Override
	public InternalPropnetRole roleToInternalRole(ProverRole role) {
		return this.backingStateMachine.roleToInternalRole(role);
	}

	@Override
	public ProverMove internalMoveToMove(InternalPropnetMove move) {
		return this.backingStateMachine.internalMoveToMove(move);
	}

	@Override
	public InternalPropnetMove moveToInternalMove(ProverMove move) {
		return this.backingStateMachine.moveToInternalMove(move);
	}

	@Override
	public List<InternalPropnetMove> movesToInternalMoves(List<ProverMove> moves) {
		return this.backingStateMachine.movesToInternalMoves(moves);
	}

}