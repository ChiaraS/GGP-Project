package org.ggp.base.util.statemachine.abstractsm;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.utils.MyPair;

/**
 * TODO: using instanceof is not elegant. Find a better way to deal with state machines using different types of moves, states and roles.
 */
public class ExplicitStateMachine extends AbstractStateMachine {

	private ExplicitStateMachineInterface theMachine;

	public ExplicitStateMachine(ExplicitStateMachineInterface theMachine) {

		this.theMachine = theMachine;

	}

	@Override
	public void initialize(List<Gdl> description, long timeout)	throws StateMachineInitializationException {

		super.initialize(description, timeout);
		this.theMachine.initialize(description, timeout);

	}

	@Override
	public List<Double> getAllGoalsForOneRole(MachineState state, Role role) throws StateMachineException {
		if(state instanceof ExplicitMachineState && role instanceof ExplicitRole){

			return this.theMachine.getAllGoalsForOneRole((ExplicitMachineState)state, (ExplicitRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-getAllGoalsForOneRole(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		if(state instanceof ExplicitMachineState){

			return this.theMachine.isTerminal((ExplicitMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-isTerminal(): detected wrong type for machine state : [" + state.getClass().getSimpleName() + "].");
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

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-getLegalMoves(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException {

		if(state instanceof ExplicitMachineState){

			return this.theMachine.getExplicitNextState((ExplicitMachineState)state, this.convertListOfMoves(moves));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-getNextState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public ExplicitMachineState convertToExplicitMachineState(MachineState state) {
		if(state instanceof ExplicitMachineState){

			return (ExplicitMachineState)state;

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-convertToExplicitMachineState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitMove convertToExplicitMove(Move move) {
		if(move instanceof ExplicitMove){

			return (ExplicitMove)move;

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-convertToExplicitMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitRole convertToExplicitRole(Role role) {
		if(role instanceof ExplicitRole){

			return (ExplicitRole)role;

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-convertToExplicitRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
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

	private List<ExplicitMove> convertListOfMoves(List<Move> moves){

		List<ExplicitMove> explicitMoves = new ArrayList<ExplicitMove>();

		for(Move m : moves){

			if(m instanceof ExplicitMove){

				explicitMoves.add((ExplicitMove)m);

			}else{
				// Not throwing StateMachineException because failure here is not the fault of the state machine but
				// the fault of some programming error that caused the wrong state and role formats to end up here.
				throw new RuntimeException("ExplicitStateMachine-checkListOfMoves(): detected wrong type for move: [" + m.getClass().getSimpleName() + "].");
			}

		}

		return explicitMoves;

	}

	@Override
	public String getName() {

		return this.getClass().getSimpleName() + "(" + this.theMachine.getName() + ")";

	}

	@Override
	public MyPair<double[], Double> fastPlayouts(MachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException {
		if(state instanceof ExplicitMachineState){

			return this.theMachine.fastPlayouts((ExplicitMachineState)state, numSimulationsPerPlayout, maxDepth);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-fastPlayouts(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	/*
	@Override
	public List<Move> getJointMove(List<List<Move>> legalMovesPerRole, MachineState state) throws MoveDefinitionException, StateMachineException {
		if(state instanceof ExplicitMachineState){

			List<List<ExplicitMove>> explicitLegalMovesPerRole = new ArrayList<List<ExplicitMove>>();
			for(List<Move> legalMoves: legalMovesPerRole) {
				List<ExplicitMove> explicitLegalMoves = new ArrayList<ExplicitMove>();
				for(Move move: legalMoves) {
					if(move instanceof ExplicitMove) {
						explicitLegalMoves.add((ExplicitMove)move);
					}else {
						throw new RuntimeException("ExplicitStateMachine-getJointMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
					}
				}
				explicitLegalMovesPerRole.add(explicitLegalMoves);
			}

			return new ArrayList<Move>(this.theMachine.getJointMove(explicitLegalMovesPerRole, (ExplicitMachineState)state));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-getJointMove(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}
	*/

	@Override
	public Move getMoveForRole(List<Move> legalMoves, MachineState state, Role role) throws StateMachineException, MoveDefinitionException {
		if(state instanceof ExplicitMachineState){

			if(role instanceof ExplicitRole) {
				List<ExplicitMove> explicitLegalMoves;
				if(legalMoves != null) {
					explicitLegalMoves = new ArrayList<ExplicitMove>();
					for(Move move: legalMoves) {
						if(move instanceof ExplicitMove) {
							explicitLegalMoves.add((ExplicitMove)move);
						}else {
							throw new RuntimeException("ExplicitStateMachine-getMoveForRole(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
						}
					}
				}else {
					explicitLegalMoves = null;
				}
				return this.theMachine.getMoveForRole(explicitLegalMoves, (ExplicitMachineState)state, (ExplicitRole)role);
			}else {
				throw new RuntimeException("ExplicitStateMachine-getMoveForRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
			}

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("ExplicitStateMachine-getMoveForRole(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public MachineState convertToInternalMachineState(ExplicitMachineState explicitState) {
		return explicitState;
	}

	@Override
	public Move convertToInternalMove(ExplicitMove explicitMove, ExplicitRole explicitRole) {
		return explicitMove;
	}

	@Override
	public Role convertToInternalRole(ExplicitRole explicitRole) {
		return explicitRole;
	}

	@Override
	public void doPerMoveWork() {
		this.theMachine.doPerMoveWork();
	}

	@Override
	public StateMachine getActualStateMachine() {
		return this.theMachine.getActualStateMachine();
	}

}
