package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachinenew.AbstractStateMachine;

public class MASTJointMoveSelector implements JointMoveSelector {

	private AbstractStateMachine theMachine;

	private Random random;

	private Map<Move, MoveStats> mastStatistics;

	public MASTJointMoveSelector(AbstractStateMachine theMachine, Random random, Map<Move, MoveStats> mastStatistics) {
		this.theMachine = theMachine;
		this.random = random;
		this.mastStatistics = mastStatistics;
	}

	/**
	 * This method returns a joint move according to the MAST strategy.
	 * For each role it gets the list of all its legal moves in the state and picks the one with highest MAST expected score.
	 * @throws StateMachineException
	 */
	@Override
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();
        List<List<Move>> allLegalMoves;

        // Get a list containing for each player a lists of legal moves.
    	allLegalMoves = this.theMachine.getAllLegalMovesForAllRoles(state);

    	// For the list of legal moves of each player, pick the one with highest MAST value.
    	for(List<Move> moves : allLegalMoves){
    		jointMove.add(this.getMASTMove(moves));
    	}

		return jointMove;
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
	public String getJointMoveSelectorParameters() {
		return null;
	}

	@Override
	public String printJointMoveSelector() {
		String params = this.getJointMoveSelectorParameters();

		if(params != null){
			return "(JOINT_MOVE_SEL = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(JOINT_MOVE_SEL = " + this.getClass().getSimpleName() + ")";
		}
	}


}
