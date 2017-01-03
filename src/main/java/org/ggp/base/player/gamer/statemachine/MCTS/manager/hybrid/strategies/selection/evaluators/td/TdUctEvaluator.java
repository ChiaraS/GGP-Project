package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UctEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled.TdDecoupledMctsNode;
import org.ggp.base.util.statemachine.structure.Move;

public class TdUctEvaluator extends UctEvaluator {

	private GlobalExtremeValues globalExtremeValues;

	public TdUctEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.globalExtremeValues = sharedReferencesCollector.getGlobalExtremeValues();
	}

	@Override
	protected double computeExploitation(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats){

		if(theNode instanceof TdDecoupledMctsNode){

			double moveVisits = theMoveStats.getVisits();
			double scoreSum = theMoveStats.getScoreSum();

			if(moveVisits == 0){
				return -1.0;
			}else{

				TdDecoupledMctsNode theTdNode = (TdDecoupledMctsNode)theNode;

				double minValue = theTdNode.getMinStateActionValueForRole(roleIndex);
				double maxValue = theTdNode.getMaxStateActionValueForRole(roleIndex);

				// The only case when minValue > maxValue is if both values are not set yet
				// (thus have the default values of +Double.MAX_VALUE and -infinity respectively).
				// So if they are not set or have the same value, we use the global values.
				if(minValue >= maxValue){
					minValue = this.globalExtremeValues.getGlobalMinValues()[roleIndex];
					maxValue = this.globalExtremeValues.getGlobalMaxValues()[roleIndex];
				}

				// Same check as before, even though it's more unlikely to be true
				if(minValue >= maxValue){
					minValue = this.globalExtremeValues.getDefaultGlobalMinValue();
					maxValue = this.globalExtremeValues.getDefaultGlobalMaxValue();
				}

				return this.normalize((scoreSum / moveVisits), minValue, maxValue);
			}
		}else{
			throw new RuntimeException("TdUctEvaluator-computeExploitation(): no method implemented to compute exploitation for node type (" + theNode.getClass().getSimpleName() + ").");
		}

	}

	private double normalize(double value, double leftExtreme, double rightExtreme){

		return (value - leftExtreme)/(rightExtreme - leftExtreme);

	}

	@Override
	public String getComponentParameters(String indentation) {
		return super.getComponentParameters(indentation) + indentation + "DEFAULT_GLOBAL_MIN_VALUE = " + this.globalExtremeValues.getDefaultGlobalMinValue() + indentation + "DEFAUL_GLOBAL_MAX_VALUE = " + this.globalExtremeValues.getDefaultGlobalMaxValue();
	}

}
