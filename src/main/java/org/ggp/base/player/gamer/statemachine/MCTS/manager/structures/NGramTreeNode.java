package org.ggp.base.player.gamer.statemachine.MCTS.manager.structures;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class is used to represent a node in the tree that represents N-grams of
 * moves. Each node corresponds to a move, and memorizes the statistics of the
 * n-gram of moves that go form the root to such node. Each node also has a reference
 * to the statistics of n-grams of length+1 (i.e. n-grams formed by the n-gram
 * corresponding to the node plus any of the possible moves that can be added to
 * the n-gram increasing its length by one). If the node corresponds to an n-gram
 * of maximum feasible length, the reference to the next moves will be null.
 *
 * @author C.Sironi
 *
 * @param <E> The type of statistics used in the n-grams tree.
 */
public class NGramTreeNode<E> {

	/**
	 * The MAST or PPA statistics for the N-gram formed by the sequence of moves
	 * from the root to the current node.
	 */
	private E statistic;

	/**
	 * All the possible moves that can be added to the n-gram with their role index.
	 */
	private Map<Move,NGramTreeNode<E>> nextMoveNodes;
	//private Map<MyPair<Integer,Move>,NGramTreeNode<E>> nextMoveNodes;

	public NGramTreeNode(E statistic){
		this.statistic = statistic;
		this.nextMoveNodes = new HashMap<Move,NGramTreeNode<E>>();
		//this.nextMoveNodes = new HashMap<MyPair<Integer,Move>,NGramTreeNode<E>>();
	}

	public E getStatistic(){
		return this.statistic;
	}

	public void addNextMoveNode(Move move, NGramTreeNode<E> nextMoveNode){
		this.nextMoveNodes.put(move, nextMoveNode);
	}

	//public void addNextMoveNode(int roleIndex, Move move, NGramTreeNode<E> nextMoveNode){
	//	this.nextMoveNodes.put(new MyPair<Integer,Move>(new Integer(roleIndex),move), nextMoveNode);
	//}

	public NGramTreeNode<E> getNextMoveNode(Move move){
		return this.nextMoveNodes.get(move);
	}

	//public NGramTreeNode<E> getNextMoveNode(int roleIndex, Move move){
	//	return this.nextMoveNodes.get(new MyPair<Integer,Move>(new Integer(roleIndex),move));
	//}

	public Map<Move,NGramTreeNode<E>> getNextMoveNodes(){
		return this.nextMoveNodes;
	}

	//public Map<MyPair<Integer,Move>,NGramTreeNode<E>> getNextMoveNodes(){
	//	return this.nextMoveNodes;
	//}

	public void clear(){
		this.statistic = null;
		this.nextMoveNodes.clear();
	}

	public boolean hasNoChildren(){
		return this.nextMoveNodes.isEmpty();
	}

}
