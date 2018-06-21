package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.continuoustuners;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.CMAESManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SelfAdaptiveESProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

/**
 * This class tunes the parameters for each role independently. Each role has its own population.
 *
 * ATTENTION! Parts of this class assume that the size of the populations is always the same
 * (e.g. the CombosOfIndividualsIterator).
 * If a population can change size over time, those parts must be fixed.
 *
 * TODO: this class shares a lot of similarities with MultiPopEvoParametersTuner, but it's a separate
 * class because it needs to use a ContinuousParametersManager instead of a DiscreteParametersManager.
 * Refactor the code in such a way that the two classes can share the common code (not done yet for lack
 * of time).
 *
 */
public class SelfAdaptiveESTuner extends ContinuousParametersTuner {

	/**
	 * We do not decide when CMA-ES stops the execution, so if the game is over before the CMA-ES instance
	 * for a role managed to finish the optimization we have to compute the best solution so far, instead
	 * of the best solution overall. When memorizing the best solution (i.e. parameter combination) for a
	 * role we also memorize its type, that describes how the solution was computed. The possible types of
	 * solution are the following:
	 * - INTERMEDIATE: if the currently set combination for a role is not a solution but just the combination
	 * 				   currently being evaluated.
	 * - PREVIOUS_POPULATION: the solution corresponds to the best evaluated solution returned by the CMA-ES
	 * 						  before it could finish its execution. The population currently being evaluated
	 * 						  is not used to update the distribution because not all individuals have been
	 * 						  evaluated at least once, so the best evaluated solution depends on the execution
	 * 						  of CMA-ES up until the previous population's fitness has been updated.
	 * - UNDERSAMPLED_POPULATION: the solution corresponds to the best evaluated solution returned by the
	 * 							  CMA-ES before it could finish its execution and after the population
	 * 							  currently being evaluated has been used to update the distribution, even
	 * 							  if not all individuals have been sampled the predefined amount of times
	 * 							  (but all of them have been sampled at least once).
	 * - UNDERSAMPLED_MEAN: the solution corresponds to the best evaluated solution returned by the CMA-ES
	 * 						after its execution has terminated properly, and the fitness of the mean solution
	 * 						has been computed with LESS THAN the predefined amount of samples and updated.
	 * - FINAL: the solution corresponds to the best evaluated solution returned by the CMA-ES after its
	 * 			execution has terminated properly, and the fitness of the mean solution has been computed
	 * 			with the predefined amount of samples and updated.
	 *
	 * @author c.sironi
	 *
	 */
    public enum SolutionType{
    	INTERMEDIATE, PREVIOUS_POPULATION, UNDERSAMPLED_POPULATION, UNDERSAMPLED_MEAN, FINAL
    }

    private boolean logPopulations;

    /**
     * Takes care of evolving a given population depending on the fitness of its individuals.
     */
    protected CMAESManager cmaesManager;

    /**
     * Given the statistics of each combination, selects the best one among them.
     */
    //protected TunerSelector bestCombinationSelector;

    /**
     * NOTE: this feature is not available for this tuner, the individuals of the population are evaluated in a
     * random order against each other. Different populations can have different sizes and be evolved at different
     * moments in time (i.e. the instances of CMA-ES proceed in their execution independently of each other).
     *
     *
     * If true, when evaluating a population, the fitness of each individual will result from
     * testing with a simulation ALL combinations of individuals, one from each population.
     * If false, random combinations of individuals (one for each population) will be evaluated,
     * so that each individual is evaluated the same number of times as the other individuals.
     *
     * When evaluating the current populations, all combinations of individuals, one from each population,
     * are tested in a MCTS simulation.
     *
     * E.g. if we have the following populations:
     * 	p_0 = {c_00; c_01; c_02}
     * 	p_1 = {c_10; c_11; c_12}
     *	p_2 = {c_20; c_21; c_22}
     * where c_ij is combination j of population of role i, when we want to compute the fitness of each
     * combination (i.e. individual) we perform k MCTS simulations for each possible combination of
     * combinations. Thus we will run MCTS simulation with the following combinations of combinations:
     * (c_00, c_10, c20) (c_00, c_10, c21) (c_00, c_10, c22) (c_00, c_11, c20) (c_00, c_11, c21) (c_00, c_11, c22)
     * (c_00, c_12, c20) (c_00, c_12, c21) (c_00, c_12, c22) (c_01, c_10, c20) (c_01, c_10, c21) (c_01, c_10, c22)
     * (c_01, c_11, c20) (c_01, c_11, c21) (c_01, c_11, c22) (c_01, c_12, c20) (c_01, c_12, c21) (c_01, c_12, c22)
     * (c_02, c_10, c20) (c_02, c_10, c21) (c_02, c_10, c22) (c_02, c_11, c20) (c_02, c_11, c21) (c_02, c_11, c22)
     * (c_02, c_12, c20) (c_02, c_12, c21) (c_02, c_12, c22).
     */
    //private boolean evaluateAllCombosOfIndividuals;

