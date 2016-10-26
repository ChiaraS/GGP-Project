/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnStandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnRandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnRandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.PnUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverStandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverRandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverRandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * @author C.Sironi
 *
 */
public class SingleGameDUCTMCTSGamer extends UctMctsGamer{

	/**
	 *
	 */
	public SingleGameDUCTMCTSGamer() {
		super();
		this.propnetBuild = PROPNET_BUILD.ONCE;
	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new PnRandomExpansion(numRoles, myRole, r), new PnRandomPlayout(this.thePropnetMachine),
	       		new PnStandardBackpropagation(numRoles, myRole), new PnMaximumScoreChoice(myRole, r), null,
	       		null, null, new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		ExplicitRole myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		return new ProverMCTSManager(new ProverUCTSelection(numRoles, myRole, r, this.valueOffset, new ProverUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new ProverRandomExpansion(numRoles, myRole, r), new ProverRandomPlayout(this.getStateMachine()),
	       		new ProverStandardBackpropagation(numRoles, myRole), new ProverMaximumScoreChoice(myRoleIndex, r), null,
	       		null, null, new ProverDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

	}

}
