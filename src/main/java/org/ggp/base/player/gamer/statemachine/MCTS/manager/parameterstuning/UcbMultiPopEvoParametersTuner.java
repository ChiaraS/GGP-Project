package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NTuple;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.NTupleEvoProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.MyPair;

public class UcbMultiPopEvoParametersTuner extends MultiPopEvoParametersTuner {

	/**
	 * Problem representation for each role being tuned.
	 */
	private NTupleEvoProblemRepresentation[] roleProblems;

	/**
	 * If true, when selecting the best combination of parameters the global MAB will be used.
	 * If false, when selecting the best combination, each parameter will be selected from the
	 * corresponding local MAB independently of the other parameters.
	 */
	private boolean useGlobalBest;

	/**
	 * Set containing the lengths (as Integer) of the n-tuples that we want to consider
	 * when computing the UCB value of a combination of parameters (e.g. we might not
	 * want to consider n-tuples of all the possible lengths [1,numParams], but only some
	 * of them).
	 * If not specified in the settings (i.e. == null) then all lengths will be considered.
	 */
	private Set<Integer> nTuplesForUCBLengths;

	public UcbMultiPopEvoParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		this.roleProblems = null;

		this.useGlobalBest = gamerSettings.getBooleanPropertyValue("ParametersTuner.useGlobalBest");

