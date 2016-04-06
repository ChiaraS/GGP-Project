/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.slowsequential.PnSlowSequentialTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;



/**
 * @author C.Sironi
 *
 */
public class SlowSUCTMCTSGamer extends UctMctsGamer {

	/**
	 *
	 */
	public SlowSUCTMCTSGamer() {
		super();
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager(){

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new RandomExpansion(numRoles, myRole, r), new RandomPlayout(this.thePropnetMachine),
	       		new StandardBackpropagation(numRoles, myRole),	new MaximumScoreChoice(myRole, r),
	       		null, null, new PnSlowSequentialTreeNodeFactory(this.thePropnetMachine, myRole),
	       		this.thePropnetMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	/**
	 *
	 */
	/*
	public SlowSUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
		this.DUCT = false;
	}
	*/

}
