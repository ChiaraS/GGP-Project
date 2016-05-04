/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;


/**
 * Standard (i.e. non single-game) gamer that performs DUCT/MCTS.
 * @author C.Sironi
 *
 */
public class DuctMctsGamer extends UctMctsGamer {

	public DuctMctsGamer(){

		this.unexploredMoveDefaultSelectionValue = 1.0;
	}

	@Override
	public InternalPropnetMCTSManager createMCTSManager(){

	Random r = new Random();

	InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
	int numRoles = this.thePropnetMachine.getInternalRoles().length;

	return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
       		new RandomExpansion(numRoles, myRole, r), new RandomPlayout(this.thePropnetMachine),
       		new StandardBackpropagation(numRoles, myRole),	new MaximumScoreChoice(myRole, r),
       		null, null, new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
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
