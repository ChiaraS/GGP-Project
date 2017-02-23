package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs;


public abstract class Mab {

	/**
	 *  Number of times any of the moves has been evaluated.
	 */
	protected int numUpdates;

	public Mab() {
		this.numUpdates = 0;
	}

    public int getNumUpdates(){
    	return this.numUpdates;
    }

    public void incrementNumUpdates(){
    	this.numUpdates++;
    }

    /**
     * Ths method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    public abstract void decreaseStatistics(double factor);
}
