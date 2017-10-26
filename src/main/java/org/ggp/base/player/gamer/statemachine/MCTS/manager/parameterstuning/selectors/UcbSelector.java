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

import csironi.ggp.course.utils.MyPair;

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

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
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


	/**
	 * TODO: adapt the MctsManager code to also use this class.
	 *
	 * @param moveStats list of lists with the statistics for each move.
	 * @param valuesFeasibility for each entry each of the movesStats arrays specifies if it has to be considered (true)
	 * or not (false) in the current selection. This array allows to exclude form the selection the statistics of the
	 * moves that are not feasible (i.e. when selecting a value for a tunable parameter allows to exclude those values
	 * that are not feasible for the current configuration of other parameter values). If this array is null, all moves
	 * will be considered feasible and the corresponding movesStats will be taken into account.
	 * @param movesPenalty penalty of each move that depends on empirical tests on the performance
	 * of the move (i.e. parameters combination). NOTE: when the penalty is not specified we use the
	 * value 0. This is needed if we are computing the bias but the penalty is not specified. If we are not using a bias
	 * computer any value for the penalty will be ignored.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 * @return the index of the selected move.
	 */
	@Override
	public int selectMove(MoveStats[] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates){

		int selectedMove = -1;
		// This should mean that no combination of values has been evaluated yet (1st simulation),
		// thus we return a random combination.
		if(numUpdates == 0 && this.biasComputer == null){

			if(valuesFeasibility != null){
				// Count number of feasible moves and then pick random one
				int feasibleMoves = 0;
				for(int i = 0; i < valuesFeasibility.length; i++){
					if(valuesFeasibility[i]){
						feasibleMoves++;
					}
				}

				int randomNum = this.random.nextInt(feasibleMoves);
				for(int i = 0; i < valuesFeasibility.length; i++){
					if(valuesFeasibility[i]){
						if(randomNum == 0){
							selectedMove = i;
							break;
						}
						randomNum--;
					}
				}

				// Extra check (should never be true).
				if(selectedMove == -1){
					throw new RuntimeException("UcbSelector - SelectMove(MoveStats[], boolean[], double[], int): detected no feasible move when selecting.");
				}
			}else{
				selectedMove = this.random.nextInt(movesStats.length);
			}
		}else{

			double minExtreme = 0.0;
			double maxExtreme = 100.0;

			double maxValue = -1;
			double[] movesValues = new double[movesStats.length];

			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			//if(movesPenalty != null){
			for(int i = 0; i < movesStats.length; i++){

				if(valuesFeasibility == null || valuesFeasibility[i]){
					movesValues[i] = this.computeCombinationValue(movesStats[i], movesPenalty[i], numUpdates, minExtreme, maxExtreme);

					/*
					if(combinationsValues[i] == Double.MAX_VALUE){
						maxValue = combinationsValues[i];
						selectedCombinationsIndices.add(new Integer(i));
					}else*/
					if(movesValues[i] > maxValue){
						maxValue = movesValues[i];
					}
				}

			}
			/*}else{
				for(int i = 0; i < movesStats.length; i++){

					movesValues[i] = this.computeCombinationValue(movesStats[i], -1, numUpdates, minExtreme, maxExtreme);

					//if(combinationsValues[i] == Double.MAX_VALUE){
					//	maxValue = combinationsValues[i];
					//	selectedCombinationsIndices.add(new Integer(i));
					//}else
					if(movesValues[i] > maxValue){
						maxValue = movesValues[i];
					}
				}
			} */

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
				if((valuesFeasibility == null || valuesFeasibility[i]) && movesValues[i] >= (maxValue-valueOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("UcbSelector - SelectMove(MoveStats[], boolean[], double[], int): detected no combinations with value higher than -1.");
			}

			selectedMove = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();

		}

		return selectedMove;

	}

	/**
	 * @param movesInfo map with the statistics for each move considered so far. More precisely,
	 * each move is mapped to the corresponding statistics and to the corresponding penalty value.
	 * NOTE: we assume here that the penalty is always specified even when we are not using a bias
	 * computer, thus if no penalty is specified in the gamers settings this value should be
	 * initialized to 0.
	 * ALSO NOTE: this method assumes that all moves are feasible. If you want to exclude some moves
	 * from the selection when using this method then you have to remove them from the map before
	 * passing it to this method. TODO: change the other selectMove method to keep track of the move
	 * associated to each movesStats so that also for that method whenever there are values that
	 * are not feasible we just have to exclude them from the movesStats array before passing it
	 * to the method. If we do so now, we loose the information of the move associated to a certain
	 * move statistic because the index of the move statistic in the array of movesStats might not
	 * correspond to the correct move index.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 * @return the index of the selected move.
	 */
	@Override
	public Move selectMove(Map<Move,MyPair<MoveStats,Double>> movesInfo, int numUpdates) {

		// Extra check to make sure that this method is never called with an empty map of moves
		if(movesInfo.isEmpty()){
			throw new RuntimeException("UcbSelector - selectMove(Map, int): cannot select next combination becase the map is empty.");
		}

		Move selectedMove = null;

		// When no combination of values has been evaluated yet (1st simulation) and there is no
		// bias being added to the combinations we can simply return a random combination, because
		// all combinations will have the same value (i.e. fpu).
		// NOTE: numUpdates == 0 should never be true, because it only happens when the movesInfo
		// map is empty, and in that case we throw an exception!
		if(numUpdates == 0 && this.biasComputer == null){
			int randomNum = this.random.nextInt(movesInfo.size());

			for(Entry<Move,MyPair<MoveStats,Double>> entry : movesInfo.entrySet()){
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
			double[] movesValues = new double[movesInfo.size()];
			Move[] moves = new Move[movesInfo.size()];

			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			int i = 0;
			for(Entry<Move,MyPair<MoveStats,Double>> entry : movesInfo.entrySet()){

				movesValues[i] = this.computeCombinationValue(entry.getValue().getFirst(), entry.getValue().getSecond().doubleValue(), numUpdates, minExtreme, maxExtreme);
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
				throw new RuntimeException("UcbSelector - SelectMove(Map, int): detected no feasible moves with value higher than -1.");
			}

			selectedMove = moves[selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue()];

		}

		return selectedMove;
	}

	@Override
	public MyPair<Integer,Integer> selectMove(MoveStats[][] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates){

		MyPair<Integer,Integer> selectedMove;

		// This should mean that no combination of values has been evaluated yet (1st simulation),
		// thus we return a random combination.
		if(numUpdates == 0 && this.biasComputer == null){

			if(valuesFeasibility != null){
				// Pick a random move among the total number of feasible moves.

				// Count total number of feasible moves. Note that all arrays of MoveStats have the same number of feasible moves.
				// Thus, count number of feasible moves for 1 array, then multiply it for number of arrays.
				int feasibleMovesPerArray = 0;
				for(int i = 0; i < valuesFeasibility.length; i++){
					if(valuesFeasibility[i]){
						feasibleMovesPerArray++;
					}
				}
				int feasibleMoves = feasibleMovesPerArray * movesStats.length;

				int randomNum = this.random.nextInt(feasibleMoves);
				int roleStatsIndex = randomNum/feasibleMovesPerArray;
				randomNum = randomNum%feasibleMovesPerArray;
				int statsIndex = -1;
				for(int i = 0; i < valuesFeasibility.length; i++){
					if(valuesFeasibility[i]){
						if(randomNum == 0){
							statsIndex = i;
							break;
						}
						randomNum--;
					}
				}
					// Extra check (should never be true).
				if(roleStatsIndex == -1 || statsIndex == -1){
					throw new RuntimeException("UcbSelector - SelectMove(MoveStats[][], boolean[], double[], int): detected no feasible move when selecting.");
				}

				selectedMove = new MyPair<Integer,Integer>(roleStatsIndex,statsIndex);
			}else{
				// Compute total number of MoveStats. Note that all arrays of MoveStats have the same length.
				// Then get random MoveStats among them.
				int randomNum = this.random.nextInt(movesStats.length * movesStats[0].length);
				selectedMove = new MyPair<Integer,Integer>(randomNum/movesStats[0].length, randomNum%movesStats[0].length);
			}
		}else{

			double minExtreme = 0.0;
			double maxExtreme = 100.0;

			double maxValue = -1;
			double[][] movesValues = new double[movesStats.length][movesStats[0].length];

			List<MyPair<Integer,Integer>> selectedMovesIndices = new ArrayList<MyPair<Integer,Integer>>();

			//if(movesPenalty != null){
			for(int roleStatsIndex = 0; roleStatsIndex < movesStats.length; roleStatsIndex++){

				for(int statsIndex = 0; statsIndex < movesStats[roleStatsIndex].length; statsIndex++){

					if(valuesFeasibility == null || valuesFeasibility[statsIndex]){
						movesValues[roleStatsIndex][statsIndex] = this.computeCombinationValue(movesStats[roleStatsIndex][statsIndex], movesPenalty[statsIndex], numUpdates, minExtreme, maxExtreme);

						/*
						if(combinationsValues[i] == Double.MAX_VALUE){
							maxValue = combinationsValues[i];
							selectedCombinationsIndices.add(new Integer(i));
						}else*/
						if(movesValues[roleStatsIndex][statsIndex] > maxValue){
							maxValue = movesValues[roleStatsIndex][statsIndex];
						}
					}
				}

			}
			/*}else{
				for(int i = 0; i < movesStats.length; i++){

					movesValues[i] = this.computeCombinationValue(movesStats[i], -1, numUpdates, minExtreme, maxExtreme);

					//if(combinationsValues[i] == Double.MAX_VALUE){
					//	maxValue = combinationsValues[i];
					//	selectedCombinationsIndices.add(new Integer(i));
					//}else
					if(movesValues[i] > maxValue){
						maxValue = movesValues[i];
					}
				}
			} */

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

			for(int roleStatsIndex = 0; roleStatsIndex < movesValues.length; roleStatsIndex++){
				for(int statsIndex = 0; statsIndex < movesStats[roleStatsIndex].length; statsIndex++){
					if((valuesFeasibility == null || valuesFeasibility[statsIndex]) &&
							movesValues[roleStatsIndex][statsIndex] >= (maxValue-valueOffset)){
						selectedMovesIndices.add(new MyPair<Integer,Integer>(roleStatsIndex, statsIndex));
					}
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("UcbSelector - SelectMove(MoveStats[][], boolean[], double[], int): detected no fesible moves with value higher than -1.");
			}

			selectedMove = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size()));
		}

		return selectedMove;

	}

	/**
	 * @param movesInfo map with the statistics for each move considered so far. More precisely,
	 * each move is mapped to the corresponding statistics and to the corresponding penalty value.
	 * NOTE: we assume here that the penalty is always specified even when we are not using a bias
	 * computer, thus if no penalty is specified in the gamers settings this value should be
	 * initialized to 0.
	 * ALSO NOTE: this method assumes that all moves are feasible. If you want to exclude some moves
	 * from the selection when using this method then you have to remove them from the map before
	 * passing it to this method. TODO: change the other selectMove method to keep track of the move
	 * associated to each movesStats so that also for that method whenever there are values that
	 * are not feasible we just have to exclude them from the movesStats array before passing it
	 * to the method. If we do so now, we loose the information of the move associated to a certain
	 * move statistic because the index of the move statistic in the array of movesStats might not
	 * correspond to the correct move index.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 * @return the index of the selected move.
	 */
	@Override
	public MyPair<Integer,Move> selectMove(List<Map<Move,MyPair<MoveStats,Double>>> movesInfo, int numUpdates) {

		int totalNumStats = 0;
		for(Map<Move,MyPair<MoveStats,Double>> map : movesInfo){
			totalNumStats += map.size();
		}

		if(totalNumStats == 0){
			throw new RuntimeException("UcbSelector - selectMove(List<Map>, int): cannot select next combination because every map is empty.");
		}

		int listIndex = 0;
		Move selectedMove = null;

		// When no combination of values has been evaluated yet (1st simulation) and there is no
		// bias being added to the combinations we can simply return a random combination, because
		// all combinations will have the same value (i.e. fpu).
		// NOTE: numUpdates == 0 should never be true, because it only happens when the movesInfo
		// map is empty, and in that case we throw an exception!
		if(numUpdates == 0 && this.biasComputer == null){

			int randomNum = this.random.nextInt(totalNumStats);

			for(Map<Move,MyPair<MoveStats,Double>> map : movesInfo){
				if(randomNum-map.size() < 0){
					break;
				}else{
					randomNum -= map.size();
					listIndex++;
				}
			}

			for(Entry<Move,MyPair<MoveStats,Double>> entry : movesInfo.get(listIndex).entrySet()){
				if(randomNum == 0){
					selectedMove = entry.getKey();
				}
				randomNum--;
			}

			if(selectedMove == null){
				throw new RuntimeException("UcbSelector - selectMove(List<Map>, int): found no move when selecting.");
			}

			return new MyPair<Integer,Move>(new Integer(listIndex), selectedMove);

		}else{
			// If this evaluator is adding a bias to the combinations we execute the following code even when it's the 1st
			// simulation. This is because there will be a bias that will distinguish among the combinations and be used to
			// select the first combination to explore.
			double minExtreme = 0.0;
			double maxExtreme = 100.0;

			double maxValue = -1;
			double[] movesValues = new double[totalNumStats];
			Move[] moves = new Move[totalNumStats];
			int[] roleStatsIndices = new int[totalNumStats];

			List<MyPair<Integer,Move>> selectedMovesIndices = new ArrayList<MyPair<Integer,Move>>();

			int roleStatsIndex = 0;
			int index = 0;
			for(Map<Move,MyPair<MoveStats,Double>> map : movesInfo){

				for(Entry<Move,MyPair<MoveStats,Double>> entry : map.entrySet()){

					movesValues[index] = this.computeCombinationValue(entry.getValue().getFirst(), entry.getValue().getSecond().doubleValue(), numUpdates, minExtreme, maxExtreme);
					moves[index] = entry.getKey();
					roleStatsIndices[index] = roleStatsIndex;

					/*
					if(combinationsValues[i] == Double.MAX_VALUE){
						maxValue = combinationsValues[i];
						selectedCombinationsIndices.add(new Integer(i));
					}else*/
					if(movesValues[index] > maxValue){
						maxValue = movesValues[index];
					}

					index++;
				}
				roleStatsIndex++;
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

			for(index = 0; index < movesValues.length; index++){
				if(movesValues[index] >= (maxValue-this.valueOffset)){
					selectedMovesIndices.add(new MyPair<Integer,Move>(roleStatsIndices[index], moves[index]));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("UcbSelector - SelectMove(List<Map>, int): detected no combinations with value higher than -1.");
			}

			return selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size()));

		}

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
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "C = " + this.c +
				indentation + "VALUE_OFFSET = " + this.valueOffset +
				indentation + "FPU = " + this.fpu;

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
