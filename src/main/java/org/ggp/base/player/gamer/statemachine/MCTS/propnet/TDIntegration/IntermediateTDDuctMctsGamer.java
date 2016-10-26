package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;

import java.util.Random;

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
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class IntermediateTDDuctMctsGamer extends TDDuctMctsGamer {

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

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

}
