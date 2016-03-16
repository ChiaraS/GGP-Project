/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;


/**
 * Standard (i.e. non single-game) gamer that performs DUCT/MCTS.
 * @author C.Sironi
 *
 */
public class DUCTMCTSGamer extends UCTMCTSGamer {

	/**
	 *
	 */
	public DUCTMCTSGamer() {
		super();
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager(){

	Random r = new Random();

	InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
	int numRoles = this.thePropnetMachine.getInternalRoles().length;

	return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, uctOffset, c),
       		new RandomExpansion(numRoles, myRole, r), new RandomPlayout(this.thePropnetMachine),
       		new StandardBackpropagation(numRoles, myRole),	new MaximumScoreChoice(myRole, r),
       		null, null, new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
       		gameStepOffset, maxSearchDepth);
}

	/**
	 *
	 */
	/*
	public SlowDUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
	}
	*/

}
