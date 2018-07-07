package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftergame;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class CompositeAfterGame extends AfterGameStrategy {

	private List<AfterGameStrategy> afterGameStrategies;

	public CompositeAfterGame(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.afterGameStrategies = new ArrayList<AfterGameStrategy>();

		int i = 0;
		AfterGameStrategy afterGameStrategy;
		String[] afterGameStrategyDetails;

		while(gamerSettings.specifiesProperty("AfterGameStrategy" + id + ".subAfterGameStrategy"+i)){

			afterGameStrategyDetails = gamerSettings.getIDPropertyValue("AfterGameStrategy" + id + ".subAfterGameStrategy"+i);

			try {
				afterGameStrategy = (AfterGameStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(
						SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_GAME_STRATEGIES.getConcreteClasses(),
								afterGameStrategyDetails[0])).newInstance(gameDependentParameters, random, gamerSettings,
										sharedReferencesCollector, afterGameStrategyDetails[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating subAfterGameStrategy " + gamerSettings.getPropertyValue("AfterGameStrategy" + id + ".subAfterGameStrategy"+i) + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}

			this.afterGameStrategies.add(afterGameStrategy);

			i++;
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		for(AfterGameStrategy afterGameStrategy : this.afterGameStrategies){
			afterGameStrategy.setReferences(sharedReferencesCollector);
		}
	}

	@Override
	public void clearComponent() {
		for(AfterGameStrategy afterGameStrategy : this.afterGameStrategies){
			afterGameStrategy.clearComponent();
		}

	}

	@Override
	public void setUpComponent() {
		for(AfterGameStrategy afterGameStrategy : this.afterGameStrategies){
			afterGameStrategy.setUpComponent();
		}
	}

	@Override
	public void afterGameActions(List<Double> terminalGoals) {
		for(AfterGameStrategy afterGameStrategy : this.afterGameStrategies){
			afterGameStrategy.afterGameActions(terminalGoals);
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = "";

		int i = 0;
		for(AfterGameStrategy afterGameStrategy : this.afterGameStrategies){
			params += indentation + "AFTER_GAME_STRATEGY_" + i + " = " + afterGameStrategy.printComponent(indentation + "  ");
			i++;
		}

		return params;
	}

}
