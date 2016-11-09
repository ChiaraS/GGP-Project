package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MASTSingleMoveSelector implements SingleMoveSelector {

	private AbstractStateMachine theMachine;

	private Random random;

	private Map<Move, MoveStats> mastStatistics;

	public MASTSingleMoveSelector(AbstractStateMachine theMachine, Random random, Map<Move, MoveStats> mastStatistics) {
		this.theMachine = theMachine;
		this.random = random;
		this.mastStatistics = mastStatistics;
	}



	/**
	 * This method returns a move according to the MAST strategy.
	 * For the given role it gets the list of all its legal moves in the state
	 * and picks the one with highest MAST expected score.
	 *
	 * @throws MoveDefinitionException, StateMachineException
	 */
	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {

		// Get the list of all legal moves for the rle in the state
        List<Move> legalMovesForRole = this.theMachine.getLegalMoves(state, this.theMachine.getRoles().get(roleIndex));

    	// Pick the move with highest MAST value.
		return this.getMASTMove(legalMovesForRole);
	}

	private Move getMASTMove(List<Move> moves) {

		List<Move> chosenMoves = new ArrayList<Move>();
		MoveStats moveStats;
		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(Move move : moves){

			moveStats = this.mastStatistics.get(move);

			if(moveStats != null && moveStats.getVisits() != 0){
				currentAvgScore = moveStats.getScoreSum() / ((double) moveStats.getVisits());
			}else{
				currentAvgScore = 100;
			}

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentAvgScore > maxAvgScore){
				maxAvgScore = currentAvgScore;
				chosenMoves.clear();
				chosenMoves.add(move);
			}else if(currentAvgScore == maxAvgScore){
				chosenMoves.add(move);
			}
		}

		return chosenMoves.get(this.random.nextInt(chosenMoves.size()));

	}

	@Override
	public String getSingleMoveSelectorParameters() {
		return null;
	}

	@Override
	public String printSingleMoveSelector() {
		String params = this.getSingleMoveSelectorParameters();

		if(params != null){
			return "(SINGLE_MOVE_SEL = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(SINGLE_MOVE_SEL = " + this.getClass().getSimpleName() + ")";
		}
	}

}
