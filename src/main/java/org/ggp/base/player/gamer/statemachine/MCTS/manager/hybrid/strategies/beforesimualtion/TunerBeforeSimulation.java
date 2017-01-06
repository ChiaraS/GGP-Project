package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.CombinatorialTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.UcbCombinatorialTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class TunerBeforeSimulation extends BeforeSimulationStrategy {

	private boolean tuneAllRoles;

	private CombinatorialTuner combinatorialTuner;

	/**
	 * List of the components that we are tuning.
	 */
	private List<OnlineTunableComponent> tunableComponents;

	public TunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tuneAllRoles = Boolean.parseBoolean(gamerSettings.getPropertyValue("BeforeSimulationStrategy.tuneAllRoles"));

		double tunerC = Double.parseDouble(gamerSettings.getPropertyValue("BeforeSimulationStrategy.tunerC"));
		double tunerValueOffset = Double.parseDouble(gamerSettings.getPropertyValue("BeforeSimulationStrategy.tunerValueOffset"));
		String tunerFpuString = gamerSettings.getPropertyValue("BeforeSimulationStrategy.tunerFpu");
		double tunerFpu;
		if(tunerFpuString.equalsIgnoreCase("max")){
			tunerFpu = Double.MAX_VALUE;
		}else{
			tunerFpu = Double.parseDouble(tunerFpuString);

		}

		this.combinatorialTuner = new UcbCombinatorialTuner(random, tunerC, tunerValueOffset, tunerFpu);

		sharedReferencesCollector.setCombinatorialTuner(combinatorialTuner);

		// Here the combinatorial tuner has no classes lengths.
		// They will be initialized when setting references to the tunable components.

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.tunableComponents = sharedReferencesCollector.getTheComponentsToTune();

		int[] classesLength = new int[this.tunableComponents.size()];

		int i = 0;
		for(OnlineTunableComponent c : this.tunableComponents){
			classesLength[i] = c.getPossibleValues().length;
			i++;
		}

		this.combinatorialTuner.setClassesLength(classesLength);

	}

	@Override
	public void clearComponent(){
		// It's not the job of this class to clear the tunable component because the component
		// is for sure either another strategy or part of another strategy. A class must be
		// responsible of clearing only the objects that it was responsible for creating.
		this.combinatorialTuner.clear();
	}

	@Override
	public void setUpComponent(){

		if(this.tuneAllRoles){
			this.combinatorialTuner.setUp(this.gameDependentParameters.getNumRoles());
		}else{
			this.combinatorialTuner.setUp(1);
		}

	}

	@Override
	public void beforeSimulationActions() {

		int[][] nextCombinations = this.combinatorialTuner.selectNextCombinations();

		int i = 0;
		for(OnlineTunableComponent c : this.tunableComponents){
			int[] newValuesIndices = new int[nextCombinations.length]; // nextCombinations.length equals the number of roles for which we are tuning

			for(int j = 0; j < nextCombinations.length; j++){
				newValuesIndices[j] = nextCombinations[j][i];
			}

			c.setNewValuesFromIndices(newValuesIndices);

			i++;
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "TUNE_ALL_ROLES = " + this.tuneAllRoles + indentation + "COMBINATORIAL_TUNER = " + this.combinatorialTuner.printCombinatorialTuner(indentation + "  ");

		if(this.tunableComponents != null){

			String tunableComponentsString = "[ ";

			for(OnlineTunableComponent c : this.tunableComponents){

				tunableComponentsString += c.printOnlineTunableComponent(indentation + "  ");

			}

			tunableComponentsString += "\n]";

			params += indentation + "ONLINE_TUNABLE_COMPONENTS = " + tunableComponentsString;
		}else{
			params += indentation + "ONLINE_TUNABLE_COMPONENTS = null";
		}

		return params;

	}

}
