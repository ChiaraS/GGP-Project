package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class GameDependentParameters {

	// Parameters that change for every game:

	private AbstractStateMachine theMachine;

	private int numRoles;

	private int myRoleIndex;

	/**
	 * Time available for each game step to perform the search (in milliseconds).
	 * This time is not equal to the playClock given by the server, but is equal to
	 * (playClock - searchSafetyMargin), so it corresponds to the actual time available for the search.
	 */
	private long actualPlayClock;

	/**
	 * This parameter is true if we are performing the metagame, false otherwise;
	 */
	private boolean metagame;

	// Parameters that change for every game step:

	/**
	 * Game step for which we are currently performing the search.
	 */
	private int gameStep;

	/**
	 * Sum of the scores obtained for each role over all the iterations performed
	 * so far for the current game step.
	 */
	private double[] stepScoreSumForRole;

	/**
	 * Iterations performed so far for the current step.
	 * NOTE: when using multiple playouts in the same iteration the number of iterations
	 * will be increased by the number of performed playouts and not only by 1.
	 */
	private int stepIterations;

	/**
	 * Iterations performed so far during the whole game. Corresponds to the sum of stepIterations
	 * for each step, therefore when using multiple playouts in the same iteration the number of
	 * iterations will be increased by the number of performed playouts and not only by 1.
	 */
	private int totIterations;

	/**
	 * Node visited so far for the current step.
	 * NOTE: when performing multiple playouts each node visited by any playout counts as 1.
	 * The nodes visited in the tree before performing multiple playouts are counted only once
	 * and not once per performed playout.
	 *
	 * NOTE: this value is not up-to-date at any moment but only in between playouts because
	 * it is updated only at the end of the playout.
	 */
	private double stepVisitedNodes;

	/**
	 * Exact time spent on search for the current game step.
	 * NOTE that this value is consistent only at the end of the search.
	 * This value is updated at the end of the search for the current step
	 */
	private long stepSearchDuration;

	/**
	 * Sum of the length of the game in each iteration of the search for the current time step.
	 * NOTE: when using multiple playouts in the same iteration, the gameLengthSum will be increased
	 * with the length of the game for each playout (i.e. x playouts => x complete game lengths added
	 * to this sum).
	 */
	private double stepGameLengthSum;

	/**
	 * Number of states that are expanded during the current step (a state is considered expanded when
	 * it is added to the tree as a node).
	 */
	private int stepAddedNodes;

	/**
	 * Number of states added to the tree without a node (i.e. they are memorized in the parent node).
	 * Note that this will be non-zero only when we use state-memorizing nodes.
	 */
	private int stepMemorizedStates;

	// Parameters that change for every iteration:

	/**
	 * Number of visited nodes in the current iteration.
	 */
	private double currentIterationVisitedNodes;


	public GameDependentParameters(){

		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;
		this.actualPlayClock = -1;

		this.metagame = false;

		this.gameStep = 0;
		this.stepScoreSumForRole = null;
		this.stepIterations = 0;
		this.totIterations = 0;
		this.stepVisitedNodes = 0;
		this.stepSearchDuration = 0L;
		this.stepGameLengthSum = 0;
		this.stepAddedNodes = 0;
		this.stepMemorizedStates = 0;

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

	public long getActualPlayClock(){
		return this.actualPlayClock;
	}

	public void setMetagame(boolean metagame){
		this.metagame = metagame;
	}

	public boolean isMetagame(){
		return this.metagame;
	}

	public void setGameStep(int newGameStep){
		this.gameStep = newGameStep;

		this.resetStepStatistics();

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

	public int getGameStep(){
		return this.gameStep;
	}

	public void increaseStepScoreSumForRoles(double[] roleScores){
		for(int roleIndex = 0; roleIndex < this.stepScoreSumForRole.length; roleIndex++){
			this.stepScoreSumForRole[roleIndex] += roleScores[roleIndex];
		}
	}

	public double[] getStepScoreSumForRoles(){
		return this.stepScoreSumForRole;
	}

	public void increaseStepIterations(){
		this.stepIterations++;
		this.totIterations++;
	}

	public int getStepIterations(){
		return this.stepIterations;
	}

	public int getTotIterations(){
		return this.totIterations;
	}

	public void increaseStepVisitedNodes(double increase){
		this.stepVisitedNodes += increase;
	}

	public double getStepVisitedNodes(){
		return this.stepVisitedNodes;
	}

	public void increaseStepSearchDuration(long increase){
		this.stepSearchDuration += increase;
	}

	public long getStepSearchDuration(){
		return this.stepSearchDuration;
	}

	public void increaseStepGameLengthSum(double gameLength){
		this.stepGameLengthSum += gameLength;
	}

	public double getStepGameLengthSum(){
		return this.stepGameLengthSum;
	}

	public void increaseStepAddedNodes(){
		this.stepAddedNodes++;
	}

	public int getStepAddedNodes(){
		return this.stepAddedNodes;
	}

	public void increaseMemorizedStates(int increase){
		this.stepMemorizedStates += increase;
	}

	public int getMemorizedStates(){
		return this.stepMemorizedStates;
	}

	public void increaseCurrentIterationVisitedNodes(double increase){
		this.currentIterationVisitedNodes += increase;
	}

	public void increaseCurrentIterationVisitedNodes(){
		this.currentIterationVisitedNodes++;
	}

	public void decreaseCurrentIterationVisitedNodes(){
		this.currentIterationVisitedNodes--;
	}

	public double getCurrentIterationVisitedNodes(){
		return this.currentIterationVisitedNodes;
	}

	public void clearGameDependentParameters(){
		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;
		this.actualPlayClock = -1;

		this.metagame = false;

		this.gameStep = 0;
		this.stepScoreSumForRole = null;
		this.stepIterations = 0;
		this.totIterations = 0;
		this.stepVisitedNodes = 0;
		this.stepSearchDuration = 0L;
		this.stepGameLengthSum = 0;
		this.stepAddedNodes = 0;
		this.stepMemorizedStates = 0;

		this.currentIterationVisitedNodes = 0;
	}

	public void resetGameDependentParameters(AbstractStateMachine theMachine, int numRoles, int myRoleIndex, long actualPlayClock){
		this.theMachine = theMachine;
		this.numRoles = numRoles;
		this.myRoleIndex = myRoleIndex;
		this.actualPlayClock = actualPlayClock;

		this.metagame = false;

		this.gameStep = 0;
		this.stepScoreSumForRole = new double[this.numRoles]; // Initialized to all 0s by default
		this.stepIterations = 0;
		this.totIterations = 0;
		this.stepVisitedNodes = 0;
		this.stepSearchDuration = 0L;
		this.stepGameLengthSum = 0;
		this.stepAddedNodes = 0;
		this.stepMemorizedStates = 0;

		this.currentIterationVisitedNodes = 0;
	}

	private void resetStepStatistics(){
		this.stepScoreSumForRole = new double[this.numRoles]; // Initialized to all 0s by default
		this.stepIterations = 0;
		this.stepVisitedNodes = 0;
		this.stepSearchDuration = 0L;
		this.stepGameLengthSum = 0;
		this.stepAddedNodes = 0;
		this.stepMemorizedStates = 0;

		this.currentIterationVisitedNodes = 0;
	}

	public void resetIterationStatistics(){
		this.currentIterationVisitedNodes = 0;
	}

}
