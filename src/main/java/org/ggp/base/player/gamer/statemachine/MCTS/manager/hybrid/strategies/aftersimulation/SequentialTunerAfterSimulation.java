package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion.SequentialTunerBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

/**
 * Use this after-simulation strategy for the sequential tuning of parameters if you want it
 * to change the tuned parameter after each fixed number of simulations.
 *
 * @author C.Sironi
 *
 */
public class SequentialTunerAfterSimulation extends TunerAfterSimulation {

	private SequentialTunerBeforeSimulation sequentialTunerBeforeSimulation;

	/**
	 * Number of simulations that must be used to tune every single parameter.
	 * After tuning a parameter for simPerParameter simulations, switch to tuning
	 * the next parameter.
	 *
	 * NOTE: it makes no sense to set this smaller than the number of simulations
	 * per batch used by the SequentialTunerBeforeSimulation because otherwise we
	 * will only evaluate the same single value for each parameter.
	 *
	 * ALSO NOTE: whenever we change the parameter being tuned we also reset the
	 * simCountForBatch in the SequentialTunerBeforeSimulation class!
	 */
	private int simPerParameter;

	/**
	 * Counts the simulations that have been ran for the current parameter.
	 * When it reaches the value simPerParameter it is reset to 0 and it means we must
	 * change the parameter being tuned.
	 */
	private int simCountForParameter;

	public SequentialTunerAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.simPerParameter = gamerSettings.getIntPropertyValue("AfterSimulationStrategy" + id + ".simPerParameter");

		this.simCountForParameter = 0;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.sequentialTunerBeforeSimulation = sharedReferencesCollector.getSequentialTunerBeforeSimulation();
	}

	@Override
	public void clearComponent() {

		super.clearComponent();

		this.simCountForParameter = 0;

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.simCountForParameter = 0;

	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {

		super.afterSimulationActions(simulationResult);

		this.simCountForParameter++;

		if(this.simCountForParameter == this.simPerParameter){
	//		this.sequentialTunerBeforeSimulation.startTuningNextParameter();
			this.simCountForParameter = 0;
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "BEFORE_SIMULATION_STRATEGY = " + this.sequentialTunerBeforeSimulation.getClass().getSimpleName() +
				indentation + "SIM_PER_PARAMETER = " + this.simPerParameter +
				indentation + "sim_count_for_parameter = " + this.simCountForParameter;

		if(superParams == null){
			return params;
		}


		return superParams + params;
	}

}
