package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;

public class ProverRandomJointMoveSelector implements ProverJointMoveSelector {

	protected StateMachine theMachine;

	public ProverRandomJointMoveSelector(StateMachine theMachine) {
		this.theMachine = theMachine;
	}

	@Override
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException {
		return this.theMachine.getRandomJointMove(state);
	}

	@Override
	public String getJointMoveSelectorParameters() {
		return null;
	}

	@Override
	public String printJointMoveSelector() {
		String params = this.getJointMoveSelectorParameters();

		if(params != null){
			return "(JOINT_MOVE_SEL = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(JOINT_MOVE_SEL = " + this.getClass().getSimpleName() + ")";
		}
	}

}
