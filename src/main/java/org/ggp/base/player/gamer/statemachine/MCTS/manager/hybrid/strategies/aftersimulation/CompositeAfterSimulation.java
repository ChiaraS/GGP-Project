package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

/**
 * This class creates and calls sequentially in the order specified by the gamers settings file
 * an arbitrary number of after-simulation strategies. This is useful when using multiple strategies
 * together that require different processing after each simulation.
 *
 * For example, when using GRAVE and TUNING at the same time use this class to call sequentially
 * after each simulation the GraveAfterSimulation strategy and the TunerAfterSimulation strategy.
 *
 * @author C.Sironi
 *
 */
public class CompositeAfterSimulation extends AfterSimulationStrategy {

	private List<AfterSimulationStrategy> simulationStrategies;

	public CompositeAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.simulationStrategies = new ArrayList<AfterSimulationStrategy>();

		int i = 0;
		AfterSimulationStrategy afterSimulationStrategy;
		String[] afterSimulationStrategyDetails;

		while(gamerSettings.specifiesProperty("AfterSimulationStrategy" + id + ".subAfterSimulationStrategy"+i)){

			afterSimulationStrategyDetails = gamerSettings.getIDPropertyValue("AfterSimulationStrategy" + id + ".subAfterSimulationStrategy"+i);

			try {
				afterSimulationStrategy = (AfterSimulationStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(
						SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_SIMULATION_STRATEGIES.getConcreteClasses(),
								afterSimulationStrategyDetails[0])).newInstance(gameDependentParameters, random,
										gamerSettings, sharedReferencesCollector, afterSimulationStrategyDetails[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating subAfterSimulationStrategy " + gamerSettings.getPropertyValue("AfterSimulationStrategy" + id + ".subAfterSimulationStrategy"+i) + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}

			this.simulationStrategies.add(afterSimulationStrategy);

			i++;
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		for(AfterSimulationStrategy afterSimulationStrategy : this.simulationStrategies){
			afterSimulationStrategy.setReferences(sharedReferencesCollector);
		}
	}

	@Override
	public void clearComponent() {
		for(AfterSimulationStrategy afterSimulationStrategy : this.simulationStrategies){
			afterSimulationStrategy.clearComponent();
		}

	}

	@Override
	public void setUpComponent() {
		for(AfterSimulationStrategy afterSimulationStrategy : this.simulationStrategies){
			afterSimulationStrategy.setUpComponent();
		}
	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {
		for(AfterSimulationStrategy afterSimulationStrategy : this.simulationStrategies){
			afterSimulationStrategy.afterSimulationActions(simulationResult);
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = "";

		int i = 0;
		for(AfterSimulationStrategy afterSimulationStrategy : this.simulationStrategies){
			params += indentation + "AFTER_SIM_STRATEGY_" + i + " = " + afterSimulationStrategy.printComponent(indentation + "  ");
			i++;
		}

		return params;
	}

}
