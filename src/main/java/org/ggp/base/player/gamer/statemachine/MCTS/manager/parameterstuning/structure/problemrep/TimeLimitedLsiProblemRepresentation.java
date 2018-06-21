package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SimLimitedLsiProblemRepresentation.Phase;

import csironi.ggp.course.utils.MyPair;

public class TimeLimitedLsiProblemRepresentation /*extends LsiProblemRepresentation*/ {

    /**
     * Phase of this LSI problem.
     */
    private Phase phase;

    //******************************* For the generation phase ********************************//

	/**
	 * For each parameter, a list of statistics, each of which corresponds to a possible
	 * value that can be assigned to that parameter.
	 */
	private MoveStats[][] paramsStats;

	/**
	 * True if when receiving the reward for a certain combinatorial action we want to use it to update the stats
	 * of all unit actions that form the combinatorial action. False if we want to use it only to update the stats
	 * for the only unit action that wasn't generated randomly when creating the combinatorial action.
	 */
	private boolean updateAll;

	/**
	 * List of pairs (ParamIndex,ValueIndex) in a random order, specifying the order in which the parameter values
	 * must be tested during the generation phase. Using this order each value will be tested once, then the order
	 * will be randomized again. This is repeated until the time budget expires. The use of this order guarantees
	 * that each parameter value for a role won't always be tested against the same parameter value(s) for the other
	 * role(s). Moreover, it will guarantee that each parameter value will be tested at most n times and at least
	 * n-1 times (i.e. no oversampling of some values and undersampling of others)
	 */
	private List<MyPair<Integer,Integer>> testOrder;

    /**
     * Index that keeps track of the next value to test in the testOrder order.
     */
    private int testOrderIndex;

    //******************************* For the evaluation phase ********************************//

	/**
	 * Stats of the combinatorial actions (i.e. combinations of parameters) generated during the generation
	 * phase that must be evaluated with sequential halving during the evaluation phase.
	 */
	private List<CompleteMoveStats> generatedCandidatesStats;

	/**
	 * Timeout for the evaluation phase.
	 */
	private long evalPhaseTimeout;

	/**
	 * Timeout for the current iteration of sequential halving.
	 */
	private long currentTimeout;

	/**
	 * Number of candidates being considered during the current iteration of the evaluation phase.
	 * Each time we finish evaluating all of them, they will be halved and only the best half of
	 * them will be considered.
	 */
	private int numCandidatesOfCurrentIteration;

	/**
	 * List of indices of the combinations for the evaluation phase in a random order, specifying the order in which the
	 * generated combinations will be evaluated. When all combination shave been evaluated once, shuffle this order and
	 * start evaluating them again.
	 */
	private List<Integer> evalOrder;

    /**
     * Index that keeps track of the next combination to return.
     */
    private int evalOrderIndex;

	public TimeLimitedLsiProblemRepresentation(int[] numValuesPerParam, boolean updateAll) {

		this.phase = Phase.GENERATION;

		this.paramsStats = new MoveStats[numValuesPerParam.length][];
		this.testOrder = new ArrayList<MyPair<Integer,Integer>>();

		for(int paramIndex = 0; paramIndex < paramsStats.length; paramIndex++){
			this.paramsStats[paramIndex] = new MoveStats[numValuesPerParam[paramIndex]];
			for(int valueIndex = 0; valueIndex < paramsStats[paramIndex].length; valueIndex++){
				this.paramsStats[paramIndex][valueIndex] = new MoveStats();
				this.testOrder.add(new MyPair<Integer,Integer>(new Integer(paramIndex), new Integer(valueIndex)));
			}
		}

		Collections.shuffle(this.testOrder);

		this.updateAll = updateAll;

		this.testOrderIndex = 0;

		this.generatedCandidatesStats = null;

		this.evalPhaseTimeout = -1;

		this.currentTimeout = -1;

		this.numCandidatesOfCurrentIteration = -1;

		this.evalOrder = null;

		this.evalOrderIndex = -1;

	}

	public MyPair<Integer,Integer> getNextParamValueToTest(){
		this.phase = Phase.GENERATION;
		return this.testOrder.get(this.testOrderIndex);
	}

	/**
	 *
	 * @param combination the combination used to test the selected value (the selected value is set as returned
	 * by this class, while other values have been filled in randomly).
	 * @param reward
	 */
	public void updateStatsOfParamValue(int[] combination, double reward){

		if(this.updateAll){
			for(int pramIndex = 0; pramIndex < combination.length; pramIndex++){
				this.paramsStats[pramIndex][combination[pramIndex]].incrementScoreSum(reward);
				this.paramsStats[pramIndex][combination[pramIndex]].incrementVisits();
			}
		}else{
			int paramIndex = this.testOrder.get(this.testOrderIndex).getFirst();
			int valueIndex = this.testOrder.get(this.testOrderIndex).getSecond();
			this.paramsStats[paramIndex][valueIndex].incrementScoreSum(reward);
			this.paramsStats[paramIndex][valueIndex].incrementVisits();
		}

		this.testOrderIndex++;

		if(this.testOrderIndex == this.testOrder.size()){
			// All values have been tested another time. Shuffle their order for next test round.
			Collections.shuffle(this.testOrder);
			this.testOrderIndex = 0;
		}
	}

