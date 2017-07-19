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
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MultiplePlayout extends PlayoutStrategy {


	// Logging purpose
	private int totalCalls;
	private int multiPlayoutCalls;



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
	 * Used by the conditions checkers to decide if to perform multiple playouts or not.
	 * The checkers, in order to decide if to perform multiple playouts, check if the rewards associated
	 * with a move/all moves are (> avg+scoreOffset).
	 */
	private double scoreOffset;

	/**
	 * Number of playouts to be performed on the given state when the MAST score is above the threshold.
	 */
	private int numPlayouts;

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

		this.scoreOffset = gamerSettings.getDoublePropertyValue("PlayoutStrategy" + id + ".scoreOffset");

		this.numPlayouts = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".numPlayouts");

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

				// Compute the average return value for the current step
				double currentStepAvgScore;
				if(this.gameDependentParameters.getStepIterations() == 0){
					currentStepAvgScore = 50.0;
				}else{
					currentStepAvgScore = this.gameDependentParameters.getScoreSumForStep()[roleIndex] / ((double)this.gameDependentParameters.getStepIterations());
				}


				// If the score is below the threshold, perform a single playout
				if(theMoveAvgScore < currentStepAvgScore + this.scoreOffset){
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

				// If the score is below the threshold, perform a single playout
				if(theMoveAvgScore < 50.0 + this.scoreOffset){
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

				// If we haven't performed enough iterations for this step yet, don't perform multiple playouts,
				// because the average return value for this step is probably still inaccurate.
				if(this.gameDependentParameters.getStepIterations() < this.minIterationsThreshold){
					return false;
				}

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

				// If we haven't performed enough iterations for this step yet, don't perform multiple playouts,
				// because the average return value for this step is probably still inaccurate.
				if(this.gameDependentParameters.getStepIterations() < this.minIterationsThreshold){
					return false;
				}

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

				// If we haven't performed enough iterations for this step yet, don't perform multiple playouts,
				// because the average return value for this step is probably still inaccurate.
				if(this.gameDependentParameters.getStepIterations() < this.minIterationsThreshold){
					return false;
				}

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

		this.totalCalls = 0;
		this.multiPlayoutCalls = 0;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.subPlayoutStrategy.setReferences(sharedReferencesCollector);
		this.mastStatistics = sharedReferencesCollector.getMastStatistics();

	}

	@Override
	public void clearComponent() {

		this.subPlayoutStrategy.clearComponent();

		//this.currentStepScoreSum = null;
		//this.currentStepIterations = 0;


	}

	@Override
	public void setUpComponent() {

		this.subPlayoutStrategy.setUpComponent();

		//this.currentStepScoreSum = new double[this.gameDependentParameters.getNumRoles()];

		//this.resetStepStatistics();

		this.totalCalls = 0;
		this.multiPlayoutCalls = 0;


	}

	@Override
	public SimulationResult singlePlayout(MachineState state, int maxDepth) {
		return this.subPlayoutStrategy.singlePlayout(state, maxDepth);
	}

	@Override
	public SimulationResult[] playout(List<Move> jointMove, MachineState state, int maxDepth) {

		SimulationResult[] results;

		// If the joint move is interesting, perform multiple playouts,...
		if(this.condChecker.isMoveInteresting(jointMove)){
			results = new SimulationResult[this.numPlayouts];
			for(int repetition = 0; repetition < results.length; repetition++){
				results[repetition] = this.subPlayoutStrategy.singlePlayout(state, maxDepth);
			}
			this.multiPlayoutCalls++;
		}else{
			results = this.subPlayoutStrategy.playout(jointMove, state, maxDepth);
		}

		this.totalCalls++;

		return results;

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
				indentation + "SCORE_OFFSET = " + this.scoreOffset +
				indentation + "NUM_PLAYOUTS = " + this.numPlayouts +
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

		//params += indentation + "current_step_score_sum = " + this.currentStepScoreSum +
		//		indentation + "current_step_iterations = " + this.currentStepIterations;

		return params;

	}

	public void logAndResetCallStatistics(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "MultiPlayoutStats", "STEP=;" + this.gameDependentParameters.getPreviousGameStep() +
				";TOTAL_PLAYOUT_CALLS=;" + this.totalCalls + ";MULTI_PLAYOUT_CALLS=;" + this.multiPlayoutCalls + ";");

		this.totalCalls = 0;
		this.multiPlayoutCalls = 0;

	}

}
