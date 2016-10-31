package org.ggp.base.util.statemachine.abstractsm;

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

public class ExplicitAndCompactStateMachine extends AbstractStateMachine {

	private ExplicitAndCompactStateMachineInterface theMachine;

	public ExplicitAndCompactStateMachine(ExplicitAndCompactStateMachineInterface theMachine) {

		this.theMachine = theMachine;

	}

	@Override
	public void initialize(List<Gdl> description, long timeout)	throws StateMachineInitializationException {

		this.theMachine.initialize(description, timeout);

	}

	@Override
	public List<Integer> getAllGoalsForOneRole(MachineState state, Role role) throws StateMachineException {
		if(state instanceof ExplicitMachineState && role instanceof ExplicitRole){

			return this.theMachine.getAllGoalsForOneRole((ExplicitMachineState)state, (ExplicitRole)role);

		}else if(state instanceof CompactMachineState && role instanceof CompactRole){

			return this.theMachine.getAllGoalsForOneRole((CompactMachineState)state, (CompactRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-getAllGoalsForOneRole(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		if(state instanceof ExplicitMachineState){

			return this.theMachine.isTerminal((ExplicitMachineState)state);

		}else if(state instanceof CompactMachineState){

			return this.theMachine.isTerminal((CompactMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-isTerminal(): detected wrong type for machine state : [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public MachineState getInitialState() {

		return this.theMachine.getExplicitInitialState();

	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{

		if(state instanceof ExplicitMachineState && role instanceof ExplicitRole){

			return new ArrayList<Move>(this.theMachine.getExplicitLegalMoves((ExplicitMachineState)state, (ExplicitRole)role));

		}else if(state instanceof CompactMachineState && role instanceof CompactRole){

			return new ArrayList<Move>(this.theMachine.getCompactLegalMoves((CompactMachineState)state, (CompactRole)role));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-getLegalMoves(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException {

		if(state instanceof ExplicitMachineState){

			return this.theMachine.getExplicitNextState((ExplicitMachineState)state, this.convertListOfExplicitMoves(moves));

		}else if(state instanceof CompactMachineState){

			return this.theMachine.getCompactNextState((CompactMachineState)state, this.convertListOfCompactMoves(moves));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-getNextState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public ExplicitMachineState convertToExplicitMachineState(MachineState state) {
		if(state instanceof ExplicitMachineState){

			return (ExplicitMachineState)state;

		}else if(state instanceof CompactMachineState){

			return this.theMachine.convertToExplicitMachineState((CompactMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-convertToExplicitMachineState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitMove convertToExplicitMove(Move move) {
		if(move instanceof ExplicitMove){

			return (ExplicitMove)move;

		}else if(move instanceof CompactMove){

			return this.theMachine.convertToExplicitMove((CompactMove)move);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-convertToExplicitMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitRole convertToExplicitRole(Role role) {
		if(role instanceof ExplicitRole){

			return (ExplicitRole)role;

		}else if(role instanceof CompactRole){

			return this.theMachine.convertToExplicitRole((CompactRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitAndCompactStateMachine-convertToExplicitRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public void shutdown() {

		this.theMachine.shutdown();

	}

	@Override
	protected List<Role> computeRoles() {

		return new ArrayList<Role>(this.theMachine.getExplicitRoles());
	}

	private List<ExplicitMove> convertListOfExplicitMoves(List<Move> moves){

		List<ExplicitMove> explicitMoves = new ArrayList<ExplicitMove>();

		for(Move m : moves){

			if(m instanceof ExplicitMove){

				explicitMoves.add((ExplicitMove)m);

			}else{
				// Not throwing StateMachineException because failure here is not the fault of the state machine but
				// the fault of some programming error that caused the wrong state and role formats to end up here.
				throw new RuntimeException("ExplicitAndCompactStateMachine-checkListOfMoves(): detected wrong type for move: [" + m.getClass().getSimpleName() + "].");
			}

		}

		return explicitMoves;

	}

	private List<CompactMove> convertListOfCompactMoves(List<Move> moves){

		List<CompactMove> compactMoves = new ArrayList<CompactMove>();

		for(Move m : moves){

			if(m instanceof CompactMove){

				compactMoves.add((CompactMove)m);

			}else{
				// Not throwing StateMachineException because failure here is not the fault of the state machine but
				// the fault of some programming error that caused the wrong state and role formats to end up here.
				throw new RuntimeException("ExplicitAndCompactStateMachine-checkListOfMoves(): detected wrong type for move: [" + m.getClass().getSimpleName() + "].");
			}

		}

		return compactMoves;

	}

}
