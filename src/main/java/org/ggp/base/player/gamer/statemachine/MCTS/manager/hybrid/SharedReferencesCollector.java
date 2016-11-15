package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class is used to keep a reference to all the objects that are created by any of the components
 * of the search manager and are needed also by other components of the manager.
 *
 * During creation object that must be shared are placed in this class. After creation this class is used
 * by the components to find the references to the shared objects they need.
 *
 * NOTE that this class is used only during creation and then is useless.
 *
 * @author C.Sironi
 *
 */
public class SharedReferencesCollector {

	private Map<Move, MoveStats> mastStatistics;

	private OnlineTunableComponent theComponentToTune;

	public SharedReferencesCollector() {
		// TODO Auto-generated constructor stub
	}

	public void setMastStatistics(Map<Move, MoveStats> mastStatistics){
		// Can only be set once
		if(this.mastStatistics == null){
			this.mastStatistics = mastStatistics;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set mastStatistics multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set mastStatistics multiple times!");
		}
	}

	public Map<Move, MoveStats> getMastStatistics(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.mastStatistics != null){
			return this.mastStatistics;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get mastStatistics that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get mastStatistics that have never been set!");
		}
	}

	public void setTheComponentToTune(OnlineTunableComponent theComponentToTune){
		// Can only be set once
		if(this.theComponentToTune == null){
			this.theComponentToTune = theComponentToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set theComponentToTune multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set theComponentToTune multiple times!");
		}
	}

	public OnlineTunableComponent getTheComponentToTune(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.theComponentToTune != null){
			return this.theComponentToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get theComponentToTune that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get theComponentToTune that has never been set!");
		}
	}

}
