package org.ggp.base.util.statemachine.abstractsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
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
import org.ggp.base.util.statemachine.structure.fpga.FpgaMachineState;
import org.ggp.base.util.statemachine.structure.fpga.FpgaMove;
import org.ggp.base.util.statemachine.structure.fpga.FpgaRole;

import csironi.ggp.course.utils.MyPair;

public class FpgaStateMachine extends AbstractStateMachine {

	private FpgaStateMachineInterface theMachine;

	public FpgaStateMachine(FpgaStateMachineInterface theMachine) {

		this.theMachine = theMachine;

	}

	@Override
	public void initialize(List<Gdl> description, long timeout)	throws StateMachineInitializationException {

		this.theMachine.initialize(description, timeout);

	}

	@Override
	public List<Double> getAllGoalsForOneRole(MachineState state, Role role) throws StateMachineException {
		if(state instanceof FpgaMachineState && role instanceof FpgaRole){

			return this.theMachine.getAllGoalsForOneRole((FpgaMachineState)state, (FpgaRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getAllGoalsForOneRole(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		if(state instanceof FpgaMachineState){

			return this.theMachine.isTerminal((FpgaMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-isTerminal(): detected wrong type for machine state : [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public MachineState getInitialState() {

		return this.theMachine.getFpgaInitialState();

	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{

		if(state instanceof FpgaMachineState && role instanceof FpgaRole){

			return new ArrayList<Move>(this.theMachine.getFpgaLegalMoves((FpgaMachineState)state, (FpgaRole)role));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getLegalMoves(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException {

		if(state instanceof FpgaMachineState){

			return this.theMachine.getFpgaNextState((FpgaMachineState)state, this.convertListOfMoves(moves));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getNextState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public ExplicitMachineState convertToExplicitMachineState(MachineState state) {
		if(state instanceof FpgaMachineState){

			return this.theMachine.convertToExplicitMachineState((FpgaMachineState)state);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-convertToExplicitMachineState(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitMove convertToExplicitMove(Move move) {
		if(move instanceof FpgaMove){

			return this.theMachine.convertToExplicitMove((FpgaMove)move);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-convertToExplicitMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public ExplicitRole convertToExplicitRole(Role role) {
		if(role instanceof FpgaRole){

			return this.theMachine.convertToExplicitRole((FpgaRole)role);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-convertToExplicitRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public void shutdown() {

		this.theMachine.shutdown();

	}

	@Override
	protected List<Role> computeRoles() {

		return new ArrayList<Role>(this.theMachine.getFpgaRoles());
	}

	private List<FpgaMove> convertListOfMoves(List<Move> moves){

		List<FpgaMove> fpgaMoves = new ArrayList<FpgaMove>();

		for(Move m : moves){

			if(m instanceof FpgaMove){

				fpgaMoves.add((FpgaMove)m);

			}else{
				// Not throwing StateMachineException because failure here is not the fault of the state machine but
				// the fault of some programming error that caused the wrong state and role formats to end up here.
				throw new RuntimeException("FpgaStateMachine-checkListOfMoves(): detected wrong type for move: [" + m.getClass().getSimpleName() + "].");
			}

		}

		return fpgaMoves;

	}

	@Override
	public String getName() {

		return this.getClass().getSimpleName() + "(" + this.theMachine.getName() + ")";

	}

	@Override
	public Map<List<Move>, MachineState> getAllJointMovesAndNextStates(MachineState state) throws MoveDefinitionException, StateMachineException, TransitionDefinitionException {

		if(state instanceof FpgaMachineState){

			List<MyPair<FpgaMachineState,List<FpgaMove>>> allFpgaJointMovesAndNextStates = this.theMachine.getAllFpgaJointMovesAndNextStates((FpgaMachineState)state);

			Map<List<Move>, MachineState> allJointMovesAndNextStates = new HashMap<List<Move>, MachineState>();

			for(MyPair<FpgaMachineState,List<FpgaMove>> nextFpgaStateAndJointMove : allFpgaJointMovesAndNextStates) {

				List<Move> jointMove = new ArrayList<Move>();
				for(FpgaMove fpgaMove : nextFpgaStateAndJointMove.getSecond()) {
					jointMove.add(fpgaMove);
				}
				allJointMovesAndNextStates.put(jointMove, nextFpgaStateAndJointMove.getFirst());
			}

			return allJointMovesAndNextStates;

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getAllJointMovesAndNextStates(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public MyPair<double[], Double> fastPlayouts(MachineState state, int numSimulationsPerPlayout, int maxDepth) {
		if(state instanceof FpgaMachineState) {

			return this.theMachine.fastPlayouts((FpgaMachineState)state, numSimulationsPerPlayout, maxDepth);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-fastPlayouts(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	/*
	@Override
	public List<Move> getJointMove(List<List<Move>> legalMovesPerRole, MachineState state) {
		if(state instanceof FpgaMachineState){

			List<List<FpgaMove>> fpgaLegalMovesPerRole = new ArrayList<List<FpgaMove>>();
			for(List<Move> legalMoves: legalMovesPerRole) {
				List<FpgaMove> fpgaLegalMoves = new ArrayList<FpgaMove>();
				for(Move move: legalMoves) {
					if(move instanceof FpgaMove) {
						fpgaLegalMoves.add((FpgaMove)move);
					}else {
						throw new RuntimeException("FpgaStateMachine-getJointMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
					}
				}
				fpgaLegalMovesPerRole.add(fpgaLegalMoves);
			}

			return new ArrayList<Move>(this.theMachine.getJointMove(fpgaLegalMovesPerRole, (FpgaMachineState)state));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getJointMove(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}
	*/

	@Override
	public Move getMoveForRole(List<Move> legalMoves, MachineState state, Role role) {
		if(state instanceof FpgaMachineState){

			if(role instanceof FpgaRole) {
				List<FpgaMove> fpgaLegalMoves;
				if(legalMoves != null) {
					fpgaLegalMoves = new ArrayList<FpgaMove>();
					for(Move move: legalMoves) {
						if(move instanceof FpgaMove) {
							fpgaLegalMoves.add((FpgaMove)move);
						}else {
							throw new RuntimeException("FpgaStateMachine-getMoveForRole(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
						}
					}
				}else {
					fpgaLegalMoves = null;
				}
				return this.theMachine.getMoveForRole(fpgaLegalMoves, (FpgaMachineState)state, (FpgaRole)role);
			}else {
				throw new RuntimeException("FpgaStateMachine-getMoveForRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
			}

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getMoveForRole(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public MachineState convertToInternalMachineState(ExplicitMachineState explicitState) {
		return this.theMachine.convertToFpgaMachineState(explicitState);
	}

	@Override
	public Move convertToInternalMove(ExplicitMove explicitMove) {
		return this.theMachine.convertToFpgaMove(explicitMove);
	}

	@Override
	public Role convertToInternalRole(ExplicitRole explicitRole) {
		return this.theMachine.convertToFpgaRole(explicitRole);
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
