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
public class DUCTTranspositionTable {

	private int currentGameStepStamp;

	private int gameStepOffset;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private Map<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode> transpositionTable;

	/**
	 *
	 */
	public DUCTTranspositionTable(int gameStepOffset){
		this.transpositionTable = new HashMap<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode>();
		this.gameStepOffset = gameStepOffset;
	}

	public InternalPropnetDUCTMCTreeNode getNode(InternalPropnetMachineState state){
		InternalPropnetDUCTMCTreeNode node = this.transpositionTable.get(state);
		if(node != null){
			node.setGameStepStamp(this.currentGameStepStamp);
		}
		return node;
	}

	public void putNode(InternalPropnetMachineState state, InternalPropnetDUCTMCTreeNode node){
		if(node != null){
			this.transpositionTable.put(state, node);
			node.setGameStepStamp(this.currentGameStepStamp);
		}
	}

	public void clean(int newGameStepStamp){
		this.currentGameStepStamp = newGameStepStamp;
		// Remove all nodes last accessed earlier than the game step (newGameStepStamp-GameStepOffset)
		Iterator<Entry<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode>> iterator = this.transpositionTable.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<InternalPropnetMachineState,InternalPropnetDUCTMCTreeNode> entry = iterator.next();
			if(entry.getValue().getGameStepStamp() < (this.currentGameStepStamp-this.gameStepOffset)){
				iterator.remove();
			}
		}
	}

}
