package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.TdBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion.SequentialTunerBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.GraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
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

	private List<TunableParameter> theParametersToTune;

	private GlobalExtremeValues globalExtremeValues;

	private SingleParameterEvolutionManager singleParameterEvolutionManager;

	private ParametersTuner parametersTuner;

	private GraveSelection graveSelection;

	private TdBackpropagation tdBackpropagation;

	private ProgressiveHistoryGraveSelection progressiveHistoryGraveSelection;

	private PlayoutStrategy playoutStrategy;

	private SequentialTunerBeforeSimulation sequentialTunerBeforeSimulation;

	public SharedReferencesCollector() {
		// TODO Auto-generated constructor stub
	}

	public void setMastStatistics(Map<Move, MoveStats> mastStatistics){
		// Can only be set once
		if(this.mastStatistics == null){
			this.mastStatistics = mastStatistics;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set MastStatistics multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set MastStatistics multiple times!");
		}
	}

	public Map<Move, MoveStats> getMastStatistics(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.mastStatistics != null){
			return this.mastStatistics;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get MastStatistics that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get MastStatistics that have never been set!");
		}
	}

	public void addParameterToTune(TunableParameter theParameterToTune){
		// Can only be set once
		if(this.theParametersToTune == null){
			this.theParametersToTune = new ArrayList<TunableParameter>();
			this.theParametersToTune.add(theParameterToTune);
		}else if(this.theParametersToTune.contains(theParameterToTune)){
			GamerLogger.logError("SearchManagerCreation", "Trying to add duplicate TunableParameter to theParametersToTune multiple times!");
			throw new RuntimeException("Trying to add duplicate TunableParameter to theParametersToTune multiple times!");
		}else{
			this.theParametersToTune.add(theParameterToTune);
		}
	}

	public List<TunableParameter> getTheParametersToTune(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.theParametersToTune != null && !this.theParametersToTune.isEmpty()){
			return this.theParametersToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TheParametersToTune that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TheParametersToTune that have never been set!");
		}
	}

	public void setGlobalExtremeValues(GlobalExtremeValues globalExtremeValues){
		// Can only be set once
		if(this.globalExtremeValues == null){
			this.globalExtremeValues = globalExtremeValues;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set GlobalExtremeValues multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set GlobalExtremeValues multiple times!");
		}
	}

	public GlobalExtremeValues getGlobalExtremeValues(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.globalExtremeValues != null){
			return this.globalExtremeValues;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get GlobalExtremeValues that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get GlobalExtremeValues that have never been set!");
		}
	}

	public void setSingleParameterEvolutionManager(SingleParameterEvolutionManager singleParameterEvolutionManager){
		// Can only be set once
		if(this.singleParameterEvolutionManager == null){
			this.singleParameterEvolutionManager = singleParameterEvolutionManager;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set SingleParameterEvolutionManager multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set SingleParameterEvolutionManager multiple times!");
		}
	}

	public SingleParameterEvolutionManager getSingleParameterEvolutionManager(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.singleParameterEvolutionManager != null){
			return this.singleParameterEvolutionManager;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get SingleParameterEvolutionManager that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get SingleParameterEvolutionManager that has never been set!");
		}
	}

	public void setParametersTuner(ParametersTuner parametersTuner){
		// Can only be set once
		if(this.parametersTuner == null){
			this.parametersTuner = parametersTuner;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set ParameterTuner multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set ParameterTuner multiple times!");
		}
	}

	public ParametersTuner getParametersTuner(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.parametersTuner != null){
			return this.parametersTuner;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get ParametersTuner that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get ParametersTuner that has never been set!");
		}
	}

	public void setGraveSelection(GraveSelection graveSelection){
		// Can only be set once
		if(this.graveSelection == null){
			this.graveSelection = graveSelection;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set GraveSelection multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set GraveSelection multiple times!");
		}
	}

	public GraveSelection getGraveSelection(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.graveSelection != null){
			return this.graveSelection;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get GraveSelection that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get GraveSelection that has never been set!");
		}
	}

	public void setTdBackpropagation(TdBackpropagation tdBackpropagation){
		// Can only be set once
		if(this.tdBackpropagation == null){
			this.tdBackpropagation = tdBackpropagation;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set TdBackpropagation multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set TdBackpropagation multiple times!");
		}
	}

	public TdBackpropagation getTdBackpropagation(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.tdBackpropagation != null){
			return this.tdBackpropagation;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TdBackpropagation that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TdBackpropagation that has never been set!");
		}
	}

	public void setProgressiveHistoryGraveSelection(ProgressiveHistoryGraveSelection progressiveHistoryGraveSelection){
		// Can only be set once
		if(this.progressiveHistoryGraveSelection == null){
			this.progressiveHistoryGraveSelection = progressiveHistoryGraveSelection;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set ProgressiveHistoryGraveSelection multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set ProgressiveHistoryGraveSelection multiple times!");
		}
	}

	public ProgressiveHistoryGraveSelection getProgressiveHistoryGraveSelection(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.progressiveHistoryGraveSelection != null){
			return this.progressiveHistoryGraveSelection;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get ProgressiveHistoryGraveSelection that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get ProgressiveHistoryGraveSelection that has never been set!");
		}
	}

	public void setPlayoutStrategy(PlayoutStrategy playoutStrategy){
		// Can only be set once
		if(this.playoutStrategy == null){
			this.playoutStrategy = playoutStrategy;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set PlayoutStrategy multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set PlayoutStrategy multiple times!");
		}
	}

	public PlayoutStrategy getPlayoutStrategy(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.playoutStrategy != null){
			return this.playoutStrategy;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get PlayoutStrategy that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get PlayoutStrategy that has never been set!");
		}
	}

	public void setSequentialTunerBeforeSimulation(SequentialTunerBeforeSimulation sequentialTunerBeforeSimulation){
		// Can only be set once
		if(this.sequentialTunerBeforeSimulation == null){
			this.sequentialTunerBeforeSimulation = sequentialTunerBeforeSimulation;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set SequentialTunerBeforeSimulation multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set SequentialTunerBeforeSimulation multiple times!");
		}
	}

	public SequentialTunerBeforeSimulation getSequentialTunerBeforeSimulation(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.sequentialTunerBeforeSimulation != null){
			return this.sequentialTunerBeforeSimulation;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get SequentialTunerBeforeSimulation that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get SequentialTunerBeforeSimulation that has never been set!");
		}
	}

}
