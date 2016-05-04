package org.ggp.base.player.gamer.statemachine.MCTS.prover;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverStandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverRandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverRandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.decoupled.ProverDecoupledTreeNodeFactory;

public class ProverDuctMctsGamer extends ProverUctMctsGamer {

	public ProverDuctMctsGamer() {
		// TODO: change code so that the parameters can be set from outside.

		super();

		this.metagameSearch = false;

		this.withCache = false;
	}

	@Override
	public ProverMCTSManager createMCTSManager(){

	Random r = new Random();

	int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

	int numRoles = this.getStateMachine().getRoles().size();

	return new ProverMCTSManager(new ProverUCTSelection(numRoles, this.getRole(), r, this.valueOffset, new ProverUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
       		new ProverRandomExpansion(numRoles, this.getRole(), r), new ProverRandomPlayout(this.getStateMachine()),
       		new ProverStandardBackpropagation(numRoles, this.getRole()), new ProverMaximumScoreChoice(myRoleIndex, r),
       		null, null, new ProverDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
       		this.gameStepOffset, this.maxSearchDepth);
	}

	/**
	 *
	 */
	/*
	public SlowDUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
	}
	*/

}
