package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * Same as the StandardPlayout but allows to repeat a playout multiple times from the given state,
 * returning a SINGLE playout result where the goals are computed as the average goals obtained by
 * each role over all the playout repetitions, and the playout depth is the average depth of all repeated
 * playouts.
 *
 * @author C.Sironi
 *
 */
public class MultipleStandardPlayout extends StandardPlayout {

	/**
	 * Specifies for each playout how many repetitions should be performed.
	 * NOTE that, even if multiple repetitions are performed, it will be considered as a single sample
	 * with reward corresponding to the average reward over all repetitions.
	 */
	private int playoutRepetitions;

	public MultipleStandardPlayout(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.playoutRepetitions = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".playoutRepetitions");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
	}

	@Override
	public SimulationResult[] playout(List<Move> jointMove, MachineState state, int maxDepth) {

		SimulationResult[] result = new SimulationResult[1];

		int[] avgGoals = new int[this.gameDependentParameters.getNumRoles()];
		int avgPlayoutLength = 0;
		int[] goals = new int[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.playoutRepetitions; i++) {
			result[0] = this.singlePlayout(state, maxDepth);
			goals = result[0].getTerminalGoals();
			for(int j = 0; j < goals.length; j++){
				avgGoals[j] += goals[j];
			}
			avgPlayoutLength += result[0].getPlayoutLength();
		}

		for(int j = 0; j < avgGoals.length; j++){
			avgGoals[j] = (int) Math.round((double)avgGoals[j]/(double)this.playoutRepetitions);
		}
		avgPlayoutLength = (int) Math.round((double)avgPlayoutLength/(double)this.playoutRepetitions);

		result[0] = new SimulationResult(avgPlayoutLength, avgGoals);

		return result;
	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "PLAYOUT_REPETIITONS = " + this.playoutRepetitions;

		String superParams = super.getComponentParameters(indentation);

		if(superParams == null){
			return params;
		}else{
			return superParams + params;
		}
	}

	@Override
	public List<Move> getJointMove(MachineState state) {
		try {
			return this.moveSelector.getJointMove(state);
		} catch (MoveDefinitionException | StateMachineException e) {
			GamerLogger.logError("MctsManager", "Exception getting a joint move using the playout strategy.");
			GamerLogger.logStackTrace("MctsManager", e);
			throw new RuntimeException("Exception getting a joint move using the playout strategy.", e);
		}
	}

	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) {
		try {
			return this.moveSelector.getMoveForRole(state, roleIndex);
		} catch (MoveDefinitionException | StateMachineException e) {
			GamerLogger.logError("MctsManager", "Exception getting a move for role with index " + roleIndex + " using the playout strategy.");
			GamerLogger.logStackTrace("MctsManager", e);
			throw new RuntimeException("Exception getting a move for role with index " + roleIndex + " using the playout strategy.", e);
		}
	}

}
