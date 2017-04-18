package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;

import csironi.ggp.course.utils.MyPair;

public class LsiProblemRepresentation {

	/**
	 * Combinatorial actions (i.e. combinations of parameters) that must be tested during the
	 * generation phase, paired with the index of the only parameter that was not picked randomly.
	 * This allows to update only the statistics of this parameter value with the outcome of the
	 * simulation that tested the combination. An alternative would be to ignore this parameter
	 * index and always update all values with the outcome of the simulation, even if they were
	 * selected randomly to complete the combination.
	 */
	private List<MyPair<CombinatorialCompactMove,Integer>> combinationsToTest;

	/**
	 * For each parameter, a list of statistics, each of which corresponds to a possible
	 * value that can be assigned to that parameter.
	 */
	private MoveStats[][] paramsStats;

	/**
	 * Stats of the combinatorial actions (i.e. combinations of parameters) generated during the generation
	 * phase that must be evaluated with sequential halving during the evaluation phase.
	 */
	private List<CompleteMoveStats> generatedCombinations;

	/**
	 * List of indices of the combinations for the evaluation phase in a random order, specifying the order in which the
	 * generated combinations will be evaluated (indices might appear more than once because each sample might need to be
	 * evaluated multiple times).
	 */
	private List<Integer> evalOrder;

	public LsiProblemRepresentation(List<MyPair<CombinatorialCompactMove,Integer>> combinationsToTest, int[] numValuesPerParam) {
		this.combinationsToTest = combinationsToTest;

		this.paramsStats = new MoveStats[numValuesPerParam.length][];

		for(int paramIndex = 0; paramIndex < paramsStats.length; paramIndex++){
			this.paramsStats[paramIndex] = new MoveStats[numValuesPerParam[paramIndex]];
			for(int valueIndex = 0; valueIndex < paramsStats[paramIndex].length; valueIndex++){
				this.paramsStats[paramIndex][valueIndex] = new MoveStats();
			}
		}

		this.generatedCombinations = null;

		this.evalOrder = null;
	}

	public List<MyPair<CombinatorialCompactMove,Integer>> getCombinationsToTest(){
		return this.combinationsToTest;
	}

	public MoveStats[][] getParamsStats(){
		return this.paramsStats;
	}

	public void setGeneratedCombinationsStats(List<CompleteMoveStats> generatedCombinations){
		this.generatedCombinations = generatedCombinations;
	}

	public List<CompleteMoveStats> getGeneratedCombinations(){
		return this.generatedCombinations;
	}

	public void setEvalOrder(List<Integer> evalOrder){
		this.evalOrder = evalOrder;
	}

	public List<Integer> getEvalOrder(){
		return this.evalOrder;
	}
}
