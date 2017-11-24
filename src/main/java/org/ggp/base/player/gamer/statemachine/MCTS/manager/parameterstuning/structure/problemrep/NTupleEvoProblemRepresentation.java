package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NTuple;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;


public class NTupleEvoProblemRepresentation extends EvoProblemRepresentation{

	/**
	 *  N tuple landscape: map that has one entry for each n-tuple that must be
	 *  considered when computing the UCB value of a candidate individual.
	 *  The stats of these n-tuples are used when we want to compute the UCB value
	 *  of a combination of parameters.
	 */
	private Map<NTuple,IncrementalMab> landscapeModelForUCBComputation;

	/**
	 *  N tuple landscape: map that has one entry for each n-tuple that must be
	 *  considered when computing the value of a candidate individual AND one
	 *  entry for 1-tuples and numParams-tuples (even if we don't want to use
	 *  the last two when computing the UCB value.)
	 *  This model is used whenever we want to update statistics to make sure that,
	 *  even if we are not using 1-tuples and numParam-tuples when computing the
	 *  UCB value of a combination we can still collect statistics about them that
	 *  can be logged to memorize the local and global statistics about the parameters
	 *  usage.
	 *
	 *  NOTE that for each n-tuple there us only ONE IncrementalMab that is referenced
	 *  by both landscape models when needed by both of them
	 */
	private Map<NTuple,IncrementalMab> landscapeModelForStatsUpdate;



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

	/**
	 *
	 * @param population the population of combinations.
	 * @param classesLength the number of possible values for each of the parameters (i.e. classes).
	 * @param nTuplesForUCBLengths a set containing the lengths (as Integer) of the n-tuples that we
	 * want to consider when computing the UCB value of a combination of parameters (e.g. we might not
	 * want to consider n-tuples of all the possible lengths [1,numParams], but only some of them).
	 */
	public NTupleEvoProblemRepresentation(CompleteMoveStats[] population, int[] classesLength, Set<Integer> nTuplesForUCBLengths) {

		super(population);

		this.landscapeModelForUCBComputation = new HashMap<NTuple,IncrementalMab>();
		this.landscapeModelForStatsUpdate = new HashMap<NTuple,IncrementalMab>();

		// Compute all possible n-tuples and create an empty IncrementalMab for each of them.
		this.computeNTuples(new int[0], 0, classesLength.length, nTuplesForUCBLengths);

		/*
		this.globalMab = new IncrementalMab();

		this.localMabs = new FixedMab[classesLength.length];

		for(int i = 0; i < this.localMabs.length; i++){
			this.localMabs[i] = new FixedMab(classesLength[i]);
		}*/

	}

	private void computeNTuples(int[] paramIndices, int beginning, int end, Set<Integer> nTuplesForUCBLengths) {
		int[] newParamIndices;
		NTuple newNTuple;
		IncrementalMab newIncrementalMab;
		for(int i = beginning; i < end; i++) {
			newParamIndices = this.extendArray(paramIndices, i);
			newNTuple = new NTuple(newParamIndices);
			newIncrementalMab = new IncrementalMab();
			if(nTuplesForUCBLengths == null || nTuplesForUCBLengths.contains(new Integer(newParamIndices.length))) {
				this.landscapeModelForUCBComputation.put(newNTuple, newIncrementalMab);
				this.landscapeModelForStatsUpdate.put(newNTuple, newIncrementalMab);
			}else if(newParamIndices.length == 1 || newParamIndices.length == end) { // I's a 1-tuple or a numParamsTuple
				this.landscapeModelForStatsUpdate.put(newNTuple, newIncrementalMab);
			}
			this.computeNTuples(newParamIndices, i+1, end, nTuplesForUCBLengths);
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

    public Map<NTuple, IncrementalMab> getLandscapeModelForUCBComputation() {
		return this.landscapeModelForUCBComputation;
	}

    public Map<NTuple, IncrementalMab> getLandscapeModelForStatsUpdate() {
		return this.landscapeModelForStatsUpdate;
	}

	/**
	 * Given a parameter combination, this method extracts from it only (the indices of) the values
	 * of the parameters considered by the given n-tuple.
	 *
	 * @param parameterCombination combination of (indices of) parameter values.
	 * @param nTuple the n-tuple characterized by the indices of the parameters being considered by
	 * the n-tuple.
	 * @return
	 */
	public CombinatorialCompactMove getNTupleValues(CombinatorialCompactMove parameterCombination, NTuple nTuple) {
		int[] paramValuesIndices = new int[nTuple.getParamIndices().length];

		for(int i = 0; i < nTuple.getParamIndices().length; i++) {
			paramValuesIndices[i] = parameterCombination.getIndices()[nTuple.getParamIndices()[i]];
		}

		return new CombinatorialCompactMove(paramValuesIndices);
	}

	/**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    @Override
	public void decreaseStatistics(double factor){
    	super.decreaseStatistics(factor);

    	// This set contains the IncrementalMabs that have been already decayed.
    	// Used to keep track of decayed MABs so that they are decayed only once
    	// even if encountered twice.
    	Set<IncrementalMab> updatedMabs = new HashSet<IncrementalMab>();

    	for(IncrementalMab nTupleMab : this.landscapeModelForUCBComputation.values()) {
    		if(updatedMabs.add(nTupleMab)) {
    			nTupleMab.decreaseStatistics(factor);
    			//System.out.println("Decreasing " + nTupleMab);
    		}
    	}
    	for(IncrementalMab nTupleMab : this.landscapeModelForStatsUpdate.values()) {
    		if(updatedMabs.add(nTupleMab)) {
    			nTupleMab.decreaseStatistics(factor);
    			//System.out.println("Decreasing " + nTupleMab);
    		}
    	}
    	/*
    	this.globalMab.decreaseStatistics(factor);
    	for(int i = 0; i < this.localMabs.length; i++){
    		this.localMabs[i].decreaseStatistics(factor);
    	}*/
    }

    private void printNTuples() {
    	System.out.println("UCB n-tuples");
    	for(NTuple tuple : this.landscapeModelForUCBComputation.keySet()) {
    		System.out.println(tuple + " -> " + this.landscapeModelForUCBComputation.get(tuple));
    	}
    	System.out.println("To update n-tuples");
    	for(NTuple tuple : this.landscapeModelForStatsUpdate.keySet()) {
    		System.out.println(tuple + " -> " + this.landscapeModelForStatsUpdate.get(tuple));
    	}
    }

    public static void main(String args[]) {
    	int[] cl = new int[]{5, 6, 4, 8};

    	Set<Integer> nTupleLengths = new HashSet<Integer>();
    	//nTupleLengths.add(new Integer(1));
    	nTupleLengths.add(new Integer(2));
    	//nTupleLengths.add(new Integer(3));
    	nTupleLengths.add(new Integer(4));

    	NTupleEvoProblemRepresentation p = new NTupleEvoProblemRepresentation(new CompleteMoveStats[0], cl, nTupleLengths);
    	p.printNTuples();

    	p.decreaseStatistics(0.5);

    }

}
