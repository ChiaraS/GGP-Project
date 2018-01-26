package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.rescalers;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class LinearValueRescaler extends ValueRescaler {

    public LinearValueRescaler(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	/**
     * This class re-scales a value x in R using a LINEAR function that maps the interval [min, max]
     * to the interval [newMin, newMax].
     */
	@Override
	public double mapToInterval(double x, double min, double max, double newMin, double newMax) {
		return newMin + (((x-min)/(max-min)) * (newMax-newMin));
	}


	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

	/*
	public static void main(String args[]) {

		double[] xes = new double[]{-1.5, -1.4, -1.3, -1.2, -1.1, -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5};

		for(int i = 0; i < xes.length; i++) {
			System.out.println("f(" + xes[i] + ") = " + toInterval(xes[i], 0, 1, 0, 30));
		}

	}

	public static double toInterval(double x, double min, double max, double newMin, double newMax) {
		return newMin + (((x-min)/(max-min)) * (newMax-newMin));
	}*/


}
