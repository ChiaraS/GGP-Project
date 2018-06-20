package org.ggp.base.util.statemachine.abstractsm;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
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

import csironi.ggp.course.utils.MyPair;


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

		return this.theMachine.getCompactInitialState();

	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException, StateMachineException{

		if(state instanceof CompactMachineState && role instanceof CompactRole){

			return new ArrayList<Move>(this.theMachine.getCompactLegalMoves((CompactMachineState)state, (CompactRole)role));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-getLegalMoves(): detected wrong type for machine state and/or role: [" + state.getClass().getSimpleName() + ", " + role.getClass().getSimpleName() + "].");
		}

	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException, StateMachineException {

		if(state instanceof CompactMachineState){

			return this.theMachine.getCompactNextState((CompactMachineState)state, this.convertListOfMoves(moves));

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

		return new ArrayList<Role>(this.theMachine.getCompactRoles());
	}

	private List<CompactMove> convertListOfMoves(List<Move> moves){

		List<CompactMove> compactMoves = new ArrayList<CompactMove>();

		for(Move m : moves){

			if(m instanceof CompactMove){

				compactMoves.add((CompactMove)m);

			}else{
				// Not throwing StateMachineException because failure here is not the fault of the state machine but
				// the fault of some programming error that caused the wrong state and role formats to end up here.
				throw new RuntimeException("CompactStateMachine-checkListOfMoves(): detected wrong type for move: [" + m.getClass().getSimpleName() + "].");
			}

		}

		return compactMoves;

	}

	@Override
	public String getName() {

		return this.getClass().getSimpleName() + "(" + this.theMachine.getName() + ")";

	}

	@Override
	public MyPair<double[], Double> fastPlayouts(MachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException {
		if(state instanceof CompactMachineState){

			return this.theMachine.fastPlayouts((CompactMachineState)state, numSimulationsPerPlayout, maxDepth);

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-fastPlayouts(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public List<Move> getJointMove(List<List<Move>> legalMovesPerRole, MachineState state) throws MoveDefinitionException {
		if(state instanceof CompactMachineState){

			List<List<CompactMove>> comapctLegalMovesPerRole = new ArrayList<List<CompactMove>>();
			for(List<Move> legalMoves: legalMovesPerRole) {
				List<CompactMove> compactLegalMoves = new ArrayList<CompactMove>();
				for(Move move: legalMoves) {
					if(move instanceof CompactMove) {
						compactLegalMoves.add((CompactMove)move);
					}else {
						throw new RuntimeException("CompactStateMachine-getJointMove(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
					}
				}
				comapctLegalMovesPerRole.add(compactLegalMoves);
			}

			return new ArrayList<Move>(this.theMachine.getJointMove(comapctLegalMovesPerRole, (CompactMachineState)state));

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-getJointMove(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

	@Override
	public Move getMoveForRole(List<Move> legalMoves, MachineState state, Role role) throws MoveDefinitionException {
		if(state instanceof CompactMachineState){

			if(role instanceof CompactRole) {

				List<CompactMove> compactLegalMoves = new ArrayList<CompactMove>();
				for(Move move: legalMoves) {
					if(move instanceof CompactMove) {
						compactLegalMoves.add((CompactMove)move);
					}else {
						throw new RuntimeException("CompactStateMachine-getMoveForRole(): detected wrong type for move: [" + move.getClass().getSimpleName() + "].");
					}
				}

				return this.theMachine.getMoveForRole(compactLegalMoves, (CompactMachineState)state, (CompactRole)role);

			}else {
				throw new RuntimeException("CompactStateMachine-getMoveForRole(): detected wrong type for role: [" + role.getClass().getSimpleName() + "].");
			}

		}else{
			// Not throwing StateMachineException because failure here is not the fault of the state machine but
			// the fault of some programming error that caused the wrong state and role formats to end up here.
			throw new RuntimeException("CompactStateMachine-getMoveForRole(): detected wrong type for machine state: [" + state.getClass().getSimpleName() + "].");
		}
	}

}
