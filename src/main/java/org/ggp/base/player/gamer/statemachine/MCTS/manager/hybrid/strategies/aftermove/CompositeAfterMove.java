package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

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

public class CompositeAfterMove extends AfterMoveStrategy {

	private List<AfterMoveStrategy> afterMoveStrategies;

	public CompositeAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.afterMoveStrategies = new ArrayList<AfterMoveStrategy>();

		int i = 0;
		AfterMoveStrategy afterMoveStrategy;
		String[] afterMoveStrategyDetails;

		while(gamerSettings.specifiesProperty("AfterMoveStrategy" + id + ".subAfterMoveStrategy"+i)){

			afterMoveStrategyDetails = gamerSettings.getIDPropertyValue("AfterMoveStrategy" + id + ".subAfterMoveStrategy"+i);

			try {
				afterMoveStrategy = (AfterMoveStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(
						SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_MOVE_STRATEGIES.getConcreteClasses(),
								afterMoveStrategyDetails[0])).newInstance(gameDependentParameters, random, gamerSettings,
										sharedReferencesCollector, afterMoveStrategyDetails[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating subAfterMoveStrategy " + gamerSettings.getPropertyValue("AfterMoveStrategy" + id + ".subAfterMoveStrategy"+i) + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}

			this.afterMoveStrategies.add(afterMoveStrategy);

			i++;
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		for(AfterMoveStrategy afterMoveStrategy : this.afterMoveStrategies){
			afterMoveStrategy.setReferences(sharedReferencesCollector);
		}
	}

	@Override
	public void clearComponent() {
		for(AfterMoveStrategy afterMoveStrategy : this.afterMoveStrategies){
			afterMoveStrategy.clearComponent();
		}

	}

	@Override
	public void setUpComponent() {
		for(AfterMoveStrategy afterMoveStrategy : this.afterMoveStrategies){
			afterMoveStrategy.setUpComponent();
		}
	}

	@Override
	public void afterMoveActions() {
		for(AfterMoveStrategy afterMoveStrategy : this.afterMoveStrategies){
			afterMoveStrategy.afterMoveActions();
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = "";

		int i = 0;
		for(AfterMoveStrategy afterMoveStrategy : this.afterMoveStrategies){
			params += indentation + "AFTER_MOVE_STRATEGY_" + i + " = " + afterMoveStrategy.printComponent(indentation + "  ");
			i++;
		}

		return params;
	}

}
