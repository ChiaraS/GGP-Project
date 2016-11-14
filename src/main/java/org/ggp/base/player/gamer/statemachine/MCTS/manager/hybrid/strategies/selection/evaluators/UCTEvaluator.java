package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public class UCTEvaluator extends MoveEvaluator implements OnlineTunableComponent{

	/**
	 * This is an array so that it can memorize a different value for C for each role in the game.
	 * If a single value has to be used then all values in the array will be the same.
	 */
	protected double[] c;

	protected double initialC;

	/**
	 * Default value to assign to an unexplored move.
	 */
	protected double defaultValue;

	public UCTEvaluator(GameDependentParameters gameDependentParameters, double initialC, double defaultValue){

		super(gameDependentParameters);

		this.c = null;
		this.initialC = initialC;
		this.defaultValue = defaultValue;

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
	public double computeMoveValue(MCTSNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats) {

		double exploitation = this.computeExploitation(theNode, theMove, roleIndex, theMoveStats);
		double exploration = this.computeExploration(theNode, roleIndex, theMoveStats);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			return this.defaultValue;
		}
	}

	protected double computeExploitation(MCTSNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats){

		double moveVisits = theMoveStats.getVisits();
		double score = theMoveStats.getScoreSum();

		if(moveVisits == 0){
			return -1.0;
		}else{
			return ((score / moveVisits) / 100.0);
		}

	}

	protected double computeExploration(MCTSNode theNode, int roleIndex, MoveStats theMoveStats){

		int nodeVisits = theNode.getTotVisits();

		double moveVisits = theMoveStats.getVisits();

		if(nodeVisits != 0 && moveVisits != 0){

			return (this.c[roleIndex] * (Math.sqrt(Math.log(nodeVisits)/moveVisits)));
		}else{
			return -1.0;
		}

	}

	@Override
	public String getEvaluatorParameters() {

		String roleParams = "[ ";

		for(int i = 0; i <this.c.length; i++){

			roleParams += this.c[i] + " ";

		}

		roleParams += "]";

		return "C_CONSTANTS = " + roleParams + ", DEFAULT_VALUE = " + this.defaultValue;
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
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printEvaluator() + ")";

	}

}
