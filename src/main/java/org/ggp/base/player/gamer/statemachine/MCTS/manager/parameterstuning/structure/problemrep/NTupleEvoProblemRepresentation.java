package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NTuple;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;


public class NTupleEvoProblemRepresentation extends EvoProblemRepresentation{

	/**
	 *  N tuple landscape: each entry corresponds to one possible n-tuple
	 */
	private Map<NTuple,IncrementalMab> landscapeModel;

	/**
	 * The global Multi-Armed Bandit problem that keeps track of statistics for the
	 * n-dimensional tuple (n = num tuned parameters).
	 * Its arms correspond to the possible combinatorial moves (individuals) evaluated so far.
	 */
	//private IncrementalMab globalMab;

	/**
	 * For each parameter, a multi-armed bandit problem where each arm corresponds to a possible
	 * value that can be assigned to that parameter. Keep track of the statistics for the n
	 * 1-dimensional tuples (one for each parameter being tuned).
	 */
	//private FixedMab[] localMabs;

	public NTupleEvoProblemRepresentation(CompleteMoveStats[] population, int[] classesLength) {

		super(population);

		this.landscapeModel = new HashMap<NTuple,IncrementalMab>();

		// Compute all possible n-tuples and create an empty IncrementalMab for each of them.
		this.computeNTuples(new int[0], 0, classesLength.length);

		/*
		this.globalMab = new IncrementalMab();

		this.localMabs = new FixedMab[classesLength.length];

		for(int i = 0; i < this.localMabs.length; i++){
			this.localMabs[i] = new FixedMab(classesLength[i]);
		}*/

	}

	private void computeNTuples(int[] paramIndices, int beginning, int end) {
		int[] newParamIndices;
		for(int i = beginning; i < end; i++) {
			newParamIndices = this.extendArray(paramIndices, i);
			this.landscapeModel.put(new NTuple(newParamIndices), new IncrementalMab());
			this.computeNTuples(newParamIndices, i+1, end);
		}
	}

	/**
	 * Given an array, copies it increasing the length by 1 and adding the specified element.
	 *
	 * @param array the array to copy and extend.
	 * @param newElement the new element to add at the end of the array.
	 * @return the extended array.
	 */
    private int[] extendArray(int[] array, int newElement){
    	int[] extendedArray = new int[array.length + 1];
    	for(int i = 0; i < array.length; i++){
    		extendedArray[i] = array[i];
    	}
    	extendedArray[extendedArray.length-1] = newElement;
    	return extendedArray;
    }

    /*
	public IncrementalMab getGlobalMab(){
		return this.globalMab;
	}

	public FixedMab[] getLocalMabs(){
		return this.localMabs;
	}*/

    public Map<NTuple, IncrementalMab> getLandscapeModel() {
		return this.landscapeModel;
	}

	/**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    @Override
	public void decreaseStatistics(double factor){
    	super.decreaseStatistics(factor);
    	for(IncrementalMab nTupleMab : this.landscapeModel.values()) {
    		nTupleMab.decreaseStatistics(factor);
    	}
    	/*
    	this.globalMab.decreaseStatistics(factor);
    	for(int i = 0; i < this.localMabs.length; i++){
    		this.localMabs[i].decreaseStatistics(factor);
    	}*/
    }

    private void printNTuples() {
    	for(NTuple tuple : this.landscapeModel.keySet()) {
    		System.out.println(tuple);
    	}
    }

    public static void main(String args[]) {
    	int[] cl = new int[]{5, 6, 4, 8};
    	NTupleEvoProblemRepresentation p = new NTupleEvoProblemRepresentation(null, cl);
    	p.printNTuples();
    }

}
