package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.ProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.ProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnGRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftermove.ProverProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation.ProverGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverGRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.GRAVE.ProverProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.amafdecoulped.ProverAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public abstract class PhGRDuctMctsGamer extends GRDuctMctsGamer {

	protected double w;

	public PhGRDuctMctsGamer(){

		super();

		this.w = 5.0;

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		PnProgressiveHistoryGRAVESelection graveSelection = new PnProgressiveHistoryGRAVESelection(numRoles, myRole, r,	this.valueOffset, this.minAMAFVisits,
				new PnProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration, this.w));

		return new InternalPropnetMCTSManager(graveSelection, new PnNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new PnGRAVEPlayout(this.thePropnetMachine), new PnGRAVEBackpropagation(numRoles, myRole),
				new PnMaximumScoreChoice(myRole, r), null, new PnGRAVEAfterSimulation(graveSelection),
				new PnProgressiveHistoryAfterMove(graveSelection), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		ExplicitRole myRole = this.getRole();
		int numRoles = this.getStateMachine().getExplicitRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		ProverProgressiveHistoryGRAVESelection graveSelection = new ProverProgressiveHistoryGRAVESelection(numRoles, myRole, r,	this.valueOffset, this.minAMAFVisits,
				new ProverProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration, this.w));

		return new ProverMCTSManager(graveSelection, new ProverNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new ProverGRAVEPlayout(this.getStateMachine()), new ProverGRAVEBackpropagation(numRoles, myRole),
				new ProverMaximumScoreChoice(myRoleIndex, r), null, new ProverGRAVEAfterSimulation(graveSelection),
				new ProverProgressiveHistoryAfterMove(graveSelection), new ProverAMAFDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);


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

		ProgressiveHistoryGRAVESelection graveSelection = new ProgressiveHistoryGRAVESelection(numRoles, myRoleIndex, r,	this.valueOffset, this.minAMAFVisits,
				new ProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration, this.w, numRoles));

		return new HybridMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new GRAVEPlayout(theMachine), new GRAVEBackpropagation(numRoles, myRoleIndex),
				new MaximumScoreChoice(myRoleIndex, r), null, new GRAVEAfterSimulation(graveSelection),
				new ProgressiveHistoryAfterMove(graveSelection), new AMAFDecoupledTreeNodeFactory(theMachine), theMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}
}