    /**
     * Keeps track of which combinations of individuals must be evaluated and in which order.
     */
    //private CombosOfIndividualsIterator combosOfIndividualsIterator;

    /**
     * If evaluating all combinations of individuals before evolving the populations (i.e. evaluateAllCombosOfIndividuals == true),
     * this parameter specifies the number of times all possible combinations of combinations (i.e. individuals) must be evaluated
     * before using the collected statistics to evolve the population.
     *
     * If evaluating random combinations of individuals before evolving the populations (i.e. evaluateAllCombosOfIndividuals == false),
     * this parameter specifies the number of times each individual must be evaluated against other random individuals. More precisely,
     * when evaluateAllCombosOfIndividuals == false, the evaluation is performed as follows:
     *
     * (Note that all populations will have the same number of individuals)
     * For evalRepetitions times do
     * 	For each population p in {p_0,...,p_n}:
     * 		shuffle the individuals in p
     * 	EndFor
     * 	For (i = 0; i < p_0.length; i++)
     * 		test combination of individuals (p_0[i],...,p_n[i])
     * 	EndFor
     * EndFor
     *
     */
    private int evalRepetitions;    // resampling number

    /**
     * Number of times we want to evaluate the mean combination at the end of the execution of CMA-ES
     * for a role before we update its fitness in the CMA-ES instance of the role.
     */
    private int evalRepetitionsForMeanCombo;

    /**
     * Memorize the currently set combinations of parameters for each role.
     * Memorizing it here is redundant, but this parameter is also used to temporarily memorize
     * the best combinations until we know if the final game result was a win (and thus we memorize
     * the combinations permanently in bestCombinations), or a loss.
     */
    protected double[][] selectedCombinations;

    /**
     * Memorizes the best so far combination of parameters values for each role.
     */
    private double[][] bestCombinations;

    /**
     * For each role, memorizes the type of combination that is set in the corresponding selectedCombinations
     * entry. It will be INTERMEDIATE for each combination that is not the final solution, and one of the other
     * types if the combination has been computed as solution at the end of the game. NOTE that it can be the
     * final solution properly computed by CMA-ES or an intermediate solution computed before CMA-ES could
     * properly terminate because the game being played is over.
     */
    private SolutionType[] solutionTypes;

    private SelfAdaptiveESProblemRepresentation[] roleProblems;

