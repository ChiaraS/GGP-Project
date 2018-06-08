package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This playout strategy has the same behavior as the StandardPlayout, but performs the full
 * playout using the FPGA propnet instead of advancing state by state as it is done with the
 * software propnet. How the moves are selected (i.e. completely at random, with MAST, etc...)
 * fully depends on how the playout is implemented with the FPGA. We cannot decide it by setting
 * different MoveEvaluators as in StandardPlayout.
 *
 * @author C.Sironi
 *
 */
public class StandardFPGAPlayout extends PlayoutStrategy {

	/**
	 * Specifies for each playout how many repetitions should be performed.
	 * NOTE that, even if multiple repetitions are performed, it will be considered as a single sample
	 * with reward corresponding to the average reward over all repetitions.
	 */
	private int numSimulationsPerPlayout;

	public StandardFPGAPlayout(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.numSimulationsPerPlayout = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".numSimulationsPerPlayout");

	}

	@Override
	public SimulationResult singlePlayout(MachineState state, int maxDepth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimulationResult[] playout(List<Move> jointMove, MachineState state, int maxDepth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Move> getJointMove(MachineState state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getComponentParameters(String indentation) {
		// TODO Auto-generated method stub
		return null;
	}

}
