package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaInfo;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class NppaMoveSelector extends MoveSelector {

	/**
	 * List with all the joint moves selected so far in the current simulation.
	 */
	protected List<MctsJointMove> currentSimulationJointMoves;

	/**
	 * ATTENTION: the correct functioning of NPPA is based on the assumption that
	 * the roles in the GDL file are specified in the same order in which they
	 * alternate their turns in sequential-move games;
	 */
	protected List<NGramTreeNode<PpaInfo>> nppaStatistics;

	protected int minNGramVisits;

	private double nppaFpu;

	public NppaMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.minNGramVisits = gamerSettings.getIntPropertyValue("MoveSelector.minNGramVisits");

		this.nppaFpu = gamerSettings.getDoublePropertyValue("MoveSelector.nppaFpu");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.nppaStatistics = sharedReferencesCollector.getNppaStatistics();

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

	protected double computeNppaValue(int roleIndex, Move move){

		//System.out.println("Computing value for move " + this.gameDependentParameters.getTheMachine().convertToExplicitMove(move));

		// Find the n-gram tree node for the move of the role
		NGramTreeNode<PpaInfo> nGramTreeNode = this.nppaStatistics.get(roleIndex).getNextMoveNode(move);

		// If the move has never been visited, return the FPU
		if(nGramTreeNode == null || nGramTreeNode.getStatistic().getVisits() == 0){
			//System.out.println("FPU value " + this.nppaFpu);
			return this.nppaFpu;
		}

		// If the move has been visited at last once, average the value of the
		// 1-gram with the value of all the n-grams (n > 1) ending with the move
		// that have been visited at least minNGramVisits times.
		double nGramSum = nGramTreeNode.getStatistic().getWeight();
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
			nGramSum += (nGramTreeNode.getStatistic().getWeight());
			numNGrams++;

			//System.out.println(currentNGramLength + " gram move = " + this.gameDependentParameters.getTheMachine().convertToExplicitMove(currentMove));
			//System.out.println(currentNGramLength + " gram = " + nGramSum);

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
		String nppaStatisticsString;

		if(this.nppaStatistics != null){
			nppaStatisticsString = "[ ";

			int roleIndex = 0;
			for(NGramTreeNode<PpaInfo> roleNstStats : this.nppaStatistics){
				nppaStatisticsString += (roleNstStats == null ? "null " : "Tree" + roleIndex + " ");
				roleIndex++;
			}

			nppaStatisticsString += "]";

		}else{
			nppaStatisticsString = "null";
		}

		return indentation + "MIN_N_GRAM_VISITS = " + this.minNGramVisits +
				indentation + "NPPA_FPU = " + this.nppaFpu +
				indentation + "nppa_statistics = " + nppaStatisticsString +
				indentation + "current_simulation_joint_moves = " +
				(this.currentSimulationJointMoves != null ? this.currentSimulationJointMoves.size() + " entries" : "null");
	}


	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex)
			throws MoveDefinitionException, StateMachineException {
		// TODO Auto-generated method stub
		return null;
	}

}
