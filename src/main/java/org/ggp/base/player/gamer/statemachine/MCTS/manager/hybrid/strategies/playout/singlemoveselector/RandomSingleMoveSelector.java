package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class RandomSingleMoveSelector implements SingleMoveSelector {

	protected AbstractStateMachine theMachine;

	public RandomSingleMoveSelector(AbstractStateMachine theMachine) {
		this.theMachine = theMachine;
	}

	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {
		return this.theMachine.getRandomMove(state, this.theMachine.getRoles().get(roleIndex));
	}

	@Override
	public String getSingleMoveSelectorParameters() {
		return null;
	}

	@Override
	public String printSingleMoveSelector() {
		String params = this.getSingleMoveSelectorParameters();

		if(params != null){
			return "(SINGLE_MOVE_SEL = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(SINGLE_MOVE_SEL = " + this.getClass().getSimpleName() + ")";
		}
	}

}
