package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.ContinuousTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders.ParametersOrder;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class ContinuousParametersManager extends SearchManagerComponent {
    /**
     * List of parameters to be tuned
     * @param gameDependentParameters
     * @param random
     * @param gamerSettings
     * @param sharedReferencesCollector
     */
    private List<ContinuousTunableParameter> tunableParameters;
    private ParametersOrder initialParametersOrder;

    public ContinuousParametersManager(GameDependentParameters gameDependentParameters, Random random,
                                       GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
        super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
        this.tunableParameters = null;
        try {
            this.initialParametersOrder = (ParametersOrder) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PARAMETERS_ORDER.getConcreteClasses(),
                    gamerSettings.getPropertyValue("ParametersManager.initialParametersOrderType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            // TODO: fix this!
            GamerLogger.logError("SearchManagerCreation", "Error when instantiating ParametersOrder " + gamerSettings.getPropertyValue("ParametersManager.initialParametersOrderType") + ".");
            GamerLogger.logStackTrace("SearchManagerCreation", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
        this.tunableParameters = sharedReferencesCollector.getTheContinuousParametersToTune();

        if(this.tunableParameters == null || this.tunableParameters.size() == 0){
            GamerLogger.logError("SearchManagerCreation", "ParametersManager - Initialization with null or empty list of tunable parameters!");
            throw new RuntimeException("ParametersManager - Initialization with null or empty list of tunable parameters!");
        }

        this.initialParametersOrder.imposeOrder(this.tunableParameters);
    }

    @Override
    public void clearComponent() {
        this.initialParametersOrder.clearComponent();
    }

    @Override
    public void setUpComponent() {
        this.initialParametersOrder.setUpComponent();
    }


    @Override
    // TODO: 03/11/2017 This is not useful for continuous case
    public String getComponentParameters(String indentation) {
        return null;
    }
}
