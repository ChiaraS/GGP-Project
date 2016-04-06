package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.MASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.MASTGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.MASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.BetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.CADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public abstract class GRMastDuctMctsGamer extends MastDuctMctsGamer {

	protected int minAMAFVisits;

	protected BetaComputer betaComputer;

	protected double defaultExploration;

	public GRMastDuctMctsGamer() {

		super();

		this.c = 0.2;
		this.unexploredMoveDefaultSelectionValue = 1.0;

		this.logTranspositionTable = true;

		this.minAMAFVisits = 0;
		this.betaComputer = new CADIABetaComputer(500);
		this.defaultExploration = 1.0;
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager() {

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new GRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration));

		Map<InternalPropnetMove, MoveStats> mastStatistics = new HashMap<InternalPropnetMove, MoveStats>();

		List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

		MASTPlayout mastPlayout = new MASTPlayout(this.thePropnetMachine, r, mastStatistics, this.epsilon, allJointMoves);

		// Note that the after simulation strategy GRAVEAfterSimulation already performs all the after simulation
		// actions needed by the MAST strategy, so we don't need to change it when we use GRAVE and MAST together.
		return new InternalPropnetMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				mastPlayout, new MASTGRAVEBackpropagation(numRoles, myRole, mastStatistics, allJointMoves),
				new MaximumScoreChoice(myRole, r),	new GRAVEAfterSimulation(graveSelection, mastPlayout),
				new MASTAfterMove(mastStatistics, this.decayFactor), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
				this.thePropnetMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

}