    public SelfAdaptiveESTuner(GameDependentParameters gameDependentParameters, Random random,
                               GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
        super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

        this.logPopulations = gamerSettings.getBooleanPropertyValue("ParametersTuner.logPopulations");

        /* This class explicitly needs the evolution manager to be of type CMAESManager, so we just create it instead of
         * allowing to set it from the settings file
        try {
            this.cmaesManager = (CMAESManager) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.CONTINUOUS_EVOLUTION_MANAGERS.getConcreteClasses(),
                    gamerSettings.getPropertyValue("ParametersTuner.evolutionManagerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            // TODO: fix this!
            GamerLogger.logError("SearchManagerCreation", "Error when instantiating EvolutionManager " + gamerSettings.getPropertyValue("ParametersTuner.EvolutionManagerType") + ".");
            GamerLogger.logStackTrace("SearchManagerCreation", e);
            throw new RuntimeException(e);
        }*/
        this.cmaesManager = new CMAESManager(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

        /*
        String[] componentDetails = gamerSettings.getIDPropertyValue("ParametersTuner.bestCombinationSelectorType");

        try {
            this.bestCombinationSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), componentDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, componentDetails[1]);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            // TODO: fix this!
            GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.bestCombinationSelectorType") + ".");
            GamerLogger.logStackTrace("SearchManagerCreation", e);
            throw new RuntimeException(e);
        }*/

        //this.evaluateAllCombosOfIndividuals = gamerSettings.getBooleanPropertyValue("ParametersTuner.evaluateAllCombosOfIndividuals");

        this.evalRepetitions = gamerSettings.getIntPropertyValue("ParametersTuner.evalRepetitions");

        this.evalRepetitionsForMeanCombo = gamerSettings.getIntPropertyValue("ParametersTuner.evalRepetitionsForMeanCombo");

        //this.populations = null;

        //this.combosOfIndividualsIndices = null;

        //this.currentComboIndex = 0;

        this.selectedCombinations = null;

        this.bestCombinations = null;

        this.solutionTypes = null;

        this.roleProblems = null;


    }

    @Override
    public void setReferences(SharedReferencesCollector sharedReferencesCollector){
        super.setReferences(sharedReferencesCollector);

        this.cmaesManager.setReferences(sharedReferencesCollector);

        //this.bestCombinationSelector.setReferences(sharedReferencesCollector);
    }

    @Override
    public void clearComponent() {
        super.clearComponent();

        this.cmaesManager.clearComponent();

        //this.bestCombinationSelector.clearComponent();
    }

    @Override
    public void setUpComponent() {
        super.setUpComponent();

        this.cmaesManager.setUpComponent();

        //this.bestCombinationSelector.setUpComponent();

        int numRolesToTune;

        if(this.tuneAllRoles){
            numRolesToTune = this.gameDependentParameters.getNumRoles();
        }else{
            numRolesToTune = 1;
        }

        // We need to initialize the populations if:
        // 1. We are not going to reuse the best combo of previous games
        // 2. We are going to reuse the best combo of previous games, but that has not been computed yet
        // 3. We are going to reuse the best combo of previous games, it has been computed, but its size
     	// doesn't correspond to the number of roles that we have to tune.
        // (NOTE: we can reuse the best combo not only if we are playing the exact same game, but for any
        // game (it probably doesn't make much sense, though).
        if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

            // If we need to initialize the populations, here we have to check if we need a new one or if we should
            // reuse the previous ones that have been saved.
            // We need new ones if:
            // 1. We don't want to reuse the previous ones
            // 2. We want to reuse the previous ones, but we have none yet
        	// 3. We want to reuse the previous ones, we have them but their size doesn't correspond to the number
        	// of roles that we have to tune.
            // (NOTE: we can reuse the population not only if we are playing the exact same game, but for any
            // game (might make sense. We will start evolving for the current game a population that is not random
            // but already pretty good for another game - still makes little sense if the two games are completely
            // different and require very different parameter values).
            if(!this.reuseStats || this.getRoleProblems() == null || this.getRoleProblems().length != numRolesToTune) {

                // Create log file for populations.
                if(this.logPopulations){
                    String globalParamsOrder = this.getGlobalParamsOrder();
                    GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Populations", "PARAMS=;" + globalParamsOrder + ";");
                }

                // Create the initial population for each role
                this.createRoleProblems(numRolesToTune);
				/*
				this.roleProblems = new EvoProblemRepresentation[numRolesToTune];
				for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
					roleProblems[roleProblemIndex] = new EvoProblemRepresentation(this.saesManager.getInitialPopulation());
				}*/

				/*
				this.populations = new CompleteMoveStats[numRolesToTune][];
				for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
					populations[populationIndex] = this.saesManager.getInitialPopulation();
				}*/

                if(this.logPopulations){
                    this.logPopulations();
                }

                /*
                int[] popSizes = new int[this.getRoleProblems().length];
                for(int i = 0; i < popSizes.length; i++){
                    popSizes[i] = this.getRoleProblems()[i].getPopulation().length;
                }*/

                /*
                if(this.evaluateAllCombosOfIndividuals){
                    this.combosOfIndividualsIterator = new AllCombosOfIndividualsIterator(popSizes);
                }else{
                    this.combosOfIndividualsIterator = new RandomCombosOfIndividualsIterator(popSizes);
                }*/

                //this.evalRepetitionsCount = -1;

                this.selectedCombinations = new double[numRolesToTune][this.continuousParametersManager.getNumTunableParameters()];

                this.solutionTypes = new SolutionType[this.selectedCombinations.length];
                for(int i = 0; i < this.solutionTypes.length; i++) {
                	this.solutionTypes[i] = SolutionType.INTERMEDIATE;
                }
            }

            this.bestCombinations = null;
        }else{
            //this.populations = null;
            this.setRoleProblemsToNull();
            this.selectedCombinations = null;
        }

    }

