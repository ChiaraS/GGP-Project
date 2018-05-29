package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MultiplePlayout extends PlayoutStrategy {

	/**
	 * Shared parameter that contains all the joint moves played so far for the current simulation in the tree.
	 */
	private List<MctsJointMove> currentSimulationJointMoves;

	// Logging purpose
	private int totalStepCalls;
	private int multiPlayoutStepCalls;

	interface CondChecker {
        boolean isMoveInteresting(List<Move> jointMove);
    }

	interface SingleRoleCondChecker {
        boolean isRoleMoveInteresting(Move move, int roleIndex);
    }


	private PlayoutStrategy subPlayoutStrategy;

	/**
	 * Check the condition to perform multiple playouts only if the search for the current
	 * game step has performed at least minIterationsThreshold.
	 * NOTE: not all condCheckers use this.
	 */
	private int minIterationsThreshold;

	/**
	 * Minimum number of visits that a move must have in the MAST statistics to be considered
	 * when checking if to perform multiple playouts.
	 */
	private int mastVisitsThreshold;

	/**
	 * If true, a move is considered interesting not only when its expected value is much higher
	 * than the average simulation value, but also when it is much lower.
	 */
	private boolean includeBadMoves;

	/**
	 * Used by the conditions checkers to decide if to perform multiple playouts or not.
	 * The checkers, in order to decide if to perform multiple playouts, check if the rewards associated
	 * with a move/all moves are (> avg+scoreOffset).
	 */
	private double scoreOffset;

	/**
	 * Number of playouts to be performed on the given state when the MAST score is above the threshold.
	 */
	private int numPlayouts;

	/**
	 * Search statistics decay. The statistics on the search performed so far are decayed after every game
	 * step by keeping searchStatsDecay of them (e.g. if searchStatsDecay=0.2 we will keep (0.2*searchStats)).
	 */
	private double searchStatsDecay;

	private List<Map<Move,MoveStats>> mastStatistics;

	/**
	 * Checks if the condition for performing multiple playouts is satisfied.
	 */
	private CondChecker condChecker;

	/**
	 * Memorized only for logging purposes.
	 */
	private String condCheckerType;

	/**
	 * (Decayed) sum of all the iterations performed till the end of the search for the
	 * previous game step.
	 */
	private int iterationsTillPreviousStep;

	/**
	 * (Decayed) sum of all the scores obtained by each role till the end of the search
	 * for the previous game step.
	 */
	private double[] scoreSumForRolesTillPreviousStep;

	/**
	 * For each role keep track of the average score obtained over all the iterations performed
	 * so far by the search for the current game step (NOTE that if multiple playouts are performed
	 * each of them counts as one iteration).
	 *
	 * TODO: unify these values for the whole MCTSManager instead of keeping them memorized
	 * in different parts of the code.
	 */
	/*
	private double[] currentStepScoreSum; // One value per role
	private int currentStepIterations; // Same value for all roles
	*/

	public MultiplePlayout(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		String[] subPlayoutStrategyDetails = gamerSettings.getIDPropertyValue("PlayoutStrategy" + id + ".subPlayoutStrategyType");

		try {
			this.subPlayoutStrategy = (PlayoutStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PLAYOUT_STRATEGIES.getConcreteClasses(), subPlayoutStrategyDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, subPlayoutStrategyDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating sub-playoutStrategy " + gamerSettings.getPropertyValue("PlayoutStrategy.subPlayoutStrategyType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.minIterationsThreshold = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".minIterationsThreshold");

		this.mastVisitsThreshold = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".mastVisitsThreshold");

		this.includeBadMoves = gamerSettings.getBooleanPropertyValue("PlayoutStrategy" + id + ".includeBadMoves");

		this.scoreOffset = gamerSettings.getDoublePropertyValue("PlayoutStrategy" + id + ".scoreOffset");

		this.numPlayouts = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".numPlayouts");

		this.searchStatsDecay = gamerSettings.getDoublePropertyValue("PlayoutStrategy" + id + ".searchStatsDecay");

		SingleRoleCondChecker singleRoleCondChecker;

		boolean dynamicAvg = gamerSettings.getBooleanPropertyValue("PlayoutStrategy" + id + ".dynamicAvg");

		if(dynamicAvg){

			this.condCheckerType = "DynamicAvg";

			singleRoleCondChecker = (move, roleIndex) -> {

				// Get the MAST score of my move that led to this state
				MoveStats theMoveStats = this.mastStatistics.get(roleIndex).get(move);

				/*System.out.println(myMoveStats == null ? "Null" : ("MOVE[ " + myMoveStats.getVisits() + " visits, " +
						myMoveStats.getScoreSum() + " scoreSum, " + (myMoveStats.getScoreSum() / ((double) myMoveStats.getVisits())) + " avg ]"));*/

				// If there is no MAST score, return the result of a single playout
				if(theMoveStats == null || theMoveStats.getVisits() < this.mastVisitsThreshold){
					return false;
				}

				// Compute the average MAST score
				double theMoveAvgScore = theMoveStats.getScoreSum() / ((double) theMoveStats.getVisits());

				// Compute the average return value of the game
				double currentStepAvgScore;
				int iterations = this.iterationsTillPreviousStep + this.gameDependentParameters.getStepIterations();
				if(iterations == 0){
					currentStepAvgScore = 50.0;
				}else{
					double scoreSumForRole = this.scoreSumForRolesTillPreviousStep[roleIndex] + this.gameDependentParameters.getStepScoreSumForRoles()[roleIndex];
					currentStepAvgScore = scoreSumForRole / ((double)iterations);
				}


				// If we are considering only good moves and the score is below the upper threshold or if we are considering
				// both bad and good moves and the score is included between the upper and lower threshold, perform a single playout.
				if(theMoveAvgScore < currentStepAvgScore + this.scoreOffset && (!this.includeBadMoves || theMoveAvgScore > currentStepAvgScore - this.scoreOffset)){
					return false;
				}
				return true;
			};

		}else{

			this.condCheckerType = "StaticAvg";

			singleRoleCondChecker = (move, roleIndex) -> {

				// Get the MAST score of my move that led to this state
				MoveStats theMoveStats = this.mastStatistics.get(roleIndex).get(move);

				/*System.out.println(myMoveStats == null ? "Null" : ("MOVE[ " + myMoveStats.getVisits() + " visits, " +
						myMoveStats.getScoreSum() + " scoreSum, " + (myMoveStats.getScoreSum() / ((double) myMoveStats.getVisits())) + " avg ]"));*/

				// If there is no MAST score, return the result of a single playout
				if(theMoveStats == null || theMoveStats.getVisits() < this.mastVisitsThreshold){
					return false;
				}

				// Compute the average MAST score
				double theMoveAvgScore = theMoveStats.getScoreSum() / ((double) theMoveStats.getVisits());

				// If we are considering only good moves and the score is below the upper threshold or if we are considering
				// both bad and good moves and the score is included between the upper and lower threshold, perform a single playout.
				if(theMoveAvgScore < 50.0 + this.scoreOffset && (!this.includeBadMoves || theMoveAvgScore > 50.0 - this.scoreOffset)){
					return false;
				}
				return true;
			};
		}

		String checkType = gamerSettings.getPropertyValue("PlayoutStrategy" + id + ".conditionOnMastType");
		this.condCheckerType += checkType;
		switch(checkType){
		case "MyRole":

			//System.out.println("MyRole");

			this.condChecker = (jointMove) -> {

				//System.out.println("MyRole");
				return singleRoleCondChecker.isRoleMoveInteresting(jointMove.get(this.gameDependentParameters.getMyRoleIndex()),
						this.gameDependentParameters.getMyRoleIndex());

				//System.out.println(toReturn);
				//return toReturn;
			};
			break;
		case "AllRolesAnd":

			//System.out.println("AllRolesAnd");

			this.condChecker = (jointMove) -> {

				for(int roleIndex = 0; roleIndex < jointMove.size(); roleIndex++){
					if(!singleRoleCondChecker.isRoleMoveInteresting(jointMove.get(roleIndex), roleIndex)){
						return false;
					}
				}
				return true;
			};
			break;
		case "AllRolesOr": default:

			//System.out.println("AllRolesOr");

			this.condChecker = (jointMove) -> {

				// If we haven't performed enough iterations for this game yet, don't perform multiple playouts,
				// because the average return value is probably still inaccurate.
				//if((this.iterationsTillPreviousStep + this.gameDependentParameters.getStepIterations()) < this.minIterationsThreshold){
				//	return false;
				//}

				for(int roleIndex = 0; roleIndex < jointMove.size(); roleIndex++){
					if(singleRoleCondChecker.isRoleMoveInteresting(jointMove.get(roleIndex), roleIndex)){
						return true;
					}
				}
				return false;
			};
			break;
		}

		//this.currentStepScoreSum = null;
		//this.currentStepIterations = 0;

		this.totalStepCalls = 0;
		this.multiPlayoutStepCalls = 0;

		this.iterationsTillPreviousStep = 0;
		this.scoreSumForRolesTillPreviousStep = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.subPlayoutStrategy.setReferences(sharedReferencesCollector);
		this.mastStatistics = sharedReferencesCollector.getMastStatistics();
		this.currentSimulationJointMoves = sharedReferencesCollector.getCurrentSimulationJointMoves();

	}

	@Override
	public void clearComponent() {

		this.subPlayoutStrategy.clearComponent();

		this.totalStepCalls = 0;
		this.multiPlayoutStepCalls = 0;

		this.iterationsTillPreviousStep = 0;
		this.scoreSumForRolesTillPreviousStep = null;

	}

	@Override
	public void setUpComponent() {

		this.subPlayoutStrategy.setUpComponent();

		//this.currentStepScoreSum = new double[this.gameDependentParameters.getNumRoles()];

		//this.resetStepStatistics();

		this.totalStepCalls = 0;
		this.multiPlayoutStepCalls = 0;

		this.iterationsTillPreviousStep = 0;
		this.scoreSumForRolesTillPreviousStep = new double[this.gameDependentParameters.getNumRoles()];

	}

	@Override
	public SimulationResult singlePlayout(MachineState state, int maxDepth) {
		this.totalStepCalls++;
		return this.subPlayoutStrategy.singlePlayout(state, maxDepth);
	}

	@Override
	public SimulationResult[] playout(List<Move> jointMove, MachineState state, int maxDepth) {

		SimulationResult[] results;

		if(this.anyInterestingMoveSoFar()){
			results = new SimulationResult[this.numPlayouts];
			for(int repetition = 0; repetition < results.length; repetition++){
				results[repetition] = this.subPlayoutStrategy.singlePlayout(state, maxDepth);
			}
			this.multiPlayoutStepCalls++;
		}else {
			results = this.subPlayoutStrategy.playout(jointMove, state, maxDepth);
		}

		this.totalStepCalls++;

		return results;

	}

	/**
	 * Checks all the joint moves performed so far in the tree and if at least one of those is
	 * interesting returns true.
	 *
	 * @return
	 */
	private boolean anyInterestingMoveSoFar() {

		// If we haven't performed enough iterations for this game yet, don't perform multiple playouts,
		// because the average return value is probably still inaccurate.
		if(this.iterationsTillPreviousStep + this.gameDependentParameters.getStepIterations() < this.minIterationsThreshold){
			return false;
		}

		// If we have performed enough iterations for this game check if there is any interesting joint move performed so far.
		boolean performMultiPlayout = false;

		System.out.println("All simulation moves = " + this.currentSimulationJointMoves.size());
		int i = 0;

		for(MctsJointMove move : this.currentSimulationJointMoves) {
			performMultiPlayout = performMultiPlayout || this.condChecker.isMoveInteresting(move.getJointMove());
			if(performMultiPlayout) {
				System.out.println("Found interesting move = " + i);
				break;
			}
			i++;
		}

		return performMultiPlayout;

	}

	@Override
	public List<Move> getJointMove(MachineState state) {
		return this.subPlayoutStrategy.getJointMove(state);
	}

	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) {
		return this.subPlayoutStrategy.getMoveForRole(state, roleIndex);
	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "SUB_PLAYOUT_STRATEGY = " + this.subPlayoutStrategy.printComponent(indentation + "  ") +
				indentation + "MIN_ITERATIONS_THRESHOLD = " + this.minIterationsThreshold +
				indentation + "MAST_VISITS_THRESHOLD = " + this.mastVisitsThreshold +
				indentation + "INCLUDE_BAD_MOVES = " + this.includeBadMoves +
				indentation + "SCORE_OFFSET = " + this.scoreOffset +
				indentation + "NUM_PLAYOUTS = " + this.numPlayouts +
				indentation + "SEARCH_STATS_DECAY = " + this.searchStatsDecay +
				indentation + "COND_CHECKER_TYPE = " + this.condCheckerType;

		if(this.mastStatistics != null){
			String mastStatisticsString = "[ ";

			for(Map<Move, MoveStats> roleMastStats : this.mastStatistics){
				mastStatisticsString += roleMastStats.size() + " entries, ";
			}

			mastStatisticsString += "]";

			params += indentation + "mast_statistics = " + mastStatisticsString;
		}else{
			params += indentation + "mast_statistics = null";
		}

		String scoreSumForRolesTillPreviousStepStirng;
		if(this.scoreSumForRolesTillPreviousStep != null){
			scoreSumForRolesTillPreviousStepStirng = "[ ";
			for(int roleIndex = 0; roleIndex < this.scoreSumForRolesTillPreviousStep.length; roleIndex++){
				scoreSumForRolesTillPreviousStepStirng += (this.scoreSumForRolesTillPreviousStep[roleIndex] + " ");
			}
			scoreSumForRolesTillPreviousStepStirng += "]";
		}else{
			scoreSumForRolesTillPreviousStepStirng =	"null";
		}

		params += indentation + "total_step_calls = " + this.totalStepCalls +
				indentation + "multi_playout_step_calls = " + this.multiPlayoutStepCalls +
				indentation + "iterations_till_previous_step = " + this.iterationsTillPreviousStep +
				indentation + "score_sum_for_roles_till_previous_step = " + scoreSumForRolesTillPreviousStepStirng;

		return params;

	}

	public void resetOrDecayStats(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "MultiPlayoutStats", "STEP=;" + this.gameDependentParameters.getGameStep() +
				";TOTAL_PLAYOUT_CALLS=;" + this.totalStepCalls + ";MULTI_PLAYOUT_CALLS=;" + this.multiPlayoutStepCalls + ";");

		this.totalStepCalls = 0;
		this.multiPlayoutStepCalls = 0;

		// Update search stats with stats of last step
		this.iterationsTillPreviousStep += this.gameDependentParameters.getStepIterations();
		for(int roleIndex = 0; roleIndex < this.scoreSumForRolesTillPreviousStep.length; roleIndex++){
			this.scoreSumForRolesTillPreviousStep[roleIndex] += this.gameDependentParameters.getStepScoreSumForRoles()[roleIndex];
		}

		// Decay search stats
		if(this.searchStatsDecay == 0.0){
			this.iterationsTillPreviousStep = 0;
			this.scoreSumForRolesTillPreviousStep = new double[this.gameDependentParameters.getNumRoles()];
		}else if(this.searchStatsDecay != 1.0){
			int oldIterations = this.iterationsTillPreviousStep;
			this.iterationsTillPreviousStep = (int) Math.round(((double)this.iterationsTillPreviousStep)*this.searchStatsDecay);
			double avg;
			for(int roleIndex = 0; roleIndex < this.scoreSumForRolesTillPreviousStep.length; roleIndex++){
				avg = this.scoreSumForRolesTillPreviousStep[roleIndex]/((double)oldIterations);
				this.scoreSumForRolesTillPreviousStep[roleIndex] = ((double)this.iterationsTillPreviousStep)*avg;
			}
		}

	}

}
