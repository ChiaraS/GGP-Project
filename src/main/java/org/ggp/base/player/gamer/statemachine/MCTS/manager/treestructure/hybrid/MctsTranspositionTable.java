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
	 * This method logs the GRAVE/RAVE statistics (if this.log == true), then leans the table
	 */
	public void clean(){

		// Print to check if everything is reset properly
		/*Iterator<Entry<MachineState,MctsNode>> iterator2 = this.transpositionTable.entrySet().iterator();
		while(iterator2.hasNext()){
			System.out.println(iterator2.next().getValue().toString());
		}*/

		// Clean the table only if the game-step stamp changed (this is already checked by the caller).
		//if(newGameStepStamp != this.currentGameStepStamp){

		if(this.log){

			int stepBeforeCleaning = this.gameDependentParameters.getPreviousGameStep();

			// TODO: make transposition table log only after move or also after metagame?

			int sizeBeforeCleaning = this.transpositionTable.size();

			int actionsStatsBeforeCleaning = 0;
			int raveAmafBeforeCleaning = 0;
			int graveAmafBeforeCleaning = 0;

			int actionsStatsAfterCleaning = 0;
			int raveAmafAfterCleaning = 0;
			int graveAmafAfterCleaning = 0;


			//System.out.println("Current TT game step: " + newGameStepStamp);
			//System.out.println("Cleaning TT with game step: " + newGameStepStamp);
			//System.out.println("Current TT size: " + this.transpositionTable.size());

			// Remove all nodes last accessed earlier than the game step (newGameStepStamp-gameStepOffset)
			Iterator<Entry<MachineState,MctsNode>> iterator = this.transpositionTable.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<MachineState,MctsNode> entry = iterator.next();

				if(entry.getValue() instanceof AmafDecoupledMctsNode){
					int actionsStats = ((AmafDecoupledMctsNode) entry.getValue()).getActionsStatsNumber();
					int raveAmaf = ((AmafDecoupledMctsNode) entry.getValue()).getRaveAMAFStatsNumber();
					int graveAmaf = ((AmafDecoupledMctsNode) entry.getValue()).getGraveAMAFStatsNumber();

					actionsStatsBeforeCleaning += actionsStats;
					raveAmafBeforeCleaning += raveAmaf;
					graveAmafBeforeCleaning += graveAmaf;

					if(entry.getValue().getGameStepStamp() < (this.gameDependentParameters.getGameStep()-this.gameStepOffset)){
						iterator.remove();
					}else{
						/*
						actionsStatsAfterCleaning += actionsStats;
						raveAmafAfterCleaning += raveAmaf;
						graveAmafAfterCleaning += graveAmaf;
						*/

						entry.getValue().decayStatistics(this.treeDecay);
						((AmafDecoupledMctsNode)entry.getValue()).decayAmafStatistics(this.amafDecay);

						actionsStatsAfterCleaning += ((AmafDecoupledMctsNode) entry.getValue()).getActionsStatsNumber();
						raveAmafAfterCleaning += ((AmafDecoupledMctsNode) entry.getValue()).getRaveAMAFStatsNumber();
						graveAmafAfterCleaning += ((AmafDecoupledMctsNode) entry.getValue()).getGraveAMAFStatsNumber();

					}
				}else{
					if(entry.getValue().getGameStepStamp() < (this.gameDependentParameters.getGameStep()-this.gameStepOffset)){
						iterator.remove();
					}else{
						entry.getValue().decayStatistics(this.treeDecay);
					}
				}
			}

			double actionsStatsPerNode = ((double) actionsStatsBeforeCleaning) / ((double) sizeBeforeCleaning);
			double raveAmafPerNode = ((double) raveAmafBeforeCleaning) / ((double) sizeBeforeCleaning);
			double graveAmafPerNode = ((double) graveAmafBeforeCleaning) / ((double) sizeBeforeCleaning);

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", stepBeforeCleaning +
					";End;" + sizeBeforeCleaning + ";" + actionsStatsBeforeCleaning + ";" +
					raveAmafBeforeCleaning + ";" + graveAmafBeforeCleaning + ";" + actionsStatsPerNode + ";" +
					raveAmafPerNode + ";" + graveAmafPerNode + ";");

			int stepAfterCleaning = this.gameDependentParameters.getGameStep();
			int sizeAfterCleaning = this.transpositionTable.size();

			actionsStatsPerNode = ((double) actionsStatsAfterCleaning) / ((double) sizeAfterCleaning);
			raveAmafPerNode = ((double) raveAmafAfterCleaning) / ((double) sizeAfterCleaning);
			graveAmafPerNode = ((double) graveAmafAfterCleaning) / ((double) sizeAfterCleaning);

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", stepAfterCleaning +
					";Start;" + sizeAfterCleaning + ";" + actionsStatsAfterCleaning + ";" +
					raveAmafAfterCleaning + ";" + graveAmafAfterCleaning + ";" + actionsStatsPerNode + ";" +
					raveAmafPerNode + ";" + graveAmafPerNode + ";");

		}else{

			// Remove all nodes last accessed earlier than the game step (newGameStepStamp-gameStepOffset)
			Iterator<Entry<MachineState,MctsNode>> iterator = this.transpositionTable.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<MachineState,MctsNode> entry = iterator.next();

				if(entry.getValue().getGameStepStamp() < (this.gameDependentParameters.getGameStep()-this.gameStepOffset)){
					iterator.remove();
				}else{
					entry.getValue().decayStatistics(this.treeDecay);
				}
			}

		}

		//this.currentGameStepStamp = newGameStepStamp;

		// Print to check if everything is reset properly
		/*Iterator<Entry<MachineState,MctsNode>> iterator = this.transpositionTable.entrySet().iterator();
		while(iterator.hasNext()){
			System.out.println(iterator.next().getValue().toString());
		}*/

			//System.out.println("TT size after cleaning: " + this.transpositionTable.size());
		//}
	}

	/*
	public int getLastGameStep(){
		return this.currentGameStepStamp;
	}*/

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "LOGGING = " + this.log +
				indentation + "TREE_DECAY = " + this.treeDecay +
				indentation + "AMAF_DECAY = " + this.amafDecay +
				indentation + "game_step_offset = " + this.gameStepOffset;
	}

	public void turnOffLogging(){
		this.log = false;
	}

}
