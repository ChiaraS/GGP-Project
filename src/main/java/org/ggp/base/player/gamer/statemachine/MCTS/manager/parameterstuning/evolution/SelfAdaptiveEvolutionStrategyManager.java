package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;

public class SelfAdaptiveEvolutionStrategyManager extends ContinuousEvolutionManager {
    protected double stepSize[];  // consider isotropic step size control
    protected int resampling = 1;
    protected double[][] population;

    protected int nbTunableParams;
    protected double[][] bounds;
    protected boolean[] isContinuous;   // set if a parameter is continuous, this is a fast but dirty solution

    // TODO: 03/11/2017
    public SelfAdaptiveEvolutionStrategyManager(GameDependentParameters gameDependentParameters,
                                                Random random,
                                                GamerSettings gamerSettings,
                                                SharedReferencesCollector sharedReferencesCollector) {
        super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

        this.resampling = gamerSettings.getIntPropertyValue("SelfAdaptiveEvolutionStrategyManager.resampling");

        // set the pop size, etc and ignore the related setting in the properties
        this.nbTunableParams = this.continuousParametersManager.getNumTunableParameters();
        // according to the rule in a paper that I forgot title...
        this.populationsSize = (int) Math.ceil(4 + Math.log(nbTunableParams)) + 1;
        initStepSize();
        intBounds();
        initPopulation();
    }


