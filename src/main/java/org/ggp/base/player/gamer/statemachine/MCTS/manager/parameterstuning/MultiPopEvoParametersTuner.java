package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class tunes the parameters for each role independently. Each role has its own population.
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
 *
 * @author C.Sironi
 *
 */
public class MultiPopEvoParametersTuner extends ParametersTuner {

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
	 * Number of time all possible combinations of combinations (i.e. individuals) must be evaluated
	 * before using the collected statistics to evolve the population.
	 */
	private int evalRepetitions;

	/**
	 * Used to count the repetitions performed so far.
	 */
	private int evalRepetitionsCount;

	/**
	 * One population of combinations (individuals) for each role being tuned.
	 */
	private CompleteMoveStats[][] populations;

	/**
	 * List with the indices of all possible combinations that can be obtained by taking one
	 * combination (i.e individual) for each role.
	 */
	private List<List<Integer>> combosOfIndividualsIndices;

	/**
	 * Index of the currently tested combination of combinations (i.e. individuals) in the
	 * combosOfCombosIndices list.
	 */
	private int currentComboIndex;

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

		this.evalRepetitions = gamerSettings.getIntPropertyValue("ParametersTuner.evalRepetitions");

		this.evalRepetitionsCount = 0;

		this.populations = null;

		this.combosOfIndividualsIndices = null;

		this.currentComboIndex = 0;

		this.selectedCombinations = null;

		this.bestCombinations = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		super.setReferences(sharedReferencesCollector);

		this.evolutionManager.setReferences(sharedReferencesCollector);

		this.evolutionManager.setParametersManager(this.parametersManager);

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
			if(!this.reuseStats || this.populations == null || this.populations.length != numRolesToTune){

				// Create log file for populations.
				if(this.logPopulations){
					String globalParamsOrder = this.getGlobalParamsOrder();
					GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Populations", "PARAMS=;" + globalParamsOrder + ";");
				}

				// Create a two phase representation of the combinatorial problem for each role
				this.populations = new CompleteMoveStats[numRolesToTune][];
				for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
					populations[populationIndex] = this.evolutionManager.getInitialPopulation();
				}

				if(this.logPopulations){
					this.logStats();
				}

				// combosOfCombosIndices is fixed for the whole game until we change number of roles, so
				// we can initialize it here.
				this.combosOfIndividualsIndices = new ArrayList<List<Integer>>();

				this.computeCombosOfCombosIndices(new ArrayList<Integer>());

				Collections.shuffle(this.combosOfIndividualsIndices);

				this.currentComboIndex = 0;

				this.evalRepetitionsCount = 0;

				this.selectedCombinations = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];
			}

			this.bestCombinations = null;
		}else{
			this.populations = null;
			this.selectedCombinations = null;
		}

	}

	private void computeCombosOfCombosIndices(List<Integer> partialCombo){

		if(partialCombo.size() == this.populations.length){ // The combination of individuals is complete
			this.combosOfIndividualsIndices.add(new ArrayList<Integer>(partialCombo));
		}else{
			for(int i = 0; i < this.populations[partialCombo.size()].length; i++){
				partialCombo.add(new Integer(i));
				this.computeCombosOfCombosIndices(partialCombo);
				partialCombo.remove(partialCombo.size()-1);
			}
		}

	}

	@Override
	public void setNextCombinations() {

		 List<Integer> individualsIndices = this.combosOfIndividualsIndices.get(this.currentComboIndex);

		 Move theParametersCombination;

		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 theParametersCombination = this.populations[populationIndex][individualsIndices.get(populationIndex)].getTheMove();
			 if(theParametersCombination instanceof CombinatorialCompactMove){
				 this.selectedCombinations[populationIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			 }else{
				 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 }
		 }

		 this.parametersManager.setParametersValues(this.selectedCombinations);

	}

	@Override
	public void setBestCombinations() {

		int numUpdates;

		Move theParametersCombination;

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
		 }

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

		if(neededRewards.length != this.populations.length){
			GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the fitness of the individuals (" + this.populations.length + ").");
			throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		// Update fitness of evaluated individuals
		List<Integer> individualsIndices = this.combosOfIndividualsIndices.get(this.currentComboIndex);

		CompleteMoveStats toUpdate;

		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 toUpdate = this.populations[populationIndex][individualsIndices.get(populationIndex)];
			 toUpdate.incrementScoreSum(neededRewards[populationIndex]);
			 toUpdate.incrementVisits();
		 }

		 // Check if we tested all combinations.
		 if(this.currentComboIndex == this.combosOfIndividualsIndices.size()-1){

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

			 // Prepare to start another repetition, after shuffling the order of evaluation.
			 Collections.shuffle(this.combosOfIndividualsIndices);
			 this.currentComboIndex = 0;

		 }else{
			 // If we didn't test all combinations, point to the next combination.
			 this.currentComboIndex++;
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
				indentation + "EVAL_REPETITIONS = " + this.evalRepetitions +
				indentation + "eval_repetitions_count = " + this.evalRepetitionsCount +
				indentation + "num_populations = " + (this.populations != null ? this.populations.length : 0) +
				indentation + "num_combos_of_individuals = " + (this.combosOfIndividualsIndices != null ? this.combosOfIndividualsIndices.size() : 0) +
				indentation + "current_combo_index = " + this.currentComboIndex;

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
