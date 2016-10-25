package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class PnEpsilonMASTJointMoveSelector implements PnJointMoveSelector {

	private PnMASTJointMoveSelector mastSelector;

	private PnRandomJointMoveSelector randomSelector;

	private Random random;

	private double epsilon;

	public PnEpsilonMASTJointMoveSelector(InternalPropnetStateMachine theMachine, Random random, Map<InternalPropnetMove, MoveStats> mastStatistics, double epsilon) {
		this.mastSelector = new PnMASTJointMoveSelector(theMachine, random, mastStatistics);
		this.randomSelector = new PnRandomJointMoveSelector(theMachine);
		this.random = random;
		this.epsilon = epsilon;
	}

	@Override
	public List<InternalPropnetMove> getJointMove(
			InternalPropnetMachineState state) throws MoveDefinitionException {

		if(this.random.nextDouble() < this.epsilon){
    		// Choose random action with probability epsilon
    		return this.randomSelector.getJointMove(state);
    	}else{
    		// Choose move with highest average score
    		return this.mastSelector.getJointMove(state);
    	}
	}

	@Override
	public String getJointMoveSelectorParameters() {
		return "SUB_SELECTOR1 = " + this.mastSelector.printJointMoveSelector() + ", SUB_SELECTOR2 = " + this.randomSelector.printJointMoveSelector() + ", EPSILON = " + this.epsilon;
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
