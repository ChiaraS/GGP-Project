package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class represents a move as an array of double.
 *
 * Used for parameters tuning. A move is a list of parameter values, one for each parameter
 * being tuned. This is used to integrate continuous parameter tuning to the current GGP project.
 *
 * @author J. Liu
 *
 */
@SuppressWarnings("serial")
public class ContinuousMove extends Move {

    private int length;
    private double[] continuousMove;
    private ArrayList<Double> fitness;
    private int nbSamples;
    private double meanFitness;
    private double sumFitness;
    private double stepSize = 1;

    public ContinuousMove(double[] entry) {
        if (entry == null || entry.length == 0) {
            GamerLogger.logError("ParametersTuner", "ContinuousMove - Initialization with null or empty array of continuous tunable parameter!");
            throw new RuntimeException("ContinuousMove - Initialization with null or empty array of continuous tunable parameter!");
        }

        this.length = entry.length;
        this.continuousMove = new double[length];
        for (int i=0; i<this.continuousMove.length; i++) {
            this.continuousMove[i] = entry[i];
        }
        this.fitness = new ArrayList<Double>();
        this.nbSamples = 0;
        this.sumFitness = 0.0;
    }

    public ContinuousMove(double[] entry, double stepSize) {
        if (entry == null || entry.length == 0) {
            GamerLogger.logError("ParametersTuner", "ContinuousMove - Initialization with null or empty array of continuous tunable parameter!");
            throw new RuntimeException("ContinuousMove - Initialization with null or empty array of continuous tunable parameter!");
        }

        this.length = entry.length;
        this.continuousMove = new double[length];
        for (int i=0; i<this.continuousMove.length; i++) {
            this.continuousMove[i] = entry[i];
        }
        this.stepSize = stepSize;
    }

    public double addFitness(double newFitness) {
        this.nbSamples++;
        this.sumFitness += newFitness;
        this.fitness.add(newFitness);
        this.meanFitness = this.sumFitness / this.nbSamples;
        return this.meanFitness;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double[] getContinuousMove() {
        return continuousMove;
    }

    public void setContinuousMove(double[] continuousMove) {
        this.continuousMove = continuousMove;
    }

    public double getNbSamples() {
        return nbSamples;
    }

    public void setNbSamples(int nbSamples) {
        this.nbSamples = nbSamples;
    }

    public double getMeanFitness() {
        return meanFitness;
    }

    public void setMeanFitness(double meanFitness) {
        this.meanFitness = meanFitness;
    }

    public double getSumFitness() {
        return this.sumFitness;
    }

    public double getStepSize() {
        return this.stepSize;
    }

    public void updateStepSize(ContinuousMove parent, Random rdm) {
        this.stepSize = parent.getStepSize() * rdm.nextGaussian()/2/length;
    }

    public void updateGenome(ContinuousMove parent, Random rdm) {
        updateStepSize(parent, rdm);
        double[] parentMove = parent.getContinuousMove();
        for (int i=0; i<length; i++) {
            this.continuousMove[i] = parentMove[i] * this.stepSize;
        }
    }

    public int compare(ContinuousMove o1, ContinuousMove o2) {

        double value1;
        if (o1.getNbSamples() == 0) {
            value1 = Double.MAX_VALUE;
        } else {
            value1 = o1.getMeanFitness();
        }
        double value2;
        if (o2.getNbSamples() == 0) {
            value2 = Double.MAX_VALUE;
        } else {
            value2 = o2.getMeanFitness();
        }
        // Sort from largest to smallest
        if (value1 > value2) {
            return -1;
        } else if (value1 < value2) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
    	if ((o != null) && (o instanceof ContinuousMove)) {
    		ContinuousMove move = (ContinuousMove) o;
            return Arrays.equals(this.continuousMove, move.getContinuousMove());
        }

        return false;
    }

    @Override
    public int hashCode() {
    	return Arrays.hashCode(this.continuousMove);
    }

    @Override
    public String toString() {

    	if(this.continuousMove != null){
    		String s = "[ ";
    		for(int i = 0; i < this.continuousMove.length; i++){
    			s += this.continuousMove[i] + " ";
    		}
    		s += "]";
    		return s;
    	}else{
    		return "null";
    	}

    }
}
