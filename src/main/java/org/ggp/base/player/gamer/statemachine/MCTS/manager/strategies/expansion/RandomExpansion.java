package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class RandomExpansion implements ExpansionStrategy {

	private Random random;

	public RandomExpansion(Random random){
		this.random = random;
	}

	@Override
	public boolean expansionRequired(InternalPropnetDUCTMCTreeNode node) {
		return (!(node.getUnvisitedJointMoves().isEmpty()));
	}

	@Override
	public List<InternalPropnetMove> expand(InternalPropnetDUCTMCTreeNode node) {

		List<List<InternalPropnetMove>> unvisitedJointMoves = node.getUnvisitedJointMoves();

		return unvisitedJointMoves.remove(this.random.nextInt(unvisitedJointMoves.size()));
	}

}
