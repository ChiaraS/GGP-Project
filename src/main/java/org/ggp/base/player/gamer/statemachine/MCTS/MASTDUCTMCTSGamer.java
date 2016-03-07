package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.MAST.MASTStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.UCTSelection;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MASTDUCTMCTSGamer extends DUCTMCTSGamer {

	double epsilon;

	public MASTDUCTMCTSGamer() {
		super();
		this.epsilon = 0.4;
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager(){

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		MASTStrategy mast = new MASTStrategy(this.thePropnetMachine, this.epsilon, r, numRoles, myRole);

		return new InternalPropnetMCTSManager(this.mctsType, myRole, new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), mast,	mast, new MaximumScoreChoice(myRole, r),
	       		this.thePropnetMachine, gameStepOffset, maxSearchDepth);
	}

}
