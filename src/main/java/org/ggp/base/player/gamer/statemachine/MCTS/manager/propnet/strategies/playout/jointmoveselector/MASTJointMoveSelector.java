package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MASTJointMoveSelector implements JointMoveSelector {

	private InternalPropnetStateMachine theMachine;

	private Random random;

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

	public MASTJointMoveSelector(InternalPropnetStateMachine theMachine, Random random, Map<InternalPropnetMove, MoveStats> mastStatistics) {
		this.theMachine = theMachine;
		this.random = random;
		this.mastStatistics = mastStatistics;
	}

	@Override
	public List<InternalPropnetMove> getJointMove(
			InternalPropnetMachineState state) throws MoveDefinitionException {

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>();
        List<List<InternalPropnetMove>> allLegalMoves;

    	allLegalMoves = this.theMachine.getAllLegalMoves(state);

    	for(List<InternalPropnetMove> moves : allLegalMoves){
    		jointMove.add(this.getMASTMove(moves));
    	}

		return jointMove;
	}

	private InternalPropnetMove getMASTMove(List<InternalPropnetMove> moves) {

		List<InternalPropnetMove> chosenMoves = new ArrayList<InternalPropnetMove>();
		MoveStats moveStats;
		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(InternalPropnetMove move : moves){

			moveStats = this.mastStatistics.get(move);

			if(moveStats != null && moveStats.getVisits() != 0){
				currentAvgScore = ((double) moveStats.getScoreSum()) / ((double) moveStats.getVisits());
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
