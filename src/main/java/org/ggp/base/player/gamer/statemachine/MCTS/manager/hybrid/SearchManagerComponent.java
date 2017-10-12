package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.ContinuousTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.DiscreteTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.Interval;
import org.ggp.base.util.logging.GamerLogger;

import com.google.common.collect.ImmutableSet;

/**
 * This class specifies parameters and methods that must be common to all components of the search manager
 * (the components of the search manager are strategies (e.g. selection, playout, before/after simulation,
 * backpropagation, ecc...), move evaluators (e.g. UCTEvaluator, GraveEvaluator, ecc...), single or joint
 * move selectors (e.g RandomMoveSelector, EpsilonMastMoveSelector, ecc...)).
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
 * NOTE: each instance of SearchManagerComponent in the methods setReferences(), clearComponent() and setUpComponent()
 * must make sure to call the same methods for all of its parameters that extend SearchManagerComponent, but ONLY
 * if those parameters were created by such SearchManagerComponent (e.g. the component GraveAfterSimulation has a
 * reference to the GraveSelection, but it sets it in the setReferences() method,  doesn't create the GraveSelection
 * itself, thus it doesn't have to call setReferences(), clearComponent() and setUpComponent() for the GraveSelection).
 *
 * @author c.sironi
 *
 */
public abstract class SearchManagerComponent {

	/**
	 * Arguments of the main constructor.
	 */
	public static Class<?>[] CONSTRUCTOR_ARGS = new Class[] {GameDependentParameters.class, Random.class,
		GamerSettings.class, SharedReferencesCollector.class};

	/**
	 * Arguments of the constructor for the components that can be created in multiple instances.
	 * The constructor needs an extra parameter, the ID, that is needed when we want to create more than one instance
	 * of the same SearchManagerComponent abstract subclass.
	 */
	public static Class<?>[] MULTI_INSTANCE_CONSTRUCTOR_ARGS = new Class[] {GameDependentParameters.class, Random.class,
		GamerSettings.class, SharedReferencesCollector.class, String.class};

	/**
	 * This method looks in the given list of classes for the class with the given name.
	 *
	 * @param allClasses
	 * @param searchManagerComponentName
	 * @return
	 */
	public static <T> Constructor<?> getConstructorForSearchManagerComponent(Class<?> theCorrespondingClass){

		Constructor<?> constructor;
		try {
			constructor = theCorrespondingClass.getConstructor(CONSTRUCTOR_ARGS);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when retrieving constructor for class " + theCorrespondingClass.getSimpleName() + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		return constructor;

	}

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
			constructor = theCorrespondingClass.getConstructor(MULTI_INSTANCE_CONSTRUCTOR_ARGS);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when retrieving cnstructor for multi instance class " + theCorrespondingClass.getSimpleName() + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		return constructor;

	}

