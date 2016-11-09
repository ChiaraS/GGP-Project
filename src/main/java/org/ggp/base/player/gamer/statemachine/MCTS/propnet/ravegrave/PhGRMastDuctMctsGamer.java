package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.MASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.PhMASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.ProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.MASTGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.MASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.BetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.CADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GRAVEBetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.ProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnMASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnPhMASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnProgressiveHistoryAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnMASTGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnMASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnProgressiveHistoryGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProverBetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProverCADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProverGRAVEBetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public abstract class PhGRMastDuctMctsGamer extends GRMastDuctMctsGamer {

	protected double w;

	public PhGRMastDuctMctsGamer() {

		super();

		this.w = 5.0;

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		PnProverBetaComputer pnProverBetaComputer;

		if(this.cadiaBetaComputer){
			pnProverBetaComputer = new PnProverCADIABetaComputer(this.k);
		}else{
			pnProverBetaComputer = new PnProverGRAVEBetaComputer(this.bias);
		}

		PnProgressiveHistoryGRAVESelection graveSelection = new PnProgressiveHistoryGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new PnProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, pnProverBetaComputer, this.defaultExploration, this.w));

		Map<CompactMove, MoveStats> mastStatistics = new HashMap<CompactMove, MoveStats>();

		// Note that the after simulation strategy GRAVEAfterSimulation already performs all the after simulation
		// actions needed by the MAST strategy, so we don't need to change it when we use GRAVE and MAST together.
		return new InternalPropnetMCTSManager(graveSelection, new PnNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new PnMASTPlayout(this.thePropnetMachine, r, mastStatistics, this.epsilon), new PnMASTGRAVEBackpropagation(numRoles, myRole, mastStatistics),
				new PnMaximumScoreChoice(myRole, r), null, new PnGRAVEAfterSimulation(graveSelection),
				new PnPhMASTAfterMove(new PnMASTAfterMove(mastStatistics, this.decayFactor), new PnProgressiveHistoryAfterMove(graveSelection)),
				new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
				this.thePropnetMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){


		// ZZZ!

		return null;


	}

	@Override
	public HybridMCTSManager createHybridMCTSManager(){

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

		BetaComputer betaComputer;

		if(this.cadiaBetaComputer){
			betaComputer = new CADIABetaComputer(this.k, numRoles, myRoleIndex);
		}else{
			betaComputer = new GRAVEBetaComputer(this.bias, numRoles, myRoleIndex);
		}

		ProgressiveHistoryGRAVESelection graveSelection = new ProgressiveHistoryGRAVESelection(numRoles, myRoleIndex, r, this.valueOffset, this.minAMAFVisits, new ProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, betaComputer, this.defaultExploration, this.w, numRoles, myRoleIndex));

		Map<Move, MoveStats> mastStatistics = new HashMap<Move, MoveStats>();

		// Note that the after simulation strategy GRAVEAfterSimulation already performs all the after simulation
		// actions needed by the MAST strategy, so we don't need to change it when we use GRAVE and MAST together.
		return new HybridMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new MASTPlayout(theMachine, new EpsilonMASTJointMoveSelector(theMachine, r, mastStatistics, this.epsilon, numRoles, myRoleIndex)),
				new MASTGRAVEBackpropagation(numRoles, myRoleIndex, mastStatistics),
				new MaximumScoreChoice(myRoleIndex, r), null, new GRAVEAfterSimulation(graveSelection),
				new PhMASTAfterMove(new MASTAfterMove(mastStatistics, this.decayFactor), new ProgressiveHistoryAfterMove(graveSelection)),
				new AMAFDecoupledTreeNodeFactory(theMachine), theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);


	}

}
