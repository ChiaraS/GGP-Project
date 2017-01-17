package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.FixedMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.IncrementalMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NaiveProblemRepresentation;
import org.ggp.base.util.statemachine.structure.Move;

public class NaiveParametersTuner extends ParametersTuner {

	private double epsilon0;

	private TunerSelector globalMabSelector;

	private TunerSelector localMabsSelector;

	private NaiveProblemRepresentation[] roleProblems;

	private int[][] selectedCombinations;

	public NaiveParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings,
				sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// Create a MAB representation of the combinatorial problem for each role
		this.roleProblems = new NaiveProblemRepresentation[numRolesToTune];

		for(int i = 0; i < this.roleProblems.length; i++){
			roleProblems[i] = new NaiveProblemRepresentation(this.classesLength);
		}

		this.selectedCombinations = new int[numRolesToTune][this.classesLength.length];

	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public int[][] selectNextCombinations() {

		// For each role, we check the corresponding naive problem and select a combination of parameters
		for(int i = 0; i < this.roleProblems.length; i++){
			// TODO: the strategy that selects if to use the global or the local mabs is hard-coded.
			// Can be refactored to be customizable.
			if(this.roleProblems[i].getGlobalMab().getNumUpdates() > 0 &&
					this.random.nextDouble() > this.epsilon0){// Exploit

				this.selectedCombinations[i] = this.exploit(this.roleProblems[i].getGlobalMab());

			}else{ //Explore

				this.selectedCombinations[i] = this.explore(this.roleProblems[i].getLocalMabs());

			}
		}

		return this.selectedCombinations;
	}

	/**
	 * Given the global MAB, selects one of the combinatorial moves in the MAB according to
	 * the strategy specified for the global MAB.
	 *
	 * @param globalMab
	 * @return
	 */
	private int[] exploit(IncrementalMab globalMab){
		Move m = this.globalMabSelector.selectMove(globalMab.getMoveStats(), globalMab.getNumUpdates());
		return ((CombinatorialCompactMove) m).getIndices();
	}

	private int[] explore(FixedMab[] localMabs){

		int[] indices = new int[localMabs.length];

		// Select a value for each local mab independently
		for(int i = 0; i < localMabs.length; i++){
			indices[i] = this.localMabsSelector.selectMove(localMabs[i].getMoveStats(), localMabs[i].getNumUpdates());
		}

		return indices;

	}

	@Override
	public void updateStatistics(int[] rewards) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		return this.roleProblems.length;
	}


	@Override
	public void logStats() {
		// TODO Auto-generated method stub

	}



}
