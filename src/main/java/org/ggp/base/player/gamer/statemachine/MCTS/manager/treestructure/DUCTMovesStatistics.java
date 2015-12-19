package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.List;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class DUCTMovesStatistics{

	private List<InternalPropnetMove> moves;

	private int[] visits;

	private int[] scores;

	// TODO: add a memorization of the top n moves (e.g. the n moves with uct_score = max_uct_score or the n moves
	// with uct_score in [max_uct_score-epsilon, max_uct_score]) so that at each iteration you don't have to check
	// the value of all actions but just update this list every time you do back-propagation.
	// private List<Integer> topMoveIndices;

	public DUCTMovesStatistics(List<InternalPropnetMove> moves) {
		this.moves = moves;
		this.visits = new int[this.moves.size()];
		this.scores = new int[this.moves.size()];
	}

	public List<InternalPropnetMove> getLegalMoves(){
		return this.moves;
	}

	public int[] getVisits(){
		return this.visits;
	}

	public int[] getScores(){
		return this.scores;
	}

}
