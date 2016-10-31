package org.ggp.base.util.statemachinenew;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class CompactStateMachine extends AbstractStateMachine {

	private CompactStateMachineInterface theMachine;

	public CompactStateMachine(CompactStateMachineInterface theMachine) {

		this.theMachine = theMachine;

	}

	@Override
	public void initialize(List<Gdl> description, long timeout)	throws StateMachineInitializationException {

		this.theMachine.initialize(description, timeout);

	}

	@Override
	public List<Integer> getAllGoalsForOneRole(MachineState state, Role role) throws StateMachineException {
		if(state instanceof CompactMachineState && role instanceof CompactRole){

			return this.theMachine.getAllGoalsForOneRole((CompactMachineState)state, (CompactRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-getAllGoalsForOneRole(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		if(state instanceof CompactMachineState){

			return this.theMachine.isTerminal((CompactMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-isTerminal(): detected wrong type for machine state : [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public MachineState getInitialState() {

		return this.theMachine.getInitialState();

	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{

		if(state instanceof CompactMachineState && role instanceof CompactRole){

			return new ArrayList<Move>(this.theMachine.getLegalMoves((CompactMachineState)state, (CompactRole)role));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-getLegalMoves(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException {

		if(state instanceof CompactMachineState){

			return this.theMachine.getNextState((CompactMachineState)state, this.convertListOfMoves(moves));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-getNextState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public ExplicitMachineState convertToExplicitMachineState(MachineState state) {
		if(state instanceof CompactMachineState){

			return this.theMachine.convertToExplicitMachineState((CompactMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-convertToExplicitMachineState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitMove convertToExplicitMove(Move move) {
		if(move instanceof CompactMove){

			return this.theMachine.convertToExplicitMove((CompactMove)move);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-convertToExplicitMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitRole convertToExplicitRole(Role role) {
		if(role instanceof CompactRole){

			return this.theMachine.convertToExplicitRole((CompactRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-convertToExplicitRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public void shutdown() {

		this.theMachine.shutdown();

	}

	@Override
	protected List<Role> computeRoles() {

		return new ArrayList<Role>(this.theMachine.getRoles());
	}

	private List<CompactMove> convertListOfMoves(List<Move> moves){

		List<CompactMove> CompactMoves = new ArrayList<CompactMove>();

		for(Move m : moves){

			if(m instanceof CompactMove){

				CompactMoves.add((CompactMove)m);

			}else{
				// Not throwing StateMachineException because failure here is not the fault of the state machine but
				// the fault of some programming error that caused the wrong state and role formats to end up here.
				throw new RuntimeException("CompactStateMachine-checkListOfMoves(): detected wrong type for move: [" + m.getClass().getSimpleName() + "].");
			}

		}

		return CompactMoves;

	}

}
