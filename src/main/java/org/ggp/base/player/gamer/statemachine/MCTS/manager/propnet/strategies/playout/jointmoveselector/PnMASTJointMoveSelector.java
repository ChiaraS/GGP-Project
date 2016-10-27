package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnMASTJointMoveSelector implements PnJointMoveSelector{

	private InternalPropnetStateMachine theMachine;

	private Random random;

	private Map<CompactMove, MoveStats> mastStatistics;

	public PnMASTJointMoveSelector(InternalPropnetStateMachine theMachine, Random random, Map<CompactMove, MoveStats> mastStatistics) {
		this.theMachine = theMachine;
		this.random = random;
		this.mastStatistics = mastStatistics;
	}

	@Override
	public List<CompactMove> getJointMove(CompactMachineState state) throws MoveDefinitionException {

		List<CompactMove> jointMove = new ArrayList<CompactMove>();
        List<List<CompactMove>> allLegalMoves;

    	allLegalMoves = this.theMachine.getAllLegalMoves(state);

    	for(List<CompactMove> moves : allLegalMoves){
    		jointMove.add(this.getMASTMove(moves));
    	}

		return jointMove;
	}

	private CompactMove getMASTMove(List<CompactMove> moves) {

		List<CompactMove> chosenMoves = new ArrayList<CompactMove>();
		MoveStats moveStats;
		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(CompactMove move : moves){

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
