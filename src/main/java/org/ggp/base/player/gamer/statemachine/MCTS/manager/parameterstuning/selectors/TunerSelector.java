package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.logging.GamerLogger;

import com.google.common.collect.ImmutableSet;

public abstract class TunerSelector extends SearchManagerComponent{

	/**
	 * Arguments of the constructor for the TunerSelectors.
	 * The constructor needs an extra parameter, the ID, that is needed when we want to create more than one instance
	 * of the same SearchManagerComponent abstract subclass.
	 */
	public static Class<?>[] CONSTRUCTOR_ARGS = new Class[] {GameDependentParameters.class, Random.class,
		GamerSettings.class, SharedReferencesCollector.class, String.class};

	public TunerSelector(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,	SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	/**
	 * This method looks in the given list of classes for the class with the given name.
	 *
	 * @param allClasses
	 * @param searchManagerComponentName
	 * @return
	 */
	public static <T> Constructor<?> getConstructorForTunerSelector(ImmutableSet<Class<? extends T>> allClasses, String searchManagerComponentName){

		Class<?> theCorrespondingClass = null;
		for (Class<?> componentClass : allClasses) {
    		if(componentClass.getSimpleName().equals(searchManagerComponentName)){
    			theCorrespondingClass = componentClass;
    		}
    	}

		if(theCorrespondingClass == null){
			GamerLogger.logError("SearchManagerCreation", "Cannot find class " + searchManagerComponentName + " to create SearchManagerComponent.");
			throw new RuntimeException("Cannot find class " + searchManagerComponentName + " to create SearchManagerComponent.");
		}

		Constructor<?> constructor;
		try {
			constructor = theCorrespondingClass.getConstructor(CONSTRUCTOR_ARGS);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when retrieving cnstructor for class " + searchManagerComponentName + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		return constructor;

	}

	public abstract int selectMove(MoveStats[] movesStats, int numUpdates);

}
