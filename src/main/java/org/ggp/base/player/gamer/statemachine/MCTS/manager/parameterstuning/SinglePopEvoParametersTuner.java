package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.AllCombosOfIndividualsIterator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombosOfIndividualsIterator;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.statsSummarizer.PlayerStatistics;

public class SinglePopEvoParametersTuner extends ParametersTuner {

	private boolean logPopulations;

	/**
	 * Takes care of evolving a given population depending on the fitness of its individuals.
	 */
	private EvolutionManager evolutionManager;

	/**
	 * Given the statistics of each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSelector;

	/**
	 * Keeps track of which combinations of individuals must be evaluated and in which order.
	 */
	private CombosOfIndividualsIterator combosOfIndividualsIterator;

	/**
	 * When evaluating all combinations of individuals before evolving the population this parameter specifies the
	 * number of times all possible combinations of combinations (i.e. individuals) must be evaluated before using
	 * the collected statistics to evolve the population.
	 */
	private int evalRepetitions;

	/**
	 * Used to count the repetitions performed so far.
	 */
	private int evalRepetitionsCount;

	/**
	 * One single population of combinations (individuals) is used for all roles being tuned.
	 */
	private CompleteMoveStats[] population;

	/**
	 * Parameter used to temporarily memorize the best combination of parameters until we know if
	 * the final game result was a win (and thus we memorize the combination permanently in bestCombination),
	 * or a loss.
	 *
	 * NOTE that as opposed to all other tuners, this tuner uses this parameter to temporarily memorize a combo
	 * ONLY when the setBestCombinations() method is called.
	 */
	private int[] selectedCombination;

	/**
	 * Memorizes the best so far combination of parameters values.
	 * NOTE that since only one population is used and statistics are collected in general and not per
	 * role, the best combination will be the same for each role.
	 */
	private int[] bestCombination;

	public SinglePopEvoParametersTuner(GameDependentParameters gameDependentParameters, Random random,
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

		this.combosOfIndividualsIterator = null;

		this.evalRepetitions = gamerSettings.getIntPropertyValue("ParametersTuner.evalRepetitions");

		this.evalRepetitionsCount = 0;

		this.population = null;

		this.selectedCombination = null;

		this.bestCombination = null;
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
		// (NOTE: we can reuse the best combo not only if we are playing the exact same game, but for any
		// game (it probably doesn't make much sense, though).
		if(!this.reuseBestCombos || this.bestCombination == null){

			// If we need to initialize the population, here we have to check if we need a new one or if we should
			// reuse the previous one that has been saved.
			// We need a new one if:
			// 1. We don't want to reuse the previous one
			// 2. We want to reuse the previous one, but we have none yet
			// (NOTE: we can reuse the population not only if we are playing the exact same game, but for any
			// game (might make sense. We will start evolving for the current game a population that is not random
			// but already pretty good for another game - still makes little sense if the two games are completely
			// different and require very different parameter values).
			if(!this.reuseStats || this.population == null){

				// Create log file for population.
				if(this.logPopulations){
					String globalParamsOrder = this.getGlobalParamsOrder();
					GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Populations", "PARAMS=;" + globalParamsOrder + ";");
				}

				// Create the initial population
				this.population = this.evolutionManager.getInitialPopulation();

				if(this.logPopulations){
					this.logStats();
				}

				// Create all possible combinations of individuals that can be obtained by assigning one individual to each role,
				// excluding the combinations that assign the same individual to each role (they don't give any information about
				// the fitness of the individual if the individual is matched only against himself).
				int[] popSizes = new int[numRolesToTune];
				for(int i = 0; i < popSizes.length; i++){
					popSizes[i] = this.population.length;
				}
				this.combosOfIndividualsIterator = new AllCombosOfIndividualsIterator(popSizes, true);

				this.evalRepetitionsCount = -1;

				this.selectedCombination = new int[this.parametersManager.getNumTunableParameters()];
			}

			this.bestCombination = null;
		}else{
			this.population = null;
			this.selectedCombination = null;
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

				// If yes, evolve the population.
				this.evolutionManager.evolvePopulation(this.population);

				if(this.logPopulations){
					this.logStats();
				}

				this.evalRepetitionsCount = 0;
			}

			// Prepare to start another repetition, after resetting the iterator.
			this.combosOfIndividualsIterator.startNewIteration();

		}

		List<Integer> individualsIndices = this.combosOfIndividualsIterator.getCurrentComboOfIndividualsIndices();

		Move theParametersCombination;

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		int[][] combinationsToSet = new int[numRolesToTune][];

