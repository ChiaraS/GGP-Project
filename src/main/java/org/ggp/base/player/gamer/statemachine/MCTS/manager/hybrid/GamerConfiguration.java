package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.util.Properties;

import org.ggp.base.util.logging.GamerLogger;

public class GamerConfiguration {

	/**
	 * The properties read from the configuration file.
	 */
	private Properties properties;

	public GamerConfiguration(Properties properties) {

		this.properties = properties;

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

}