	public void setGeneratedCandidatesStats(List<CompleteMoveStats> generatedCandidatesStats, long evalPhaseTimeout){

		this.phase = Phase.EVALUATION;

		this.generatedCandidatesStats = generatedCandidatesStats;
		this.evalPhaseTimeout = evalPhaseTimeout;

		this.numCandidatesOfCurrentIteration = this.generatedCandidatesStats.size();

		this.evalOrderIndex = 0;

		if(this.numCandidatesOfCurrentIteration > 1){
			int numIterations = (int) Math.ceil(Math.log(this.numCandidatesOfCurrentIteration)/Math.log(2.0));
			this.computeEvalOrder();
			long currentTime = System.currentTimeMillis();
			if(currentTime < this.evalPhaseTimeout){
				this.currentTimeout = currentTime + (this.evalPhaseTimeout-currentTime)/numIterations;
			}else{
				this.currentTimeout = this.evalPhaseTimeout;
			}
		}else{ // Otherwise we only have one candidate, that is automatically the best
			this.evalOrder = null;
			this.phase = Phase.STOP;
		}

	}

	public void computeEvalOrder(){

		// Prepare random order of testing for the best elements
		this.evalOrder = new ArrayList<Integer>();

		for(int comboIndex = 0; comboIndex < this.numCandidatesOfCurrentIteration; comboIndex++){
			this.evalOrder.add(new Integer(comboIndex));
		}

		Collections.shuffle(evalOrder);

	}

	public int[] getNextCandidateToEvaluate(){

		while(System.currentTimeMillis() >= this.currentTimeout){
			this.halveCandidates();

			this.computeEvalOrder();
			this.evalOrderIndex = 0;

			if(this.numCandidatesOfCurrentIteration > 1){
				int numIterations = (int) Math.ceil(Math.log(this.numCandidatesOfCurrentIteration)/Math.log(2.0));

				long currentTime = System.currentTimeMillis();
				if(currentTime < this.evalPhaseTimeout){
					this.currentTimeout = currentTime + (this.evalPhaseTimeout-currentTime)/numIterations;
				}else{
					this.currentTimeout = this.evalPhaseTimeout;
				}
			}else{ // Otherwise we only have one candidate, that is automatically the best
				this.phase = Phase.STOP;
				break;
			}
		}
		return ((CombinatorialCompactMove) this.generatedCandidatesStats.get(this.evalOrder.get(this.evalOrderIndex)).getTheMove()).getIndices();
	}

	private void halveCandidates(){
		// We must half the candidates and recompute the order.
		Collections.sort(this.generatedCandidatesStats.subList(0,this.numCandidatesOfCurrentIteration),
				new Comparator<CompleteMoveStats>(){

					@Override
					public int compare(CompleteMoveStats o1, CompleteMoveStats o2) {

						double value1;
						if(o1.getVisits() == 0){
							value1 = 0;
						}else{
							value1 = o1.getScoreSum()/o1.getVisits();
						}
						double value2;
						if(o2.getVisits() == 0){
							value2 = 0;
						}else{
							value2 = o2.getScoreSum()/o2.getVisits();
						}
						// Sort from largest to smallest
						if(value1 > value2){
							return -1;
						}else if(value1 < value2){
							return 1;
						}else{
							return 0;
						}
					}

				});

		this.numCandidatesOfCurrentIteration = (int) Math.ceil((double)this.numCandidatesOfCurrentIteration/2.0);
		//this.evalOrderIndex = 0;

		/*
		if(this.numCandidatesOfCurrentIteration > 1){
			this.computeEvalOrder();
		}else{ // Otherwise we only have one candidate, that is automatically the best
			this.evalOrder = null;
			this.phase = Phase.STOP;
		}*/
	}

	public void updateStatsOfCandidate(double reward){

		this.generatedCandidatesStats.get(this.evalOrder.get(this.evalOrderIndex)).incrementScoreSum(reward);
		this.generatedCandidatesStats.get(this.evalOrder.get(this.evalOrderIndex)).incrementVisits();

		this.evalOrderIndex++;

		if(this.evalOrderIndex == this.evalOrder.size()){ // All candidates have been tested once
			// Shuffle the order and restart from the beginning of evalOrder.
			Collections.shuffle(this.evalOrder);
			this.evalOrderIndex = 0;
		}

	}



	public int getNumCandidatesOfCurrentIteration(){
		return this.numCandidatesOfCurrentIteration;
	}

	public MoveStats[][] getParamsStats(){
		return this.paramsStats;
	}

	public List<CompleteMoveStats> getGeneratedCandidatesStats(){
		return this.generatedCandidatesStats;
	}

	public Phase getPhase(){
		return this.phase;
	}

	public void setPhase(Phase phase){
		this.phase = phase;
	}
}