		for(int roleIndex = 0; roleIndex < numRolesToTune; roleIndex++){
			theParametersCombination = this.population[individualsIndices.get(roleIndex)].getTheMove();
			if(theParametersCombination instanceof CombinatorialCompactMove){
				combinationsToSet[roleIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			}else{
				GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			}
		}

		this.parametersManager.setParametersValues(combinationsToSet);
	}

	@Override
	public void setBestCombinations() {

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		int numUpdates = 0;

		for(int individualIndex = 0; individualIndex < this.population.length; individualIndex++){
			numUpdates += this.population[individualIndex].getVisits();
		}

		// Get best combination
		Move theParametersCombination = this.population[this.bestCombinationSelector.selectMove(this.population, null,
				 new double[this.population.length], numUpdates)].getTheMove();

		if(theParametersCombination instanceof CombinatorialCompactMove){
			this.selectedCombination = ((CombinatorialCompactMove) theParametersCombination).getIndices();
		}else{
			 GamerLogger.logError("ParametersTuner", "SinglePopEvoParametersTuner - Impossible to set best combination. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set best combination. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
		}

		int[][] combinationsToSet = new int[numRolesToTune][];

		for(int roleIndex = 0; roleIndex < numRolesToTune; roleIndex++){
			combinationsToSet[roleIndex] = this.selectedCombination;
		}

		this.parametersManager.setParametersValues(combinationsToSet);

	}








	@Override
	public void updateStatistics(int[] goals) {
		// TODO Auto-generated method stub

		//!!! update statistics using the rewards as in the experiments:
		// if an individual appears more than once in the combination, update its
		// stats only once with the best result it obtained (i.e. compute its
		// reward as follows:
		// r = 1/#unique individuals that achieved the maximum score   if the individual achieved the maximum score
		// r = 0   if the individual didn't achieve the maximum score)

		int[] neededRewards;

		// We have to check if the ParametersTuner is tuning parameters only for the playing role
		// or for all roles and update the statistics with appropriate rewards.
		if(this.tuneAllRoles){
			neededRewards = goals;
		}else{
			neededRewards = new int[1];
			neededRewards[0] = goals[this.gameDependentParameters.getMyRoleIndex()];

		}

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		if(neededRewards.length != numRolesToTune){
			GamerLogger.logError("ParametersTuner", "SinglePopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the fitness of the individuals (" + numRolesToTune + ").");
			throw new RuntimeException("SinglePopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		// Update fitness of evaluated individuals
		List<Integer> individualsIndices = this.combosOfIndividualsIterator.getCurrentComboOfIndividualsIndices();

		CompleteMoveStats toUpdate;

		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 toUpdate = this.populations[populationIndex][individualsIndices.get(populationIndex)];
			 toUpdate.incrementScoreSum(neededRewards[populationIndex]);
			 toUpdate.incrementVisits();
		 }


			// Add the wins
         if(playersNames.length > 1){

         	// For more roles we need to find the algorithm(s) that won and split 1 win between them

         	maxScore = Integer.MIN_VALUE;
         	playerTypesSet = new HashSet<String>();
         	maxScorePlayerTypes = new HashSet<String>();

         	for(int i = 0; i < playersGoals.length; i++){
         		playerTypesSet.add(playersNames[i]);
         		if(playersGoals[i] > maxScore){
         			maxScore = playersGoals[i];
         			maxScorePlayerTypes.clear();
         			maxScorePlayerTypes.add(playersNames[i]);
         		}else if(playersGoals[i] == maxScore){
         			maxScorePlayerTypes.add(playersNames[i]);
         		}
				}

         	splitWin = 1.0/((double)maxScorePlayerTypes.size());

         	// Memorize the outcome for every player in the MatchInfo
         	for(int i = 0; i < playersGoals.length; i++){
         		if(playersGoals[i] == maxScore){
	            		if(!mi.addFinalOutcome(playersNames[i], playersRoles[i], splitWin)){
	            			System.out.println("Error when adding final outcome " + splitWin + " to MatchInfo for player " + playersNames[i] + " playing role " + playersRoles[i] + ". The BestComboStats, if any, will be incomplete.");
	            		}
         		}else{
         			if(!mi.addFinalOutcome(playersNames[i], playersRoles[i], 0)){
	            			System.out.println("Error when adding final outcome " + 0 + " to MatchInfo for player " + playersNames[i] + " playing role " + playersRoles[i] + ". The BestComboStats, if any, will be incomplete.");
	            		}
         		}
         	}

         	// For each distinct player type that won, update the statistics adding the (split) win
         	// and for the losers add a loss (i.e. 0).
	            for(String thePlayer: playerTypesSet){

	            	// Get the stats of the player
	            	theStats = playersStatistics.get(thePlayer);
	            	if(theStats == null){
	            		playersStatistics.put(thePlayer, new PlayerStatistics());
	            		theStats = playersStatistics.get(thePlayer);
	            	}

	            	if(maxScorePlayerTypes.contains(thePlayer)){
	            		theStats.addWins(splitWin, mi.getCombination(), mi.getMatchNumber());
	            	}else{
	            		theStats.addWins(0, mi.getCombination(), mi.getMatchNumber());
	            	}

	            }

         }else{
         	// Get the stats of the player
         	theStats = playersStatistics.get(playersNames[0]);
         	if(theStats == null){
         		playersStatistics.put(playersNames[0], new PlayerStatistics());
         		theStats = playersStatistics.get(playersNames[0]);
         	}

         	if(playersGoals[0] != 100){
         		theStats.addWins(0, mi.getCombination(), mi.getMatchNumber());
         		if(!mi.addFinalOutcome(playersNames[0], playersRoles[0], 0)){
         			System.out.println("Error when adding final outcome " + 0 + " to MatchInfo for player " + playersNames[0] + ". The BestComboStats, if sny, will be incomplete.");
         		}
         	}else{
         		theStats.addWins(1, mi.getCombination(), mi.getMatchNumber());
         		if(!mi.addFinalOutcome(playersNames[0], playersRoles[0], 1)){
         			System.out.println("Error when adding final outcome " + 1 + " to MatchInfo for player " + playersNames[0] + ". The BestComboStats, if sny, will be incomplete.");
         		}
         	}
         }


	}

	@Override
	public void logStats() {
		// TODO Auto-generated method stub

	}

	@Override
	public void decreaseStatistics(double factor) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMemorizingBestCombo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void memorizeBestCombinations() {
		// TODO Auto-generated method stub

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

		if(neededRewards.length != this.populations.length){
			GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the fitness of the individuals (" + this.populations.length + ").");
			throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		// Update fitness of evaluated individuals
		List<Integer> individualsIndices = this.combosOfIndividualsIterator.getCurrentComboOfIndividualsIndices();

		CompleteMoveStats toUpdate;

		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 toUpdate = this.populations[populationIndex][individualsIndices.get(populationIndex)];
			 toUpdate.incrementScoreSum(neededRewards[populationIndex]);
			 toUpdate.incrementVisits();
		 }

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
				 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
					 this.evolutionManager.evolvePopulation(this.populations[populationIndex]);
				 }

				 if(this.logPopulations){
					 this.logStats();
				 }

				 this.evalRepetitionsCount = 0;
			 }

			 // Prepare to start another repetition, after resetting the iterator.
			 this.combosOfIndividualsIterator.startNewIteration();

		 }

	}

	/**
	 * This method doesn't exactly log the stats, but logs the combinations (i.e. individuals)
	 * that are part of the current population for each role.
	 */
	@Override
	public void logStats() {

		if(this.populations != null){

			//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");
			String toLog = "";

			Move theParametersCombination;

			int[] comboIndices;

			String theValues;

			for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){

				int roleIndex;
				if(this.tuneAllRoles){
					roleIndex = populationIndex;
				}else{
					roleIndex = this.gameDependentParameters.getMyRoleIndex();
				}

				toLog += "ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
						";POPULATION=;";

				for(int comboIndex = 0; comboIndex < this.populations[populationIndex].length; comboIndex++){

					 theParametersCombination = this.populations[populationIndex][comboIndex].getTheMove();

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
	public void decreaseStatistics(double factor) {
		// Not really needed, so probably this method will never be used.
		for(int i = 0; i < this.populations.length; i++){
			for(int j = 0; j < this.populations[i].length; j++){
				this.populations[i][j].decreaseByFactor(factor);
			}
		}
	}

	@Override
	public boolean isMemorizingBestCombo() {
		return (this.reuseBestCombos && this.populations == null);
	}

	@Override
	public void memorizeBestCombinations() {
		this.bestCombinations = this.selectedCombinations;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ") +
				indentation + "EVALUATE_ALL_COMBOS_OF_INDIVIDUALS = " + this.evaluateAllCombosOfIndividuals +
				indentation + "INDIVIDUALS_ITERATOR = " + (this.combosOfIndividualsIterator != null ? this.combosOfIndividualsIterator.getClass().getSimpleName() : "null") +
				indentation + "EVAL_REPETITIONS = " + this.evalRepetitions +
				indentation + "eval_repetitions_count = " + this.evalRepetitionsCount +
				indentation + "num_populations = " + (this.populations != null ? this.populations.length : 0);
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


}
