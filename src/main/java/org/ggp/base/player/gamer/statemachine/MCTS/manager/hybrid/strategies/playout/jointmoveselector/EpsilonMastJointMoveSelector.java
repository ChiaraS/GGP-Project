package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.DoubleTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.MastSingleMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.RandomSingleMoveSelector;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class EpsilonMastJointMoveSelector extends JointMoveSelector{

	private MastSingleMoveSelector mastSelector;

	private RandomSingleMoveSelector randomSelector;

	private DoubleTunableParameter epsilon;

	public EpsilonMastJointMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastSelector = new MastSingleMoveSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		this.randomSelector = new RandomSingleMoveSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		// Get default value for Epsilon (this is the value used for the roles for which we are not tuning the parameter)
		double fixedEpsilon = gamerSettings.getDoublePropertyValue("JointMoveSelector.fixedEpsilon");

		if(gamerSettings.getBooleanPropertyValue("JointMoveSelector.tuneEpsilon")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			this.epsilon = new DoubleTunableParameter(fixedEpsilon, gamerSettings.getDoublePropertyMultiValue("JointMoveSelector.valuesForEpsilon"));

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.epsilon);

		}else{
			this.epsilon = new DoubleTunableParameter(fixedEpsilon);
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastSelector.setReferences(sharedReferencesCollector);
		this.randomSelector.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent(){

		this.mastSelector.clearComponent();
		this.randomSelector.clearComponent();

		this.epsilon.clearParameter();

	}

	@Override
	public void setUpComponent(){

		this.mastSelector.setUpComponent();
		this.randomSelector.setUpComponent();

		this.epsilon.setUpParameter(this.gameDependentParameters.getNumRoles());

	}

	@Override
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();

		// For each role we check if the move for the role must be picked randomly or according to the MAST statistics
		// NOTE that a joint move might be composed of moves that have been picked randomly for some roles and moves that
		// have been picked according to MAST statistics for other roles.
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			if(this.random.nextDouble() < this.epsilon.getValuePerRole(i)){
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
	public String getComponentParameters(String indentation) {

		return indentation + "SINGLE_MOVE_SELECTOR_1 = " + this.mastSelector.printComponent(indentation + "  ") +
				indentation + "SINGLE_MOVE_SELECTOR_2 = " + this.randomSelector.printComponent(indentation + "  ") +
				indentation + "EPSILON = " + this.epsilon.getParameters(indentation + "  ");

	}

}

