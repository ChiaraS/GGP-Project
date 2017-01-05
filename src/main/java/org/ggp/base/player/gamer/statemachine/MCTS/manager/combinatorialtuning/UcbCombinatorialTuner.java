package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning;

import java.util.LinkedList;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.structure.CombinatorialMoveStats;
import org.ggp.base.util.logging.GamerLogger;

/**
 * This tuner selects the combinations of values for the parameter of a single role.
 *
 * @author C.Sironi
 *
 */
public class UcbCombinatorialTuner extends CombinatorialTuner {

	private Random random;

	private double evoC;

	private double evoValueOffset;

	/**
	 * If true the tuner will re-scale the move values between 0 and 1 with respect
	 * to the maximum and the minimum fitness of the currently considered individuals.
	 * If false the values will be rescaled using [0, 100] as extremes, like normally.
	 *
	 * NOTE: normalization should help stretching the values more, thus making more evident the difference
	 * between them and steering the manager towards using even more often the best values.
	 */
	private boolean useNormalization;






	/**
	 * Statistics for all possible combinatorial moves.
	 * (Combinatorial moves = all possible combinations of indices of the unit moves).
	 */
	private CombinatorialMoveStats[] combinatorialMovesStats;

	/**
	 *  Number of times any of the combinatorial actions has been evaluated.
	 */
	private int numUpdates;

	/**
	 * Index in the array of statistics of the currently selected combinatorial action
	 * (i.e. configuration of parameters).
	 */
	private int currentSelectedCombination;

	public UcbCombinatorialTuner(int[] classesLength) {
		super(classesLength);

		int numCombinatorialMoves = 1;

		// Count all the possible combinatorial moves
		for(int i = 0; i < this.classesLength.length; i++){
			if(classesLength[i] <= 0){
				GamerLogger.logError("SearchManagerCreation", "UcbCombinatorialTuner - Initialization with class of moves of length less than 1. No values for the calss!");
				throw new RuntimeException("UcbCombinatorialTuner - Initialization with class of moves of length 0. No values for the calss!");
			}

			numCombinatorialMoves *= classesLength[i];
		}

		// Create all the possible combinatorial moves
		this.combinatorialMovesStats = new CombinatorialMoveStats[numCombinatorialMoves];

		this.crossProduct(new int[1], new LinkedList<Integer>());

		/*
		for(int i = 0; i < this.combinatorialMovesStats.length; i++){
			System.out.println(combinatorialMovesStats[i]);
		}
		*/

	}

    private void crossProduct(int[] nextFreeIndex, LinkedList<Integer> partial){
        if (partial.size() == this.classesLength.length) {
            this.combinatorialMovesStats[nextFreeIndex[0]] = new CombinatorialMoveStats(this.toIntArray(partial));
            nextFreeIndex[0]++;
        } else {
            for(int i = 0; i < this.classesLength[partial.size()]; i++) {
                partial.addLast(new Integer(i));
                this.crossProduct(nextFreeIndex, partial);
                partial.removeLast();
            }
        }
    }

    protected int[] toIntArray(LinkedList<Integer> partial){
    	int[] intArray = new int[partial.size()];

    	int index = 0;
    	for(Integer i : partial){
    		intArray[index] = i.intValue();
    		index++;
    	}
    	return intArray;
    }

    /*
	public int[] selectNextCombination(){

		// This should mean that no combination of values has been evaluated yet (1st simulation),
		// thus we return a random combination.
		if(this.numUpdates == 0){
			this.currentSelectedCombination = this.random.nextInt(this.combinatorialMovesStats.length);
		}else{

			double minExtreme = 0.0;
			double maxExtreme = 100.0;
			double avgValue;

			// If we want to normalize the values of the combinations before selecting one...
			if(this.useNormalization){

				// We need to compute the extremes in which to normalize
				minExtreme = Double.MAX_VALUE;
				maxExtreme = -Double.MAX_VALUE;

					for(int j = 0; j < this.populations[i].length; j++){

						int individualEvaluations = this.populations[i][j].getNumEvaluations();
						int totalFitness = this.populations[i][j].getTotalFitness();

						if(individualEvaluations != 0){
							avgFitness = (((double)totalFitness)/((double)individualEvaluations));

							if(avgFitness < minExtreme){
								minExtreme = avgFitness;
							}

							if(avgFitness > maxExtreme){
								maxExtreme = avgFitness;
							}
						}
					}

					// If no max or min value has been found because no individual has been evaluated once yet,
					// or max and min values are the same, then initilize them to the standard expected extremes,
					// that is [0, 100]
					if(minExtreme >= maxExtreme){
						minExtreme = 0.0;
						maxExtreme = 100.0;
					}

				}

				double maxValue = -1;
				double[] individualsValues = new double[this.populations[i].length];

				List<Integer> selectedIndividualsIndices = new ArrayList<Integer>();

				for(int j = 0; j < this.populations[i].length; j++){

					individualsValues[j] = this.computeIndividualsValue(populations[i][j], this.numUpdates[i], minExtreme, maxExtreme);

					if(individualsValues[j] == Double.MAX_VALUE){
						maxValue = individualsValues[j];
						selectedIndividualsIndices.add(new Integer(j));
					}else if(individualsValues[j] > maxValue){
						maxValue = individualsValues[j];
					}
				}

				/*
				System.out.print("Values for population " + i + ": [ ");
				for(int j = 0; j < individualsValues.length; j++){
					System.out.print(individualsValues[j] + " ");
				}
				System.out.println("]");



				// NOTE: as is, this code always selects an "unexplored" individual if there is one, even if
				// there is an individual that has a value higher than (Double.MAX_VALUE-this.valueOffset).
				if(maxValue < Double.MAX_VALUE){
					for(int j = 0; j < individualsValues.length; j++){
						if(individualsValues[j] >= (maxValue-this.evoValueOffset)){
							selectedIndividualsIndices.add(new Integer(j));
						}
					}
				}

				// Extra check (should never be true).
				if(selectedIndividualsIndices.isEmpty()){
					throw new RuntimeException("Evolution manager - SelectNextIndividuals(): detected no individuals with fitness value higher than -1.");
				}

				this.currentSelectedIndividuals[i] = selectedIndividualsIndices.get(this.random.nextInt(selectedIndividualsIndices.size())).intValue();
				nextIndividuals[i] = this.populations[i][this.currentSelectedIndividuals[i]].getParameter();

			}

		}

		/*
		System.out.print("Returning best individuals: [ ");
		for(int j = 0; j < nextIndividuals.length; j++){
			System.out.print(nextIndividuals[j] + " ");
		}
		System.out.println("]");

		System.out.println();
		//System.out.println("C = " + nextIndividuals[0]);


		return nextIndividuals;

	}*/


    /*
    public static void main(String args[]){
    	int[] lengths = new int[3];

    	lengths[0] = 3;
    	lengths[1] = 2;
    	lengths[2] = 4;

    	UcbCombinatorialTuner tuner = new UcbCombinatorialTuner(lengths);
    }
    */

}
