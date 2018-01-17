package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NTuple;
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
					this.discreteParametersManager.getNumPossibleValuesForAllParams(), this.nTuplesForUCBLengths);
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

				int[] indices = new int[this.discreteParametersManager.getNumTunableParameters()];
				for(int i = 0; i < indices.length; i++){
					indices[i] = -1;
				}

				// Select a value for each local mab independently
				IncrementalMab oneTupleMab;
				for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
					oneTupleMab = this.roleProblems[roleProblemIndex].getLandscapeModelForStatsUpdate().get(new NTuple(new int[] {paramIndex}));
					Move m = this.bestCombinationSelector.selectMove(oneTupleMab.getMovesInfo(),
							this.discreteParametersManager.getValuesFeasibility(paramIndex, oneTupleMab.getMovesInfo().keySet(), indices),
							oneTupleMab.getNumUpdates());
					indices[paramIndex] = ((CombinatorialCompactMove) m).getIndices()[0];
				}
				this.selectedCombinations[roleProblemIndex] = indices;
			}
		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations));

		this.discreteParametersManager.setParametersValues(this.selectedCombinations);

	}

	private NTuple getCompleteNTuple() {
		int[] completeCombo = new int[this.discreteParametersManager.getNumTunableParameters()];
		for(int i = 0; i < this.discreteParametersManager.getNumTunableParameters(); i++) {
			completeCombo[i] = i;
		}
		return new NTuple(completeCombo);
	}

	@Override
	public void updateRoleProblems(List<Integer> individualsIndices, int[] neededRewards) {

		int individualIndex;

		CompleteMoveStats individualStats;

		Map<NTuple,IncrementalMab> landscapeModel;

		CombinatorialCompactMove theIndividual;

		CombinatorialCompactMove nTupleValueToUpdate;

		IncrementalMab nTupleMab;

		MyPair<MoveStats,Double> nTupleValueInfo;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			individualIndex = individualsIndices.get(roleProblemIndex);

			/****** Update individual's stats ******/

			individualStats = this.roleProblems[roleProblemIndex].getPopulation()[individualIndex];
			individualStats.incrementScoreSum(neededRewards[roleProblemIndex]);
			individualStats.incrementVisits();
			this.roleProblems[roleProblemIndex].incrementTotalUpdates();

			/********** Update landscape model **********/

			landscapeModel = this.roleProblems[roleProblemIndex].getLandscapeModelForStatsUpdate();

			theIndividual = (CombinatorialCompactMove) individualStats.getTheMove();

			for(Entry<NTuple,IncrementalMab> nTupleEntry : landscapeModel.entrySet()) {
				nTupleValueToUpdate = this.roleProblems[roleProblemIndex].getNTupleValues(theIndividual, nTupleEntry.getKey());
				nTupleMab = nTupleEntry.getValue();
				nTupleValueInfo = nTupleMab.getMovesInfo().get(nTupleValueToUpdate);
				if(nTupleValueInfo == null) {
					nTupleValueInfo = new MyPair<MoveStats,Double>(new MoveStats(), this.discreteParametersManager.computeCombinatorialMovePenalty(nTupleValueToUpdate.getIndices()));
					nTupleMab.getMovesInfo().put(nTupleValueToUpdate, nTupleValueInfo);
				}
				nTupleValueInfo.getFirst().incrementScoreSum(neededRewards[roleProblemIndex]);
				nTupleValueInfo.getFirst().incrementVisits();
				nTupleMab.incrementNumUpdates();
			}
		}
	}

	@Override
	public void logStats() {

		super.logStats();

		/* ********************************* Log n-tuple stats ************************************* */

		// If the roleProblems are of type UcbEvoProblemRepresentation, log also the Ucb statistics.

		int roleIndex;

		Map<NTuple,IncrementalMab> landscapeModel;

		int nTupleLength;

		String parameterNames;

		for(int roleProblemIndex = 0; roleProblemIndex < this.getRoleProblems().length; roleProblemIndex++){

			// We create one string to log for each n-tuple length
			// NOTE that if we are not using a certain length, the corresponding string will stay
			// empty and not be logged.
			String[] toLog = new String[this.discreteParametersManager.getNumTunableParameters()];
			for(int i = 0; i < toLog.length; i++) {
				toLog[i] = "";
			}

			if(this.tuneAllRoles){
				roleIndex = roleProblemIndex;
			}else{
				roleIndex = this.gameDependentParameters.getMyRoleIndex();
			}

			landscapeModel = this.roleProblems[roleProblemIndex].getLandscapeModelForStatsUpdate();

			for(Entry<NTuple,IncrementalMab> nTupleEntry : landscapeModel.entrySet()){
				parameterNames = this.getParamsNames(nTupleEntry.getKey());
				nTupleLength = nTupleEntry.getKey().getParamIndices().length;
				for(Entry<Move,MyPair<MoveStats,Double>> nTupleValueInfo : nTupleEntry.getValue().getMovesInfo().entrySet()){
					toLog[nTupleLength-1] += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
							";PARAMS=;" + parameterNames + ";VALUES=;" + this.valuesToString(((CombinatorialCompactMove) nTupleValueInfo.getKey()).getIndices(), nTupleEntry.getKey()) +
							";PENALTY=;" + nTupleValueInfo.getValue().getSecond() + ";VISITS=;" + nTupleValueInfo.getValue().getFirst().getVisits() +
							";SCORE_SUM=;" + nTupleValueInfo.getValue().getFirst().getScoreSum() +
							";AVG_VALUE=;" + (nTupleValueInfo.getValue().getFirst().getVisits() <= 0 ? "0" : (nTupleValueInfo.getValue().getFirst().getScoreSum()/((double)nTupleValueInfo.getValue().getFirst().getVisits()))) + ";";
				}
			}

			for(int i = 0; i < toLog.length; i++) {
				if(!toLog[i].equals("")) {
					if(i == 0) {
						GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "LocalParamTunerStats", "\n" + toLog[i]);
					}else if(i == this.discreteParametersManager.getNumTunableParameters()-1) {
						GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", "\n" + toLog[i]);
					}else {
						GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "" + (i+1) + "TupleParamTunerStats", "\n" + toLog[i]);
					}
				}
			}
		}
	}

	protected String getParamsNames(NTuple nTuple){
		String paramsNames = "[ ";
		for(int paramIndex : nTuple.getParamIndices()){
			paramsNames += (this.discreteParametersManager.getName(paramIndex) + " ");
		}
		paramsNames += "]";

		return paramsNames;
	}

	private String valuesToString(int[] valueIndices, NTuple nTuple) {

		int[] paramIndices = nTuple.getParamIndices();

		String values = "[ ";
		for(int i = 0; i < paramIndices.length; i++){
			values += (this.discreteParametersManager.getPossibleValues(paramIndices[i])[valueIndices[i]] + " ");
		}
		values += "]";

		return values;

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "USE_GLOBAL_BEST = " + this.useGlobalBest;

		if(this.nTuplesForUCBLengths != null){
			String nTuplesForUCBLengthsString = "[ ";

			for(Integer i : this.nTuplesForUCBLengths){
				nTuplesForUCBLengthsString += i.intValue() + " ";
			}

			nTuplesForUCBLengthsString += "]";

			params += indentation + "N_TUPLES_FOR_UCB_LENGTHS = " + nTuplesForUCBLengthsString;
		}else{
			params += indentation + "N_TUPLES_FOR_UCB_LENGTHS = null (i.e. using all lengths)";
		}

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