		if(gamerSettings.specifiesProperty("ParametersTuner.nTupleLengthsToConsider")) {
			this.nTuplesForUCBLengths = new HashSet<Integer>();
			int[] nTupleLengthsToConsider = gamerSettings.getIntPropertyMultiValue("ParametersTuner.nTupleLengthsToConsider");
			for(int length : nTupleLengthsToConsider) {
				this.nTuplesForUCBLengths.add(new Integer(length));
			}
		}
	}

	@Override
	public void createRoleProblems(int numRolesToTune) {
		// Create the initial population for each role
		this.roleProblems = new NTupleEvoProblemRepresentation[numRolesToTune];
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			roleProblems[roleProblemIndex] = new NTupleEvoProblemRepresentation(this.evolutionManager.getInitialPopulation(),
					this.parametersManager.getNumPossibleValuesForAllParams(), this.nTuplesForUCBLengths);
		}
	}

	@Override
	public void setRoleProblemsToNull() {
		this.roleProblems = null;
	}

	@Override
	public EvoProblemRepresentation[] getRoleProblems() {
		return this.roleProblems;
	}

	@Override
	public void computeAndSetBestCombinations() {

		// For each role, we select a combination of parameters
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			IncrementalMab completeNTupleMab = this.roleProblems[roleProblemIndex].getLandscapeModelForStatsUpdate().get(this.getCompleteNTuple());

			// If we want to use the MAB associated with whole combinations we can only do that if it has been
			// visited at least once and so contains at least one combination.
			if(this.useGlobalBest && completeNTupleMab != null && completeNTupleMab.getNumUpdates() > 0){
				Move m = this.bestCombinationSelector.selectMove(completeNTupleMab.getMovesInfo(), null, completeNTupleMab.getNumUpdates());
				this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) m).getIndices();
			}else{
				// If we are using the MABs associated with 1-tuples, select the value for each parameter
				// from the MAB of the corresponding 1-tuple. If such MAB doesn't exist or has never been
				// visited (NOTE that this should be impossible, unless no search iterations were performed
				// at all!), a random value will be selected for the parameter among the available ones.

				int[] indices = new int[this.parametersManager.getNumTunableParameters()];
				for(int i = 0; i < indices.length; i++){
					indices[i] = -1;
				}

				// Select a value for each local mab independently
				IncrementalMab oneTupleMab;
				for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
					oneTupleMab = this.roleProblems[roleProblemIndex].getLandscapeModelForStatsUpdate().get(new NTuple(new int[] {paramIndex}));
					Move m = this.bestCombinationSelector.selectMove(oneTupleMab.getMovesInfo(),
							this.parametersManager.getValuesFeasibility(paramIndex, oneTupleMab.getMovesInfo().keySet(), indices),
							oneTupleMab.getNumUpdates());
					indices[paramIndex] = ((CombinatorialCompactMove) m).getIndices()[0];
				}
				this.selectedCombinations[roleProblemIndex] = indices;
			}
		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations));

		this.parametersManager.setParametersValues(this.selectedCombinations);

	}

	private NTuple getCompleteNTuple() {
		int[] completeCombo = new int[this.parametersManager.getNumTunableParameters()];
		for(int i = 0; i < this.parametersManager.getNumTunableParameters(); i++) {
			completeCombo[i] = i;
		}
		return new NTuple(completeCombo);
	}

	@Override
	public void updateRoleProblems(List<Integer> individualsIndices, int[] neededRewards) {

		CompleteMoveStats individualStats;

		int individualIndex;

		IncrementalMab globalMab;

		CombinatorialCompactMove theMove;

		FixedMab[] localMabs;

		int[] valueIndices;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			individualIndex = individualsIndices.get(roleProblemIndex);

			/****** Update individual's stats ******/

			individualStats = this.roleProblems[roleProblemIndex].getPopulation()[individualIndex];
			individualStats.incrementScoreSum(neededRewards[roleProblemIndex]);
			individualStats.incrementVisits();
			this.roleProblems[roleProblemIndex].incrementTotalUpdates();

			/********** Update global MAB **********/

			globalMab = this.roleProblems[roleProblemIndex].getGlobalMab();

			// Get the info of the combinatorial move in the global MAB
			theMove = (CombinatorialCompactMove) individualStats.getTheMove();

			MyPair<MoveStats,Double> globalInfo = globalMab.getMovesInfo().get(theMove);

			// If the info doesn't exist, add the move to the MAB, computing the corresponding penalty
			if(globalInfo == null){
				globalInfo = new MyPair<MoveStats,Double>(new MoveStats(), this.parametersManager.computeCombinatorialMovePenalty(theMove.getIndices()));
				globalMab.getMovesInfo().put(theMove, globalInfo);
			}

			// Update the stats
			globalInfo.getFirst().incrementScoreSum(neededRewards[roleProblemIndex]);
			globalInfo.getFirst().incrementVisits();

			// Increase total num updates
			globalMab.incrementNumUpdates();

			/********** Update local MABS **********/

			// Update the stats for each local MAB
		localMabs = this.roleProblems[roleProblemIndex].getLocalMabs();

			valueIndices = theMove.getIndices();

			if(valueIndices.length != localMabs.length){
				GamerLogger.logError("ParametersTuner", "UcbMultiPopEvoParametersTuner - Impossible to update move statistics in the local MABs! Wrong number of parameter value indices (" +
						valueIndices.length + ") to update the stats in the MABs (" + localMabs.length + ").");
				throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to update move statistics! Wrong number of value indices!");
			}

			for(int paramIndex = 0; paramIndex < valueIndices.length; paramIndex++){

				/*
				// TODO: temporary solution! Fix with something general that works for any parameter and can be set from file.
				if(j == this.indexOfRef){ // Check if we have to avoid updating stats for Ref

					if(this.classesValues[this.indexOfK][this.selectedCombinations[i][this.indexOfK]].equals("0")){
						continue; // Don't update the MAB of Ref if K=0;
					}

				}*/

				MoveStats localStats = localMabs[paramIndex].getMoveStats()[valueIndices[paramIndex]];
				// Update the stats
				localStats.incrementScoreSum(neededRewards[roleProblemIndex]);
				localStats.incrementVisits();

				localMabs[paramIndex].incrementNumUpdates();
			}
		}
	}

	@Override
	public void logStats() {

		super.logStats();

		/* ********************************* Log n-tuple stats ************************************* */

		// If the roleProblems are of type UcbEvoProblemRepresentation, log also the Ucb statistics.

		int roleIndex;

		String globalParamsOrder = this.getGlobalParamsOrder();

		FixedMab[] localMabs;

		for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){

			String toLog = "";

			if(this.tuneAllRoles){
				roleIndex = roleProblemIndex;
			}else{
				roleIndex = this.gameDependentParameters.getMyRoleIndex();
			}

			localMabs = this.roleProblems[roleProblemIndex].getLocalMabs();

			for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){
				for(int paramValueIndex = 0; paramValueIndex < localMabs[paramIndex].getMoveStats().length; paramValueIndex++){
					//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;LOCAL" + j + ";UNIT_MOVE=;" + k + ";VISITS=;" + localMabs[j].getMoveStats()[k].getVisits() + ";SCORE_SUM=;" + localMabs[j].getMoveStats()[k].getScoreSum() + ";AVG_VALUE=;" + (localMabs[j].getMoveStats()[k].getVisits() <= 0 ? "0" : (localMabs[j].getMoveStats()[k].getScoreSum()/((double)localMabs[j].getMoveStats()[k].getVisits()))));
					toLog += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";PARAM=;" + this.parametersManager.getName(paramIndex) + ";UNIT_MOVE=;" + this.parametersManager.getPossibleValues(paramIndex)[paramValueIndex] + ";PENALTY=;" + (this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex)[paramValueIndex] : 0) + ";VISITS=;" + localMabs[paramIndex].getMoveStats()[paramValueIndex].getVisits() + ";SCORE_SUM=;" + localMabs[paramIndex].getMoveStats()[paramValueIndex].getScoreSum() + ";AVG_VALUE=;" + (localMabs[paramIndex].getMoveStats()[paramValueIndex].getVisits() <= 0 ? "0" : (localMabs[paramIndex].getMoveStats()[paramValueIndex].getScoreSum()/((double)localMabs[paramIndex].getMoveStats()[paramValueIndex].getVisits()))) + ";";
				}
			}

			toLog += "\n";

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "LocalParamTunerStats", toLog);

			toLog = "";

			Map<Move,MyPair<MoveStats,Double>> globalInfo = this.roleProblems[roleProblemIndex].getGlobalMab().getMovesInfo();

			CombinatorialCompactMove theValuesIndices;
			String theValues;

			for(Entry<Move,MyPair<MoveStats,Double>> entry : globalInfo.entrySet()){

				theValuesIndices = (CombinatorialCompactMove) entry.getKey();
				theValues = "[ ";
				for(int paramIndex = 0; paramIndex < theValuesIndices.getIndices().length; paramIndex++){
					theValues += (this.parametersManager.getPossibleValues(paramIndex)[theValuesIndices.getIndices()[paramIndex]] + " ");
				}
				theValues += "]";

				//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;GLOBAL;COMBINATORIAL_MOVE=;" + entry.getKey() + ";VISITS=;" + entry.getValue().getVisits() + ";SCORE_SUM=;" + entry.getValue().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getVisits() <= 0 ? "0" : (entry.getValue().getScoreSum()/((double)entry.getValue().getVisits()))));
				toLog += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";PARAMS=;" + globalParamsOrder + ";COMB_MOVE=;" + theValues + ";PENALTY=;" + entry.getValue().getSecond() + ";VISITS=;" + entry.getValue().getFirst().getVisits() + ";SCORE_SUM=;" + entry.getValue().getFirst().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getFirst().getVisits() <= 0 ? "0" : (entry.getValue().getFirst().getScoreSum()/((double)entry.getValue().getFirst().getVisits()))) + ";";
			}

			toLog += "\n";

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", toLog);
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "USE_GLOBAL_BEST = " + this.useGlobalBest;

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
