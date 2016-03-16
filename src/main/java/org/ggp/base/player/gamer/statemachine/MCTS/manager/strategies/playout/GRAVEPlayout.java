package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import java.util.List;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class GRAVEPlayout extends RandomPlayout {

    private List<List<InternalPropnetMove>> allJointMoves;

	public GRAVEPlayout(InternalPropnetStateMachine theMachine, List<List<InternalPropnetMove>> allJointMoves) {
		super(theMachine);
		this.allJointMoves = allJointMoves;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state,
			int[] playoutVisitedNodes, int maxDepth) {

		this.allJointMoves.clear();

		return super.playout(state, playoutVisitedNodes, maxDepth);

	}

	@Override
	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{

        List<InternalPropnetMove> jointMove = super.getJointMove(state);

		this.allJointMoves.add(jointMove);

		return jointMove;
	}

	@Override
	public String getStrategyParameters() {
		return super.getStrategyParameters();
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}







}
