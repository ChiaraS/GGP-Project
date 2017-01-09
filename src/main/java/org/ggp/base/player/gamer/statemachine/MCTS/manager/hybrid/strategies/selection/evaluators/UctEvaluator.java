package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.statemachine.structure.Move;
/**
 * !!! FOR NOW THIS CLASS CANNOT TUNE FPU!
 * @author C.Sironi
 *
 */
public class UctEvaluator extends MoveEvaluator implements OnlineTunableComponent{

	/**
	 * This is an array so that it can memorize a different value for C for each role in the game.
	 * If a single value has to be used then all values in the array will be the same.
	 */
	protected double[] c;

	protected double initialC;

	private double[] valuesForC;
	/**
	 * Default value to assign to an unexplored move (first play urgency).
	 * This is an array so that it can memorize a different value for C for each role in the game.
	 * If a single value has to be used then all values in the array will be the same.
	 */
	protected double[] fpu;

	protected double initialFpu;

	private double[] valuesForFpu;

	public UctEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.c = null;

		this.initialC = gamerSettings.getDoublePropertyValue("MoveEvaluator.initialC");

		this.fpu = null;

		this.initialFpu = gamerSettings.getDoublePropertyValue("MoveEvaluator.initialFpu");

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		boolean tuneC = gamerSettings.getBooleanPropertyValue("MoveEvaluator.tuneC");
		if(tuneC){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			this.valuesForC = gamerSettings.getDoublePropertyMultiValue("MoveEvaluator.valuesForC");
		}else{
			this.valuesForC = null;
		}
		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		boolean tuneFpu = gamerSettings.getBooleanPropertyValue("MoveEvaluator.tuneFpu");
		if(tuneFpu){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			this.valuesForFpu = gamerSettings.getDoublePropertyMultiValue("MoveEvaluator.valuesForFpu");
		}else{
			this.valuesForFpu = null;
		}

		if(tuneC || tuneFpu){
			sharedReferencesCollector.addComponentToTune(this);
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.c = null;
		this.fpu = null;
	}

	@Override
	public void setUpComponent(){
		this.c = new double[this.gameDependentParameters.getNumRoles()];
		this.fpu = new double[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.c[i] = this.initialC;
			this.fpu[i] = this.initialFpu;
		}
	}

	@Override
	public double computeMoveValue(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats) {

		double exploitation = this.computeExploitation(theNode, theMove, roleIndex, theMoveStats);
		double exploration = this.computeExploration(theNode, roleIndex, theMoveStats);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			return this.fpu[roleIndex];
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

		String params = indentation + "INITIAL_C_CONSTANT = " + this.initialC + indentation + "INITIAL_FPU = " + this.initialFpu;

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

		if(this.valuesForFpu != null){

			String valuesForFpuString = "[ ";

			for(int i = 0; i < this.valuesForFpu.length; i++){

				valuesForFpuString += this.valuesForFpu[i] + " ";

			}

			valuesForFpuString += "]";

			params += indentation + "VALUES_FOR_TUNING_FPU = " + valuesForFpuString;
		}else{
			params += indentation + "VALUES_FOR_TUNING_FPU = " + "null";
		}

		if(this.fpu != null){

			String fpuString = "[ ";

			for(int i = 0; i < this.c.length; i++){

				fpuString += this.fpu[i] + " ";

			}

			fpuString += "]";

			params += indentation + "fpu = " + fpuString;
		}else{
			params += indentation + "fpu = " + "null";
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

	@Override
	public void setNewValuesFromIndices(int[] newValuesIndices) {
		// We are tuning only the parameter of myRole
		if(newValuesIndices.length == 1){

			this.c[this.gameDependentParameters.getMyRoleIndex()] = this.valuesForC[newValuesIndices[0]];

		}else{ // We are tuning all parameters
			for(int i = 0; i <this.c.length; i++){
				this.c[i] = this.valuesForC[newValuesIndices[i]];
			}
		}
	}

}
