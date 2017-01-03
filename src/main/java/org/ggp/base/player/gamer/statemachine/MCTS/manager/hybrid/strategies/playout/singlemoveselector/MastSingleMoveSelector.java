package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MastSingleMoveSelector extends SingleMoveSelector {

	private Map<Move, MoveStats> mastStatistics;

	public MastSingleMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastStatistics = sharedReferencesCollector.getMastStatistics();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
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
        List<Move> legalMovesForRole = this.gameDependentParameters.getTheMachine().getLegalMoves(state, this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex));

    	// Pick the move with highest MAST value.
		return this.getMastMove(legalMovesForRole);
	}

	private Move getMastMove(List<Move> moves) {

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
	public String getComponentParameters(String indentation) {
		return null;
	}

}
