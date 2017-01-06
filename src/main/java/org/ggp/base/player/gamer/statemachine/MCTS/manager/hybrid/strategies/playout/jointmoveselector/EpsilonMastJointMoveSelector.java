package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.MastSingleMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector.RandomSingleMoveSelector;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class EpsilonMastJointMoveSelector extends JointMoveSelector implements OnlineTunableComponent {

	private MastSingleMoveSelector mastSelector;

	private RandomSingleMoveSelector randomSelector;

	private double[] epsilon;

	private double initialEpsilon;

	private double[] valuesForEpsilon;

	public EpsilonMastJointMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastSelector = new MastSingleMoveSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		this.randomSelector = new RandomSingleMoveSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.epsilon = null;

		this.initialEpsilon = Double.parseDouble(gamerSettings.getPropertyValue("JointMoveSelector.initialEpsilon"));

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		String toTuneString = gamerSettings.getPropertyValue("JointMoveSelector.tune");
		boolean toTune = Boolean.parseBoolean(toTuneString);
		if(toTune){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			String[] values = gamerSettings.getPropertyMultiValue("JointMoveSelector.valuesForEpsilon");
			this.valuesForEpsilon = new double[values.length];
			for(int i = 0; i < values.length; i++){
				this.valuesForEpsilon[i] = Double.parseDouble(values[i]);
			}
			sharedReferencesCollector.addComponentToTune(this);
		}else{
			this.valuesForEpsilon = null;
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
	public String getComponentParameters(String indentation) {

		String params = indentation + "SINGLE_MOVE_SELECTOR_1 = " + this.mastSelector.printComponent(indentation + "  ") +
				indentation + "SINGLE_MOVE_SELECTOR_2 = " + this.randomSelector.printComponent(indentation + "  ") +
				indentation + "INITIAL_EPSILON = " + this.initialEpsilon;

		if(this.valuesForEpsilon != null){
			String valuesForEpsilonString = "[ ";

			for(int i = 0; i < this.valuesForEpsilon.length; i++){

				valuesForEpsilonString += this.valuesForEpsilon[i] + " ";

			}

			valuesForEpsilonString += "]";

			params += indentation + "VALUES_FOR_TUNING_EPSILON = " + valuesForEpsilonString;
		}else{
			params += indentation + "VALUES_FOR_TUNING_EPSILON = null";
		}

		if(this.epsilon != null){
			String epsilonString = "[ ";

			for(int i = 0; i < this.epsilon.length; i++){

				epsilonString += this.epsilon[i] + " ";

			}

			epsilonString += "]";

			params += indentation + "epsilon = " + epsilonString;
		}else{
			params += indentation + "epsilon = null";
		}

		return params;

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
	public String printOnlineTunableComponent(String indentation) {
		return this.printComponent(indentation);
	}

	@Override
	public double[] getPossibleValues() {
		return this.valuesForEpsilon;
	}

	@Override
	public void setNewValuesFromIndices(int[] newValuesIndices) {
		// We are tuning only the parameter of myRole
		if(newValuesIndices.length == 1){

			this.epsilon[this.gameDependentParameters.getMyRoleIndex()] = this.valuesForEpsilon[newValuesIndices[0]];

		}else{ // We are tuning all parameters
			for(int i = 0; i <this.epsilon.length; i++){
				this.epsilon[i] = this.valuesForEpsilon[newValuesIndices[i]];
			}
		}
	}

}

