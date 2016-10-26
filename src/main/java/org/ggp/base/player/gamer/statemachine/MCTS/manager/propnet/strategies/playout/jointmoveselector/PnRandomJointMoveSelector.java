package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnRandomJointMoveSelector implements PnJointMoveSelector {

	protected InternalPropnetStateMachine theMachine;

	public PnRandomJointMoveSelector(InternalPropnetStateMachine theMachine) {
		this.theMachine = theMachine;
	}

	@Override
	public List<CompactMove> getJointMove(CompactMachineState state) throws MoveDefinitionException {
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
