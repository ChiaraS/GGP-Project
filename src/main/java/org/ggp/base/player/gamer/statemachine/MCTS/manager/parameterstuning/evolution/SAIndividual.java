package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Random;

public class SAIndividual {
    private int rank;
    private double[] genome;
    private double meanFitness;
    private int samples;
    private double stepSize;
    private Random rdm;


    public SAIndividual(int nbGenes) {
        this.genome = new double[nbGenes];
        this.stepSize = 1;
        for (int i=0; i<genome.length; i++) {
            this.genome[i] = rdm.nextGaussian();
        }
        this.samples = 0;
        this.meanFitness = Double.NEGATIVE_INFINITY;
    }

    public SAIndividual(int nbGenes, Random rdm) {
        this.rdm = rdm;
        this.genome = new double[nbGenes];
        this.stepSize = 1;
        for (int i=0; i<genome.length; i++) {
            this.genome[i] = rdm.nextGaussian();
        }
        this.samples = 0;
        this.meanFitness = Double.NEGATIVE_INFINITY;
    }

}
