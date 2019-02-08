package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.HashMap;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;

import csironi.ggp.course.utils.MyPair;

public class LogTreeNode {

	/**
	 * Game step in which this node was added to the tree.
	 */
	private int step;

	/**
	 * Index that keeps track of the order in which nodes are added to the tree.
	 * Corresponds to the number of simulations performed until this node was added.
	 * E.g. if insertionOrder=4 it means that this node was added after 4 MCTS
	 * simulations since the start of the game, thus during the 5th simulation.
	 */
	private int insertionOrder;

	/**
	 * True if this node is part of the path of actions played during the real game, false otherwise.
	 */
	private boolean onPath;

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

	public LogTreeNode(int step, int insertionOrder) {
		super();
		this.step = step;
		this.insertionOrder = insertionOrder;
		this.onPath = false;
		this.children = new HashMap<MctsJointMove,LogTreeNode>();
		this.coordinates = null;
	}

	public int getStep() {
		return step;
	}

	public int getInsertionOrder() {
		return insertionOrder;
	}

	public boolean isOnPath() {
		return onPath;
	}

	public void setOnPath() {
		this.onPath = true;
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
		return "[ " + this.step + ", " + this.insertionOrder + " ]";
	}

}