    @Override
    public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
        super.setReferences(sharedReferencesCollector);
    }

    @Override
    public void clearComponent() {
        super.clearComponent();
    }

    @Override
    public void setUpComponent() {
        super.setUpComponent();
    }

    /**
     * Returns random initial population with populationSize individuals.
     */
    public CompleteMoveStats[] getInitialPopulation() {

        CompleteMoveStats[] populationStats = new CompleteMoveStats[this.populationsSize];

        initPopulation();

        for(int i = 0; i < this.populationsSize; i++){
            populationStats[i] = new CompleteMoveStats(new ContinuousMove(this.population[i]));
        }

        return populationStats;
    }


    /**
     * For the moment, take the discrete values and find the lower/upper bound
     */
    public void intBounds() {
        if (nbTunableParams>0) {
            bounds = new double[nbTunableParams][2];
            // set the bounds
            for (int i=0; i<nbTunableParams; i++) {
                bounds[i][0] = this.continuousParametersManager.getPossibleValuesInterval(i).getLeftExtreme();  // lower bound
                bounds[i][1] = this.continuousParametersManager.getPossibleValuesInterval(i).getRightExtreme();  // upper bound
                /*
                for (int j=1; j<_possbileValues.length; j++) {
                    double _value = _possbileValues[j];
                    if (_value < bounds[i][0]) {
                        bounds[i][0] = _value;
                    } else if (_value > bounds[i][1]) {
                        bounds[i][1] = _value;
                    }
                }*/
            }
        } else {
            System.err.println("[ERROR] SelfAdaptiveEvolutionStrategyManager: nbTunableParams == 0");
        }
    }

    public void initStepSize() {
        if (populationsSize <= 0) {
            GamerLogger.logError("SelfAdaptiveEvolutionStrategyManager", "Constructor - populationsSize <= 0!");
            throw new RuntimeException("Constructor - populationsSize <= 0!");
        }

        this.stepSize = new double[populationsSize];
        for (int i=0; i<stepSize.length; i++) {
            this.stepSize[i] = 1;
        }
    }
    /**
     * Indicate the continuous tunable parameters
     * @param isContinuousTunable
     */
    public void setContinuousTunableIdx(boolean[] isContinuousTunable) {
        if (isContinuousTunable == null) {
            GamerLogger.logError("SelfAdaptiveEvolutionStrategyManager", "setContinuousTunableIdx - Initialization with null list of continuous tunable parameter indices!");
            throw new RuntimeException("setContinuousTunableIdx - Initialization with null list of continuous tunable parameter indices!");
        }

        int _nbIdx = isContinuousTunable.length;

        if (_nbIdx != nbTunableParams) {
            GamerLogger.logError("SelfAdaptiveEvolutionStrategyManager", "setContinuousTunableIdx - Array length not match!");
            throw new RuntimeException("setContinuousTunableIdx - Array length not match!");
        }

        this.isContinuous = new boolean[_nbIdx];

        for (int i=0; i<_nbIdx; i++) {
            this.isContinuous[i] = isContinuousTunable[i];

        }
    }

    public void initPopulation() {
        this.population = new double[populationsSize][nbTunableParams];
        for (int i=0; i<populationsSize; i++) {
            for (int j=0; j<nbTunableParams; j++) {
                this.population[i][j] = random.nextGaussian();
            }
        }
    }

    /**
     * Rank the population by high -> low mean fitness
     */
    public void rankPopulation(EvoProblemRepresentation roleProblem) {
        Arrays.sort(roleProblem.getPopulation(),
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
    }


    public double[] meanOfElite() {
        double[] meanOfElite = new double[nbTunableParams];
        for (int j=0; j<nbTunableParams; j++) {
            for (int i=0; i<eliteSize; i++) {
                meanOfElite[j] += population[i][j];
            }
            meanOfElite[j] /= eliteSize;
        }
        return meanOfElite;
    }




//    @Override
//    public void evolvePopulation(EvoProblemRepresentation roleProblem) {
//        // The size of the elite must be at least 1
//        if(this.eliteSize <= 0){
//            GamerLogger.logError("EvolutionManager", "StandardEvolutionManager - Impossible to evolve the population. Elite size " + this.eliteSize + " <= 0.");
//            throw new RuntimeException("StandardEvolutionManager - Impossible to evolve the population. Elite size " + this.eliteSize + " <= 0.");
//        }
//
//        // Generate new population
//        double[] parent = meanOfElite();
//        for (int i=this.eliteSize; i<populationsSize; i++) {
//            this.stepSize[i] = this.stepSize[i] * Math.exp(random.nextGaussian()/(2*nbTunableParams));
//            for (int j=0; j<nbTunableParams; j++) {
//                this.population[i][j] = parent[j] + random.nextGaussian()*stepSize[i];
//
//            }
//            roleProblem.getPopulation()[i].resetStats(new ContinuousMove(this.population[i]));
//        }
//
//        rankPopulation(roleProblem);
//    }

    public void evolvePopulation(EvoProblemRepresentation roleProblem) {
        // The size of the elite must be at least 1
        if(this.eliteSize <= 0){
            GamerLogger.logError("EvolutionManager", "StandardEvolutionManager - Impossible to evolve the population. Elite size " + this.eliteSize + " <= 0.");
            throw new RuntimeException("StandardEvolutionManager - Impossible to evolve the population. Elite size " + this.eliteSize + " <= 0.");
        }

        // Get previous elites
        double[][] prevElites = new double[eliteSize][nbTunableParams];
        double[] prevElitesStepSize = new double[eliteSize];
        CompleteMoveStats[] lastPopulationStats = roleProblem.getPopulation();
        for (int i=0; i<eliteSize; i++) {
            if (lastPopulationStats[i].getTheMove() instanceof ContinuousMove) {
                ContinuousMove thisMove = (ContinuousMove) lastPopulationStats[i].getTheMove();
                prevElitesStepSize[i] = thisMove.getStepSize();
                for (int j=0; j<nbTunableParams; j++) {
                    prevElites[i][j] = thisMove.getContinuousMove()[j];
                }
            }
        }

        int totalUpdates = roleProblem.getTotalUpdates();

        // Generate new population
        for (int i=0; i<populationsSize; i++) {
            // Get parent Index
            int parentIdx = i % eliteSize;
            // New stepsize
            double newStepSize = prevElitesStepSize[parentIdx] * random.nextGaussian() / 2 / nbTunableParams;
            // New genome
            double[] newMove = new double[nbTunableParams];
            for (int j=0; j<nbTunableParams; j++) {
                newMove[j] =  prevElites[i][j] * newStepSize;
            }
            totalUpdates -= roleProblem.getPopulation()[i].getVisits();
            roleProblem.getPopulation()[i].resetStats(new ContinuousMove(newMove, newStepSize));
        }

        roleProblem.setTotalUpdates(totalUpdates);

        rankPopulation(roleProblem);

    }

    public double generateStepSize(ContinuousMove parent) {
        return parent.getStepSize() * random.nextGaussian()/2/nbTunableParams;
    }

    public ContinuousMove generateNewMove(ContinuousMove parent) {
        double[] newMove = new double[nbTunableParams];
        double newStepSize = generateStepSize(parent);
        double[] parentMove = parent.getContinuousMove();
        for (int i=0; i<nbTunableParams; i++) {
            newMove[i] = parentMove[i] * newStepSize;
        }
        return new ContinuousMove(newMove, newStepSize);
    }

    @Override
    public String getComponentParameters(String indentation) {

        String superParams = super.getComponentParameters(indentation);

        String params = indentation + "POPULATION_SIZE = " + this.populationsSize +
                indentation + "ELITE_SIZE = " + this.eliteSize +
                indentation + "RESAMPLING = " + this.resampling;

        if(superParams != null){
            return superParams + params;
        }else{
            return params;
        }

    }
}
