package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.MASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.MASTBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.MASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnMASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnMASTBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnRandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnMASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.PnUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class MastDuctMctsGamer extends DuctMctsGamer {

	protected double epsilon;

	protected double decayFactor;

	public MastDuctMctsGamer() {
		super();
		this.epsilon = 0.4;
		this.decayFactor = 0.2;
	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		Map<CompactMove, MoveStats> mastStatistics = new HashMap<CompactMove, MoveStats>();

		/*
		MASTStrategy mast = new MASTStrategy(this.thePropnetMachine, this.epsilon, r, mastStatistics, numRoles, myRole);

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), mast,	mast, new MaximumScoreChoice(myRole, r),
	       		null, new MASTAfterMove(mastStatistics, decayFactor), new PnDUCTTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, gameStepOffset,	maxSearchDepth);
		*/

		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new PnRandomExpansion(numRoles, myRole, r), new PnMASTPlayout(this.thePropnetMachine, r, mastStatistics, this.epsilon),
	       		new PnMASTBackpropagation(numRoles, myRole, mastStatistics),
	       		new PnMaximumScoreChoice(myRole, r), null, null,
	       		new PnMASTAfterMove(mastStatistics, this.decayFactor),
	       		new PnDecoupledTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
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

		Map<Move, MoveStats> mastStatistics = new HashMap<Move, MoveStats>();

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, numRoles, myRoleIndex)),
	       		new RandomExpansion(numRoles, myRoleIndex, r), new MASTPlayout(theMachine, new EpsilonMASTJointMoveSelector(theMachine, r, mastStatistics, this.epsilon, numRoles, myRoleIndex)),
	       		new MASTBackpropagation(numRoles, myRoleIndex, mastStatistics),
	       		new MaximumScoreChoice(myRoleIndex, r), null, null,
	       		new MASTAfterMove(mastStatistics, this.decayFactor),
	       		new DecoupledTreeNodeFactory(theMachine),
	       		theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

}
