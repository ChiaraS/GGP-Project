package org.ggp.base.util.statemachine.abstractsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ggp.base.util.Pair;
import org.ggp.base.util.gdl.grammar.Gdl;
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
	public List<Integer> getAllGoalsForOneRole(MachineState state, Role role) throws StateMachineException {
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

			Vector<Pair<FpgaMachineState,Vector<FpgaMove>>> allFpgaJointMovesAndNextStates = this.theMachine.getAllFpgaJointMovesAndNextStates((FpgaMachineState)state);

			Map<List<Move>, MachineState> allJointMovesAndNextStates = new HashMap<List<Move>, MachineState>();

			for(Pair<FpgaMachineState,Vector<FpgaMove>> nextFpgaStateAndJointMove : allFpgaJointMovesAndNextStates) {

				List<Move> jointMove = new ArrayList<Move>();
				for(FpgaMove fpgaMove : nextFpgaStateAndJointMove.right) {
					jointMove.add(fpgaMove);
				}
				allJointMovesAndNextStates.put(jointMove, nextFpgaStateAndJointMove.left);
			}

			return allJointMovesAndNextStates;

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("FpgaStateMachine-getAllJointMovesAndNextStates(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}

	}

}
