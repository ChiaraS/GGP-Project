package org.ggp.base.player.gamer.statemachine.MCTS.prover.ravegrave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnBetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnCADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation.ProverGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverGRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.GRAVE.ProverGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.amafdecoulped.ProverAMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.prover.ProverDuctMctsGamer;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

public class ProverGRDuctMctsGamer extends ProverDuctMctsGamer {

	protected int minAMAFVisits;

	protected PnBetaComputer betaComputer;

	protected double defaultExploration;

	public ProverGRDuctMctsGamer(){

		super();

		this.c = 0.2;
		this.unexploredMoveDefaultSelectionValue = 1.0;

		this.logTranspositionTable = true;

		this.minAMAFVisits = 0;
		this.betaComputer = new PnCADIABetaComputer(250);
		this.defaultExploration = 1.0;

	}

	@Override
	public ProverMCTSManager createMCTSManager() {

		Random r = new Random();

		ProverRole myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		ProverGRAVESelection graveSelection = new ProverGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new ProverGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration));

		return new ProverMCTSManager(graveSelection, new ProverNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new ProverGRAVEPlayout(this.getStateMachine()), new ProverGRAVEBackpropagation(numRoles, myRole),
				new ProverMaximumScoreChoice(myRoleIndex, r), null, new ProverGRAVEAfterSimulation(graveSelection),
				null, new ProverAMAFDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

	}
}

