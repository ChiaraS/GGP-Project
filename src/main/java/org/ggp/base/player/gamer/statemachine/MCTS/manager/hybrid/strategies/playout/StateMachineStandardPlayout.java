package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

import csironi.ggp.course.utils.MyPair;

/**
 * This playout strategy has the same behavior as the StandardPlayout, but performs the full playout
 * using the state machine (e.g. the FPGA-based state machine) instead of advancing state by state as
 * it is done with the software propnet. How the moves are selected (i.e. completely at random, with
 * MAST, etc...) fully depends on how the playout is implemented in the StateMachine. We cannot decide
 * it by setting different MoveEvaluators as in StandardPlayout. This playout makes sense when there is
 * a faster way for the state machine to perform the playout than advancing state by state as it's done
 * by StandardPlayout. An example is the FPGA propnet, that can perform much faster playouts if the full
 * playout is performed on the FPGA side, rather than calling the FPGA side to advance one state at a
 * time in the playout.
 *
 * @author C.Sironi
 *
 */
public class StateMachineStandardPlayout extends PlayoutStrategy {

	/**
	 * Specifies for each playout how many repetitions should be performed.
	 * NOTE that, even if multiple repetitions are performed, it will be considered as a single sample
	 * with reward corresponding to the average reward over all repetitions.
	 */
	private int numSimulationsPerPlayout;

	public StateMachineStandardPlayout(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.numSimulationsPerPlayout = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".numSimulationsPerPlayout");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing.
	}

	@Override
	public void clearComponent() {
		// Do nothing.
	}

	@Override
	public void setUpComponent() {
		// Do nothing.
	}

	@Override
	public SimulationResult[] playout(MctsNode node, List<Move> jointMove, MachineState state, int maxDepth) {
		SimulationResult[] result = new SimulationResult[1];

		result[0] = this.singlePlayout(node, state, maxDepth);

		return result;
	}

	@Override
	public SimulationResult singlePlayout(MctsNode node, MachineState state, int maxDepth) {

        boolean terminal = true;

        if(node != null) {
        	terminal = node.isTerminal();
        }else {
	        try {
				terminal = this.gameDependentParameters.getTheMachine().isTerminal(state);
			} catch (StateMachineException e) {
				GamerLogger.logError("MctsManager", "Exception computing state terminality while performing a playout.");
				GamerLogger.logStackTrace("MctsManager", e);
				terminal = true;
			}
        }

        // NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
     	// we just return the final goals of the state. At the moment the MCTS manager already doesn't call the
        // play-out if the state is terminal or if the depth limit has been reached, so this check will never be
        // true, but it's here just to be safe.
		if(terminal || maxDepth == 0){

			GamerLogger.logError("MctsManager", "Playout strategy shouldn't be called on a terminal node. The MctsManager must take care of computing the simulation result in this case.");

			return new SimulationResult(0, this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state));

		}

        MyPair<double[],Double> avgGoalsAndDepth = this.gameDependentParameters.getTheMachine().fastPlayouts(state, this.numSimulationsPerPlayout, maxDepth);

        return new SimulationResult(avgGoalsAndDepth.getSecond().doubleValue(), avgGoalsAndDepth.getFirst());

	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "NUM_SIMULATIONS_PER_PLAYOUT = " + this.numSimulationsPerPlayout;
	}

	/**
	 * To implement this method we need the reasoner to offer a method that returns a move
	 * for a role given a state, according to its choice policy. In this way we will pick a
	 * move according to the same playout policy.
	 */
	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex) {
		List<Move> legalMoves;
		if(node != null && node instanceof DecoupledMctsNode) {
			legalMoves = ((DecoupledMctsNode)node).getLegalMovesForRole(roleIndex);
		}else {
			legalMoves = null;
		}
		Role role = this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex);
		try {
			return this.gameDependentParameters.getTheMachine().getMoveForRole(legalMoves, state, role);
		} catch (MoveDefinitionException | StateMachineException e) {
			GamerLogger.logError("MctsManager", "Exception getting a move for role " + this.gameDependentParameters.getTheMachine().convertToExplicitRole(role) + " using the playout strategy.");
			GamerLogger.logStackTrace("MctsManager", e);
			throw new RuntimeException("Exception getting a move for role " + this.gameDependentParameters.getTheMachine().convertToExplicitRole(role) + " using the playout strategy.", e);
		}
	}

}
