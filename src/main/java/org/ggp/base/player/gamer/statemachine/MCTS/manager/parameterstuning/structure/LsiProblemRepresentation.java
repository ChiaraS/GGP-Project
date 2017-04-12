package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.Pair;

public class LsiProblemRepresentation {

	/**
	 * Combinatorial actions (i.e. combinations of parameters) that must be tested during the
	 * generation phase, paired with the index of the only parameter that was not picked randomly.
	 * This allows to update only the statistics of this parameter value with the outcome of the
	 * simulation that tested the combination. An alternative would be to ignore this parameter
	 * index and always update all values with the outcome of the simulation, even if they were
	 * selected randomly to complete the combination.
	 */
	private List<Pair<CombinatorialCompactMove,Integer>> actionsToTest;

	private int nextActionToTest;

	/**
	 * For each parameter, a list of statistics, each of which corresponds to a possible
	 * value that can be assigned to that parameter.
	 */
	private MoveStats[][] paramsStats;

	/**
	 * Combinatorial actions (i.e. combinations of parameters) generated during the generation
	 * phase that must be evaluated with sequential halving during the evaluation phase.
	 */
	private List<CombinatorialCompactMove> generatedActions;

	public LsiProblemRepresentation(List<Pair<CombinatorialCompactMove,Integer>> actionsToTest, int[] numValuesPerParam) {
		this.actionsToTest = actionsToTest;
		this.nextActionToTest = 0;

		this.paramsStats = new MoveStats[numValuesPerParam.length][];

		for(int paramIndex = 0; paramIndex < paramsStats.length; paramIndex++){
			this.paramsStats[paramIndex] = new MoveStats[numValuesPerParam[paramIndex]];
			for(int valueIndex = 0; valueIndex < paramsStats[paramIndex].length; valueIndex++){
				this.paramsStats[paramIndex][valueIndex] = new MoveStats();
			}
		}
	}

	public int[] getNextActionToTest(){
		if(this.nextActionToTest == this.actionsToTest.size()){
			GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Error! All the combinatorial action to test during the generation phase have been tested. The evaluation phase should have started!");
			throw new RuntimeException("LsiParametersTuner - All the combinatorial action to test during the generation phase have been tested. The evaluation phase should have started!");
		}
		this.nextActionToTest++;

		return this.actionsToTest.get(this.nextActionToTest-1).getFirst().getIndices();
	}
}
