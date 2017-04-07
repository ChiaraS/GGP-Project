package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.util.List;
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
			throw new RuntimeException("RandomSelector - selectMove(Map, int): cannot select next combination because the map is empty.");
		}

		int randomNum = this.random.nextInt(movesInfo.size());

		Move theMove = null;
		for(Entry<Move,Pair<MoveStats,Double>> entry : movesInfo.entrySet()){
			if(randomNum == 0){
				theMove = entry.getKey();
			}
			randomNum--;
		}

		if(theMove == null){
			throw new RuntimeException("RandomSelector - selectMove(Map, int): found no move when selecting.");
		}

		return theMove;

	}

	@Override
	public Pair<Integer,Integer> selectMove(MoveStats[][] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates) {
		Pair<Integer,Integer> selectedMove;

		if(valuesFeasibility != null){
			// Pick a random move among the total number of feasible moves.

			// Count total number of feasible moves. Note that all arrays of MoveStats have the same number of feasible moves.
			// Thus, count number of feasible moves for 1 array, then multiply it for number of arrays.
			int feasibleMovesPerArray = 0;
			for(int i = 0; i < valuesFeasibility.length; i++){
				if(valuesFeasibility[i]){
					feasibleMovesPerArray++;
				}
			}
			int feasibleMoves = feasibleMovesPerArray * movesStats.length;

			int randomNum = this.random.nextInt(feasibleMoves);
			int roleStatsIndex = randomNum/feasibleMovesPerArray;
			randomNum = randomNum%feasibleMovesPerArray;
			int statsIndex = -1;
			for(int i = 0; i < valuesFeasibility.length; i++){
				if(valuesFeasibility[i]){
					if(randomNum == 0){
						statsIndex = i;
						break;
					}
					randomNum--;
				}
			}
				// Extra check (should never be true).
			if(roleStatsIndex == -1 || statsIndex == -1){
				throw new RuntimeException("RandomSelector - SelectMove(MoveStats[][], boolean[], double[], int): detected no feasible move when selecting.");
			}
			selectedMove = new Pair<Integer,Integer>(roleStatsIndex, statsIndex);
		}else{
			// Compute total number of MoveStats. Note that all arrays of MoveStats have the same length.
			// Then get random MoveStats among them.
			int randomNum = this.random.nextInt(movesStats.length * movesStats[0].length);
			selectedMove = new Pair<Integer,Integer>(randomNum/movesStats[0].length, randomNum%movesStats[0].length);
		}

		return selectedMove;
	}

	@Override
	public Pair<Integer,Move> selectMove(List<Map<Move,Pair<MoveStats,Double>>> movesInfo, int numUpdates) {

		int totalNumStats = 0;
		for(Map<Move,Pair<MoveStats,Double>> map : movesInfo){
			totalNumStats += map.size();
		}

		if(totalNumStats == 0){
			throw new RuntimeException("RandomSelector - selectMove(List<Map>, int): cannot select next combination because every map is empty.");
		}

		int randomNum = this.random.nextInt(totalNumStats);

		int listIndex = 0;
		for(Map<Move,Pair<MoveStats,Double>> map : movesInfo){
			if(randomNum-map.size() < 0){
				break;
			}else{
				randomNum -= map.size();
				listIndex++;
			}
		}

		Move theMove = null;
		for(Entry<Move,Pair<MoveStats,Double>> entry : movesInfo.get(listIndex).entrySet()){
			if(randomNum == 0){
				theMove = entry.getKey();
			}
			randomNum--;
		}

		if(theMove == null){
			throw new RuntimeException("RandomSelector - selectMove(List<Map>, int): found no move when selecting.");
		}

		return new Pair<Integer,Move>(new Integer(listIndex), theMove);

	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
