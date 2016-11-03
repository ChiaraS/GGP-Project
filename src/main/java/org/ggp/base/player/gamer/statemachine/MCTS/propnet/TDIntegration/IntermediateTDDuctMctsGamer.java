package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.TDAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.IntermediateTDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.TDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.GoalsMemorizingStandardPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.RandomJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.TDUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnTDAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnIntermediateTDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnTDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnGoalsMemorizingStandardPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnRandomJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.PnUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled.TDDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class IntermediateTDDuctMctsGamer extends TDDuctMctsGamer {

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		PnTDBackpropagation backpropagation = new PnIntermediateTDBackpropagation(this.thePropnetMachine, numRoles, this.qPlayout, this.lambda, this.gamma);

		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new PnNoExpansion(), new PnGoalsMemorizingStandardPlayout(this.thePropnetMachine, new PnRandomJointMoveSelector(this.thePropnetMachine)),
	       		backpropagation, new PnMaximumScoreChoice(myRole, r), null, new PnTDAfterSimulation(backpropagation), null,
	       		new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	@Override
	public ProverMCTSManager createProverMCTSManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HybridMCTSManager createHybridMCTSManager() {
		Random r = new Random();

		int myRoleIndex;
		int numRoles;

		AbstractStateMachine theMachine;

		if(this.thePropnetMachine != null){
			theMachine = new CompactStateMachine(this.thePropnetMachine);
			myRoleIndex = this.thePropnetMachine.convertToCompactRole(this.getRole()).getIndex();
			numRoles = this.thePropnetMachine.getCompactRoles().size();
		}else{
			theMachine = new ExplicitStateMachine(this.getStateMachine());
			numRoles = this.getStateMachine().getExplicitRoles().size();
			myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());
		}

		GlobalExtremeValues globalExtremeValues = new GlobalExtremeValues(numRoles, this.defaultGlobalMinValue, this.defaultGlobalMaxValue);

		TDBackpropagation backpropagation = new IntermediateTDBackpropagation(theMachine, numRoles, globalExtremeValues, this.qPlayout, this.lambda, this.gamma);

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new TDUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, globalExtremeValues, numRoles)),
	       		new NoExpansion(), new GoalsMemorizingStandardPlayout(theMachine, new RandomJointMoveSelector(theMachine)),
	       		backpropagation, new MaximumScoreChoice(myRoleIndex, r), null, new TDAfterSimulation(backpropagation), null,
	       		new TDDecoupledTreeNodeFactory(theMachine), theMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

}
