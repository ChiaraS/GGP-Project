package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class GameDependentParameters {

	private AbstractStateMachine theMachine;

	private int numRoles;

	private int myRoleIndex;

	/**
	 * Game step for which we are currently performing the search.
	 */
	private int gameStep;

	/**
	 * Total iterations performed since the start of the search.
	 */
	private int totalIterations;

	/**
	 * Iterations performed so far for the current step.
	 */
	private int stepIterations;

	/**
	 * Number of visited nodes in the current iteration so far.
	 */
	private int currentIterationVisitedNodes;

	public GameDependentParameters(){
		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;
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
		this.gameStep = newGameStep;
	}

	public void clearGameDependentParameters(){
		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;
	}

	public void resetGameDependentParameters(AbstractStateMachine theMachine, int numRoles, int myRoleIndex){
		this.theMachine = theMachine;
		this.numRoles = numRoles;
		this.myRoleIndex = myRoleIndex;
	}

}
