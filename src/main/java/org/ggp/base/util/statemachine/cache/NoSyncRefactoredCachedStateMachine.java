package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
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
public final class NoSyncRefactoredCachedStateMachine extends StateMachine
{
	private final StateMachine backingStateMachine;
	private final RefactoredTtlCache<ProverMachineState, MachineStateEntry> ttlCache;

	private final class MachineStateEntry{
		public Map<ProverRole, List<Integer>> goals;
		public Map<ProverRole, List<ProverMove>> moves;
		public Map<List<ProverMove>, ProverMachineState> nexts;
		public Boolean terminal;

		public MachineStateEntry(){
			goals = new HashMap<ProverRole, List<Integer>>();
			moves = new HashMap<ProverRole, List<ProverMove>>();
			nexts = new HashMap<List<ProverMove>, ProverMachineState>();
			terminal = null;
		}
	}

	public NoSyncRefactoredCachedStateMachine(StateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		ttlCache = new RefactoredTtlCache<ProverMachineState, MachineStateEntry>(1);
	}

	private MachineStateEntry getEntry(ProverMachineState state){

		//System.out.println("");

		//System.out.println("Prover: Looking for entry in the cache!");

		MachineStateEntry entry = this.ttlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.

			//System.out.println("Prover: Entry not found!");

			entry = new MachineStateEntry();
			this.ttlCache.put(state, entry);
		}//else{
			//System.out.println("Prover: Entry found!");
		//}

		return entry;
	}

	@Override
	public List<Integer> getOneRoleGoals(ProverMachineState state, ProverRole role) throws StateMachineException{
		MachineStateEntry entry = getEntry(state);

		//System.out.println("Prover: Looking for goals in the entry!");

		List<Integer> goals = entry.goals.get(role);

		if(goals == null){

			//System.out.println("Prover: Goals not found!");

			goals = this.backingStateMachine.getOneRoleGoals(state, role);
			entry.goals.put(role,goals);
		}//else{
			//System.out.println("Prover: Goals found!");
		//}

		return goals;

	}

	@Override
	public List<ProverMove> getLegalMoves(ProverMachineState state, ProverRole role) throws MoveDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);

		//System.out.println("Prover: Looking for legal moves in the cache!");

		List<ProverMove> moves = entry.moves.get(role);

		if (moves == null){

			//System.out.println("Prover: Legal moves not found!");

			moves = ImmutableList.copyOf(this.backingStateMachine.getLegalMoves(state, role));
			entry.moves.put(role, moves);
		}//else{
			//System.out.println("Prover: Legal moves found!");
		//}

		return moves;

	}

	@Override
	public ProverMachineState getNextState(ProverMachineState state, List<ProverMove> moves) throws TransitionDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);

		//System.out.println("Prover: Looking for next state in the cache!");

		ProverMachineState nextState = entry.nexts.get(moves);

		if (nextState == null){

			//System.out.println("Prover: Next state not found!");

			nextState = this.backingStateMachine.getNextState(state, moves);
			entry.nexts.put(moves, nextState);
		}//else{
			//System.out.println("Prover: Next state found!");
		//}

		return nextState;

	}

	@Override
	public boolean isTerminal(ProverMachineState state) throws StateMachineException{
		MachineStateEntry entry = getEntry(state);

		//System.out.println("Prover: Looking for terminality in the cache!");

		if (entry.terminal == null){

			//System.out.println("Prover: Terminality not found!");

			entry.terminal = this.backingStateMachine.isTerminal(state);
		}//else{
			//System.out.println("Prover: Terminality found!");
		//}

		return entry.terminal;
	}

	@Override
	public void doPerMoveWork(){
		prune();
	}

	public void prune(){

		int sizeBefore = this.ttlCache.size();

		long timeBefore = System.currentTimeMillis();

		this.ttlCache.prune();

		GamerLogger.log(FORMAT.CSV_FORMAT, "CacheStats", sizeBefore + ";" + this.ttlCache.size() + ";" + (System.currentTimeMillis()-timeBefore) + ";");
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
		this.backingStateMachine.initialize(description, timeout);
	}

	@Override
	public List<ProverRole> getRoles() {
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public ProverMachineState getInitialState() {
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
            return this.getClass().getSimpleName() + "(" + this.backingStateMachine.getName() + ")";
        }
        return this.getClass().getSimpleName() + "(null)";
    }
}