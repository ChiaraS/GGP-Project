package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.MultiplePlayout;
import org.ggp.base.util.logging.GamerLogger;

public class MultiplePlayoutAfterMove extends AfterMoveStrategy {

	private MultiplePlayout multiplePlayout;


	public MultiplePlayoutAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		if(sharedReferencesCollector.getPlayoutStrategy() instanceof MultiplePlayout){
			this.multiplePlayout = (MultiplePlayout) sharedReferencesCollector.getPlayoutStrategy();
		}else{
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating after move strategy MultiplePlayoutAfterMove. " +
					"The referenced playout strategy is of type " + sharedReferencesCollector.getPlayoutStrategy().getClass().getSimpleName() +
					"instead of the required MultiplePlayout type.");
			throw new RuntimeException("Wrong playout type when instantiating MultiplePlayoutAfterMove strategy!");
		}

	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing

	}

	@Override
	public void afterMoveActions() {
		this.multiplePlayout.logAndResetCallStatistics();
		//this.multiplePlayout.resetStepStatistics();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "MULTIPLE_PLAYOUT = " + this.multiplePlayout.getClass().getSimpleName();
	}

}
