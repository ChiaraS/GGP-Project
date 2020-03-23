package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaInfo;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaWeights;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

public class AdaptivePlayoutAfterMove extends AfterMoveStrategy {

	private PpaWeights ppaWeights;

	private double decayFactor;

	private boolean logWeights;

	public AdaptivePlayoutAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.decayFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy" + id + ".decayFactor");

		this.logWeights = gamerSettings.getBooleanPropertyValue("AfterMoveStrategy" + id + ".logWeights");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.ppaWeights = sharedReferencesCollector.getPpaWeights();
	}

	@Override
	public void clearComponent() {
		// Do nothing (because the MAST statistics will be already cleared by the strategy that populates them,
		// i.e. the backpropagation strategy that uses the MastUpdater).
	}

	@Override
	public void setUpComponent() {
		//this.gameStep = 0;
	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "DECAY_FACTOR = " + this.decayFactor +
				indentation + "LOG_WEIGHTS = " + this.logWeights;

		if(this.ppaWeights != null){
			params += indentation + "ppa_weights = " + this.ppaWeights.getMinimalInfo();
		}else{
			params += indentation + "ppa_weights = null";
		}

		return params;

	}

	@Override
	public void afterMoveActions() {

		if(this.logWeights){
			this.logWeights();
		}

		this.ppaWeights.decayWeights(this.decayFactor, this.gameDependentParameters.getTotIterations());

		if(this.logWeights){
			this.logWeights();
		}

	}

	private void logWeights(){

		String toLog = "STEP=;" + this.gameDependentParameters.getGameStep() + ";\n";

		if(this.ppaWeights == null){
			toLog += "null;\n";
		}else{

			List<Map<Move,PpaInfo>> weightsPerMove = this.ppaWeights.getWeightsPerMove();

			for(int roleIndex = 0; roleIndex < weightsPerMove.size(); roleIndex++){

				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";\n");

				if(weightsPerMove.get(roleIndex) == null){
					toLog += "null;\n";
				}else{
					for(Entry<Move, PpaInfo> ppaInfo : weightsPerMove.get(roleIndex).entrySet()){
						toLog += ("MOVE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitMove(ppaInfo.getKey()) +
								";" + ppaInfo.getValue() + "\n");
					}
				}
			}
		}

		toLog += "\n";

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "PpaStats", toLog);

	}
}
