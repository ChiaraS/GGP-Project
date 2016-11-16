package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.ggp.base.util.logging.GamerLogger;

import com.google.common.collect.ImmutableSet;

/**
 * This class specifies parameters and methods that must be common to all components of the search manager
 * (the components of the search manager are strategies (e.g. selection, playout, before/after simulation,
 * backpropagation, ecc...), move evaluators (e.g. UCTEvaluator, GRAVEEvaluator, ecc...), single or joint
 * move selectors (e.g RandomJointMoveSelector, EpsilonMASTJointMoveSelector, ecc...)).
 *
 * ATTENTION: each component must respect a rule. The constructor must ONLY set the reference to (non primitive)
 * class parameters. The initialization of the content of these parameters needed to play a game must be performed
 * in the setUpComponent() method before playing the game and after knowing which game will be played. The search
 * manager and all its component must be created only once when creating the player, and thus before knowing which
 * game the player has to play. Moreover, since the search manager will be used to play any game during the whole
 * "life" of the game player the methods clear() and setUpComponent() must make sure respectively that, between
 * two games, the search manager is cleared from all data of the previous game and initialized with the data of
 * the new game. (The choice of separating the two methods instead of having a single one that does everything
 * whenever a new game must be played is due to the fact that like this we can clear memory (and run GC) between
 * games without wasting the metagame time for that).
 *
 * @author c.sironi
 *
 */
public abstract class SearchManagerComponent {

	/**
	 * Arguments of the constructor.
	 */
	public static Class<?>[] CONSTRUCTOR_ARGS = new Class[] {GameDependentParameters.class, Random.class,
		GamerConfiguration.class, SharedReferencesCollector.class};

	/**
	 * This method looks in the given list of classes for the class with the given name.
	 *
	 * @param allClasses
	 * @param searchManagerComponentName
	 * @return
	 */
	public static <T> Constructor<?> getConstructorForSearchManagerComponent(ImmutableSet<Class<? extends T>> allClasses, String searchManagerComponentName){

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


	protected GameDependentParameters gameDependentParameters;

	protected Random random;

	/**
	 * The constructor must initialize all the primitive parameters according to the values specified in the properties,
	 * create the objects using the types specified in the properties and make sure that all the objects created by the
	 * class in the constructor that must be shared are also set in the sharedReferencesCollector.
	 *
	 * @param gameDependentParameters parameters that depend on the game being played (e.g. number of roles, index of my
	 * role, the state machine of the game, ...).
	 * @param random
	 * @param gamerConfiguration map built from the properties file that specifies the settings of the gamer (i.e. which strategies
	 * it must use, which types of components, which values for the parameters, ecc ...)
	 * @param sharedReferencesCollector collects the references to parameters created by the constructor that other search
	 * manager components also need to have.
	 */
	public SearchManagerComponent(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		this.gameDependentParameters = gameDependentParameters;
		this.random = random;
	}

	public abstract void setReferences(SharedReferencesCollector sharedReferencesCollector);

	/**
	 * Clears all the parameters and references to object that are game dependent (e.g.
	 * the reference to the state machine, the number of roles, statistics collected so
	 * far, ecc...).
	 *
	 * BE CAREFUL!: when clearing objects, don't change the reference to them (except for
	 * the state machine), but only clear their content, since they might be shared on
	 * purpose with other strategies. If possible make the reference FINAL.
	 */
	public abstract void clearComponent();

	public abstract void setUpComponent();

	/**
	 * Creates a string representing the component parameters and their values
	 * to be used for logging purposes.
	 *
	 * @return a string representing the component parameters and their values.
	 */
	public abstract String getComponentParameters();

	/**
	 * Creates a string representing the exact name of the component and the parameters
	 * it is using as returned by getStrategyParameters().
	 *
	 * @return a string representing the exact name of the component and the parameters
	 * it is using.
	 */
	public abstract String printComponent();

}
