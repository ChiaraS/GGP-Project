package org.ggp.base.util.statemachine.implementation.yapProlog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class TimesafeYapStateMachine extends StateMachine {

	private YapStateMachine yapStateMachine = null;
    private List<Gdl> gameDescription;

    private ExecutorService executor;

	public TimesafeYapStateMachine(YapStateMachine yapStateMachine) {
		this.yapStateMachine = yapStateMachine;
		this.executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void initialize(List<Gdl> description) {

	}

	@Override
	public int getGoal(MachineState state, Role role)
			throws GoalDefinitionException, StateMachineException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Role> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MachineState getInitialState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
			throws MoveDefinitionException, StateMachineException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
			throws TransitionDefinitionException, StateMachineException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}
