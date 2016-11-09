package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.MASTSingleMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.RandomSingleMoveSelector;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class EpsilonMASTJointMoveSelector implements JointMoveSelector, OnlineTunableComponent {

	private MASTSingleMoveSelector mastSelector;

	private RandomSingleMoveSelector randomSelector;

	private Random random;

	private double[] epsilon;

	private int myRoleIndex;

	public EpsilonMASTJointMoveSelector(AbstractStateMachine theMachine, Random random, Map<Move, MoveStats> mastStatistics, double initialEpsilon, int numRoles, int myRoleIndex) {
		this.mastSelector = new MASTSingleMoveSelector(theMachine, random, mastStatistics);
		this.randomSelector = new RandomSingleMoveSelector(theMachine);
		this.random = random;

		this.epsilon = new double[numRoles];

		for(int i = 0; i < numRoles; i++){
			this.epsilon[i] = initialEpsilon;
		}

		this.myRoleIndex = myRoleIndex;

	}

	@Override
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();

		// For each role we check if the move for the role must be picked randomly or according to the MAST statistics
		// NOTE that a joint move might be composed of moves that have been picked randomly for some roles and moves that
		// have been picked according to MAST statistics for other roles.
		for(int i = 0; i < this.epsilon.length; i++){
			if(this.random.nextDouble() < this.epsilon[i]){
	    		// Choose random action with probability epsilon
				jointMove.add(this.randomSelector.getMoveForRole(state, i));
	    	}else{
	    		// Choose move with highest average score
	    		jointMove.add(this.mastSelector.getMoveForRole(state, i));
	    	}
		}

		return jointMove;
	}

	@Override
	public String getJointMoveSelectorParameters() {

		String roleParams = "[ ";

		for(int i = 0; i <this.epsilon.length; i++){

			roleParams += this.epsilon[i] + " ";

		}

		roleParams += "]";

		return "SUB_SELECTOR1 = " + this.mastSelector.printSingleMoveSelector() + ", SUB_SELECTOR2 = " + this.randomSelector.printSingleMoveSelector() + ", EPSILON = " + roleParams;
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

	@Override
	public void setNewValues(double[] newValues) {
		// We are tuning only the constant of myRole
		if(newValues.length == 1){
			this.epsilon[this.myRoleIndex] = newValues[0];
		}else{ // We are tuning all constants
			for(int i = 0; i <this.epsilon.length; i++){
				this.epsilon[i] = newValues[i];
			}
		}
	}

	@Override
	public String printOnlineTunableComponent() {
		return "(ONLINE_TUNABLE_COMPONENT = " + this.printJointMoveSelector() + ")";
	}

}

