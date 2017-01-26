package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.util.logging.GamerLogger;

public abstract class MultiInstanceSearchManagerComponent extends SearchManagerComponent {

	/**
	 * Arguments of the constructor for the TunerSelectors.
	 * The constructor needs an extra parameter, the ID, that is needed when we want to create more than one instance
	 * of the same SearchManagerComponent abstract subclass.
	 */
	public static Class<?>[] CONSTRUCTOR_ARGS = new Class[] {GameDependentParameters.class, Random.class,
		GamerSettings.class, SharedReferencesCollector.class, String.class};

	/**
	 * This method looks in the given list of classes for the class with the given name.
	 *
	 * @param allClasses
	 * @param searchManagerComponentName
	 * @return
	 */
	public static <T> Constructor<?> getConstructorForMultiInstanceSearchManagerComponent(Class<?> theCorrespondingClass){

		Constructor<?> constructor;
		try {
			constructor = theCorrespondingClass.getConstructor(CONSTRUCTOR_ARGS);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when retrieving cnstructor for multi instance class " + theCorrespondingClass.getSimpleName() + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		return constructor;

	}

	public MultiInstanceSearchManagerComponent(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings,
				sharedReferencesCollector);
	}

	public MultiInstanceSearchManagerComponent(MultiInstanceSearchManagerComponent toCopy) {
		super(toCopy);
	}

}
