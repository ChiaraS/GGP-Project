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
