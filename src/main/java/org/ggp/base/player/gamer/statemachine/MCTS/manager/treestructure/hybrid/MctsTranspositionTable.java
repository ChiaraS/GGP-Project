package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AmafDecoupledMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.MachineState;

public class MctsTranspositionTable extends SearchManagerComponent{

	private boolean log;

	/**
	 * Specifies the decay factor for the statistics of the tree after each game step.
	 */
	private double treeDecay;

	/**
	 * Specifies the decay factor for the AMAF statistics in the nodes after each game step.
	 */
	private double amafDecay;

	/**
	 *
	 */
	private int gameStepOffset;

	/**
	 * The transposition table (implemented with HashMap that uses the state as key
	 * and solves collisions with linked lists).
	 */
	private Map<MachineState,MctsNode> transpositionTable;

	//private int currentGameStepStamp;

	/**
	 *
	 *
	public MctsTranspositionTable(int gameStepOffset){

		this(gameStepOffset, false, 1.0);

	}*/

	/**
	 *
	 */
	public MctsTranspositionTable(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		this.log = gamerSettings.getBooleanPropertyValue("MctsTranspositionTable.log");

		if(gamerSettings.specifiesProperty("MctsTranspositionTable.treeDecay")){
			this.treeDecay = gamerSettings.getDoublePropertyValue("MctsTranspositionTable.treeDecay");
		}else{
			this.treeDecay = 1.0; // No decay
		}

		if(gamerSettings.specifiesProperty("MctsTranspositionTable.amafDecay")){
			this.amafDecay = gamerSettings.getDoublePropertyValue("MctsTranspositionTable.amafDecay");
		}else{
			this.amafDecay = 1.0; // No decay
		}

		this.gameStepOffset = gamerSettings.getIntPropertyValue("MctsTranspositionTable.gameStepOffset");

		this.transpositionTable = new HashMap<MachineState,MctsNode>();

		//this.treeDecay = treeDecay;
		//this.amafDecay = amafDecay;
		//this.currentGameStepStamp = 1;
		//this.gameStepOffset = gameStepOffset;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
	}

	@Override
	public void clearComponent(){
		this.transpositionTable.clear();
	}

	@Override
	public void setUpComponent(){
		//this.currentGameStepStamp = 1;
		if(this.log){
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", "Step;Start/End;#Nodes;#ActionsStats;#RAVE_AMAFStats;#GRAVE_AMAFStats;ActionsStats/Node;RAVE_AMAFStats/Node;GRAVE_AMAFStats/Node;");
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", "1;Start;0;0;0;0;0;0;0;");
		}
	}

	public MctsNode getNode(MachineState state){
		MctsNode node = this.transpositionTable.get(state);
		if(node != null){
			//System.out.println("Found");
			node.setGameStepStamp(this.gameDependentParameters.getGameStep());
		}/*else{
			System.out.println("Not found");
		}*/
		return node;
	}

	public void putNode(MachineState state, MctsNode node){
		if(node != null){
			this.transpositionTable.put(state, node);
			node.setGameStepStamp(this.gameDependentParameters.getGameStep());
		}
	}

	/**
	 * This method cleans the table by removing very old statistics and decaying less old statistics.
	 */
	public void clean(){
		Iterator<Entry<MachineState,MctsNode>> iterator = this.transpositionTable.entrySet().iterator();
		Entry<MachineState,MctsNode> entry;
		while(iterator.hasNext()){
			entry = iterator.next();
			if(entry.getValue().getGameStepStamp() < (this.gameDependentParameters.getGameStep()-this.gameStepOffset)){
				iterator.remove();
			}else{
				entry.getValue().decayStatistics(this.treeDecay);
			}
		}
	}

	/**
	 * Logs the average number of action statistics and AMAF statistics memorized in the transposition table.
	 *
	 * @param logMoment "Start" if the statistics are being logged before the start of the search for the next
	 * move (and thus after being decayed after the previous move), "End" if they are being logged after the move
	 * and before being decayed.
	 */
	public void logTable(String logMoment){
		if(this.log){

			//int stepBeforeCleaning = this.gameDependentParameters.getPreviousGameStep();

			// TODO: make transposition table log only after move or also after metagame?

			int size = this.transpositionTable.size();

			int totalActionsStats = 0;
			int totalRaveAmaf = 0;
			int totalGraveAmaf = 0;

			//System.out.println("Current TT game step: " + newGameStepStamp);
			//System.out.println("Cleaning TT with game step: " + newGameStepStamp);
			//System.out.println("Current TT size: " + this.transpositionTable.size());

			// Remove all nodes last accessed earlier than the game step (newGameStepStamp-gameStepOffset)
			for(Entry<MachineState,MctsNode> entry : this.transpositionTable.entrySet()){
				if(entry.getValue() instanceof AmafDecoupledMctsNode){
					int actionsStats = ((AmafDecoupledMctsNode) entry.getValue()).getActionsStatsNumber();
					int raveAmaf = ((AmafDecoupledMctsNode) entry.getValue()).getRaveAMAFStatsNumber();
					int graveAmaf = ((AmafDecoupledMctsNode) entry.getValue()).getGraveAMAFStatsNumber();

					totalActionsStats += actionsStats;
					totalRaveAmaf += raveAmaf;
					totalGraveAmaf += graveAmaf;
				}
			}

			double actionsStatsPerNode = ((double) totalActionsStats) / ((double) size);
			double raveAmafPerNode = ((double) totalRaveAmaf) / ((double) size);
			double graveAmafPerNode = ((double) totalGraveAmaf) / ((double) size);

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", this.gameDependentParameters.getGameStep() +
					";" + logMoment + ";" + size + ";" + actionsStatsPerNode + ";" +
					raveAmafPerNode + ";" + graveAmafPerNode + ";" + actionsStatsPerNode + ";" +
					raveAmafPerNode + ";" + graveAmafPerNode + ";");
		}
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "LOGGING = " + this.log +
				indentation + "TREE_DECAY = " + this.treeDecay +
				indentation + "AMAF_DECAY = " + this.amafDecay +
				indentation + "GAME_STEP_OFFSET = " + this.gameStepOffset;
	}

	public void turnOffLogging(){
		this.log = false;
	}

}
