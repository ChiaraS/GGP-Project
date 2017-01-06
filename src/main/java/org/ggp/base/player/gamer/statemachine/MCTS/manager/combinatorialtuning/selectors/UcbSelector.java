package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

/**
 * This class gets as input a list of statistics for moves (i.e MoveStats) and selects
 * one of them according to the ucb formula.
 * @author C.Sironi
 *
 */
public class UcbSelector {

	private Random random;

	public UcbSelector(Random random) {
		this.random = random;
	}

	/**
	 *
	 * @param moveStats list with the statistics for each move.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 *
	 * @return the index of the selected move.
	 */

	/**
	 * TODO: adapt the MctsManager code to also use this class.
	 *
	 * @param moveStats list with the statistics for each move.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 * @param c c constant to be used for this selection.
	 * @param valueOffset the selected move will be a random one among the ones with value in the
	 * interval [maxValue-valueOffset, maxValue].
	 * @param fpu first play urgency.
	 * @return the index of the selected move.
	 */
	public int selectMove(MoveStats[] movesStats, int numUpdates, double c, double valueOffset, double fpu){

		int selectedMove;
		// This should mean that no combination of values has been evaluated yet (1st simulation),
		// thus we return a random combination.
		if(numUpdates == 0){
			selectedMove = this.random.nextInt(movesStats.length);
		}else{

			double minExtreme = 0.0;
			double maxExtreme = 100.0;

			double maxValue = -1;
			double[] movesValues = new double[movesStats.length];

			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int i = 0; i < movesStats.length; i++){

				movesValues[i] = this.computeCombinationValue(movesStats[i], numUpdates, minExtreme, maxExtreme, c, fpu);

				/*
				if(combinationsValues[i] == Double.MAX_VALUE){
					maxValue = combinationsValues[i];
					selectedCombinationsIndices.add(new Integer(i));
				}else*/
				if(movesValues[i] > maxValue){
					maxValue = movesValues[i];
				}
			}

			/*
			// NOTE: as is, this code always selects a combination that has value Double.MAX_VALUE if there is one,
			// even if there is an individual that has a value higher than (Double.MAX_VALUE-this.valueOffset).
			if(maxValue < Double.MAX_VALUE){
				for(int j = 0; j < individualsValues.length; j++){
					if(individualsValues[j] >= (maxValue-this.evoValueOffset)){
						selectedIndividualsIndices.add(new Integer(j));
					}
				}
			}
			*/

			for(int i = 0; i < movesValues.length; i++){
				if(movesValues[i] >= (maxValue-valueOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("CombinatorialTuner - SelectNextCombination(): detected no combinations with value higher than -1.");
			}

			selectedMove = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();

		}


		return selectedMove;

	}

	private double computeCombinationValue(MoveStats moveStats, int totEvaluations, double minExtreme, double maxExtreme, double c, double fpu){

		int moveEvaluations = moveStats.getVisits();
		double totalValue = moveStats.getScoreSum();

		/**
		 * Extra check to make sure that neither the numEvaluations nor the
		 * totalFitness exceed the maximum feasible value for an int type.
		 * TODO: remove this check once you are reasonably sure that this
		 * can never happen.
		 */
		if(moveEvaluations < 0 || totalValue < 0){
			throw new RuntimeException("Negative value for combinationEvaluations and/or totalValue of an individual: combinationEvaluations=" + moveEvaluations + ", totalValue=" + totalValue + ".");
		}

		if(moveEvaluations == 0){
			return fpu;
		}

		return this.computeExploitation(totalValue, moveEvaluations, minExtreme, maxExtreme) + this.computeExploration(totEvaluations, moveEvaluations, c);

	}

	private double computeExploitation(double totalValue, int moveEvaluations, double minExtreme, double maxExtreme){

		// Assume that the totalFitness has already been checked to be positive and the numEvaluations to be non-negative.

		return this.normalize(totalValue/((double)moveEvaluations), minExtreme, maxExtreme);

	}

	private double normalize(double value, double leftExtreme, double rightExtreme){

		return (value - leftExtreme)/(rightExtreme - leftExtreme);

	}

	private double computeExploration(int totEvaluations, int combinationEvaluations, double c){

		return (c * (Math.sqrt(Math.log(totEvaluations)/((double)combinationEvaluations))));

	}

}
