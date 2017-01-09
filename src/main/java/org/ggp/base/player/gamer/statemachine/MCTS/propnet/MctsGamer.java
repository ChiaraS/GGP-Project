package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.Random;

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
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class MctsGamer extends InternalPropnetGamer {

	/**
	 * Game step. Keeps track of the current game step.
	 */
	private int gameStep;

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

		this.gameStep = 0;

		this.metagameSearch = gamerSettings.getBooleanPropertyValue("Gamer.metagameSearch");

		this.mctsManager = new HybridMctsManager(new Random(), gamerSettings, this.gamerType);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineMetaGame(long)
	 */
	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();
		long realTimeout = timeout - this.selectMoveSafetyMargin;
		// Information to log at the end of metagame.
		// As default value they are initialized with "-1". A value of "-1" for a parameter means that
		// its value couldn't be computed (because there was no time or because of an error).
		long thinkingTime; // Total time used for metagame
		long searchTime = -1; // Actual time spent on the search
		int iterations = -1;
    	int visitedNodes = -1;
    	double iterationsPerSecond = -1;
    	double nodesPerSecond = -1;
		GamerLogger.log("Gamer", "Starting metagame with available thinking time " + (realTimeout-start) + "ms.");
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score");

		AbstractStateMachine abstractStateMachine;
		int myRoleIndex;
		int numRoles;

		if(this.thePropnetMachine != null){
			abstractStateMachine = new CompactStateMachine(this.thePropnetMachine);
			myRoleIndex = this.thePropnetMachine.convertToCompactRole(this.getRole()).getIndex();
			numRoles = this.thePropnetMachine.getCompactRoles().size();
		}else{
			abstractStateMachine = new ExplicitStateMachine(this.getStateMachine());
			numRoles = this.getStateMachine().getExplicitRoles().size();
			myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());
		}

		this.mctsManager.setUpManager(abstractStateMachine, numRoles, myRoleIndex);

		this.logGamerSettings();

		if(this.metagameSearch){

			// If there is enough time left start the MCT search.
			// Otherwise return from metagaming.
			if(System.currentTimeMillis() < realTimeout){
				// If search fails during metagame?? TODO: should I throw exception here and say I'm not able to play?
				// If I don't it'll throw exception later anyway! Better stop now?

				GamerLogger.log("Gamer", "Starting search during metagame.");

				try {

					this.mctsManager.search(abstractStateMachine.getInitialState(), realTimeout, this.gameStep+1);

					GamerLogger.log("Gamer", "Done searching during metagame.");
					searchTime = this.mctsManager.getSearchTime();
		        	iterations = this.mctsManager.getIterations();
		        	visitedNodes = this.mctsManager.getVisitedNodes();
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
			}else{
				GamerLogger.log("Gamer", "No time to start the search during metagame.");

				thinkingTime = System.currentTimeMillis() - start;
			}
		}else{

			GamerLogger.log("Gamer", "Gamer set to perform no search during metagame.");

			thinkingTime = System.currentTimeMillis() - start;
		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";null;-1;-1;-1;");
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
    	int visitedNodes = -1;
    	double iterationsPerSecond = -1;
    	double nodesPerSecond = -1;
    	ExplicitMove theMove = null;
    	double moveScoreSum = -1.0;
    	int moveVisits = -1;
    	double moveAvgScore = -1;

		this.gameStep++;

		GamerLogger.log("Gamer", "Starting move selection for game step " + this.gameStep + " with available time " + (realTimeout-start) + "ms.");

		if(System.currentTimeMillis() < realTimeout){

			GamerLogger.log("Gamer", "Selecting move using MCTS.");

			try {

				MctsNode currentNode;
				CompleteMoveStats selectedMove;

				// TODO: adapt the code of StateMachineGamer to use an AbstractStateMachine so we don't need to do all these checks and casts
				MachineState theState = this.getCurrentState();
				if(this.thePropnetMachine != null){
					theState = this.thePropnetMachine.convertToCompactMachineState((ExplicitMachineState)theState);
				}

				currentNode = this.mctsManager.search(theState, realTimeout, gameStep);

				selectedMove = this.mctsManager.getBestMove(currentNode);

				searchTime = this.mctsManager.getSearchTime();
				iterations = this.mctsManager.getIterations();
		    	visitedNodes = this.mctsManager.getVisitedNodes();
		    	if(searchTime != 0){
		        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
		        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
	        	}else{
	        		iterationsPerSecond = 0;
	        		nodesPerSecond = 0;
	        	}

				Move theAbstractMove = selectedMove.getTheMove();

				if(theAbstractMove instanceof CompactMove){
					theMove = this.thePropnetMachine.convertToExplicitMove((CompactMove)theAbstractMove);
				}else{
					theMove = (ExplicitMove)theAbstractMove;
				}

		    	moveScoreSum = selectedMove.getScoreSum();
		    	moveVisits = selectedMove.getVisits();
		    	moveAvgScore = moveScoreSum / ((double) moveVisits);

				GamerLogger.log("Gamer", "Returning MCTS move " + theMove + ".");
			}catch(MCTSException e){
				GamerLogger.logError("Gamer", "MCTS failed to return a move.");
				GamerLogger.logStackTrace("Gamer", e);
				// If the MCTS manager failed to return a move return a random one.
				theMove = this.getStateMachine().getRandomMove(this.getCurrentState(), this.getRole());
				GamerLogger.log("Gamer", "Returning random move " + theMove + ".");
			}
		}else{
			// If there is no time return a random move.
			//GamerLogger.log("Gamer", "No time to start the search during metagame.");
			theMove = this.getStateMachine().getRandomMove(this.getCurrentState(), this.getRole());
			GamerLogger.log("Gamer", "No time to select next move using MCTS. Returning random move " + theMove + ".");
		}

		thinkingTime = System.currentTimeMillis() - start;

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", this.gameStep + ";" + thinkingTime + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + theMove + ";" + moveScoreSum + ";" + moveVisits + ";" + moveAvgScore + ";");

		// TODO: IS THIS NEEDED? WHEN?
		notifyObservers(new GamerSelectedMoveEvent(this.getStateMachine().getExplicitLegalMoves(this.getCurrentState(), this.getRole()), theMove, thinkingTime));

		return theMove;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {

		this.gameStep = 0;
		this.mctsManager.clearManager();
		super.stateMachineStop();

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {

		this.gameStep = 0;
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
