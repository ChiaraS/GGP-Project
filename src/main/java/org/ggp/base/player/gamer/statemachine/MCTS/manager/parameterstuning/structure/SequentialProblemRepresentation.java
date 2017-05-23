package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;

public class SequentialProblemRepresentation {

	/**
	 * For each parameter, a multi-armed bandit problem where each arm corresponds to a possible
	 * value that can be assigned to that parameter.
	 */
	private FixedMab[] localMabs;

	public SequentialProblemRepresentation(int[] classesLength) {


		this.localMabs = new FixedMab[classesLength.length];

		for(int i = 0; i < this.localMabs.length; i++){
			this.localMabs[i] = new FixedMab(classesLength[i]);
		}
	}

	public FixedMab[] getLocalMabs(){
		return this.localMabs;
	}

    /**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    public void decreaseStatistics(double factor){
    	for(int i = 0; i < this.localMabs.length; i++){
    		this.localMabs[i].decreaseStatistics(factor);
    	}
    }

}
