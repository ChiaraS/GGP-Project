package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class RAVEDUCTMCTSGamer extends UCTMCTSGamer{

	private int minAMAFVisits;

	private double bias;

	public RAVEDUCTMCTSGamer() {
		super();

		this.minAMAFVisits = 0;
		this.bias = 0.001;
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager() {
		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRole, r, this.valueOffset, this.c, this.minAMAFVisits, this.bias);
		GRAVEPlayout gravePlayout = new GRAVEPlayout(this.thePropnetMachine, allJointMoves);

		return new InternalPropnetMCTSManager(graveSelection, new RandomExpansion(numRoles, myRole, r),
				gravePlayout, new GRAVEBackpropagation(numRoles, myRole, allJointMoves),
				new MaximumScoreChoice(myRole, r),	new GRAVEAfterSimulation(graveSelection, gravePlayout),
				null, new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth);
	}

}
