package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;


public abstract class Mab {

	/**
	 *  Number of times any of the moves has been evaluated.
	 */
	private int numUpdates;

	public Mab() {
		this.numUpdates = 0;
	}

    public int getNumUpdates(){
    	return this.numUpdates;
    }

    public void incrementNumUpdates(){
    	this.numUpdates++;
    }
}
