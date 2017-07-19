package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class GameDependentParameters {

	// Parameters that change for every game:

	private AbstractStateMachine theMachine;

	private int numRoles;

	private int myRoleIndex;

	// Parameters that change for every game step:

	/**
	 * Game step for which we are currently performing the search.
	 */
	private int gameStep;
	/**
	 * Game step for which we performing the last search.
	 * NOTE that this might not equal (gameStep-1) because the gamer that uses the MctsManager
	 * might not call the search for each game step (e.g. if for a step there is no sufficient
	 * time before the timeout). Keeping track of the previous step for which the search was
	 * performed might be needed for some of the strategies.
	 */
	private int previousGameStep;

	/**
	 * Iterations performed so far for the current step.
	 * NOTE: when using multiple playouts in the same iteration the number of iterations
	 * will be increased by the number of performed playouts and not only by 1.
	 */
	private int stepIterations;

	/**
	 * Node visited so far for the current step.
	 * NOTE: when performing multiple playouts each node visited by any playout counts as 1.
	 * The nodes visited in the tree before performing multiple playouts are counted only once
	 * and not once per performed playout.
	 *
	 * NOTE: this value is not up-to-date at any moment but only in between playouts because
	 * it is updated only at the end of the playout.
	 */
	private int stepVisitedNodes;

	/**
	 * Sum of the scores obtained for each role over all the iterations performed
	 * so far for the current game step.
	 */
	private double[] scoreSumForStep;

	// Parameters that change for every iteration:

	/**
	 * Number of visited nodes in the current iteration.
	 */
	private int currentIterationVisitedNodes;

	public GameDependentParameters(){
		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;

		this.gameStep = 0;
		this.previousGameStep = 0;

		this.stepIterations = 0;
		this.stepVisitedNodes = 0;
		this.scoreSumForStep = null;

		this.currentIterationVisitedNodes = 0;
	}

	public AbstractStateMachine getTheMachine(){
		return this.theMachine;
	}

	public int getNumRoles(){
		return this.numRoles;
	}

	public int getMyRoleIndex(){
		return this.myRoleIndex;
	}

	public int getGameStep(){
		return this.gameStep;
	}

	public void setGameStep(int newGameStep){
		this.previousGameStep = this.gameStep;
		this.gameStep = newGameStep;

		/*
		if(this.scoreSumForStep != null){
			for(int roleIndex = 0; roleIndex < this.scoreSumForStep.length; roleIndex++){
				System.out.println("ROLE=" +
						this.theMachine.convertToExplicitRole(this.theMachine.getRoles().get(roleIndex)) +
						", SCORE_SUM=" + this.scoreSumForStep[roleIndex] + ", ITERATIONS=" + this.stepIterations + ", AVG=" +
						(this.stepIterations != 0 ? (this.scoreSumForStep[roleIndex]/((double)this.stepIterations)) : "50"));
			}
		}else{
			System.out.println("null");
		}
		*/

	}

	public int getPreviousGameStep(){
		return this.previousGameStep;
	}

	public void increaseStepIterations(){
		this.stepIterations++;
	}

	public int getStepIterations(){
		return this.stepIterations;
	}

	public void increaseStepVisitedNodes(int increase){
		this.stepVisitedNodes += increase;
	}

	public int getStepVisitedNodes(){
		return this.stepVisitedNodes;
	}

	public void increaseScoreSumForStep(int[] roleScores){
		for(int roleIndex = 0; roleIndex < this.scoreSumForStep.length; roleIndex++){
			this.scoreSumForStep[roleIndex] += roleScores[roleIndex];
		}
	}

	public double[] getScoreSumForStep(){
		return this.scoreSumForStep;
	}

	public void increaseCurrentIterationVisitedNodes(int increase){
		this.currentIterationVisitedNodes += increase;
	}

	public void increaseCurrentIterationVisitedNodes(){
		this.currentIterationVisitedNodes++;
	}

	public void decreaseCurrentIterationVisitedNodes(){
		this.currentIterationVisitedNodes--;
	}

	public int getCurrentIterationVisitedNodes(){
		return this.currentIterationVisitedNodes;
	}

	public void clearGameDependentParameters(){
		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;

		this.gameStep = 0;
		this.previousGameStep = 0;

		this.stepIterations = 0;
		this.stepVisitedNodes = 0;
		this.scoreSumForStep = null;

		this.currentIterationVisitedNodes = 0;
	}

	public void resetGameDependentParameters(AbstractStateMachine theMachine, int numRoles, int myRoleIndex){
		this.theMachine = theMachine;
		this.numRoles = numRoles;
		this.myRoleIndex = myRoleIndex;

		this.gameStep = 0;
		this.previousGameStep = 0;

		this.resetStepStatistics();
	}

	public void resetStepStatistics(){

		this.stepIterations = 0;
		this.stepVisitedNodes = 0;
		this.scoreSumForStep = new double[this.numRoles]; // Initialized to all 0s by default

		this.resetIterationStatistics();
	}

	public void resetIterationStatistics(){
		this.currentIterationVisitedNodes = 0;
	}

}
