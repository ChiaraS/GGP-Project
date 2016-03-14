package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.aftermove.MASTAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.MASTBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.MASTPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.PnDUCTTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MASTDUCTMCTSGamer extends DUCTMCTSGamer {

	private double epsilon;

	private double decayFactor;

	public MASTDUCTMCTSGamer() {
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

		/*
		MASTStrategy mast = new MASTStrategy(this.thePropnetMachine, this.epsilon, r, mastStatistics, numRoles, myRole);

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), mast,	mast, new MaximumScoreChoice(myRole, r),
	       		null, new MASTAfterMove(mastStatistics, decayFactor), new PnDUCTTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, gameStepOffset,	maxSearchDepth);
		*/

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), new MASTPlayout(this.thePropnetMachine, this.epsilon, r, mastStatistics),
	       		new MASTBackpropagation(numRoles, myRole, mastStatistics), new MaximumScoreChoice(myRole, r),
	       		null, new MASTAfterMove(mastStatistics, this.decayFactor), new PnDUCTTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, this.gameStepOffset, this.maxSearchDepth);
	}

}
