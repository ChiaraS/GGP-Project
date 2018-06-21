package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class MastMoveSelector extends MoveSelector {

	private List<Map<Move, MoveStats>> mastStatistics;

	/**
	 * First play urgency for a move never explored before.
	 * Note that this must be in the range [0,100] because MAST doesn't
	 * bother normalizing the move values (it's not necessary).
	 */
	private double mastFpu;

	public MastMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);


		this.mastFpu = gamerSettings.getDoublePropertyValue("MoveSelector.mastFpu");

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
	 * This method returns a joint move according to the MAST strategy.
	 * For each role it gets the list of all its legal moves in the state and picks the one with highest MAST expected score.
	 * @throws StateMachineException
	 */
/*	@Override
	public List<Move> getJointMove(MctsNode node, MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			jointMove.add(this.getMoveForRole(node, state, i));
		}

		//System.out.println(Arrays.toString(jointMove.toArray()));

		return jointMove;

	}
*/
	/**
	 * This method returns a move according to the MAST strategy.
	 * For the given role it gets the list of all its legal moves in the state
	 * and picks the one with highest MAST expected score.
	 *
	 * @throws MoveDefinitionException, StateMachineException
	 */
	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {

		List<Move> legalMoves;

		if(node != null && node instanceof DecoupledMctsNode) {
			legalMoves = ((DecoupledMctsNode)node).getLegalMovesForRole(roleIndex);
		}else {
			Role role = this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex);
			legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(state, role);
		}

		// Pick the move with highest MAST value.
		return this.getMastMove(roleIndex, legalMoves);
	}

	/**
	 * WARNING! Be very careful! MAST, as opposed to UCT, doesn't normalize value in [0, 1] to select
	 * the best move. It would be irrelevant to normalize since the choices of MAST are not influenced
	 * by the range of move values.
	 *
	 * @param moves
	 * @return
	 */
	private Move getMastMove(int roleIndex, List<Move> moves) {

		List<Move> chosenMoves = new ArrayList<Move>();
		MoveStats moveStats;
		double maxAvgScore = -1;
		double currentAvgScore;

		Move move;
		// For each legal move check the average score
		for(int moveIndex = 0; moveIndex < moves.size(); moveIndex++){

			move = moves.get(moveIndex);
			moveStats = this.mastStatistics.get(roleIndex).get(move);

			if(moveStats != null && moveStats.getVisits() != 0){
				currentAvgScore = moveStats.getScoreSum() / ((double) moveStats.getVisits());
			}else{
				currentAvgScore = this.mastFpu;
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
		String params = indentation + "MAST_FPU = " + this.mastFpu;

		if(this.mastStatistics != null){
			String mastStatisticsString = "[ ";

			for(Map<Move, MoveStats> roleMastStats : this.mastStatistics){
				mastStatisticsString += roleMastStats.size() + " entries, ";
			}

			mastStatisticsString += "]";

			params += indentation + "mast_statistics = " + mastStatisticsString;
		}else{
			params += indentation + "mast_statistics = null";
		}

		return params;

	}

}
