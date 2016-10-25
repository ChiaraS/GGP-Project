/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled.PnAMAFDecoupledMCTSNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

/**
 * @author C.Sironi
 *
 */
public class PnMCTSTranspositionTable {

	private boolean log;

	private int currentGameStepStamp;

	private int gameStepOffset;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private Map<InternalPropnetMachineState,MCTSNode> transpositionTable;

	/**
	 *
	 */
	public PnMCTSTranspositionTable(int gameStepOffset){

		this(gameStepOffset, false);

	}

	/**
	 *
	 */
	public PnMCTSTranspositionTable(int gameStepOffset, boolean log){
		this.log = log;
		this.currentGameStepStamp = 1;
		this.transpositionTable = new HashMap<InternalPropnetMachineState,MCTSNode>();
		this.gameStepOffset = gameStepOffset;

		if(this.log){
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", "Step;Start/End;#Nodes;#ActionsStats;#RAVE_AMAFStats;#GRAVE_AMAFStats;ActionsStats/Node;RAVE_AMAFStats/Node;GRAVE_AMAFStats/Node;");
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", "1;Start;0;0;0;0;0;0;0;");
		}
	}

	public MCTSNode getNode(InternalPropnetMachineState state){
		MCTSNode node = this.transpositionTable.get(state);
		if(node != null){
			//System.out.println("Found");
			node.setGameStepStamp(this.currentGameStepStamp);
		}/*else{
			System.out.println("Not found");
		}*/
		return node;
	}

	public void putNode(InternalPropnetMachineState state, MCTSNode node){
		if(node != null){
			this.transpositionTable.put(state, node);
			node.setGameStepStamp(this.currentGameStepStamp);
		}
	}

	public void clean(int newGameStepStamp){

		if(this.log){

			int stepBeforeCleaning = this.currentGameStepStamp;
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

			// Clean the table only if the game-step stamp changed (this is already checked by the caller).
			//if(newGameStepStamp != this.currentGameStepStamp){
			this.currentGameStepStamp = newGameStepStamp;
			// Remove all nodes last accessed earlier than the game step (newGameStepStamp-gameStepOffset)
			Iterator<Entry<InternalPropnetMachineState,MCTSNode>> iterator = this.transpositionTable.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<InternalPropnetMachineState,MCTSNode> entry = iterator.next();

				if(entry.getValue() instanceof PnAMAFDecoupledMCTSNode){
					int actionsStats = ((PnAMAFDecoupledMCTSNode) entry.getValue()).getActionsStatsNumber();
					int raveAmaf = ((PnAMAFDecoupledMCTSNode) entry.getValue()).getRaveAMAFStatsNumber();
					int graveAmaf = ((PnAMAFDecoupledMCTSNode) entry.getValue()).getGraveAMAFStatsNumber();

					actionsStatsBeforeCleaning += actionsStats;
					raveAmafBeforeCleaning += raveAmaf;
					graveAmafBeforeCleaning += graveAmaf;

					if(entry.getValue().getGameStepStamp() < (this.currentGameStepStamp-this.gameStepOffset)){
						iterator.remove();
					}else{
						actionsStatsAfterCleaning += actionsStats;
						raveAmafAfterCleaning += raveAmaf;
						graveAmafAfterCleaning += graveAmaf;
					}
				}else{
					if(entry.getValue().getGameStepStamp() < (this.currentGameStepStamp-this.gameStepOffset)){
						iterator.remove();
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

			int stepAfterCleaning = this.currentGameStepStamp;
			int sizeAfterCleaning = this.transpositionTable.size();

			actionsStatsPerNode = ((double) actionsStatsAfterCleaning) / ((double) sizeAfterCleaning);
			raveAmafPerNode = ((double) raveAmafAfterCleaning) / ((double) sizeAfterCleaning);
			graveAmafPerNode = ((double) graveAmafAfterCleaning) / ((double) sizeAfterCleaning);

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreeSizeStatistics", stepAfterCleaning +
					";Start;" + sizeAfterCleaning + ";" + actionsStatsAfterCleaning + ";" +
					raveAmafAfterCleaning + ";" + graveAmafAfterCleaning + ";" + actionsStatsPerNode + ";" +
					raveAmafPerNode + ";" + graveAmafPerNode + ";");

		}else{

			this.currentGameStepStamp = newGameStepStamp;
			// Remove all nodes last accessed earlier than the game step (newGameStepStamp-gameStepOffset)
			Iterator<Entry<InternalPropnetMachineState,MCTSNode>> iterator = this.transpositionTable.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<InternalPropnetMachineState,MCTSNode> entry = iterator.next();

				if(entry.getValue().getGameStepStamp() < (this.currentGameStepStamp-this.gameStepOffset)){
					iterator.remove();
				}
			}

		}

			//System.out.println("TT size after cleaning: " + this.transpositionTable.size());
		//}
	}

	public int getLastGameStep(){
		return this.currentGameStepStamp;
	}

}
