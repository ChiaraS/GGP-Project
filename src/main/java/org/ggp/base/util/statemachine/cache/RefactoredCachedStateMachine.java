package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import com.google.common.collect.ImmutableList;

public class RefactoredCachedStateMachine extends StateMachine{

	private final StateMachine backingStateMachine;
	private final RefactoredTtlCache<MachineState, MachineStateEntry> ttlCache;

	private final class MachineStateEntry{
		public Map<Role, Integer> goals;
		public Map<Role, List<Move>> moves;
		public Map<List<Move>, MachineState> nexts;
		public Boolean terminal;

		public MachineStateEntry(){
			goals = new HashMap<Role, Integer>();
			moves = new HashMap<Role, List<Move>>();
			nexts = new HashMap<List<Move>, MachineState>();
			terminal = null;
		}
	}

	public RefactoredCachedStateMachine(StateMachine backingStateMachine){
		this.backingStateMachine = backingStateMachine;
		ttlCache = new RefactoredTtlCache<MachineState, MachineStateEntry>(1);
	}

	private MachineStateEntry getEntry(MachineState state){

		MachineStateEntry entry = ttlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
			entry = new MachineStateEntry();
			ttlCache.put(state, entry);
		}

		return entry;
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){
			Integer goal = entry.goals.get(role);

			if(goal == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
				goal = new Integer(backingStateMachine.getGoal(state, role));
				entry.goals.put(role, goal);
			}

			return goal.intValue();
		}
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){

			List<Move> moves = entry.moves.get(role);

			if (moves == null){
				moves = ImmutableList.copyOf(backingStateMachine.getLegalMoves(state, role));
				entry.moves.put(role, moves);
			}

			return moves;
		}
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){

			MachineState nextState = entry.nexts.get(moves);

			if(nextState == null){

				nextState = this.backingStateMachine.getNextState(state, moves);
				entry.nexts.put(moves, nextState);
			}

			return nextState;
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException{
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
	public List<Role> getRoles(){
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getRoles();
	}

	@Override
	public MachineState getInitialState(){
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
            return "Cache(" + this.backingStateMachine.getName() + ")";
        }
        return "Cache(null)";
    }
}