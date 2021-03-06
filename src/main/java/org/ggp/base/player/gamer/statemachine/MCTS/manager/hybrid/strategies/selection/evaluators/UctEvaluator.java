package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.statemachine.structure.Move;
/**
 * @author C.Sironi
 *
 */
public class UctEvaluator extends MoveEvaluator {

	/**
	 * This is an array so that it can memorize a different value for C for each role in the game.
	 * If a single value has to be used then all values in the array will be the same.
	 */
	protected TunableParameter c;

	/**
	 * Default value to assign to an unexplored move (first play urgency).
	 * This is an array so that it can memorize a different value for C for each role in the game.
	 * If a single value has to be used then all values in the array will be the same.
	 */
	protected TunableParameter fpu;

	public UctEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);


		this.c = this.createTunableParameter("MoveEvaluator", "C", gamerSettings, sharedReferencesCollector);

		/*
		// Get default value for C (this is the value used for the roles for which we are not tuning the parameter)
		double fixedC = gamerSettings.getDoublePropertyValue("MoveEvaluator.fixedC");

		if(gamerSettings.getBooleanPropertyValue("MoveEvaluator.tuneC")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// MoveEvaluator.valuesForC=v1;v2;...;vn
			// The values are listed separated by ; with no spaces.
			// We also need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters.
			// Moreover, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			double[] possibleValues = gamerSettings.getDoublePropertyMultiValue("MoveEvaluator.valuesForC");
			double[] possibleValuesPenalty = null;
			if(gamerSettings.specifiesProperty("MoveEvaluator.possibleValuesPenaltyForC")){
				possibleValuesPenalty =  gamerSettings.getDoublePropertyMultiValue("MoveEvaluator.possibleValuesPenaltyForC");
			}
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty("MoveEvaluator.tuningOrderIndexC")){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue("MoveEvaluator.tuningOrderIndexC");
			}

			this.c = new DoubleTunableParameter("C", fixedC, possibleValues, possibleValuesPenalty, tuningOrderIndex);

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.c);

		}else{
			this.c = new DoubleTunableParameter("C", fixedC);
		}*/

		this.fpu = this.createTunableParameter("MoveEvaluator", "Fpu", gamerSettings, sharedReferencesCollector);

		/*
		// Get default value for Fpu (this is the value used for the roles for which we are not tuning the parameter)
		double fixedFpu = gamerSettings.getDoublePropertyValue("MoveEvaluator.fixedFpu");

		if(gamerSettings.getBooleanPropertyValue("MoveEvaluator.tuneFpu")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// MoveEvaluator.valuesForFpu=v1;v2;...;vn
			// The values are listed separated by ; with no spaces.
			// We also need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters.
			// Moreover, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			double[] possibleValues = gamerSettings.getDoublePropertyMultiValue("MoveEvaluator.valuesForFpu");
			double[] possibleValuesPenalty = null;
			if(gamerSettings.specifiesProperty("MoveEvaluator.possibleValuesPenaltyForFpu")){
				possibleValuesPenalty =  gamerSettings.getDoublePropertyMultiValue("MoveEvaluator.possibleValuesPenaltyForFpu");
			}
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty("MoveEvaluator.tuningOrderIndexFpu")){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue("MoveEvaluator.tuningOrderIndexFpu");
			}

			this.fpu = new DoubleTunableParameter("Fpu", fixedFpu, possibleValues, possibleValuesPenalty, tuningOrderIndex);

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.fpu);

		}else{
			this.fpu = new DoubleTunableParameter("Fpu", fixedFpu);
		}
		*/

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){
		this.c.clearParameter();
		this.fpu.clearParameter();
	}

	@Override
	public void setUpComponent(){
		this.c.setUpParameter(this.gameDependentParameters.getNumRoles());
		this.fpu.setUpParameter(this.gameDependentParameters.getNumRoles());
	}

	@Override
	public double computeMoveValue(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats, int parentVisits) {

		double exploitation = this.computeExploitation(theNode, theMove, roleIndex, theMoveStats);
		double exploration = this.computeExploration(theNode, roleIndex, theMoveStats, parentVisits);

		double fpuValuePerRole = this.fpu.getValuePerRole(roleIndex);
		//System.out.println("Role=" + roleIndex + "Fpu=" + fpuValuePerRole);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			return fpuValuePerRole;
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

	protected double computeExploration(MctsNode theNode, int roleIndex, MoveStats theMoveStats, int parentVisits){

		//int parentVisits = theNode.getTotVisits()[roleIndex];

		double cValuePerRole = this.c.getValuePerRole(roleIndex);

		//System.out.println("Role=" + roleIndex + "C=" + cValuePerRole);

		double moveVisits = theMoveStats.getVisits();

		if(parentVisits != 0 && moveVisits != 0){

			return (cValuePerRole * (Math.sqrt(Math.log(parentVisits)/moveVisits)));
		}else{
			return -1.0;
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		return indentation + "C = " + this.c.getParameters(indentation + "  ") +
				indentation + "FPU = " + this.fpu.getParameters(indentation + "  ");

	}

}
