package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled.TDDecoupledMCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public class TDUCTEvaluator extends UCTEvaluator {

	private GlobalExtremeValues globalExtremeValues;

	public TDUCTEvaluator(GameDependentParameters gameDependentParameters, double c, double defaultValue, GlobalExtremeValues globalExtremeValues) {
		super(gameDependentParameters, c, defaultValue);

		this.globalExtremeValues = globalExtremeValues;
	}

	@Override
	protected double computeExploitation(MCTSNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats){

		if(theNode instanceof TDDecoupledMCTSNode){

			double moveVisits = theMoveStats.getVisits();
			double scoreSum = theMoveStats.getScoreSum();

			if(moveVisits == 0){
				return -1.0;
			}else{

				TDDecoupledMCTSNode theTDNode = (TDDecoupledMCTSNode)theNode;

				double minValue = theTDNode.getMinStateActionValueForRole(roleIndex);
				double maxValue = theTDNode.getMaxStateActionValueForRole(roleIndex);

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
			throw new RuntimeException("TDUCTEvaluator-computeExploitation(): no method implemented to compute exploitation for node type (" + theNode.getClass().getSimpleName() + ").");
		}

	}

	private double normalize(double value, double leftExtreme, double rightExtreme){

		return (value - leftExtreme)/(rightExtreme - leftExtreme);

	}

	@Override
	public String getEvaluatorParameters() {
		return super.getEvaluatorParameters() + ", DEFAUL_GLOBAL_MIN_VALUE = " + this.globalExtremeValues.getDefaultGlobalMinValue() + ", DEFAUL_GLOBAL_MAX_VALUE = " + this.globalExtremeValues.getDefaultGlobalMaxValue();
	}

}