    protected void createRoleProblems(int numRolesToTune) {
    	// Create the initial population for each role
    	this.roleProblems = new SelfAdaptiveESProblemRepresentation[numRolesToTune];
    	for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
    		roleProblems[roleProblemIndex] = this.cmaesManager.createRoleProblemWithInitialPopulation();
    	}
    }

    @Override
    public void setNextCombinations() {

    	// First check for each role problem that still has a population if all individuals have been evaluated
    	// the predefined amount of times and we need to evolve the population.
        for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){

        	// If the CMA-ES instance of the role problem is still optimizing (i.e. population != null),
        	// get the next individual from the population, evolving the population if we evaluated all
        	// individuals the expected amount of times.
        	if(this.getRoleProblems()[roleProblemIndex].getPopulation() != null) {

        		// Advance to the next combination to evaluate and if null it means we finished evaluating the
        		// population, so we have to evolve it.
        		// Note that to make the code simpler we should advance to the next combination at the end of the
        		// updateStatistics() method, however, at the moment there is still the possibility to set the
        		// TunerBeforeSimulationStrategy to change combination only after a batch of N simulations by calling
        		// setNextCombinations only once every N simulations, but calling updateStatistics() for each of the
        		// N simulations. If we would advance to the next combination in the updateStatistics() method and at
        		// the same time set the TunerBeforeSimulationStrategy to set the next combination only every N
        		// simulations we would advance N times anyway without actually evaluating the combinations.
        		if(this.getRoleProblems()[roleProblemIndex].advanceToNextIndividual(this.evalRepetitions) == null) {

        			// If yes, evolve the population
        			this.cmaesManager.evolvePopulation(this.getRoleProblems()[roleProblemIndex]);

                    if(this.logPopulations){
                        this.logPopulation(roleProblemIndex);
                    }

                    // If we evolved the population and if we have a new population, we have to advance again to the
                    // next individual that must be evaluated, because the iteration is reset and the index isn't
                    // pointing to any combination yet (index == -1).
                    if(this.getRoleProblems()[roleProblemIndex].getPopulation() != null) {
                    	// To be sure, check that the next individual is not null. Note that if this happens there must
                    	// be a problem in the code, because either the evolved population has popSize individuals, or
                    	// it is null because now we want to evaluate the mean solution.
                    	if(this.getRoleProblems()[roleProblemIndex].advanceToNextIndividual(this.evalRepetitions) == null) {
                    		GamerLogger.logError("ParametersTuner", "SelfAdaptiveESTuner - Impossible to set next individual for a role problem. The evolved population is null!");
        	                throw new RuntimeException("SelfAdaptiveESTuner - Impossible to set next individual for a role problem. The evolved population is null!");
                    	}// ...otherwise we have advanced to the next individual
                    }
        		}
        	}
        }

        // Now for each role problem get the next individual to be evaluated

        boolean foundAllBest = true;

        Move theParametersCombination;

        for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){

        	// If the CMA-ES instance of the role problem is still optimizing (i.e. population != null),
        	// get the next individual from the population.
        	if(this.getRoleProblems()[roleProblemIndex].getPopulation() != null) {
        		theParametersCombination = this.getRoleProblems()[roleProblemIndex].getCurrentIndividual().getTheMove();
	            if(theParametersCombination instanceof ContinuousMove){
	                this.selectedCombinations[roleProblemIndex] = ((ContinuousMove) theParametersCombination).getContinuousMove();
	            }else{
	                GamerLogger.logError("ParametersTuner", "SelfAdaptiveESTuner - Impossible to set next combinations. The Move is not of type ContinuousMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
	                throw new RuntimeException("SelfAdaptiveESTuner - Impossible to set next combinations. The Move is not of type ContinuousMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
	            }
	            foundAllBest = false;
        	}else { // If the population is null, we have two possibilities:
        		// 1. The mean combo is not null: we have two more possibilities:
        		if(this.getRoleProblems()[roleProblemIndex].getMeanValueCombo() != null) {
        			// 1.1. We haven't reached the number of predefined evaluations for the mean, so we evaluate it again
        			if(this.getRoleProblems()[roleProblemIndex].getMeanValueCombo().getVisits() < this.evalRepetitionsForMeanCombo) {
        				this.selectedCombinations[roleProblemIndex] = ((ContinuousMove) this.getRoleProblems()[roleProblemIndex].getMeanValueCombo().getTheMove()).getContinuousMove();
        				foundAllBest = false;
        			// 1.2. We have reached the number of predefined evaluations for the mean, so we can set its fitness
        			// and set the best combination found by the CMA-ES algorithm.
        			}else {
        				this.selectedCombinations[roleProblemIndex] = ((ContinuousMove) this.cmaesManager.updateMeanFitnessAndGetBest(this.getRoleProblems()[roleProblemIndex]).getTheMove()).getContinuousMove();
        				this.solutionTypes[roleProblemIndex] = SolutionType.FINAL;
        			}
        		}// 2. The mean combo is also null: this means that in the previous call to the method setNextCombinations()
        		 // we already set the best combination for the role and we don't need to do anything anymore
        	}
        }

        this.continuousParametersManager.setParametersValues(this.selectedCombinations);

		if(foundAllBest){
			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations, this.solutionTypes));
			this.stopTuning();
		}

    }

    @Override
    public void setBestCombinations() {

    	if(this.isMemorizingBestCombo()){
			 this.continuousParametersManager.setParametersValues(this.bestCombinations);
		}else{
			this.computeAndSetBestCombinations();
		}

        this.stopTuning();

    }

    protected void computeAndSetBestCombinations(){

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			// We have 3 cases:
			// 1. The population is not null, so we were still tuning with CMA-ES: query the CMA-ES manager to get
			// the best solution so far. Note that since the optimization was interrupted we might not have the
			// fitness for all the individuals in the current population. In this case we cannot update the distribution
			// for CMA-ES and we just get the best solution so far. On the contrary, if each individual has been sampled
			// at least once, we still use the fitness of the population to update the distribution, even if such fitness
			// has not been obtained with the desired amount of samples per individual. Then, we get the best solution so
			// far, considering the updated distribution.
			if(this.getRoleProblems()[roleProblemIndex].getPopulation() != null) {
				// Check if each individual has at least one visit (i.e. has been sampled at least once)
				boolean updateDistribution = true;
				int individualIndex = 0;
				while(individualIndex < this.getRoleProblems()[roleProblemIndex].getPopulation().length && updateDistribution) {
					updateDistribution = this.getRoleProblems()[roleProblemIndex].getPopulation()[individualIndex].getVisits() > 0;
					individualIndex++;
				}
				// If each individual has been sampled at least once we can update the distribution
				if(updateDistribution) {
					this.selectedCombinations[roleProblemIndex] =
							((ContinuousMove) this.cmaesManager.updatePopulationFitnessAndGetBestSoFar(this.getRoleProblems()[roleProblemIndex]).getTheMove()).getContinuousMove();
					this.solutionTypes[roleProblemIndex] = SolutionType.UNDERSAMPLED_POPULATION;
				}else {
					this.selectedCombinations[roleProblemIndex] =
							((ContinuousMove) this.cmaesManager.getBestSoFar(this.getRoleProblems()[roleProblemIndex]).getTheMove()).getContinuousMove();
					this.solutionTypes[roleProblemIndex] = SolutionType.PREVIOUS_POPULATION;
				}
			}else {
				// 2. The population is null, but the mean value combination is being evaluated: if the mean value combination
				// is not null, then it must have at least one visit (i.e. has been sampled at least once), so we can update
				// its value in the distribution and then get the best solution so far.
				if(this.getRoleProblems()[roleProblemIndex].getMeanValueCombo() != null) {
					this.selectedCombinations[roleProblemIndex] =
							((ContinuousMove) this.cmaesManager.updateMeanFitnessAndGetBest(this.getRoleProblems()[roleProblemIndex]).getTheMove()).getContinuousMove();
					this.solutionTypes[roleProblemIndex] = SolutionType.UNDERSAMPLED_MEAN;
				}
				// 3. The population is null and the mean value combination is null: CMA-ES terminated properly and the best
				// combination has been already set, so we don't need to do anything.

			}

		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations, this.solutionTypes));

		this.continuousParametersManager.setParametersValues(this.selectedCombinations);

    }

    @Override
    public void updateStatistics(double[] goals) {

    	double[] neededRewards;

        // We have to check if the ParametersTuner is tuning parameters only for the playing role
        // or for all roles and update the statistics with appropriate rewards.
        if(this.tuneAllRoles){
            neededRewards = goals;
        }else{
            neededRewards = new double[1];
            neededRewards[0] = goals[this.gameDependentParameters.getMyRoleIndex()];

        }

        if(neededRewards.length != this.getRoleProblems().length){
            GamerLogger.logError("ParametersTuner", "SelfAdaptiveESTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
                    ") to update the fitness of the individuals (" + this.getRoleProblems().length + ").");
            throw new RuntimeException("SelfAdaptiveESTuner - Impossible to update move statistics! Wrong number of rewards!");
        }

		/*
		if(neededRewards.length != this.populations.length){
			GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the fitness of the individuals (" + this.populations.length + ").");
			throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}*/

        // Update fitness of evaluated individuals
        //List<Integer> individualsIndices = this.combosOfIndividualsIterator.getCurrentComboOfIndividualsIndices();

        this.updateRoleProblems(neededRewards);

		 /*
		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 toUpdate = this.populations[populationIndex][individualsIndices.get(populationIndex)];
			 toUpdate.incrementScoreSum(neededRewards[populationIndex]);
			 toUpdate.incrementVisits();
		 }*/

    }

    protected void updateRoleProblems(double[] neededRewards) {

		CompleteMoveStats toUpdate;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			// We have 3 cases:
			// 1. The population is non-null: we update the fitness of the selected individual
			if(this.roleProblems[roleProblemIndex].getPopulation() != null) {
				toUpdate = this.roleProblems[roleProblemIndex].getCurrentIndividual();
				toUpdate.incrementScoreSum(neededRewards[roleProblemIndex]);
				toUpdate.incrementVisits();
				this.roleProblems[roleProblemIndex].incrementTotalUpdates();
			}else {
				// 2. The population is null, but we are still evaluating the mean solution: update the fitness of the mean solution
				if(this.roleProblems[roleProblemIndex].getMeanValueCombo() != null) {
					this.roleProblems[roleProblemIndex].getMeanValueCombo().incrementScoreSum(neededRewards[roleProblemIndex]);
					this.roleProblems[roleProblemIndex].getMeanValueCombo().incrementVisits();
				}
				// 3. Both the population and the mean solution are null, so we already set the best combination
				// and stopped tuning with CMA-ES: do nothing
			}
		}

    }

    /**
     * This method doesn't exactly log the stats, but logs the combinations (i.e. individuals)
     * that are part of the current population for each role.
     */
    @Override
    public void logStats() {
        this.logPopulations();
    }

    public void logPopulations() {

        if(this.getRoleProblems() != null){

            //GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");

        	for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){
        		this.logPopulation(roleProblemIndex);
        	}

        }

    }

    private void logPopulation(int roleProblemIndex) {

    	String toLog = "";

    	int roleIndex;

    	Move theParametersCombination;

        double[] combo;

        String theValues;

    	if(this.tuneAllRoles){
            roleIndex = roleProblemIndex;
        }else{
            roleIndex = this.gameDependentParameters.getMyRoleIndex();
        }

    	Role role = this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex));

        if(this.getRoleProblems()[roleProblemIndex].getPopulation() != null) {

        	toLog += "ROLE=;" + role + ";";

        	toLog += "POPULATION=;";

            for(int comboIndex = 0; comboIndex < this.getRoleProblems()[roleProblemIndex].getPopulation().length; comboIndex++){

                theParametersCombination = this.getRoleProblems()[roleProblemIndex].getPopulation()[comboIndex].getTheMove();

                if(theParametersCombination instanceof ContinuousMove){
                	combo = ((ContinuousMove) theParametersCombination).getContinuousMove();
                    theValues = "[ ";
                    for(int paramIndex = 0; paramIndex < combo.length; paramIndex++){
                        theValues += (combo[paramIndex] + " ");
                    }
                    theValues += "]";

                    toLog+= (theValues + ";");

                }else{
                    GamerLogger.logError("ParametersTuner", "SelfAdaptiveESTuner - Impossible to log populations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
                    throw new RuntimeException("SelfAdaptiveESTuner - Impossible to log populations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
                }
            }

            toLog += "\n";

            toLog += "ROLE=;" + role + ";";

            toLog += "IS_REPAIRED=;";

            for(int comboIndex = 0; comboIndex < this.getRoleProblems()[roleProblemIndex].getRepaired().length; comboIndex++){

            	theValues = "[ ";
                for(int paramIndex = 0; paramIndex < this.getRoleProblems()[roleProblemIndex].getRepaired()[comboIndex].length; paramIndex++){
                    theValues += (this.getRoleProblems()[roleProblemIndex].getRepaired()[comboIndex][paramIndex] + " ");
                }
                theValues += "]";

                toLog+= (theValues + ";");

            }

            toLog += "\n";

            toLog += "ROLE=;" + role + ";";

            toLog += "PENALTY=;[ ";

            for(int comboIndex = 0; comboIndex < this.getRoleProblems()[roleProblemIndex].getPenalty().length; comboIndex++){

            	toLog += ( this.getRoleProblems()[roleProblemIndex].getPenalty()[comboIndex] + " " );


            }

            toLog+= ("]");

        }else {

        	if(this.getRoleProblems()[roleProblemIndex].getMeanValueCombo() != null) { // Null population cause we set the mean combo, so we log the mean combo

        		toLog += "ROLE=;" + role + ";";

        		toLog += "MEAN_COMBO=;";
        		combo = ((ContinuousMove) this.getRoleProblems()[roleProblemIndex].getMeanValueCombo().getTheMove()).getContinuousMove();
        		theValues = "[ ";
                for(int paramIndex = 0; paramIndex < combo.length; paramIndex++){
                    theValues += (combo[paramIndex] + " ");
                }
                theValues += "]";

                toLog += (theValues + ";");

                toLog += "\n";

                toLog += "ROLE=;" + role + ";";

                toLog += "MEAN_COMBO_IS_REPAIRED=;";

        		theValues = "[ ";
                for(int paramIndex = 0; paramIndex < this.getRoleProblems()[roleProblemIndex].getMeanRepaired().length; paramIndex++){
                    theValues += (this.getRoleProblems()[roleProblemIndex].getMeanRepaired()[paramIndex] + " ");
                }
                theValues += "]";

                toLog += (theValues + ";");

                toLog += "\n";

                toLog += "ROLE=;" + role + ";";

                toLog += "MEAN_COMBO_PENALTY=;" + this.getRoleProblems()[roleProblemIndex].getMeanPenalty() + ";";

                toLog += "\n";

        	}else {

        		toLog += "ROLE=;" + role + ";";

        		toLog += ("POPULATION=;[];");

                toLog += "\n";

        	}

        }

        GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Populations", toLog);
        GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Populations", "\n");


    }

    @Override
    public void decreaseStatistics(double factor){
        for(int i = 0; i < this.getRoleProblems().length; i++){
            this.getRoleProblems()[i].decreaseStatistics(factor);
        }
    }

    @Override
    public boolean isMemorizingBestCombo() {
        return (this.reuseBestCombos && this.getRoleProblems() == null);
    }

    @Override
    public void memorizeBestCombinations() {
        this.bestCombinations = this.selectedCombinations;
    }

    @Override
    public String getComponentParameters(String indentation) {

        String superParams = super.getComponentParameters(indentation);

        String params = indentation + "LOG_POPULATIONS = " + this.logPopulations +
                indentation + "CONTINUOUS_EVOLUTION_MANAGER = " + this.cmaesManager.printComponent(indentation + "  ") +
                //indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ") +
                //indentation + "EVALUATE_ALL_COMBOS_OF_INDIVIDUALS = " + this.evaluateAllCombosOfIndividuals +
                //indentation + "INDIVIDUALS_ITERATOR = " + (this.combosOfIndividualsIterator != null ? this.combosOfIndividualsIterator.getClass().getSimpleName() : "null") +
                indentation + "EVAL_REPETITIONS = " + this.evalRepetitions +
                indentation + "EVAL_REPETITIONS_FOR_MEAN_COMBO = " + this.evalRepetitionsForMeanCombo +
                //indentation + "eval_repetitions_count = " + this.evalRepetitionsCount +
                indentation + "num_role_problems = " + (this.getRoleProblems() != null ? this.getRoleProblems().length : 0);
        		//indentation + "num_combos_of_individuals = " + (this.combosOfIndividualsIndices != null ? this.combosOfIndividualsIndices.size() : 0) +
        		//indentation + "current_combo_index = " + this.currentComboIndex;

        if(this.selectedCombinations != null){
            String selectedCombinationsString = "[ ";

            for(int i = 0; i < this.selectedCombinations.length; i++){

                String singleCombinationString = "[ ";
                for(int j = 0; j < this.selectedCombinations[i].length; j++) {
                	singleCombinationString += this.selectedCombinations[i][j] + " ";
                }

                singleCombinationString += "]";

                selectedCombinationsString += singleCombinationString + " ";

            }

            selectedCombinationsString += "]";

            params += indentation + "selected_combinations_indices = " + selectedCombinationsString;
        }else{
            params += indentation + "selected_combinations_indices = null";
        }

        if(this.solutionTypes != null){
            String solutionTypesString = "[ ";

            for(int i = 0; i < this.solutionTypes.length; i++){

            	solutionTypesString += this.solutionTypes[i] + " ";

            }

            solutionTypesString += "]";

            params += indentation + "solution_types = " + solutionTypesString;
        }else{
            params += indentation + "solution_types = null";
        }


        if(this.bestCombinations != null){
            String bestCombinationsString = "[ ";

            for(int i = 0; i < this.bestCombinations.length; i++){

                String bestCombinationString = "[ ";
                for(int j = 0; j < this.selectedCombinations[i].length; j++) {
                	bestCombinationString += this.bestCombinations[i][j] + " ";
                }
                bestCombinationString += "]";

                bestCombinationsString += bestCombinationString + " ";

            }

            bestCombinationsString += "]";

            params += indentation + "best_combinations_indices = " + bestCombinationsString;
        }else{
            params += indentation + "best_combinations_indices = null";
        }

        if(this.roleProblems != null){
            String initialXsString = "[ ";

            for(int i = 0; i < this.roleProblems.length; i++){

                String singleInitialXString = "[ ";
                for(int j = 0; j < this.roleProblems[i].getCMAEvolutionStrategy().getInitialX().length; j++) {
                	singleInitialXString += this.roleProblems[i].getCMAEvolutionStrategy().getInitialX()[j] + " ";
                }

                singleInitialXString += "]";

                initialXsString += singleInitialXString + " ";

            }

            initialXsString += "]";

            params += indentation + "initial_cmaes_solutions = " + initialXsString;
        }else{
            params += indentation + "selected_combinations_indices = null";
        }

        if(superParams != null){
            return superParams + params;
        }else{
            return params;
        }

    }

    protected SelfAdaptiveESProblemRepresentation[] getRoleProblems() {
        return this.roleProblems;
    }

    protected void setRoleProblemsToNull() {
        this.roleProblems = null;
    }

}
