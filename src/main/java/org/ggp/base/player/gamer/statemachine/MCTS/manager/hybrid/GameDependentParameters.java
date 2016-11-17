package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class GameDependentParameters {

	private AbstractStateMachine theMachine;

	private int numRoles;

	private int myRoleIndex;

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
