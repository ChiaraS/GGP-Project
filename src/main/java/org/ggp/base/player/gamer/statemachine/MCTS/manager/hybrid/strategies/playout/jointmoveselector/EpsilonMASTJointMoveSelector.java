package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachinenew.AbstractStateMachine;

public class EpsilonMASTJointMoveSelector implements JointMoveSelector {

	private MASTJointMoveSelector mastSelector;

	private RandomJointMoveSelector randomSelector;

	private Random random;

	private double epsilon;

	public EpsilonMASTJointMoveSelector(AbstractStateMachine theMachine, Random random, Map<Move, MoveStats> mastStatistics, double epsilon) {
		this.mastSelector = new MASTJointMoveSelector(theMachine, random, mastStatistics);
		this.randomSelector = new RandomJointMoveSelector(theMachine);
		this.random = random;
		this.epsilon = epsilon;
	}

	@Override
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException {

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

