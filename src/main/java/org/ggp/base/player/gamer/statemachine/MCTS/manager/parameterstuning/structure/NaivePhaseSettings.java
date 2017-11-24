package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.NaiveProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

public class NaivePhaseSettings extends SearchManagerComponent{

	private double epsilon0;

	private TunerSelector globalMabSelector;

	private TunerSelector localMabsSelector;

	private ParametersManager parametersManager;

	public NaivePhaseSettings(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.epsilon0 = gamerSettings.getDoublePropertyValue("NaivePhaseSettings" + id + ".epsilon0");

		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("NaivePhaseSettings" + id + ".globalMabSelectorType");

		try {
			this.globalMabSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("NaivePhaseSettings" + id + ".globalMabSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		tunerSelectorDetails = gamerSettings.getIDPropertyValue("NaivePhaseSettings" + id + ".localMabsSelectorType");

		try {
			this.localMabsSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("NaivePhaseSettings" + id + ".localMabsSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.globalMabSelector.setReferences(sharedReferencesCollector);
		this.localMabsSelector.setReferences(sharedReferencesCollector);
		this.parametersManager = sharedReferencesCollector.getParametersManager();
	}

	@Override
	public void setUpComponent() {
		this.globalMabSelector.setUpComponent();
		this.localMabsSelector.setUpComponent();
	}

	@Override
	public void clearComponent() {
		this.globalMabSelector.clearComponent();
		this.localMabsSelector.clearComponent();
	}

	public int[][] selectNextCombinations(NaiveProblemRepresentation[] roleProblems) {

		int[][] selectedCombinations = new int[roleProblems.length][];

		// For each role, we check the corresponding naive problem and select a combination of parameters
		for(int roleProblemIndex = 0; roleProblemIndex < roleProblems.length; roleProblemIndex++){
			// TODO: the strategy that selects if to use the global or the local mabs is hard-coded.
			// Can be refactored to be customizable.
			if(roleProblems[roleProblemIndex].getGlobalMab().getNumUpdates() > 0 &&
					this.random.nextDouble() >= this.epsilon0){// Exploit

				selectedCombinations[roleProblemIndex] = this.exploit(roleProblems[roleProblemIndex].getGlobalMab());

			}else{ //Explore

				selectedCombinations[roleProblemIndex] = this.explore(roleProblems[roleProblemIndex].getLocalMabs());

			}
		}

		return selectedCombinations;

	}

	/**
	 * Given the global MAB, selects one of the combinatorial moves in the MAB according to
	 * the strategy specified for the global MAB.
	 *
	 * @param globalMab
	 * @return
	 */
	private int[] exploit(IncrementalMab globalMab){
		Move m = this.globalMabSelector.selectMove(globalMab.getMovesInfo(), null, globalMab.getNumUpdates());
		return ((CombinatorialCompactMove) m).getIndices();
	}

	private int[] explore(FixedMab[] localMabs){
		int[] indices = new int[localMabs.length];
		for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){
			indices[paramIndex] = -1; // It means that the index has not been set yet
		}

		for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){
			indices[paramIndex] = this.localMabsSelector.selectMove(localMabs[paramIndex].getMoveStats(),
					this.parametersManager.getValuesFeasibility(paramIndex, indices),
					// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
					(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
					localMabs[paramIndex].getNumUpdates());
		}

		/*
		// Select a value for each local mab independently
		if(this.unitMovesPenalty != null){
			for(int i = 0; i < localMabs.length; i++){
				indices[i] = this.localMabsSelector.selectMove(localMabs[i].getMoveStats(), this.unitMovesPenalty[i], localMabs[i].getNumUpdates());
			}
		}else{
			for(int i = 0; i < localMabs.length; i++){
				indices[i] = this.localMabsSelector.selectMove(localMabs[i].getMoveStats(), null, localMabs[i].getNumUpdates());
			}
		}
		*/

		return indices;

	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "EPSILON0 = " + this.epsilon0 +
				indentation + "GLOBAL_MAB_SELECTOR = " + this.globalMabSelector.printComponent(indentation + "  ") +
				indentation + "LOCAL_MABS_SELECTOR = " + this.localMabsSelector.printComponent(indentation + "  ") +
				indentation + "PARAMETERS_MANAGER = " + this.parametersManager.printComponent(indentation + "  ");
	}

}
