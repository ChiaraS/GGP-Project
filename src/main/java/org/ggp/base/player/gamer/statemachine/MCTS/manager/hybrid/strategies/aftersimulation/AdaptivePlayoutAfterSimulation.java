package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.Move;

public class AdaptivePlayoutAfterSimulation extends AfterSimulationStrategy {

	private List<Map<Move, Double>> weightsPerMove;

	public AdaptivePlayoutAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void afterSimulationActions(SimulationResult[] simulationResult) {
		for(Map<Move, Double> weightOfPlayer : this.weightsPerMove){
			double sum = 0;
			for(Entry<Move,Double> weight : weightOfPlayer.entrySet()){
				sum += weight.getValue();
			}
			//System.out.println(sum);
		}
		//System.out.println();

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.weightsPerMove = sharedReferencesCollector.getWeightsPerMove();

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
