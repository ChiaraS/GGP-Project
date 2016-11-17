package org.ggp.base.player.gamer.statemachine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.util.logging.GamerLogger;

public abstract class ConfigurableStateMachineGamer extends StateMachineGamer {

	protected GamerConfiguration gamerSettings;

	public ConfigurableStateMachineGamer() {
		this("/home/csironi/GamersSettings/DuctMctsGamer.properties");
	}

	public ConfigurableStateMachineGamer(String settingsFilePath) {
		File settingsFile = new File(settingsFilePath);

		if(!settingsFile.isFile()){
			GamerLogger.logError("Gamer", "Impossible to create gamer, cannot find the .properties file with the settings.");
			throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
		}

		try {
			FileReader reader = new FileReader(settingsFile);
			Properties props = new Properties();

			// load the properties file:
			props.load(reader);

			reader.close();

			this.gamerSettings = new GamerConfiguration(props);

		} catch (FileNotFoundException e) {
			this.gamerSettings = null;
			GamerLogger.logError("Gamer", "Impossible to create gamer, cannot find the .properties file with the settings.");
			throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
		} catch (IOException e) {
			this.gamerSettings = null;
			GamerLogger.logError("Gamer", "Impossible to create gamer, exception when reading the .properties file with the settings.");
			throw new RuntimeException("Impossible to create gamer, exception when reading the .properties file with the settings.");
		}
	}

}
