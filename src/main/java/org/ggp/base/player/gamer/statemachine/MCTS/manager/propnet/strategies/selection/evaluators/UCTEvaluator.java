package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class UCTEvaluator implements MoveEvaluator, OnlineTunableComponent {

	protected double c;

	/**
	 * Default value to assign to an unexplored move.
	 */
	protected double defaultValue;

	public UCTEvaluator(double c, double defaultValue) {
		this.c = c;
		this.defaultValue = defaultValue;

	}

	@Override
	public double computeMoveValue(int nodeVisits,
			InternalPropnetMove theMove, MoveStats theMoveStats) {

		double exploitation = this.computeExploitation(nodeVisits, theMove, theMoveStats);
		double exploration = this.computeExploration(nodeVisits, theMoveStats);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			return this.defaultValue;
		}
	}

	protected double computeExploitation(int nodeVisits, InternalPropnetMove theMove,  MoveStats theMoveStats){

		double moveVisits = theMoveStats.getVisits();
		double score = theMoveStats.getScoreSum();

		if(moveVisits == 0){
			return -1.0;
		}else{
			return ((score / moveVisits) / 100.0);
		}

	}

	protected double computeExploration(int nodeVisits, MoveStats theMoveStats){

		double moveVisits = theMoveStats.getVisits();

		if(nodeVisits != 0 && moveVisits != 0){
			return (this.c * (Math.sqrt(Math.log(nodeVisits)/moveVisits)));
		}else{
			return -1.0;
		}

	}

	@Override
	public String getEvaluatorParameters() {
		return "C_CONSTANT = " + this.c + ", DEFAULT_VALUE = " + this.defaultValue;
	}

	@Override
	public String printEvaluator() {
		String params = this.getEvaluatorParameters();

		if(params != null){
			return "(EVALUATOR_TYPE = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(EVALUATOR_TYPE = " + this.getClass().getSimpleName() + ")";
		}
	}

	@Override
	public void setNewValue(double newValue) {

		this.c = newValue;

	}

	@Override
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printEvaluator() + ")";

	}

}
