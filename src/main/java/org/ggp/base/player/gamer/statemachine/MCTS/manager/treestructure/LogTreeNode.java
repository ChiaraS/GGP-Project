package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.HashMap;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;

import csironi.ggp.course.utils.MyPair;

public class LogTreeNode {

	/**
	 * Game step in which this node was added to the tree.
	 */
	//private int step;

	/**
	 * Index that keeps track of the order in which nodes are added to the tree.
	 * Corresponds to the number of iterations performed until this node was added.
	 * E.g. if insertionIteration=4 it means that this node was added after 4 MCTS
	 * iterations since the start of the game, thus during the 5th iteration.
	 */
	private int insertionIteration;

	/**
	 * True if this node is part of the path of actions played during the real game, false otherwise.
	 */
	//private boolean onPath;
	/**
	 * This value is set to the step at the end of which the action leading to this node was selected.
	 * If this is set to 0 it means that the action was never selected.
	 */
	 private int selectionStep;

	/**
	 * Children nodes with joint actions as keys.
	 */
	private HashMap<MctsJointMove,LogTreeNode> children;

	/**
	 * Coordinates [x, y] of the node when plotting.
	 */
	private MyPair<Double,Double> coordinates;

	/**
	 * Coordinates [x, y] of the parent node needed when plotting.
	 */
	private MyPair<Double,Double> parentCoordinates;

	public LogTreeNode(int insertionOrder) {
		super();
		//this.step = step;
		this.insertionIteration = insertionOrder;
		//this.onPath = false;
		this.selectionStep = -1;
		this.children = new HashMap<MctsJointMove,LogTreeNode>();
		this.coordinates = null;
	}

	//public int getStep() {
	//	return step;
	//}

	public int getInsertionIteration() {
		return insertionIteration;
	}

	public boolean isOnPath() {
		//return onPath;
		return this.selectionStep > -1;
	}

	public void setOnPath(int selectionStep) {
		//this.onPath = true;
		this.selectionStep = selectionStep;
	}

	public int getSelectionStep() {
		//this.onPath = true;
		return this.selectionStep;
	}

	public HashMap<MctsJointMove, LogTreeNode> getChildren() {
		return children;
	}

	public void addChild(MctsJointMove move, LogTreeNode child) {
		this.children.put(move, child);
	}

	public LogTreeNode getChild(MctsJointMove move) {
		return this.children.get(move);
	}

	public MyPair<Double, Double> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(MyPair<Double, Double> coordinates) {
		this.coordinates = coordinates;
	}

	public MyPair<Double, Double> getParentCoordinates() {
		return this.parentCoordinates;
	}

	public void setParentCoordinates(MyPair<Double, Double> coordinates) {
		this.parentCoordinates = coordinates;
	}

	@Override
	public String toString(){
		return "[ " + this.insertionIteration + ", " + this.selectionStep + " ]";
	}

}
