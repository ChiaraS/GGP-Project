package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class GameDependentParameters {

	/**
	 * TODO: find a more appropriate place where to share this value.
	 * Also consider that this value is ignored by the manager if a limit on the number
	 * of simulations per move is specified.
	 */
	private long timeout;

	private AbstractStateMachine theMachine;

	private int numRoles;

	private int myRoleIndex;

	public GameDependentParameters(){

		this.timeout = 0;

		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;
	}

	public long getTimeout(){
		return this.timeout;
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

	public void clearGameDependentParameters(){

		this.timeout = 0;

		this.theMachine = null;
		this.numRoles = -1;
		this.myRoleIndex = -1;
	}

	public void resetGameDependentParameters(AbstractStateMachine theMachine, int numRoles, int myRoleIndex){
		this.timeout = 0;

		this.theMachine = theMachine;
		this.numRoles = numRoles;
		this.myRoleIndex = myRoleIndex;
	}

	public void setTimeout(long timeout){
		this.timeout = timeout;
	}

}
