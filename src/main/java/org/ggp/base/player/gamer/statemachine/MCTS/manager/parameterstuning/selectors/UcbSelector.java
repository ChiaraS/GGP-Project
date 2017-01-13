package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

/**
 * This class gets as input a list of statistics for moves (i.e MoveStats) and selects
 * one of them according to the ucb formula.
 * @author C.Sironi
 *
 */
public class UcbSelector extends TunerSelector{

	private double c;

	private double valueOffset;

	/**
	 * First play urgency for the tuner (i.e. default value of a combination that has never been explored).
	 */
	private double fpu;

	public UcbSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id){
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.c = gamerSettings.getDoublePropertyValue("TunerSelector" + id + ".c");
		this.valueOffset = gamerSettings.getDoublePropertyValue("TunerSelector" + id + ".valueOffset");
		this.fpu = gamerSettings.getDoublePropertyValue("TunerSelector" + id + ".fpu");

	}

	/**
	 * TODO: adapt the MctsManager code to also use this class.
	 *
	 * @param moveStats list with the statistics for each move.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 * @return the index of the selected move.
	 */
	@Override
	public int selectMove(MoveStats[] movesStats, int numUpdates){

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

				movesValues[i] = this.computeCombinationValue(movesStats[i], numUpdates, minExtreme, maxExtreme);

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
				throw new RuntimeException("UcbSelector - SelectNextCombination(): detected no combinations with value higher than -1.");
			}

			selectedMove = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();

		}


		return selectedMove;

	}

	private double computeCombinationValue(MoveStats moveStats, int totEvaluations, double minExtreme, double maxExtreme){

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
			return this.fpu;
		}

		return this.computeExploitation(totalValue, moveEvaluations, minExtreme, maxExtreme) + this.computeExploration(totEvaluations, moveEvaluations);

	}

	private double computeExploitation(double totalValue, int moveEvaluations, double minExtreme, double maxExtreme){

		// Assume that the totalFitness has already been checked to be positive and the numEvaluations to be non-negative.

		return this.normalize(totalValue/((double)moveEvaluations), minExtreme, maxExtreme);

	}

	private double normalize(double value, double leftExtreme, double rightExtreme){

		return (value - leftExtreme)/(rightExtreme - leftExtreme);

	}

	private double computeExploration(int totEvaluations, int combinationEvaluations){

		if(this.c == 0){
			return 0;
		}else{
			return (this.c * (Math.sqrt(Math.log(totEvaluations)/((double)combinationEvaluations))));
		}

	}

	@Override
	public void setReferences(
			SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing

	}

	@Override
	public void clearComponent() {
		// Do nothing

	}

	@Override
	public void setUpComponent() {
		// Do nothing

	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "C = " + this.c +
				indentation + "VALUE_OFFSET = " + this.valueOffset +
				indentation + "FPU = " + this.fpu;
	}

}
