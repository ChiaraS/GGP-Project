package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

import com.google.common.collect.ImmutableList;

public class RefactoredCachedStateMachine extends StateMachine{

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

	public RefactoredCachedStateMachine(StateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		ttlCache = new RefactoredTtlCache<ProverMachineState, MachineStateEntry>(1);
	}

	private MachineStateEntry getEntry(ProverMachineState state){

		MachineStateEntry entry = ttlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
			entry = new MachineStateEntry();
			ttlCache.put(state, entry);
		}

		return entry;
	}

	@Override
	public List<Integer> getOneRoleGoals(ProverMachineState state, ProverRole role) throws StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){
			List<Integer> goals = entry.goals.get(role);

			if(goals == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
				goals = this.backingStateMachine.getOneRoleGoals(state, role);
				entry.goals.put(role, goals);
			}

			return goals;
		}
	}

	@Override
	public List<ProverMove> getLegalMoves(ProverMachineState state, ProverRole role) throws MoveDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){

			List<ProverMove> moves = entry.moves.get(role);

			if (moves == null){
				moves = ImmutableList.copyOf(backingStateMachine.getLegalMoves(state, role));
				entry.moves.put(role, moves);
			}

			return moves;
		}
	}

	@Override
	public ProverMachineState getNextState(ProverMachineState state, List<ProverMove> moves) throws TransitionDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){

			ProverMachineState nextState = entry.nexts.get(moves);

			if(nextState == null){

				nextState = this.backingStateMachine.getNextState(state, moves);
				entry.nexts.put(moves, nextState);
			}

			return nextState;
		}
	}

	@Override
	public boolean isTerminal(ProverMachineState state) throws StateMachineException{
		MachineStateEntry entry = getEntry(state);
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
		this.ttlCache.prune();
	}

	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException{
		this.backingStateMachine.initialize(description, timeout);
	}

	@Override
	public List<ProverRole> getRoles(){
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public ProverMachineState getInitialState(){
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getInitialState();
	}

	@Override
	public void shutdown(){
		if(this.backingStateMachine != null){
			this.backingStateMachine.shutdown();
		}
	}

	@Override
    public String getName(){
        if(this.backingStateMachine != null) {
            return this.getClass().getSimpleName() + "(" + this.backingStateMachine.getName() + ")";
        }
        return this.getClass().getSimpleName() + "(null)";
    }
}
