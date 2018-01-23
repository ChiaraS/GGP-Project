package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.continuoustuners;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class OnePlusOneESTuner extends ContinuousParametersTuner {

    /**
     * For each class, for each unit move in the class, this array specifies the penalty.
     * NOTE: the penalty values must be specified for either all or none of the classes,
     * otherwise an exception must be thrown.
     * If there is no penalty specified in the gamers settings for any of the classes,
     * then this pointer will be null.
     *
     * @param gameDependentParameters
     * @param random
     * @param gamerSettings
     * @param sharedReferencesCollector
     */
    public OnePlusOneESTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
        super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
    }

    @Override
    public void setNextCombinations() {

    }

    @Override
    public void setBestCombinations() {

    }

    @Override
    public void updateStatistics(int[] goals) {

    }

    @Override
    public void logStats() {

    }

    @Override
    public void decreaseStatistics(double factor) {

    }

    @Override
    public boolean isMemorizingBestCombo() {
        return false;
    }

    @Override
    public void memorizeBestCombinations() {

    }
}
