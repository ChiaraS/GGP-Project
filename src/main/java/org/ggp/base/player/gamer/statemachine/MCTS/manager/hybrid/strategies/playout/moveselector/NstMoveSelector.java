package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * NOTE: the play-out supported selection should work also when using NST in the playout,
 * because the list of joint moves kept by the MCTS manager should still be updated correctly.
 * However, test this before using the playout supported selection with NST.
 *
 * @author C.Sironi
 *
 */
public abstract class NstMoveSelector extends MoveSelector {

	/**
	 * List with all the joint moves selected so far in the current simulation.
	 */
	protected List<MctsJointMove> currentSimulationJointMoves;

	/**
	 * ATTENTION: the correct functioning of NST is based on the assumption that
	 * the roles in the GDL file are specified in the same order in which they
	 * alternate their turns in sequential-move games;
	 */
	protected List<NGramTreeNode<MoveStats>> nstStatistics;

	protected double nstFpu;

	protected int minNGramVisits;

	public NstMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.nstFpu = gamerSettings.getDoublePropertyValue("MoveSelector.nstFpu");

		this.minNGramVisits = gamerSettings.getIntPropertyValue("MoveSelector.minNGramVisits");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.nstStatistics = sharedReferencesCollector.getNstStatistics();

		this.currentSimulationJointMoves = sharedReferencesCollector.getCurrentSimulationJointMoves();

	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

	}

	protected double computeNstValue(int roleIndex, Move move){

		//System.out.println("Computing value for move " + move);

		// Find the n-gram tree node for the move of the role
		NGramTreeNode<MoveStats> nGramTreeNode = this.nstStatistics.get(roleIndex).getNextMoveNode(move);

		// If the move has never been visited, return the FPU
		if(nGramTreeNode == null || nGramTreeNode.getStatistic().getVisits() == 0){
			//System.out.println("FPU value " + this.nstFpu);
			return this.nstFpu;
		}

		// If the move has been visited at last once, average the value of the
		// 1-gram with the value of all the n-grams (n > 1) ending with the move
		// that have been visited at least minNGramVisits times.
		double nGramSum = nGramTreeNode.getStatistic().getScoreSum() / nGramTreeNode.getStatistic().getVisits();
		double numNGrams = 1;

		//System.out.println("1 gram = " + nGramSum);

		// Prepare to sum the 2-gram
		int currentRoleIndex = (roleIndex + this.gameDependentParameters.getNumRoles() - 1)%this.gameDependentParameters.getNumRoles();
		int currentNGramLength = 2;
		int currentMoveIndex = this.currentSimulationJointMoves.size()-currentNGramLength+1; // Must be >= 0, because in the selection phase we must have visited at least 1 node (makes no sense to start a simulation from the root).
		Move currentMove = this.currentSimulationJointMoves.get(currentMoveIndex).getJointMove().get(currentRoleIndex);
		nGramTreeNode = nGramTreeNode.getNextMoveNode(currentMove);

		while(nGramTreeNode != null && nGramTreeNode.getStatistic().getVisits() >= this.minNGramVisits){ // If the m-gram does not have enough visits we can stop because the (m+1)-gram will also not have enough visits

			// Sum the score of the n-gram
			nGramSum += (nGramTreeNode.getStatistic().getScoreSum() / nGramTreeNode.getStatistic().getVisits());
			numNGrams++;

			//System.out.println(numNGrams + " gram move = " + currentMove);
			//System.out.println(numNGrams + " gram = " + nGramSum);

			// Update all variables
			currentRoleIndex = (currentRoleIndex + this.gameDependentParameters.getNumRoles() - 1)%this.gameDependentParameters.getNumRoles();
			currentNGramLength++;
			currentMoveIndex = this.currentSimulationJointMoves.size()-currentNGramLength+1;
			if(currentMoveIndex >= 0){
				currentMove = this.currentSimulationJointMoves.get(currentMoveIndex).getJointMove().get(currentRoleIndex);
				nGramTreeNode = nGramTreeNode.getNextMoveNode(currentMove);
			}else{
				nGramTreeNode = null;
			}
		}

		return nGramSum/numNGrams;

	}

	@Override
	public String getComponentParameters(String indentation) {
		String nstStatisticsString;

		if(this.nstStatistics != null){
			nstStatisticsString = "[ ";

			int roleIndex = 0;
			for(NGramTreeNode<MoveStats> roleNstStats : this.nstStatistics){
				nstStatisticsString += (roleNstStats == null ? "null " : "Tree" + roleIndex + " ");
				roleIndex++;
			}

			nstStatisticsString += "]";

		}else{
			nstStatisticsString = "null";
		}

		return indentation + "MIN_N_GRAM_VISITS" + this.minNGramVisits +
				indentation + "NST_FPU = " + this.nstFpu +
				indentation + "nst_statistics = " + nstStatisticsString;
	}


}
