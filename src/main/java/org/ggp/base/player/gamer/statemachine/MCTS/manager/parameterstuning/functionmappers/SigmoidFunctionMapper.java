package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.functionmappers;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class SigmoidFunctionMapper extends FunctionMapper {

    public SigmoidFunctionMapper(GameDependentParameters gameDependentParameters, Random random,
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
     * Map the value in R to [lowerBound, upperBound]
     * @param upperBound
     * @param lowerBound
     * @param valueInR
     * @return
     */
	@Override
	public double mapToInterval(double upperBound, double lowerBound, double valueInR) {
		// TODO Auto-generated method stub
		return ( mapToOne(valueInR)*(upperBound-lowerBound) + lowerBound );
	}

    /**
     * Map the value in R to (0, 1)
     * @param valueInR
     * @return
     */
    private double mapToOne(double valueInR) {
        return ( ( valueInR / Math.sqrt(1+valueInR*valueInR) + 1 ) / 2 );
//        return Math.exp(valueInR) / (1 + Math.exp(valueInR));
    }

	@Override
	public String getComponentParameters(String indentation) {
		// TODO Auto-generated method stub
		return null;
	}

}
