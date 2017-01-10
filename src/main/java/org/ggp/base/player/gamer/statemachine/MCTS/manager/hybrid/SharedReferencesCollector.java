package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.CombinatorialTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.TdBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.GraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
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

	//private OnlineTunableComponent theComponentToTune;

	private List<OnlineTunableComponent> theComponentsToTune;

	private GlobalExtremeValues globalExtremeValues;

	private SingleParameterEvolutionManager singleParameterEvolutionManager;

	private CombinatorialTuner combinatorialTuner;

	private GraveSelection graveSelection;

	private TdBackpropagation tdBackpropagation;

	private ProgressiveHistoryGraveSelection progressiveHistoryGraveSelection;

	private PlayoutStrategy playoutStrategy;

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

	/*
	public void setTheComponentToTune(OnlineTunableComponent theComponentToTune){
		// Can only be set once
		if(this.theComponentToTune == null){
			this.theComponentToTune = theComponentToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set TheComponentToTune multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set TheComponentToTune multiple times!");
		}
	}

	public OnlineTunableComponent getTheComponentToTune(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.theComponentToTune != null){
			return this.theComponentToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TheComponentToTune that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TheComponentToTune that has never been set!");
		}
	}
	*/

	public void addComponentToTune(OnlineTunableComponent theComponentToTune){
		// Can only be set once
		if(this.theComponentsToTune == null){
			this.theComponentsToTune = new ArrayList<OnlineTunableComponent>();
			this.theComponentsToTune.add(theComponentToTune);
		}else if(this.theComponentsToTune.contains(theComponentToTune)){
			GamerLogger.logError("SearchManagerCreation", "Trying to add duplicate OnlineTunableComponent to TheComponentToTune multiple times!");
			throw new RuntimeException("Trying to add duplicate OnlineTunableComponent to TheComponentToTune multiple times!");
		}else{
			this.theComponentsToTune.add(theComponentToTune);
		}
	}

	public List<OnlineTunableComponent> getTheComponentsToTune(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.theComponentsToTune != null && !this.theComponentsToTune.isEmpty()){
			return this.theComponentsToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TheComponentsToTune that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TheComponentsToTune that have never been set!");
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

	public void setCombinatorialTuner(CombinatorialTuner combinatorialTuner){
		// Can only be set once
		if(this.combinatorialTuner == null){
			this.combinatorialTuner = combinatorialTuner;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set CombinatorialTuner multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set CombinatorialTuner multiple times!");
		}
	}

	public CombinatorialTuner getCombinatorialTuner(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.combinatorialTuner != null){
			return this.combinatorialTuner;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get CombinatorialTuner that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get CombinatorialTuner that has never been set!");
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

}
