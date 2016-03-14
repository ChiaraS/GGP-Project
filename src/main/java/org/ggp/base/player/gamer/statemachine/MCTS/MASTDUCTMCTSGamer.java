package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.MAST.MASTStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.PnDUCTTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MASTDUCTMCTSGamer extends DUCTMCTSGamer {

	double epsilon;

	double decayFactor;

	public MASTDUCTMCTSGamer() {
		super();
		this.epsilon = 0.4;
		this.decayFactor = 0.2;
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager(){

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		MASTStrategy mast = new MASTStrategy(this.thePropnetMachine, this.epsilon, this.decayFactor, r, numRoles, myRole);

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), mast,	mast, new MaximumScoreChoice(myRole, r),
	       		new PnDUCTTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine, gameStepOffset,
	       		maxSearchDepth);
	}

}
