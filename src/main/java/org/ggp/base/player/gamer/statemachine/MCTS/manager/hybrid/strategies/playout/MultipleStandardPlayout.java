package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

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
	public SimulationResult[] playout(MctsNode node, /*List<Move> jointMove,*/ MachineState state, int maxDepth) {

		SimulationResult[] result = new SimulationResult[1];

		double[] avgGoals = new double[this.gameDependentParameters.getNumRoles()];
		double avgPlayoutLength = 0.0;
		double[] goals = new double[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.playoutRepetitions; i++) {
			result[0] = this.singlePlayout(node, state, maxDepth);
			goals = result[0].getTerminalGoalsIn0_100();
			for(int j = 0; j < goals.length; j++){
				avgGoals[j] += goals[j];
			}
			avgPlayoutLength += result[0].getPlayoutLength();
		}

		for(int j = 0; j < avgGoals.length; j++){
			avgGoals[j] = avgGoals[j]/((double)this.playoutRepetitions);
		}
		avgPlayoutLength = avgPlayoutLength/((double)this.playoutRepetitions);

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

}
