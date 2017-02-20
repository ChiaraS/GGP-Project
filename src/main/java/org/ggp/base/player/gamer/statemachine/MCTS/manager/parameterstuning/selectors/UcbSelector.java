package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.structure.Move;

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
	public int selectMove(MoveStats[] movesStats, int[] movesPenalty, int numUpdates){

		int selectedMove;
		// This should mean that no combination of values has been evaluated yet (1st simulation),
		// thus we return a random combination.
		if(numUpdates == 0 && this.biasComputer == null){
			selectedMove = this.random.nextInt(movesStats.length);
		}else{

			double minExtreme = 0.0;
			double maxExtreme = 100.0;

			double maxValue = -1;
			double[] movesValues = new double[movesStats.length];

			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int i = 0; i < movesStats.length; i++){

				movesValues[i] = this.computeCombinationValue(movesStats[i], ! check if null ! movesPenalty[i], numUpdates, minExtreme, maxExtreme);

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

	@Override
	public Move selectMove(Map<Move, MoveStats> movesStats, int numUpdates) {

		Move selectedMove = null;

		// When no combination of values has been evaluated yet (1st simulation) and there is no
		// bias being added to the combinations we can simply return a random combination, because
		// all combinations will have the same value (i.e. fpu).
		if(numUpdates == 0 && this.biasComputer == null){
			int randomNum = this.random.nextInt(movesStats.size());

			for(Entry<Move,MoveStats> entry : movesStats.entrySet()){
				if(randomNum == 0){
					selectedMove = entry.getKey();
					break;
				}
				randomNum--;
			}
		}else{
			// If this evaluator is adding a bias to the combinations we execute the following code even when it's the 1st
			// simulation. This is because there will be a bias that will distinguish among the combinations and be used to
			// select the first combination to explore.
			double minExtreme = 0.0;
			double maxExtreme = 100.0;

			double maxValue = -1;
			double[] movesValues = new double[movesStats.size()];
			Move[] moves = new Move[movesStats.size()];

			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			int i = 0;
			for(Entry<Move,MoveStats> entry : movesStats.entrySet()){

				movesValues[i] = this.computeCombinationValue(entry.getValue(), numUpdates, minExtreme, maxExtreme);
				moves[i] = entry.getKey();

				/*
				if(combinationsValues[i] == Double.MAX_VALUE){
					maxValue = combinationsValues[i];
					selectedCombinationsIndices.add(new Integer(i));
				}else*/
				if(movesValues[i] > maxValue){
					maxValue = movesValues[i];
				}

				i++;
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

			for(i = 0; i < movesValues.length; i++){
				if(movesValues[i] >= (maxValue-this.valueOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("UcbSelector - SelectNextCombination(Map): detected no combinations with value higher than -1.");
			}

			selectedMove = moves[selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue()];

		}


		return selectedMove;
	}

	private double computeCombinationValue(MoveStats moveStats, double penalty, int totEvaluations, double minExtreme, double maxExtreme){

		int moveEvaluations = moveStats.getVisits();
		double totalValue = moveStats.getScoreSum();

		/**
		 * Extra check to make sure that neither the moveEvaluations nor the
		 * totalValue  exceed the maximum feasible value for an int type.
		 * TODO: remove this check once you are reasonably sure that this
		 * can never happen.
		 */
		if(moveEvaluations < 0 || totalValue < 0){
			throw new RuntimeException("Negative value for combinationEvaluations and/or totalValue of an individual: combinationEvaluations=" + moveEvaluations + ", totalValue=" + totalValue + ".");
		}

		double exploitation;
		double theValue;

		if(moveEvaluations == 0){
			// If using Progressive Bias, when moveEvaluations == 0 any value of exploitation will be
			// ignored when computing the bias because it will be multiplied by 0. Thus, here we just
			// assign an arbitrary value.
			exploitation = -1;
			theValue = this.fpu;
		}else{
			exploitation = this.computeExploitation(totalValue, moveEvaluations, minExtreme, maxExtreme);
			theValue = exploitation + this.computeExploration(totEvaluations, moveEvaluations);
		}

		if(this.biasComputer != null){
			theValue += this.biasComputer.computeMoveBias(exploitation, moveEvaluations, penalty);
		}

		return theValue;

	}

	private double computeExploitation(double totalValue, int moveEvaluations, double minExtreme, double maxExtreme){

		// Assume that the totalValue has already been checked to be positive and the numEvaluations to be non-negative.

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

	/*
	 * TODO: ATTENTION! If you add code here that does something on the state of the tuner remember that this method might
	 * be called multiple times after each game if the player is using the SequentialParametersTuner!!! If you want this method
	 * to be called only once per instance of TunerSelector then change the code in the copy constructor of the ParametersTuner
	 * subclasses in order to deep-copy also the TunerSelectors!
	 */
	@Override
	public void clearComponent() {
		// Do nothing

	}

	/*
	 * TODO: ATTENTION! If you add code here that does something on the state of the tuner remember that this method might
	 * be called multiple times before each game if the player is using the SequentialParametersTuner!!! If you want this method
	 * to be called only once per instance of TunerSelector then change the code in the copy constructor of the ParametersTuner
	 * subclasses in order to deep-copy also the TunerSelectors!
	 */
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
