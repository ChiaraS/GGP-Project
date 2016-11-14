package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.MASTSingleMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.RandomSingleMoveSelector;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class EpsilonMASTJointMoveSelector extends JointMoveSelector implements OnlineTunableComponent {

	private MASTSingleMoveSelector mastSelector;

	private RandomSingleMoveSelector randomSelector;

	private Random random;

	private double[] epsilon;

	private double initialEpsilon;

	public EpsilonMASTJointMoveSelector(GameDependentParameters gameDependentParameters, Random random, Map<Move, MoveStats> mastStatistics, double initialEpsilon){

		super(gameDependentParameters);

		this.mastSelector = new MASTSingleMoveSelector(gameDependentParameters, random, mastStatistics);
		this.randomSelector = new RandomSingleMoveSelector(gameDependentParameters);
		this.random = random;

		this.epsilon = null;

		this.initialEpsilon = initialEpsilon;

	}

	@Override
	public void clearComponent(){

		this.mastSelector.clearComponent();
		this.randomSelector.clearComponent();

		this.epsilon = null;

	}

	@Override
	public void setUpComponent(){

		this.mastSelector.setUpComponent();
		this.randomSelector.setUpComponent();

		this.epsilon = new double[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.epsilon[i] = this.initialEpsilon;
		}

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
			this.epsilon[this.gameDependentParameters.getMyRoleIndex()] = newValues[0];
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

