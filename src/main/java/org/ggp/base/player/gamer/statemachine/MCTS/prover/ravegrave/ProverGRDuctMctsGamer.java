package org.ggp.base.player.gamer.statemachine.MCTS.prover.ravegrave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProverBetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProverCADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProverGRAVEBetaComputer;
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
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class ProverGRDuctMctsGamer extends ProverDuctMctsGamer {

	protected int minAMAFVisits;

	/**
	 * True if the player must use the CADIABetaComputer, false if it must use the GRAVEBetaComputer
	 */
	protected boolean cadiaBetaComputer;

	/**
	 * Value for k to use when the player uses the CADIABetaComputer.
	 */
	protected int k;

	/**
	 * Value for bias to use when the player uses the GRAVEBetaComputer.
	 */
	protected double bias;

	protected double defaultExploration;

	public ProverGRDuctMctsGamer(){

		super();

		this.c = 0.2;
		this.unexploredMoveDefaultSelectionValue = 1.0;

		this.logTranspositionTable = true;

		this.minAMAFVisits = 0;

		this.cadiaBetaComputer = true;
		this.k = 250;

		this.defaultExploration = 1.0;

	}

	@Override
	public ProverMCTSManager createMCTSManager() {

		Random r = new Random();

		ExplicitRole myRole = this.getRole();
		int numRoles = this.getStateMachine().getExplicitRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		PnProverBetaComputer pnProverBetaComputer;

		if(this.cadiaBetaComputer){
			pnProverBetaComputer = new PnProverCADIABetaComputer(this.k);
		}else{
			pnProverBetaComputer = new PnProverGRAVEBetaComputer(this.bias);
		}

		ProverGRAVESelection graveSelection = new ProverGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new ProverGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, pnProverBetaComputer, this.defaultExploration));

		return new ProverMCTSManager(graveSelection, new ProverNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new ProverGRAVEPlayout(this.getStateMachine()), new ProverGRAVEBackpropagation(numRoles, myRole),
				new ProverMaximumScoreChoice(myRoleIndex, r), null, new ProverGRAVEAfterSimulation(graveSelection),
				null, new ProverAMAFDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

	}
}

