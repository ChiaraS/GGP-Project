/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverStandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverRandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.decoupled.ProverDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;


/**
 * Standard (i.e. non single-game) gamer that performs DUCT/MCTS.
 * @author C.Sironi
 *
 */
public class DuctMctsGamer extends UctMctsGamer {

	public DuctMctsGamer(){

		//this.unexploredMoveDefaultSelectionValue = 1.0;

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new NoExpansion(), new RandomPlayout(this.thePropnetMachine),
	       		new StandardBackpropagation(numRoles, myRole), new MaximumScoreChoice(myRole, r), null,
	       		null, null, new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}



	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		Role myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		return new ProverMCTSManager(new ProverUCTSelection(numRoles, myRole, r, this.valueOffset, new ProverUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new ProverNoExpansion(), new ProverRandomPlayout(this.getStateMachine()),
	       		new ProverStandardBackpropagation(numRoles, myRole), new ProverMaximumScoreChoice(myRoleIndex, r), null,
	       		null, null, new ProverDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

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
