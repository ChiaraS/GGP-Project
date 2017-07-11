package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.AllCombosOfIndividualsIterator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombosOfIndividualsIterator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.RandomCombosOfIndividualsIterator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class tunes the parameters for each role independently. Each role has its own population.
 *
 * ATTENTION! Parts of this class assume that the size of the populations is always the same
 * (e.g. the CombosOfIndividualsIterator).
 * If a population can change size over time, those parts must be fixed.
 *
 * @author C.Sironi
 *
 */
public abstract class MultiPopEvoParametersTuner extends ParametersTuner {

	private boolean logPopulations;

	/**
	 * Takes care of evolving a given population depending on the fitness of its individuals.
	 */
	protected EvolutionManager evolutionManager;

	/**
	 * Given the statistics of each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSelector;

	/**
	 * If true, when evaluating a population, the fitness of each individual will result from
	 * testing with a simulation ALL combinations of individuals, one from each population.
	 * If false, random combinations of individuals (one for each population) will be evaluated,
	 * so that each individual is evaluated the same number fo times as the other individuals.
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
	private boolean evaluateAllCombosOfIndividuals;

	/**
	 * Keeps track of which combinations of individuals must be evaluated and in which order.
	 */
	private CombosOfIndividualsIterator combosOfIndividualsIterator;

	/**
	 * If evaluating all combinations of individuals before evolving the populations (i.e. evaluateAllCombosOfIndividuals == true),
	 * this parameter specifies the number of times all possible combinations of combinations (i.e. individuals) must be evaluated
	 * before using the collected statistics to evolve the population.
	 *
	 * If evaluating random combinations of individuals before evolving the populations (i.e. evaluateAllCombosOfIndividuals == true),
	 * this parameter specifies the number of times each individual must be evaluated against other random individuals. More precisely,
	 * when evaluateAllCombosOfIndividuals == true, the evaluation is performed as follows:
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
	private int evalRepetitions;

	/**
	 * Used to count the repetitions performed so far.
	 */
	private int evalRepetitionsCount;

	/**
	 * One population of combinations (individuals) for each role being tuned.
	 */
	//private CompleteMoveStats[][] populations;

	/**
	 * List with the indices of all possible combinations that can be obtained by taking one
	 * combination (i.e individual) for each role.
	 */
	//private List<List<Integer>> combosOfIndividualsIndices;

	/**
	 * Index of the currently tested combination of combinations (i.e. individuals) in the
	 * combosOfCombosIndices list.
	 */
	//private int currentComboIndex;

	/**
	 * Memorize the currently set combinations of parameters for each role.
	 * Memorizing it here is redundant, but this parameter is also used to temporarily memorize
	 * the best combinations until we know if the final game result was a win (and thus we memorize
	 * the combinations permanently in bestCombinations), or a loss.
	 */
	private int[][] selectedCombinations;

	/**
	 * Memorizes the best so far combination of parameters values for each role.
	 */
	private int[][] bestCombinations;

	public MultiPopEvoParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.logPopulations = gamerSettings.getBooleanPropertyValue("ParametersTuner.logPopulations");

