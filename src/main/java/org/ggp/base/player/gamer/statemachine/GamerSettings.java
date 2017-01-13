package org.ggp.base.player.gamer.statemachine;

import java.util.Properties;

import org.ggp.base.util.logging.GamerLogger;

public class GamerSettings {

	/**
	 * The properties read from the configuration file.
	 */
	private Properties properties;

	public GamerSettings(Properties properties) {

		this.properties = properties;

	}

	public boolean specifiesProperty(String propertyName){

		String propertyValue = this.properties.getProperty(propertyName);

		return !(propertyValue == null || propertyValue.equals("") || propertyValue.equals("null"));

	}

	public String getPropertyValue(String propertyName){

		String propertyValue = this.properties.getProperty(propertyName);

		if(propertyValue == null){
			GamerLogger.logError("SearchManagerCreation", "Cannot find property " + propertyName + ". Verify if it is correctly specified in the properties file for the gamer.");
			throw new RuntimeException("Cannot find property " + propertyName + ".");
		}

		return propertyValue;
	}

	/**
	 * Checks for a property that specifies the type of a class for which we want to create multiple instances.
	 * In these cases the property specifies the exact name of the class and, if multiple instances of the same
	 * class are planned to be created, an ID to distinguish the multiple instances when setting the parameters
	 * from the settings file.
	 *
	 * The ID distinguishes the creation of multiple classes that descend from the same abstract class.
	 * This is needed to specify different parameters values when more than one instance of the same abstract
	 * class is created. For example if we are creating two TunerSelector instances they will both read the
	 * same settings from the settings file. With the ID we can specify the settings twice with separate values.
	 *
	 * @param propertyName
	 * @return an array where the first entry is the name of the class we want to instantiate, and the second entry
	 * is the ID that such instance will have and use when getting values to set from the settings file.
	 */
	public String[] getIDPropertyValue(String propertyName){

		String[] splitProperty = this.getPropertyValue(propertyName).split("\\.");

		if(splitProperty.length < 0 || splitProperty.length > 2){
			GamerLogger.logError("SearchManagerCreation", "The type property " + propertyName + " is not correctly specified. Specify an existing class name, followed by its ID if you want to create more than one instance of the class, separated by \".\".");
			throw new RuntimeException("Cannot find property " + propertyName + ".");
		}

		String[] toReturn;
		if(splitProperty.length == 1){
			toReturn = new String[2];
			toReturn[0] = splitProperty[0];
			toReturn[1] = "";
		}else{
			toReturn = splitProperty;
		}

		return toReturn;

	}

	public int getIntPropertyValue(String propertyName){

		String propertyString = this.getPropertyValue(propertyName);

		if(propertyString.equalsIgnoreCase("inf")){
			return Integer.MAX_VALUE;
		}else{
			return Integer.parseInt(propertyString);
		}

	}

	public double getDoublePropertyValue(String propertyName){

		String propertyString = this.getPropertyValue(propertyName);

		if(propertyString.equalsIgnoreCase("inf")){
			return Double.MAX_VALUE;
		}else{
			return Double.parseDouble(propertyString);
		}

	}

	public long getLongPropertyValue(String propertyName){

		String propertyString = this.getPropertyValue(propertyName);

		if(propertyString.equalsIgnoreCase("inf")){
			return Long.MAX_VALUE;
		}else{
			return Long.parseLong(propertyString);
		}

	}

	public boolean getBooleanPropertyValue(String propertyName){

		String propertyString = this.getPropertyValue(propertyName);

		return Boolean.parseBoolean(propertyString);

	}

	/**
	 * Looks for a property that specifies multiple values and returns them as an array of strings.
	 * This method assumes that the values for the property are separated by ";" and checks that
	 * at least one value is specified.
	 *
	 * @param propertyName
	 * @return
	 */
	public String[] getPropertyMultiValue(String propertyName){

		String[] propertyMultiValue = this.getPropertyValue(propertyName).split(";");

		if(propertyMultiValue.length < 1){
			GamerLogger.logError("SearchManagerCreation", "Cannot find multiple values for property " + propertyName + ". Verify if it is correctly specified in the properties file for the gamer.");
			throw new RuntimeException("Cannot find property " + propertyName + ".");
		}

		return propertyMultiValue;
	}

	/**
	 * Looks for a property that specifies multiple values and returns them as an array of integers.
	 * This method assumes that the values for the property are separated by ";" and checks that
	 * at least one value is specified.
	 *
	 * @param propertyName
	 * @return
	 */
	public int[] getIntPropertyMultiValue(String propertyName){

		String[] propertyMultiValue = this.getPropertyMultiValue(propertyName);

		int[] toReturn = new int[propertyMultiValue.length];

		for(int i = 0; i < propertyMultiValue.length; i++){
			if(propertyMultiValue[i].equalsIgnoreCase("inf")){
				toReturn[i] = Integer.MAX_VALUE;
			}else{
				toReturn[i] = Integer.parseInt(propertyMultiValue[i]);
			}
		}

		return toReturn;
	}

	/**
	 * Looks for a property that specifies multiple values and returns them as an array of integers.
	 * This method assumes that the values for the property are separated by ";" and checks that
	 * at least one value is specified.
	 *
	 * @param propertyName
	 * @return
	 */
	public double[] getDoublePropertyMultiValue(String propertyName){

		String[] propertyMultiValue = this.getPropertyMultiValue(propertyName);

		double[] toReturn = new double[propertyMultiValue.length];

		for(int i = 0; i < propertyMultiValue.length; i++){
			if(propertyMultiValue[i].equalsIgnoreCase("inf")){
				toReturn[i] = Double.MAX_VALUE;
			}else{
				toReturn[i] = Double.parseDouble(propertyMultiValue[i]);
			}
		}

		return toReturn;

	}

}
