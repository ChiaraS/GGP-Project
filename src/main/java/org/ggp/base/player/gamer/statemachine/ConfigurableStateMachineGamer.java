package org.ggp.base.player.gamer.statemachine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.logging.GamerLogger;

public abstract class ConfigurableStateMachineGamer extends StateMachineGamer {

	protected static final String defaultSettingsFileName = "HierarchicalSingleMabTunerCadiaGraveMastDuct.properties";

	protected String settingsFilePath;

	//protected GamerSettings gamerSettings;

	protected String gamerType;

	public ConfigurableStateMachineGamer() {
		this(GamerConfiguration.gamersSettingsFolderPath + "/" + defaultSettingsFileName);
	}

	public ConfigurableStateMachineGamer(String settingsFilePath) {
		this.settingsFilePath = settingsFilePath;

		File settingsFile = new File(this.settingsFilePath);

		if(!settingsFile.isFile()){
			GamerLogger.logError("Gamer", "Impossible to create gamer, cannot find the .properties file with the settings: " + settingsFilePath + ".");
			throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
		}

		this.gamerType = settingsFile.getName().split("\\.")[0];

		try {
			FileReader reader = new FileReader(settingsFile);
			Properties props = new Properties();

			// load the properties file:
			props.load(reader);

			reader.close();

			GamerSettings gamerSettings = new GamerSettings(props);

			this.configureGamer(gamerSettings);

		} catch (FileNotFoundException e) {
			//this.gamerSettings = null;
			GamerLogger.logError("Gamer", "Impossible to create gamer, cannot find the .properties file with the settings: " + settingsFilePath + ".");
			throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
		} catch (IOException e) {
			//this.gamerSettings = null;
			GamerLogger.logError("Gamer", "Impossible to create gamer, exception when reading the .properties file with the settings: " + settingsFilePath + ".");
			throw new RuntimeException("Impossible to create gamer, exception when reading the .properties file with the settings.");
		}
	}

	protected abstract void configureGamer(GamerSettings gamerSettings);

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#getName()
	 */
	@Override
	public String getName() {
		/*String type = "";
		if(this.singleGame){
			type = "SingleGame";
		}else{
			type = "Starndard";
		}
		return getClass().getSimpleName() + "-" + type;*/

		return this.gamerType + getClass().getSimpleName();
	}

	public void logGamerSettings(){

		String toLog = "\nLogging settings for gamer " + this.getName() + ".\n" + this.printGamer() + "\n";

		GamerLogger.log("GamerSettings", toLog);

	}

	protected String printGamer(){
		return "SETTINGS_FILE = " + this.settingsFilePath;
	}

}