		try {
			this.evolutionManager = (EvolutionManager) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.EVOLUTION_MANAGERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("ParametersTuner.evolutionManagerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating EvolutionManager " + gamerSettings.getPropertyValue("ParametersTuner.EvolutionManagerType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		String[] componentDetails = gamerSettings.getIDPropertyValue("ParametersTuner.bestCombinationSelectorType");

		try {
			this.bestCombinationSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), componentDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, componentDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.bestCombinationSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.evaluateAllCombosOfIndividuals = gamerSettings.getBooleanPropertyValue("ParametersTuner.evaluateAllCombosOfIndividuals");

		this.combosOfIndividualsIterator = null;

		this.evalRepetitions = gamerSettings.getIntPropertyValue("ParametersTuner.evalRepetitions");

		this.evalRepetitionsCount = 0;

		//this.populations = null;

		//this.combosOfIndividualsIndices = null;

		//this.currentComboIndex = 0;

		this.selectedCombinations = null;

		this.bestCombinations = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		super.setReferences(sharedReferencesCollector);

		this.evolutionManager.setReferences(sharedReferencesCollector);

		this.bestCombinationSelector.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();

		this.evolutionManager.clearComponent();

		this.bestCombinationSelector.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();

		this.evolutionManager.setUpComponent();

		this.bestCombinationSelector.setUpComponent();

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
		// (TODO: here we should check that we reuse the best combo ONLY if we are playing the exact same game
		// and not just a game with the same number of roles).
		if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

			// If we need to initialize the populations, here we have to check if we need new ones or if we should
			// reuse previous ones that have been saved.
			// We need new ones if:
			// 1. We don't want to reuse the previous ones
			// 2. We want to reuse the previous ones, but we have none yet
			// 3. We want to reuse the previous ones, we have them but their size doesn't correspond to the number
			// of roles that we have to tune.
			// (TODO: here we should check that we reuse role problems ONLY if we are playing the exact same game
			// and not just a game with the same number of roles).
			//if(!this.reuseStats || this.populations == null || this.populations.length != numRolesToTune){
			if(!this.reuseStats || this.getRoleProblems() == null || this.getRoleProblems().length != numRolesToTune){

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
					roleProblems[roleProblemIndex] = new EvoProblemRepresentation(this.evolutionManager.getInitialPopulation());
				}*/

				/*
				this.populations = new CompleteMoveStats[numRolesToTune][];
				for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
					populations[populationIndex] = this.evolutionManager.getInitialPopulation();
				}*/

				if(this.logPopulations){
					this.logPopulations();
				}

				int[] popSizes = new int[this.getRoleProblems().length];
				for(int i = 0; i < popSizes.length; i++){
					popSizes[i] = this.getRoleProblems()[i].getPopulation().length;
				}

				if(this.evaluateAllCombosOfIndividuals){
					/*
					int[] popSizes = new int[this.populations.length];
					for(int i = 0; i < popSizes.length; i++){
						popSizes[i] = this.populations[i].length;
					}*/
					this.combosOfIndividualsIterator = new AllCombosOfIndividualsIterator(popSizes);
				}else{
					this.combosOfIndividualsIterator = new RandomCombosOfIndividualsIterator(popSizes);
				}

				this.evalRepetitionsCount = -1;

				this.selectedCombinations = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];
			}

			this.bestCombinations = null;
		}else{
			//this.populations = null;
			this.setRoleProblemsToNull();
			this.selectedCombinations = null;
		}

	}

	@Override
	public void setNextCombinations() {

		 // Check if we tested all combinations.
		 // Try to get the next combination, if all combinations have been tested this will return null
		 // and we'll have to evolve the population or restart the evaluations. Moreover, we need to reset
		 // the individualsCombinationsIterator to start iterating over the combinations again.
		 // Otherwise, the next combination will be returned and the individualsCombinationsIterator will
		 // keep track of it as the new current combination.
		 if(this.combosOfIndividualsIterator.getNextComboOfIndividualsIndices() == null){

			 // If we tested all combinations, increment the counter since we finished
			 // another repetition of the evaluation of all combinations.
			 this.evalRepetitionsCount++;

			 // Check if we performed all repetitions of the evaluation.
			 if(this.evalRepetitionsCount == this.evalRepetitions){
				 // If yes, evolve the populations.
				 for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){
					 this.evolutionManager.evolvePopulation(this.getRoleProblems()[roleProblemIndex]);
				 }

				 /*
				 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
					 this.evolutionManager.evolvePopulation(this.populations[populationIndex]);
				 }*/

				 if(this.logPopulations){
					 this.logPopulations();
				 }

				 this.evalRepetitionsCount = 0;
			 }

			 // Prepare to start another repetition, after resetting the iterator.
			 this.combosOfIndividualsIterator.startNewIteration();

		 }

		 List<Integer> individualsIndices = this.combosOfIndividualsIterator.getCurrentComboOfIndividualsIndices();

		 Move theParametersCombination;

		 for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){
			 theParametersCombination = this.getRoleProblems()[roleProblemIndex].getPopulation()[individualsIndices.get(roleProblemIndex)].getTheMove();
			 if(theParametersCombination instanceof CombinatorialCompactMove){
				 this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			 }else{
				 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 }
		 }

		 /*
		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 theParametersCombination = this.populations[populationIndex][individualsIndices.get(populationIndex)].getTheMove();
			 if(theParametersCombination instanceof CombinatorialCompactMove){
				 this.selectedCombinations[populationIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			 }else{
				 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 }
		 }*/

		 this.parametersManager.setParametersValues(this.selectedCombinations);

	}

	@Override
	public void setBestCombinations() {

		!! Separa per Ucb e non Ucb tuner. Ucb usa per ogni ruolo la combinazione con max avg nelle stats globali.
		Move theParametersCombination;

		 for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){

			 theParametersCombination = this.getRoleProblems()[roleProblemIndex].getPopulation()[this.bestCombinationSelector.selectMove(this.getRoleProblems()[roleProblemIndex].getPopulation(), null,
					 new double[this.getRoleProblems()[roleProblemIndex].getPopulation().length], this.getRoleProblems()[roleProblemIndex].getTotalUpdates())].getTheMove();

			 if(theParametersCombination instanceof CombinatorialCompactMove){
				 this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			 }else{
				 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 }
		 }

		 /*
		 int numUpdates;

		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){

			 numUpdates = 0;

			 for(int individualIndex = 0; individualIndex < this.populations[populationIndex].length; individualIndex++){
				 numUpdates += this.populations[populationIndex][individualIndex].getVisits();
			 }

			 theParametersCombination = this.populations[populationIndex][this.bestCombinationSelector.selectMove(this.populations[populationIndex], null,
					 new double[this.populations[populationIndex].length], numUpdates)].getTheMove();

			 if(theParametersCombination instanceof CombinatorialCompactMove){
				 this.selectedCombinations[populationIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			 }else{
				 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 }
		 }*/

		 this.parametersManager.setParametersValues(this.selectedCombinations);

	}

	@Override
	public void updateStatistics(int[] goals) {

		int[] neededRewards;

		// We have to check if the ParametersTuner is tuning parameters only for the playing role
		// or for all roles and update the statistics with appropriate rewards.
		if(this.tuneAllRoles){
			neededRewards = goals;
		}else{
			neededRewards = new int[1];
			neededRewards[0] = goals[this.gameDependentParameters.getMyRoleIndex()];

		}

		if(neededRewards.length != this.getRoleProblems().length){
			GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the fitness of the individuals (" + this.getRoleProblems().length + ").");
			throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		/*
		if(neededRewards.length != this.populations.length){
			GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the fitness of the individuals (" + this.populations.length + ").");
			throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}*/

		// Update fitness of evaluated individuals
		List<Integer> individualsIndices = this.combosOfIndividualsIterator.getCurrentComboOfIndividualsIndices();

		this.updateRoleProblems(individualsIndices, neededRewards);

		 /*
		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 toUpdate = this.populations[populationIndex][individualsIndices.get(populationIndex)];
			 toUpdate.incrementScoreSum(neededRewards[populationIndex]);
			 toUpdate.incrementVisits();
		 }*/

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
			String toLog = "";

			Move theParametersCombination;

			int[] comboIndices;

			String theValues;

			int roleIndex;

			for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){

				if(this.tuneAllRoles){
					roleIndex = roleProblemIndex;
				}else{
					roleIndex = this.gameDependentParameters.getMyRoleIndex();
				}

				toLog += "ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
						";POPULATION=;";

				for(int comboIndex = 0; comboIndex < this.getRoleProblems()[roleProblemIndex].getPopulation().length; comboIndex++){

					 theParametersCombination = this.getRoleProblems()[roleProblemIndex].getPopulation()[comboIndex].getTheMove();

					 if(theParametersCombination instanceof CombinatorialCompactMove){
						 comboIndices = ((CombinatorialCompactMove) theParametersCombination).getIndices();
						 theValues = "[ ";
						 for(int paramIndex = 0; paramIndex < comboIndices.length; paramIndex++){
							 theValues += (this.parametersManager.getPossibleValues(paramIndex)[comboIndices[paramIndex]] + " ");
						 }
						 theValues += "]";

						 toLog+= (theValues + ";");

					 }else{
						 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to log populations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
						 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to log populations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
					 }

				}

				toLog += "\n";

			}

			toLog += "\n";

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Populations", toLog);
		}

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
				indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ") +
				indentation + "EVALUATE_ALL_COMBOS_OF_INDIVIDUALS = " + this.evaluateAllCombosOfIndividuals +
				indentation + "INDIVIDUALS_ITERATOR = " + (this.combosOfIndividualsIterator != null ? this.combosOfIndividualsIterator.getClass().getSimpleName() : "null") +
				indentation + "EVAL_REPETITIONS = " + this.evalRepetitions +
				indentation + "eval_repetitions_count = " + this.evalRepetitionsCount +
				indentation + "num_role_problems = " + (this.getRoleProblems() != null ? this.getRoleProblems().length : 0);
				//indentation + "num_combos_of_individuals = " + (this.combosOfIndividualsIndices != null ? this.combosOfIndividualsIndices.size() : 0) +
				//indentation + "current_combo_index = " + this.currentComboIndex;

		if(this.selectedCombinations != null){
			String selectedCombinationsString = "[ ";

			for(int i = 0; i < this.selectedCombinations.length; i++){

				String singleCombinationString = "[ ";
				for(int j = 0; j < this.selectedCombinations[i].length; j++){
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

		if(this.bestCombinations != null){
			String bestCombinationsString = "[ ";

			for(int i = 0; i < this.bestCombinations.length; i++){

				String bestCombinationString = "[ ";
				for(int j = 0; j < this.bestCombinations[i].length; j++){
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

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	public abstract void createRoleProblems(int numRolesToTune);

	public abstract void setRoleProblemsToNull();

	public abstract EvoProblemRepresentation[] getRoleProblems();

	public abstract void updateRoleProblems(List<Integer> individualsIndices, int[] neededRewards);

}
