package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.Pair;

public class RandomSelector extends TunerSelector{

	public RandomSelector(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,	SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
	}

	@Override
	public void setReferences(
			SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing

	}

	/*
	 * TODO: ATTENTION! If you add code here that does something on the state of the tuner remember that this method might
	 * be called multiple times after each game if the player is using the SequentialParametersTuner!!! If you want this method
	 * to be called only once per instance of TunerSelector then change the code in the copy constructor of the ParametersTuner
	 * subclasses in order to deep-copy also the TunerSelectors!
	 */
	@Override
	public void clearComponent() {
		// Do nothing

	}

	/*
	 * TODO: ATTENTION! If you add code here that does something on the state of the tuner remember that this method might
	 * be called multiple times before each game if the player is using the SequentialParametersTuner!!! If you want this method
	 * to be called only once per instance of TunerSelector then change the code in the copy constructor of the ParametersTuner
	 * subclasses in order to deep-copy also the TunerSelectors!
	 */
	@Override
	public void setUpComponent() {
		// Do nothing

	}

	@Override
	public int selectMove(MoveStats[] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates) {
		int selectedMove = -1;

		if(valuesFeasibility != null){
			// Count number of feasible moves and then pick random one
			int feasibleMoves = 0;
			for(int i = 0; i < valuesFeasibility.length; i++){
				if(valuesFeasibility[i]){
					feasibleMoves++;
				}
			}

			int randomNum = this.random.nextInt(feasibleMoves);
			for(int i = 0; i < valuesFeasibility.length; i++){
				if(valuesFeasibility[i]){
					if(randomNum == 0){
						selectedMove = i;
						break;
					}
					randomNum--;
				}
			}
				// Extra check (should never be true).
			if(selectedMove == -1){
				throw new RuntimeException("RandomSelector - SelectMove(MoveStats[], boolean[], double[], int): detected no feasible move when selecting.");
			}
		}else{
			selectedMove = this.random.nextInt(movesStats.length);
		}

		return selectedMove;
	}

	@Override
	public Move selectMove(Map<Move,Pair<MoveStats,Double>> movesInfo, int numUpdates) {

		// Extra check to make sure that this method is never called with an empty map of moves
		if(movesInfo.isEmpty()){
			throw new RuntimeException("RandomSelector - selectMove(Map, int): cannot select next combination becase the map is empty.");
		}

		int randomNum = this.random.nextInt(movesInfo.size());

		for(Entry<Move,Pair<MoveStats,Double>> entry : movesInfo.entrySet()){
			if(randomNum == 0){
				return entry.getKey();
			}
			randomNum--;
		}

		return null;

	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
