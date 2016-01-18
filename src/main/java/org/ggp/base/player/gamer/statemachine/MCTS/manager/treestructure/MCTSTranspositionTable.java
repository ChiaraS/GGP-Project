/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

/**
 * @author C.Sironi
 *
 */
public class MCTSTranspositionTable {

	private int currentGameStepStamp;

	private int gameStepOffset;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private Map<InternalPropnetMachineState,InternalPropnetMCTreeNode> transpositionTable;

	/**
	 *
	 */
	public MCTSTranspositionTable(int gameStepOffset){
		this.transpositionTable = new HashMap<InternalPropnetMachineState,InternalPropnetMCTreeNode>();
		this.gameStepOffset = gameStepOffset;
	}

	public InternalPropnetMCTreeNode getNode(InternalPropnetMachineState state){
		InternalPropnetMCTreeNode node = this.transpositionTable.get(state);
		if(node != null){
			node.setGameStepStamp(this.currentGameStepStamp);
		}
		return node;
	}

	public void putNode(InternalPropnetMachineState state, InternalPropnetMCTreeNode node){
		if(node != null){
			this.transpositionTable.put(state, node);
			node.setGameStepStamp(this.currentGameStepStamp);
		}
	}

	public void clean(int newGameStepStamp){

		//System.out.println("Current TT game step: " + newGameStepStamp);
		//System.out.println("Cleaning TT with game step: " + newGameStepStamp);
		//System.out.println("Current TT size: " + this.transpositionTable.size());

		// Clean the table only if the game-step stamp changed.
		if(newGameStepStamp != this.currentGameStepStamp){
			this.currentGameStepStamp = newGameStepStamp;
			// Remove all nodes last accessed earlier than the game step (newGameStepStamp-gameStepOffset)
			Iterator<Entry<InternalPropnetMachineState,InternalPropnetMCTreeNode>> iterator = this.transpositionTable.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<InternalPropnetMachineState,InternalPropnetMCTreeNode> entry = iterator.next();
				if(entry.getValue().getGameStepStamp() < (this.currentGameStepStamp-this.gameStepOffset)){
					iterator.remove();
				}
			}

			//System.out.println("TT size after cleaning: " + this.transpositionTable.size());
		}
	}

}
