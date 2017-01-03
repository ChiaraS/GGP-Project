package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.statemachine.structure.Move;

public class UctEvaluator extends MoveEvaluator implements OnlineTunableComponent{

	/**
	 * This is an array so that it can memorize a different value for C for each role in the game.
	 * If a single value has to be used then all values in the array will be the same.
	 */
	protected double[] c;

	protected double initialC;

	private double[] valuesForC;
	/**
	 * Default value to assign to an unexplored move.
	 */
	protected double unexploredMoveDefaultValue;

	public UctEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.c = null;

		this.initialC = Double.parseDouble(gamerSettings.getPropertyValue("MoveEvaluator.initialC"));

		this.unexploredMoveDefaultValue = Double.parseDouble(gamerSettings.getPropertyValue("MoveEvaluator.unexploredMoveDefaultValue"));

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		String toTuneString = gamerSettings.getPropertyValue("MoveEvaluator.tune");
		boolean toTune = Boolean.parseBoolean(toTuneString);
		if(toTune){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			String[] values = gamerSettings.getPropertyMultiValue("MoveEvaluator.valuesForC");
			this.valuesForC = new double[values.length];
			for(int i = 0; i < values.length; i++){
				this.valuesForC[i] = Integer.parseInt(values[i]);
			}
			sharedReferencesCollector.setTheComponentToTune(this);
		}else{
			this.valuesForC = null;
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.c = null;
	}

	@Override
	public void setUpComponent(){
		this.c = new double[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.c[i] = initialC;
		}
	}

	@Override
	public double computeMoveValue(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats) {

		double exploitation = this.computeExploitation(theNode, theMove, roleIndex, theMoveStats);
		double exploration = this.computeExploration(theNode, roleIndex, theMoveStats);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			return this.unexploredMoveDefaultValue;
		}
	}

	protected double computeExploitation(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats){

		double moveVisits = theMoveStats.getVisits();
		double score = theMoveStats.getScoreSum();

		if(moveVisits == 0){
			return -1.0;
		}else{
			return ((score / moveVisits) / 100.0);
		}

	}

	protected double computeExploration(MctsNode theNode, int roleIndex, MoveStats theMoveStats){

		int nodeVisits = theNode.getTotVisits();

		double moveVisits = theMoveStats.getVisits();

		if(nodeVisits != 0 && moveVisits != 0){

			return (this.c[roleIndex] * (Math.sqrt(Math.log(nodeVisits)/moveVisits)));
		}else{
			return -1.0;
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "INITIAL_C_CONSTANT = " + this.initialC + indentation + "UNEXPLORED_MOVE_DEFAULT_VALUE = " + this.unexploredMoveDefaultValue;

		if(this.valuesForC != null){

			String valuesForCString = "[ ";

			for(int i = 0; i < this.valuesForC.length; i++){

				valuesForCString += this.valuesForC[i] + " ";

			}

			valuesForCString += "]";

			params += indentation + "VALUES_FOR_TUNING_C = " + valuesForCString;
		}else{
			params += indentation + "VALUES_FOR_TUNING_C = " + "null";
		}

		if(this.c != null){

			String cString = "[ ";

			for(int i = 0; i < this.c.length; i++){

				cString += this.c[i] + " ";

			}

			cString += "]";

			params += indentation + "c = " + cString;
		}else{
			params += indentation + "c = " + "null";
		}

		return params;
	}

	@Override
	public void setNewValues(double[] newValues){

		// We are tuning only the constant of myRole
		if(newValues.length == 1){
			this.c[this.gameDependentParameters.getMyRoleIndex()] = newValues[0];

			//System.out.println("C = " + this.c[this.gameDependentParameters.getMyRoleIndex()]);

		}else{ // We are tuning all constants
			for(int i = 0; i <this.c.length; i++){
				this.c[i] = newValues[i];
			}
		}

	}

	@Override
	public String printOnlineTunableComponent(String indentation) {

		return this.printComponent(indentation);

	}

	@Override
	public double[] getPossibleValues() {
		return this.valuesForC;
	}

}
