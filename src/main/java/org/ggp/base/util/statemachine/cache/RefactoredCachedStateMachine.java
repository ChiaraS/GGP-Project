package org.ggp.base.util.statemachine.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableList;

public class RefactoredCachedStateMachine extends StateMachine{

	private final StateMachine backingStateMachine;
	private final RefactoredTtlCache<ExplicitMachineState, MachineStateEntry> ttlCache;

	private final class MachineStateEntry{
		public Map<ExplicitRole, List<Integer>> goals;
		public Map<ExplicitRole, List<ExplicitMove>> moves;
		public Map<List<ExplicitMove>, ExplicitMachineState> nexts;
		public Boolean terminal;

		public MachineStateEntry(){
			goals = new HashMap<ExplicitRole, List<Integer>>();
			moves = new HashMap<ExplicitRole, List<ExplicitMove>>();
			nexts = new HashMap<List<ExplicitMove>, ExplicitMachineState>();
			terminal = null;
		}
	}

	public RefactoredCachedStateMachine(Random random, StateMachine backingStateMachine){
		super(random);
		this.backingStateMachine = backingStateMachine;
		ttlCache = new RefactoredTtlCache<ExplicitMachineState, MachineStateEntry>(1);
	}

	private MachineStateEntry getEntry(ExplicitMachineState state){

		MachineStateEntry entry = ttlCache.get(state);

		if (entry == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
			entry = new MachineStateEntry();
			ttlCache.put(state, entry);
		}

		return entry;
	}

	@Override
	public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){
			List<Integer> goals = entry.goals.get(role);

			if(goals == null){ // If it's null because there is no such entry or because the entry is null, we must create a new one anyway.
				goals = this.backingStateMachine.getAllGoalsForOneRole(state, role);
				entry.goals.put(role, goals);
			}

			return goals;
		}
	}

	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){

			List<ExplicitMove> moves = entry.moves.get(role);

			if (moves == null){
				moves = ImmutableList.copyOf(backingStateMachine.getExplicitLegalMoves(state, role));
				entry.moves.put(role, moves);
			}

			return moves;
		}
	}

	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException{
		MachineStateEntry entry = getEntry(state);
		synchronized (entry){

			ExplicitMachineState nextState = entry.nexts.get(moves);

			if(nextState == null){

				nextState = this.backingStateMachine.getExplicitNextState(state, moves);
				entry.nexts.put(moves, nextState);
			}

			return nextState;
		}
	}

	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException{
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
	public List<ExplicitRole> getExplicitRoles(){
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getExplicitRoles();
	}

	@Override
	public ExplicitMachineState getExplicitInitialState(){
		// TODO(schreib): Should this be cached as well?
		return this.backingStateMachine.getExplicitInitialState();
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
