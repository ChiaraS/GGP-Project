package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMctsManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.propnet.InternalPropnetGamer;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class MctsGamer extends InternalPropnetGamer {

	/**
	 * Game step. Keeps track of the current game step.
	 */
	private int gameStep;
	/**
	 * Needed to memorize the thinking time of the metagame so that later it can be summed with
	 * the thinking time of the first move to obtain the total thinking time for the first game
	 * step.
	 */
	private long metagameThinkingTime;

	/**
	 * Tells to the gamer if start performing the search also during metagame or not.
	 * (Usually true, set to false only for testing purposes)
	 */
	protected boolean metagameSearch;

	/**
	 * The class that takes care of performing Monte Carlo tree search.
	 */
	protected HybridMctsManager mctsManager;
	//protected InternalPropnetMCTSManager mctsManager;

	/**
	 * True if the gamer must use the MCTSManager that uses the AbstractStateMachine, abstracting the structure of
	 * states, moves and roles. False if the gamer must create the appropriate MCTSManager depending on the type
	 * of state machine that it is using.
	 *
	 * NOTE: this will disappear when the only official MctsManager will be the hybrid one
	 */
	//protected boolean hybridManager;

	public MctsGamer() {

		this(GamerConfiguration.gamersSettingsFolderPath + "/" + defaultSettingsFileName);

		/*
		this.gameStep = 0;
		this.metagameSearch = true;

		this.valueOffset = 0.01;
		this.gameStepOffset = 2;
		this.maxSearchDepth = 500;
		this.logTranspositionTable = false;
		*/

		//this.hybridManager = true;
	}

	public MctsGamer(String settingsFilePath) {

		super(settingsFilePath);

	}

	@Override
	protected void configureGamer(GamerSettings gamerSettings){

		super.configureGamer(gamerSettings);

		// this.gameStep == 0 is to indicate that we haven't performed the stateMachineSelectMove() method yet.
		this.gameStep = 0;

		this.metagameSearch = gamerSettings.getBooleanPropertyValue("Gamer.metagameSearch");

		this.mctsManager = new HybridMctsManager(this.random, gamerSettings, this.gamerType);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineMetaGame(long)
	 */
	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();
		long realTimeout = timeout - this.metagameSafetyMargin;
		// Information to log at the end of metagame.
		// As default value they are initialized with "-1". A value of "-1" for a parameter means that
		// its value couldn't be computed (because there was no time or because of an error).
		long thinkingTime; // Total time used for metagame
		long searchTime = -1; // Actual time spent on the search
		int iterations = -1;
		int addedNodes = -1;
		int memorizedStates = -1;
		double visitedNodes = -1;
    	double iterationsPerSecond = -1;
    	double nodesPerSecond = -1;
    	String avgSearchScorePerRole = "[ ]";

		GamerLogger.log("Gamer", "Starting metagame with available thinking time " + (realTimeout-start) + "ms.");

		AbstractStateMachine theMachine = this.getStateMachine();
		int myRoleIndex = theMachine.getRoleIndices().get(this.getRole());
		int numRoles = theMachine.getRoles().size();

    	String rolesList = "[ ";
    	for(int roleIndex = 0; roleIndex < numRoles; roleIndex++){
    		rolesList += (theMachine.convertToExplicitRole((theMachine.getRoles().get(roleIndex))) + " ");
    	}
    	rolesList += "]";
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Added nodes;Memorized states;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score;Avg search score " + rolesList + ";");

		this.mctsManager.setUpManager(theMachine, numRoles, myRoleIndex, ((long)this.getMatch().getPlayClock() * 1000) - this.selectMoveSafetyMargin);

		this.logGamerSettings();

		if(this.metagameSearch){

			// Before move actions are performed ALWAYS, even when there is no time to start the search
			this.mctsManager.beforeMoveActions(1, true, this.getInternalLastJointMove());

			// If there is enough time left start the MCT search.
			// Otherwise return from metagaming.
			if(System.currentTimeMillis() < realTimeout){
				// If search fails during metagame?? TODO: should I throw exception here and say I'm not able to play?
				// If I don't it'll throw exception later anyway! Better stop now?

				GamerLogger.log("Gamer", "Starting search during metagame.");

				try {

					this.mctsManager.search(theMachine.getInitialState(), realTimeout, this.gameStep+1);

					GamerLogger.log("Gamer", "Done searching during metagame.");
					searchTime = this.mctsManager.getStepSearchDuration();
		        	iterations = this.mctsManager.getStepIterations();
		        	visitedNodes = this.mctsManager.getStepVisitedNodes();
		        	addedNodes = this.mctsManager.getStepAddedNodes();
		        	memorizedStates = this.mctsManager.getStepMemorizedStates();

		        	if(iterations > 0){
			        	double[] scoreSumPerRole = this.mctsManager.getStepScoreSumForRoles();
			        	avgSearchScorePerRole = "[ ";
			           	for(int roleIndex = 0; roleIndex < scoreSumPerRole.length; roleIndex++){
			           		avgSearchScorePerRole += ((scoreSumPerRole[roleIndex]/((double)iterations)) + " ");
			        	}
			           	avgSearchScorePerRole += "]";
		        	}else{
		        		avgSearchScorePerRole = "[ ]";
		        	}

		        	if(searchTime != 0){
			        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
			        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
		        	}else{
		        		iterationsPerSecond = 0;
		        		nodesPerSecond = 0;
		        	}
		        	thinkingTime = System.currentTimeMillis() - start;
				}catch(MCTSException e) {
					GamerLogger.logError("Gamer", "Exception during search while metagaming.");
					GamerLogger.logStackTrace("Gamer", e);

					thinkingTime = System.currentTimeMillis() - start;
				}

				this.mctsManager.afterMetagameActions();
			}else{
				GamerLogger.log("Gamer", "No time to start the search during metagame.");

				thinkingTime = System.currentTimeMillis() - start;
			}
		}else{

			GamerLogger.log("Gamer", "Gamer set to perform no search during metagame.");

			thinkingTime = System.currentTimeMillis() - start;
		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" +
				searchTime + ";" + iterations + ";" + visitedNodes + ";" + addedNodes + ";" + memorizedStates + ";" + iterationsPerSecond + ";" +
				nodesPerSecond + ";null;-1;-1;-1;" + avgSearchScorePerRole + ";");

		this.metagameThinkingTime = thinkingTime;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();
		long realTimeout = timeout - this.selectMoveSafetyMargin;

		// Information to log at the end of move selection.
		// As default value numeric parameters are initialized with "-1" and the others with "null".
		// A value of "-1" or "null" for a parameter means that its value couldn't be computed
		// (because there was no time or because of an error).
		long thinkingTime;
		long searchTime = -1;
		int iterations = -1;
		double visitedNodes = -1;
		int addedNodes = -1;
		int memorizedStates = -1;
    	double iterationsPerSecond = -1;
    	double nodesPerSecond = -1;
    	ExplicitMove theMove = null;
    	double moveScoreSum = -1.0;
    	int moveVisits = -1;
    	double moveAvgScore = -1;
    	String avgSearchScorePerRole = "[ ]";

		this.gameStep++;

		//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");
		//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "New step = " + this.gameStep + ";");
		//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");

		GamerLogger.log("Gamer", "Starting move selection for game step " + this.gameStep + " with available time " + (realTimeout-start) + "ms.");

		if((!this.metagameSearch) || this.gameStep > 1){ // For game step 1 is the metagame method that calls the before-move-actions
			this.mctsManager.beforeMoveActions(this.gameStep, false, this.getInternalLastJointMove());
		}

		if(System.currentTimeMillis() < realTimeout){

			GamerLogger.log("Gamer", "Selecting move using MCTS.");

			try {

				MctsNode currentNode;
				CompleteMoveStats selectedMove;

				MachineState theState = this.getCurrentState();
				/*if(this.thePropnetMachine != null){
					theState = this.thePropnetMachine.convertToCompactMachineState((ExplicitMachineState)theState);
				}*/

				currentNode = this.mctsManager.search(theState, realTimeout, gameStep);

				selectedMove = this.mctsManager.getBestMove(currentNode);

				searchTime = this.mctsManager.getStepSearchDuration();
				iterations = this.mctsManager.getStepIterations();
		    	visitedNodes = this.mctsManager.getStepVisitedNodes();
		    	addedNodes = this.mctsManager.getStepAddedNodes();
		    	memorizedStates = this.mctsManager.getStepMemorizedStates();
		    	if(searchTime != 0){
		        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
		        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
	        	}else{
	        		iterationsPerSecond = 0;
	        		nodesPerSecond = 0;
	        	}

				Move theAbstractMove = selectedMove.getTheMove();

				theMove = this.getStateMachine().convertToExplicitMove(theAbstractMove);

				/*
				if(theAbstractMove instanceof CompactMove){
					theMove = this.thePropnetMachine.convertToExplicitMove((CompactMove)theAbstractMove);
				}else{
					theMove = (ExplicitMove)theAbstractMove;
				}*/

		    	moveScoreSum = selectedMove.getScoreSum();
		    	moveVisits = selectedMove.getVisits();
		    	if(moveVisits != 0){
		    		moveAvgScore = moveScoreSum / ((double) moveVisits);
		    	}else{
		    		moveAvgScore = 0;
		    	}

	        	if(iterations > 0){
		        	double[] scoreSumPerRole = this.mctsManager.getStepScoreSumForRoles();
		        	avgSearchScorePerRole = "[ ";
		           	for(int roleIndex = 0; roleIndex < scoreSumPerRole.length; roleIndex++){
		           		avgSearchScorePerRole += ((scoreSumPerRole[roleIndex]/((double)iterations)) + " ");
		        	}
		           	avgSearchScorePerRole += "]";
	        	}else{
	        		avgSearchScorePerRole = "[ ]";
	        	}

				GamerLogger.log("Gamer", "Returning MCTS move " + theMove + ".");
			}catch(MCTSException e){
				GamerLogger.logError("Gamer", "MCTS failed to return a move.");
				GamerLogger.logStackTrace("Gamer", e);
				// If the MCTS manager failed to return a move return a random one.
				theMove = this.getStateMachine().convertToExplicitMove(this.getStateMachine().getRandomMove(this.getCurrentState(), this.getRole()));
				GamerLogger.log("Gamer", "Returning random move " + theMove + ".");
			}
		}else{
			// If there is no time return a random move.
			//GamerLogger.log("Gamer", "No time to start the search during metagame.");
			theMove = this.getStateMachine().convertToExplicitMove(this.getStateMachine().getRandomMove(this.getCurrentState(), this.getRole()));
			GamerLogger.log("Gamer", "No time to select next move using MCTS. Returning random move " + theMove + ".");
		}

		thinkingTime = System.currentTimeMillis() - start;

		// If it's the first step we need to add the thinking time of the metagame, too.
		if(this.gameStep == 1){
			thinkingTime += this.metagameThinkingTime;
		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" +
				iterations + ";" + visitedNodes + ";" + addedNodes + ";" + memorizedStates + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + theMove + ";" +
				moveScoreSum + ";" + moveVisits + ";" + moveAvgScore + ";" + avgSearchScorePerRole + ";");

		// TODO: IS THIS NEEDED? WHEN?
		notifyObservers(new GamerSelectedMoveEvent(this.getStateMachine().convertToExplicitMoves(this.getStateMachine().getLegalMoves(this.getCurrentState(), this.getRole())), theMove, thinkingTime));

		this.mctsManager.afterMoveActions();

		return theMove;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {

		this.gameStep = 0;
		this.mctsManager.afterGameActions(this.getMatch().getGoalValues(), this.getInternalLastJointMove());
		this.mctsManager.clearManager();
		super.stateMachineStop();

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {

		this.gameStep = 0;
		this.mctsManager.afterGameActions(this.getMatch().getGoalValues(), null); // Pass null as InternalLastJointMove because the actual InternalLastJointMove refers to the previous step.
		this.mctsManager.clearManager();
		super.stateMachineAbort();

	}

	@Override
	protected String printGamer(){
		return super.printGamer() +
				"\nMETAGAME_SEARCH = " + this.metagameSearch +
				"\n\n" + (this.mctsManager == null ? "null" : this.mctsManager.printSearchManager());
	}

}
