package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverMoveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSequDecMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledMCTSNode;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

public abstract class ProverMoveValueSelection implements ProverSelectionStrategy {

	/**
	 * The total number of roles in the game.
	 * Needed by the Sequential version of MCTS.
	 */
	//private int numRoles;

	/**
	 * The role that is actually performing the search.
	 * Needed by the Sequential version of MCTS.
	 */
	//private Role myRole;

	private Random random;

	private double valueOffset;

	protected ProverMoveEvaluator moveEvaluator;

	public ProverMoveValueSelection(int numRoles, ProverRole myRole, Random random, double valueOffset, ProverMoveEvaluator moveEvaluator) {
		//this.numRoles = numRoles;
		//this.myRole = myRole;
		this.random = random;
		this.valueOffset = valueOffset;
		this.moveEvaluator = moveEvaluator;
	}

	@Override
	public ProverMCTSJointMove select(MCTSNode currentNode) {
		if(currentNode instanceof ProverDecoupledMCTSNode){
			return this.decSelect((ProverDecoupledMCTSNode)currentNode);
		}/*else if(currentNode instanceof PnSequentialMCTSNode){
			return this.seqSelect((PnSequentialMCTSNode)currentNode);
		}else if(currentNode instanceof PnSlowSeqentialMCTSNode){
			return this.sseqSelect((PnSlowSeqentialMCTSNode)currentNode);
		}*/else{
			throw new RuntimeException("MoveValueSelection-select(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	private ProverMCTSJointMove decSelect(ProverDecoupledMCTSNode currentNode) {

		//System.out.println("decSelect");

		/* No need for this check, if the code is correct, because the node that is passed as input
		 * is always non-terminal.
		if(currentNode.isTerminal()){
			GamerLogger.logError("MCTSManager", "Trying to perform selection on a terminal node.");
			throw new RuntimeException("Trying to perform selection on a treminal node.");
		}
		*/

		ProverDecoupledMCTSMoveStats[][] moves = currentNode.getMoves();

		/* Also here we can assume that the moves will be non-null since the code takes care of only passing to
		 * this method the nodes that have all the information needed for selection.
		if(moves == null){
			GamerLogger.logError("MCTSManager", "Trying to perform selection on a node with no legal moves.");
			throw new RuntimeException("Trying to perform selection on a node with no legal moves.");
		}
		*/

		List<ProverMove> selectedJointMove = new ArrayList<ProverMove>();
		int[] movesIndices = new int[moves.length];

		double maxMoveValue;
		double[] moveValues;

		//int nodeVisits = currentNode.getTotVisits();

		// For each role check the statistics and pick a move.
		for(int i = 0; i < moves.length; i++){

			// Compute move value for all moves.
			maxMoveValue = -1;
			moveValues = new double[moves[i].length];

			// For each legal move check the moveValue.
			for(int j = 0; j < moves[i].length; j++){

				// Compute the move value.
				moveValues[j] = this.moveEvaluator.computeMoveValue(currentNode.getTotVisits(), moves[i][j].getTheMove(), moves[i][j]);

				// If it's higher than the current maximum one, replace the max value.
				if(moveValues[j] > maxMoveValue){
					maxMoveValue = moveValues[j];
				}
			}

			// Now that we have the maximum move value we can look for all moves that have their value
			// in the interval [maxMoveValue-valueOffset, maxMoveValue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int j = 0; j < moveValues.length; j++){
				if(moveValues[j] >= (maxMoveValue-this.valueOffset)){
					selectedMovesIndices.add(new Integer(j));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("Decoupled selection: detected no moves with value higher than -1.");
			}

			/*
			if(selectedMovesIndices.size() < 1){
				System.out.println();
				System.out.println();
				System.out.println("!!!");
				System.out.println("Analyzing role " + i + ".");
				System.out.println("Moves for role: " + moves[i].length);
				System.out.println("MaxValue: " + maxMoveValue);
				System.out.println("Value offset: " + this.valueOffset);
				System.out.println("THE NODE:");
				System.out.println(currentNode);
				System.out.println();
				System.out.println();
			}
			*/

			movesIndices[i] = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			selectedJointMove.add(moves[i][movesIndices[i]].getTheMove());
		}

		return new ProverSequDecMCTSJointMove(selectedJointMove, movesIndices);
	}


	/*private MCTSJointMove seqSelect(PnSequentialMCTSNode currentNode){

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>(this.numRoles);
		int[] movesIndices = new int[this.numRoles];

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.numRoles; i++){
			jointMove.add(null);
		}

		// Get the index of myRole.
		int roleIndex = this.myRole.getIndex();

		// Get the moves for myRole.
		SequentialMCTSMoveStats[] movesStats = currentNode.getMovesStats();

		double maxMoveValue;
		double[] moveValues;
		//int nodeVisits = currentNode.getTotVisits();

		while(movesStats != null){

			// Compute the value for all moves.
			maxMoveValue = -1;
			moveValues = new double[movesStats.length];

			for(int i = 0; i < movesStats.length; i++){
				// Compute the move value.
				moveValues[i] = this.moveEvaluator.computeMoveValue(currentNode.getTotVisits(), currentNode.getAllLegalMoves().get(roleIndex).get(i), movesStats[i]);

				// If it's higher than the current maximum one, replace the max value
				if(moveValues[i] > maxMoveValue){
					maxMoveValue = moveValues[i];
				}
			}

			// Now that we have the maximum move value we can look for all the moves that have their value
			// in the interval [maxMoveValue-valueOffset, maxMoveValue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int i = 0; i < moveValues.length; i++){
				if(moveValues[i] >= (maxMoveValue-this.valueOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("Sequential selection: detected no moves with value higher than -1.");
			}

			// Add one of the selected moves to the joint move.
			movesIndices[roleIndex] = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			jointMove.set(roleIndex, currentNode.getAllLegalMoves().get(roleIndex).get(movesIndices[roleIndex]));

			// Get the move statistics of the next role, given the selected move.
			movesStats = movesStats[movesIndices[roleIndex]].getNextRoleMovesStats();

			// Compute the index for the next role
			roleIndex = (roleIndex+1)%this.numRoles;

		}

		return new SequDecMCTSJointMove(jointMove, movesIndices);

	}*/

	/*
	private MCTSJointMove sseqSelect(PnSlowSeqentialMCTSNode currentNode){

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>(this.numRoles);

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.numRoles; i++){
			jointMove.add(null);
		}

		// Get the index of myRole.
		int roleIndex = this.myRole.getIndex();

		// Get the moves for myRole.
		SlowSequentialMCTSMoveStats[] movesStats = currentNode.getMovesStats();

		SlowSequentialMCTSMoveStats chosenMove = null;

		double maxMoveValue;
		double moveValues[];
		//int nodeVisits = currentNode.getTotVisits();

		while(movesStats != null){

			// Compute the value for all moves.
			maxMoveValue = -1;
			moveValues = new double[movesStats.length];

			for(int i = 0; i < movesStats.length; i++){
				// Compute the move value.
				moveValues[i] = this.moveEvaluator.computeMoveValue(currentNode.getTotVisits(), movesStats[i].getTheMove(), movesStats[i]);

				// If it's higher than the current maximum one, replace the max value
				if(moveValues[i] > maxMoveValue){
					maxMoveValue = moveValues[i];
				}
			}

			// Now that we have the maximum move value we can look for all the moves that have their value
			// in the interval [maxMoveValue-valueOffset, maxMoveValue].
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int i = 0; i < moveValues.length; i++){
				if(moveValues[i] >= (maxMoveValue-this.valueOffset)){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			// Extra check (should never be true).
			if(selectedMovesIndices.isEmpty()){
				throw new RuntimeException("Slow sequential selection: detected no moves with value higher than -1.");
			}

			// Add one of the selected moves to the joint move.
			int selectedMoveIndex = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			chosenMove = movesStats[selectedMoveIndex];
			jointMove.set(roleIndex, chosenMove.getTheMove());

			// Get the move statistics of the next role, given the selected move.
			movesStats = chosenMove.getNextRoleMovesStats();

			// Compute the index for the next role
			roleIndex = (roleIndex+1)%this.numRoles;

		}

		return new SlowSequentialMCTSJointMove(jointMove, chosenMove);

	}*/

	//public abstract double computeMoveValue(PnMCTSNode theNode, InternalPropnetMove theMove, MoveStats theMoveStats);

	@Override
	public String getStrategyParameters(){

		return "VALUE_OFFSET = " + this.valueOffset + ", " + this.moveEvaluator.printEvaluator();
	}

	@Override
	public String printStrategy(){
		String params = this.getStrategyParameters();

		if(params != null){
			return "[SELECTION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[SELECTION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
