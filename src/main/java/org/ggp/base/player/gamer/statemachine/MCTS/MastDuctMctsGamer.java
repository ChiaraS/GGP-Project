package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.MASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.MASTAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.MASTBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.MASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MastDuctMctsGamer extends DuctMctsGamer {

	protected double epsilon;

	protected double decayFactor;

	public MastDuctMctsGamer() {
		super();
		this.epsilon = 0.4;
		this.decayFactor = 0.2;
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager(){

		Random r = new Random();

		Map<InternalPropnetMove, MoveStats> mastStatistics = new HashMap<InternalPropnetMove, MoveStats>();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		MASTPlayout mastPlayout = new MASTPlayout(this.thePropnetMachine, r, mastStatistics, this.epsilon, new ArrayList<List<InternalPropnetMove>>());

		/*
		MASTStrategy mast = new MASTStrategy(this.thePropnetMachine, this.epsilon, r, mastStatistics, numRoles, myRole);

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), mast,	mast, new MaximumScoreChoice(myRole, r),
	       		null, new MASTAfterMove(mastStatistics, decayFactor), new PnDUCTTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, gameStepOffset,	maxSearchDepth);
		*/

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new RandomExpansion(numRoles, myRole, r), mastPlayout,
	       		new MASTBackpropagation(numRoles, myRole, mastStatistics),
	       		new MaximumScoreChoice(myRole, r), new MASTAfterSimulation(mastPlayout),
	       		new MASTAfterMove(mastStatistics, this.decayFactor),
	       		new PnDecoupledTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

}
