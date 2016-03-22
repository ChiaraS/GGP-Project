package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVE.BetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVE.CADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class CadiaRAVEDUCTMCTSGamer extends UCTMCTSGamer {

	private int minAMAFVisits;

	private BetaComputer betaComputer;

	public CadiaRAVEDUCTMCTSGamer() {
		super();

		this.c = 0.7;
		this.unexploredMoveDefaultSelectionValue = Double.MAX_VALUE;
		this.minAMAFVisits = 0;
		this.betaComputer = new CADIABetaComputer(500);
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager() {
		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRole, r, this.valueOffset, this.c, this.unexploredMoveDefaultSelectionValue, this.minAMAFVisits, this.betaComputer);
		GRAVEPlayout gravePlayout = new GRAVEPlayout(this.thePropnetMachine, allJointMoves);

		return new InternalPropnetMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				gravePlayout, new GRAVEBackpropagation(numRoles, myRole, allJointMoves),
				new MaximumScoreChoice(myRole, r),	new GRAVEAfterSimulation(graveSelection, gravePlayout),
				null, new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth);
	}

}
