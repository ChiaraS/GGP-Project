package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class SequentialParametersTuner extends ParametersTuner {

	/**
	 * Tuner that will be used to tune one parameter at a time.
	 * (Must be reset whenever we want to change the parameter being tuned!)
	 */
	private ParametersTuner parametersTuner;

	public SequentialParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(
			SharedReferencesCollector sharedReferencesCollector) {
		// TODO Auto-generated method stub

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
	public void setClassesLength(int[] classesLength){
		super.setClassesLength(classesLength);

		/* Untested code! Initializes an array of tuners, one for each parameter.
		// Keep a reference to the only instance we have of the parameters tuner we want to use to tune one single parameter at a time
		ParametersTuner p = this.parametersTuners[0];
		Class<?> theClass = this.parametersTuners[0].getClass();

		this.parametersTuners = (ParametersTuner[]) Array.newInstance(theClass, this.classesLength.length);

		Constructor<?> theConstructor = SearchManagerComponent.getCopyConstructorForSearchManagerComponent(theClass);

		// For each parameter create the tuner
		for(int i = 0; i < this.classesLength.length; i++){
			try {
				this.parametersTuners[i] = (ParametersTuner) theConstructor.newInstance(theClass.cast(p));
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}


	@Override
	public int[][] selectNextCombinations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateStatistics(int[] rewards) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void logStats() {
		// TODO Auto-generated method stub

	}

}