	/**
	 * This method looks in the given list of classes for the class with the given name.
	 *
	 * @param allClasses
	 * @param searchManagerComponentName
	 * @return
	 */
	public static <T> Constructor<?> getCopyConstructorForSearchManagerComponent(Class<?> theCorrespondingClass){

		Constructor<?> constructor;
		try {
			constructor = theCorrespondingClass.getConstructor(theCorrespondingClass);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when retrieving copy-constructor for class " + theCorrespondingClass.getSimpleName() + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		return constructor;

	}

	public static <T> Class<?> getCorrespondingClass(ImmutableSet<Class<? extends T>> allClasses, String className){

		Class<?> theCorrespondingClass = null;
		for (Class<?> componentClass : allClasses) {
    		if(componentClass.getSimpleName().equals(className)){
    			theCorrespondingClass = componentClass;
    		}
    	}

		if(theCorrespondingClass == null){
			GamerLogger.logError("SearchManagerCreation", "Cannot find class " + className + " to create SearchManagerComponent.");
			throw new RuntimeException("Cannot find class " + className + " to create SearchManagerComponent.");
		}

		return theCorrespondingClass;

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
	 * @param gamerSettings map built from the properties file that specifies the settings of the gamer (i.e. which strategies
	 * it must use, which types of components, which values for the parameters, ecc ...)
	 * @param sharedReferencesCollector collects the references to parameters created by the constructor that other search
	 * manager components also need to have.
	 */
	public SearchManagerComponent(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		this.gameDependentParameters = gameDependentParameters;
		this.random = random;
	}

	/*
	public SearchManagerComponent(SearchManagerComponent toCopy) {
		this.gameDependentParameters = toCopy.getGameDependentParameters();
		this.random = toCopy.getRandom();
	}*/

	/**
	 * Called after creating all components. Sets references of the component to objects created during creation.
	 *
	 * @param gamerSettings
	 * @param sharedReferencesCollector
	 */
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
	 * Parameters that are set permanently are reported with UPPER CASE names, while the ones that
	 * change during the game or between differenet games are reported with lower case names.
	 *
	 * @return a string representing the component parameters and their values.
	 */
	public abstract String getComponentParameters(String indentation);

	/**
	 * Creates a string representing the exact name of the component and the parameters
	 * it is using as returned by getStrategyParameters().
	 *
	 * @return a string representing the exact name of the component and the parameters
	 * it is using.
	 */
	public String printComponent(String indentation) {
		String params = this.getComponentParameters(indentation);

		if(params != null){
			return this.getClass().getSimpleName() + params;
		}else{
			return this.getClass().getSimpleName();
		}
	}

	public GameDependentParameters getGameDependentParameters(){
		return this.gameDependentParameters;
	}

	public Random getRandom(){
		return this.random;
	}

	/**
	 * Methods to help the construction of tunable parameters.
	 * They retrieve the appropriate settings and check that all the values are correctly specified.
	 */

	/*
	protected IntTunableParameter createIntTunableParameter(String callingClass, String parameterName, GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		IntTunableParameter theParameter;

		// Get default value for the parameter (this is the value used for the roles for which we are not tuning the parameter)
		int fixedParam = gamerSettings.getIntPropertyValue(callingClass + ".fixed" + parameterName);

		if(gamerSettings.getBooleanPropertyValue(callingClass + ".tune" + parameterName)){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// 'CallingClass'.valuesFor'ParameterName'=v1;v2;...;vn
			// (e.g. BetaComputer.valuesForK=v1;v2;...;vn)
			// The values are listed separated by ; with no spaces.
			// Moreover, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			int[] possibleValues = gamerSettings.getIntPropertyMultiValue(callingClass + ".valuesFor" + parameterName);
			// No need to check if at least one possible value is specified, because if no values are specified an exception
			// will already be thrown when trying to retrieve such values.
			// However, we must check that the specified fixed value is among the possible values.
			int index = 0;
			while(index < possibleValues.length && possibleValues[index] != fixedParam){
				index++;
			}
			if(index == possibleValues.length){
				GamerLogger.logError("SearchManagerCreation", "Error when creating tunable parameter " + parameterName + " for class " +
						callingClass + ". The fixed value " + fixedParam + "for the parameter does not appear in the set of possible values!");
				throw new RuntimeException("SearchManagerCreation - possibleValues and possibleValuesPenalty have different length!");
			}
			double[] possibleValuesPenalty = null;
			if(gamerSettings.specifiesProperty(callingClass + ".possibleValuesPenaltyFor" + parameterName)){
				possibleValuesPenalty = gamerSettings.getDoublePropertyMultiValue(callingClass + ".possibleValuesPenaltyFor" + parameterName);
				// If specified, the possibleValuesPenalty must have the same length as the possibleValues
				if(possibleValuesPenalty.length != possibleValues.length){
					GamerLogger.logError("SearchManagerCreation", "Error when creating tunable parameter " + parameterName +
							" for class " + callingClass + ". There are " + possibleValues.length +
							" possible values for the tunable parameter, but there are " + possibleValuesPenalty.length +
							" possible values penalty specified.");
					throw new RuntimeException("SearchManagerCreation - possibleValues and possibleValuesPenalty have different length!");
				}
			}
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty(callingClass + ".tuningOrderIndex" + parameterName)){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue(callingClass + ".tuningOrderIndex" + parameterName);
			}

			theParameter = new IntTunableParameter(parameterName, fixedParam, possibleValues, possibleValuesPenalty, tuningOrderIndex);

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(theParameter);

		}else{
			theParameter = new IntTunableParameter(parameterName, fixedParam);
		}

		return theParameter;

	}*/

	protected TunableParameter createTunableParameter(String callingClass, String parameterName, GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		// Get default value for the parameter (this is the value used for the roles for which we are not tuning the parameter)
		double fixedParam = gamerSettings.getDoublePropertyValue(callingClass + ".fixed" + parameterName);

		TunableParameter theParameter;

		if(gamerSettings.getBooleanPropertyValue(callingClass + ".tune" + parameterName)){

			// First, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty(callingClass + ".tuningOrderIndex" + parameterName)){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue(callingClass + ".tuningOrderIndex" + parameterName);
			}

			// If we have to tune the parameter then we look in the settings for the values that we must use
			// Note:
			// 1. The format for these values in the file must be the following when we are using discrete values:
			// 'CallingClass'.valuesFor'ParameterName'=v1;v2;...;vn
			// (e.g. BetaComputer.valuesForK=v1;v2;...;vn)
			// The values are listed separated by ; with no spaces.
			// 2. The format for these values in the file must be the following when we are using continuous values:
			// 'CallingClass'.valuesFor'ParameterName'=[v1;v2] (v1 and v2 must be the left and right exreme of the
			// interval and (v1<=v2)).
			// (e.g. BetaComputer.valuesForK=[0;inf])
			// The values must be included in square brackets and separated by ; with no spaces.
			//

			// We check in the settings if possible values are specified as a discrete list or as a continuous interval
			// and create the parameter type accordingly.
			if(gamerSettings.isIntervalProperty(callingClass + ".valuesFor" + parameterName)){
				theParameter = this.createContinuousTunableParameter(fixedParam, tuningOrderIndex, callingClass, parameterName, gamerSettings, sharedReferencesCollector);
			}else{
				theParameter = this.createDiscreteTunableParameter(fixedParam, tuningOrderIndex, callingClass, parameterName, gamerSettings, sharedReferencesCollector);
			}

		}else{
			theParameter = new TunableParameter(parameterName, fixedParam, -1); // No need for tuningOrderIndex when param is not being tuned
		}

		return theParameter;

	}

	protected DiscreteTunableParameter createDiscreteTunableParameter(double fixedParam, int tuningOrderIndex, String callingClass, String parameterName, GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		DiscreteTunableParameter theParameter;

		// If we have to tune the parameter then we look in the setting for all the values that we must use
		// Note: the format for these values in the file must be the following:
		// 'CallingClass'.valuesFor'ParameterName'=v1;v2;...;vn
		// (e.g. BetaComputer.valuesForK=v1;v2;...;vn)
		// The values are listed separated by ; with no spaces.
		double[] possibleValues = gamerSettings.getDoublePropertyMultiValue(callingClass + ".valuesFor" + parameterName);
		// No need to check if at least one possible value is specified, because if no values are specified an exception
		// will already be thrown when trying to retrieve such values.
		// However, we must check that the specified fixed value is among the possible values.
		int index = 0;
		while(index < possibleValues.length && possibleValues[index] != fixedParam){
			index++;
		}
		if(index == possibleValues.length){
			GamerLogger.logError("SearchManagerCreation", "Error when creating tunable parameter " + parameterName + " for class " +
					callingClass + ". The fixed value " + fixedParam + "for the parameter does not appear in the set of possible values!");
			throw new RuntimeException("SearchManagerCreation - possibleValues and possibleValuesPenalty have different length!");
		}

		double[] possibleValuesPenalty = null;
		if(gamerSettings.specifiesProperty(callingClass + ".possibleValuesPenaltyFor" + parameterName)){
			possibleValuesPenalty = gamerSettings.getDoublePropertyMultiValue(callingClass + ".possibleValuesPenaltyFor" + parameterName);
			// If specified, the possibleValuesPenalty must have the same length as the possibleValues
			if(possibleValuesPenalty.length != possibleValues.length){
				GamerLogger.logError("SearchManagerCreation", "Error when creating tunable parameter " + parameterName +
						" for class " + callingClass + ". There are " + possibleValues.length +
						" possible values for the tunable parameter, but there are " + possibleValuesPenalty.length +
						" possible values penalty specified.");
				throw new RuntimeException("SearchManagerCreation - possibleValues and possibleValuesPenalty have different length!");
			}
		}

		theParameter = new DiscreteTunableParameter(parameterName, fixedParam, tuningOrderIndex, possibleValues, possibleValuesPenalty);

		// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
		sharedReferencesCollector.addDiscreteParameterToTune(theParameter);

		return theParameter;

	}

	protected ContinuousTunableParameter createContinuousTunableParameter(double fixedParam, int tuningOrderIndex, String callingClass, String parameterName, GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		ContinuousTunableParameter theParameter;

		// If we have to tune the parameter then we look in the setting for all the values that we must use
		// Note: The format for these values in the file must be the following when we are using continuous values:
		// 'CallingClass'.valuesFor'ParameterName'=[v1;v2) (v1 and v2 must be the left and right extreme of the
		// interval and the interval must be non-empty).
		// (e.g. BetaComputer.valuesForK=[0;inf))
		// The values must be included in round or square brackets and separated by ; with no spaces.
		Interval possibleValuesInterval = gamerSettings.getDoublePropertyIntervalValue(callingClass + ".valuesFor" + parameterName);
		// We must check that the specified fixed value is in the interval of possible values.
		if(possibleValuesInterval.contains(fixedParam)){
			GamerLogger.logError("SearchManagerCreation", "Error when creating tunable parameter " + parameterName + " for class " +
					callingClass + ". The fixed value " + fixedParam + "for the parameter is not included in the interval of possible values!");
			throw new RuntimeException("SearchManagerCreation - fixed parameter value not included in specified interval!");
		}

		theParameter = new ContinuousTunableParameter(parameterName, fixedParam, tuningOrderIndex, possibleValuesInterval);

		// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
		sharedReferencesCollector.addContinuousParameterToTune(theParameter);

		return theParameter;

	}

}
