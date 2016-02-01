package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.InternalPropnetSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SUCTMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SUCTMCTSMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class UCTSelection implements SelectionStrategy {

	/**
	 * The total number of roles in the game.
	 * Needed by the SUCT version of MCTS.
	 */
	private int numRoles;

	/**
	 * The role that is actually performing the search.
	 * Needed by the SUCT version of MCTS.
	 */
	private InternalPropnetRole myRole;

	private Random random;

	private double uctOffset;

	private double c;

	public UCTSelection(int numRoles, InternalPropnetRole myRole, Random random, double uctOffset, double c) {
		this.numRoles = numRoles;
		this.myRole = myRole;
		this.random = random;
		this.uctOffset = uctOffset;
		this.c = c;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.SelectionStrategy#select(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode)
	 */
	@Override
	public MCTSJointMove select(InternalPropnetMCTSNode currentNode) {
		if(currentNode instanceof InternalPropnetDUCTMCTSNode){
			return this.select((InternalPropnetDUCTMCTSNode)currentNode);
		}else if(currentNode instanceof InternalPropnetSUCTMCTSNode){
			return this.select((InternalPropnetSUCTMCTSNode)currentNode);
		}else{
			throw new RuntimeException("UCTSelection-select(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	private MCTSJointMove select(InternalPropnetDUCTMCTSNode currentNode) {

		/* No need for this check, if the code is correct, because the node that is passed as input
		 * is always non-terminal.
		if(currentNode.isTerminal()){
			GamerLogger.logError("MCTSManager", "Trying to perform selection on a terminal node.");
			throw new RuntimeException("Trying to perform selection on a treminal node.");
		}
		*/

		DUCTMCTSMove[][] moves = currentNode.getMoves();

		/* Also here we can assume that the moves will be non-null since the code takes care of only passing to
		 * this method the nodes that have all the information needed for selection.
		if(moves == null){
			GamerLogger.logError("MCTSManager", "Trying to perform selection on a node with no legal moves.");
			throw new RuntimeException("Trying to perform selection on a node with no legal moves.");
		}
		*/

		List<InternalPropnetMove> selectedJointMove = new ArrayList<InternalPropnetMove>();
		int[] movesIndices = new int[moves.length];

		double maxUCTvalue;
		double UCTvalue;

		long nodeVisits = currentNode.getTotVisits();

		// For each role check the statistics and pick a move.
		for(int i = 0; i < moves.length; i++){

			// Compute UCT value for all moves.
			maxUCTvalue = -1;

			// For each legal move check the UCTvalue.
			for(int j = 0; j < moves[i].length; j++){

				// Compute the UCT value.
				UCTvalue = this.computeUCTvalue(moves[i][j].getScoreSum(), (double) moves[i][j].getVisits(), (double) nodeVisits);

				moves[i][j].setUct(UCTvalue);

				// If it's higher than the current maximum one, replace the max value.
				if(UCTvalue > maxUCTvalue){
					maxUCTvalue = UCTvalue;
				}
			}

			// Now that we have the maximum UCT value we can look for all moves that have their UCT value
			// in the interval [maxUCTvalue-offset, maxUCTvalue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int j = 0; j < moves[i].length; j++){
				if(moves[i][j].getUct() >= (maxUCTvalue-this.uctOffset)){
					selectedMovesIndices.add(new Integer(j));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("DUCT selection: detected no moves with UCT value higher than -1.");
			}

			/*
			if(selectedMovesIndices.size() < 1){
				System.out.println();
				System.out.println();
				System.out.println("!!!");
				System.out.println("Analyzing role " + i + ".");
				System.out.println("Moves for role: " + moves[i].length);
				System.out.println("MaxUCT: " + maxUCTvalue);
				System.out.println("UCTOffset: " + this.uctOffset);
				System.out.println("C constant: " + this.c);
				System.out.println("THE NODE:");
				System.out.println(currentNode);
				System.out.println();
				System.out.println();
			}
			*/

			movesIndices[i] = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			selectedJointMove.add(moves[i][movesIndices[i]].getTheMove());
		}

		return new DUCTMCTSJointMove(selectedJointMove, movesIndices);
	}

	private MCTSJointMove select(InternalPropnetSUCTMCTSNode currentNode){

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>(this.numRoles);

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.numRoles; i++){
			jointMove.add(null);
		}

		// Get the index of myRole.
		int roleIndex = this.myRole.getIndex();

		// Get the moves for myRole.
		SUCTMCTSMove[] moves = currentNode.getMoves();

		SUCTMCTSMove chosenMove = null;

		double maxUCTvalue;
		double UCTvalue;
		long nodeVisits = currentNode.getTotVisits();

		while(moves != null){

			// Compute UCT value for all moves.
			maxUCTvalue = -1;

			for(int i = 0; i < moves.length; i++){
				// Compute the UCT value.
				UCTvalue = this.computeUCTvalue(moves[i].getScoreSum(), (double) moves[i].getVisits(), (double) nodeVisits);

				moves[i].setUct(UCTvalue);

				// If it's higher than the current maximum one, replace the max value
				if(UCTvalue > maxUCTvalue){
					maxUCTvalue = UCTvalue;
				}
			}

			// Now that we have the maximum UCT value we can look for all moves that have their UCT value
			// in the interval [maxUCTvalue-offset, maxUCTvalue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int i = 0; i < moves.length; i++){
				if(moves[i].getUct() >= (maxUCTvalue-this.uctOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("SUCT selection: detected no moves with UCT value higher than -1.");
			}

			// Add one of the selected moves to the joint move.
			int selectedMoveIndex = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			chosenMove = moves[selectedMoveIndex];
			jointMove.set(roleIndex, chosenMove.getTheMove());

			// Get the move statistics of the next role, given the selected move.
			moves = chosenMove.getNextRoleMoves();

			// Compute the index for the next role
			roleIndex = (roleIndex+1)%this.numRoles;

		}

		return new SUCTMCTSJointMove(jointMove, chosenMove);

	}

	private double computeUCTvalue(double score, double moveVisits, double nodeVisits){

		// NOTE: this should never happen if we use this class together with the InternalPropnetMCTSManager
		// because the selection phase in a node starts only after all moves have been expanded and visited
		// at least once. However a check is performed to keep the computation consistent even when a move
		// has never been visited (i.e. the "infinite" value (Double.MAX_VALUE) is returned).
		if(moveVisits == 0){
			return Double.MAX_VALUE;
		}

		double avgScore = (score / moveVisits) / 100.0;
		double exploration = this.c * (Math.sqrt(Math.log(nodeVisits)/moveVisits));
		return avgScore + exploration;

	}

}