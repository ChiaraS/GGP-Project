package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.ProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.ProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.ProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftermove.ProverProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation.ProverGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverGRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.GRAVE.ProverProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.AMAFDecoupled.ProverAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public abstract class PhGRDuctMctsGamer extends GRDuctMctsGamer {

	protected double w;

	public PhGRDuctMctsGamer(){

		super();

		this.w = 5.0;

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		ProgressiveHistoryGRAVESelection graveSelection = new ProgressiveHistoryGRAVESelection(numRoles, myRole, r,	this.valueOffset, this.minAMAFVisits,
				new ProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration, this.w));

		return new InternalPropnetMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new GRAVEPlayout(this.thePropnetMachine), new GRAVEBackpropagation(numRoles, myRole),
				new MaximumScoreChoice(myRole, r), null, new GRAVEAfterSimulation(graveSelection),
				new ProgressiveHistoryAfterMove(graveSelection), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		Role myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		ProverProgressiveHistoryGRAVESelection graveSelection = new ProverProgressiveHistoryGRAVESelection(numRoles, myRole, r,	this.valueOffset, this.minAMAFVisits,
				new ProverProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration, this.w));

		return new ProverMCTSManager(graveSelection, new ProverNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new ProverGRAVEPlayout(this.getStateMachine()), new ProverGRAVEBackpropagation(numRoles, myRole),
				new ProverMaximumScoreChoice(myRoleIndex, r), null, new ProverGRAVEAfterSimulation(graveSelection),
				new ProverProgressiveHistoryAfterMove(graveSelection), new ProverAMAFDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);


	}
}
