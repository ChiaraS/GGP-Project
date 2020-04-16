package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.TdBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.GraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.discretetuners.SimLimitedLsiParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousParametersManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.DiscreteParametersManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.ContinuousTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.DiscreteTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaWeights;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsTranspositionTable;
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

	private List<Map<Move, MoveStats>> mastStatistics;

	private List<DiscreteTunableParameter> theDiscreteParametersToTune;

	private List<ContinuousTunableParameter> theContinuousParametersToTune;

	private GlobalExtremeValues globalExtremeValues;

	private SingleParameterEvolutionManager singleParameterEvolutionManager;

	private ParametersTuner parametersTuner;

	private GraveSelection graveSelection;

	private TdBackpropagation tdBackpropagation;

	private ProgressiveHistoryGraveSelection progressiveHistoryGraveSelection;

	private PlayoutStrategy playoutStrategy;

	private DiscreteParametersManager discreteParametersManager;

	private ContinuousParametersManager continuousParametersManager;

	private MctsTranspositionTable transpositionTable;

	private SimLimitedLsiParametersTuner simLimitedLsiParametersTuner;

	private List<MctsJointMove> currentSimulationJointMoves;

	private List<Map<Move, Double>> weightsPerMove;

	private PpaWeights ppaWeights;

	private List<NGramTreeNode<MoveStats>> nstStatistics;


	public SharedReferencesCollector() {
		// TODO Auto-generated constructor stub
	}

	public void setMastStatistics(List<Map<Move, MoveStats>> mastStatistics){
		// Can only be set once
		if(this.mastStatistics == null){
			this.mastStatistics = mastStatistics;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set MastStatistics multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set MastStatistics multiple times!");
		}
	}

	public List<Map<Move, MoveStats>> getMastStatistics(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.mastStatistics != null){
			return this.mastStatistics;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get MastStatistics that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get MastStatistics that have never been set!");
		}
	}

	public void addDiscreteParameterToTune(DiscreteTunableParameter theDiscreteParameterToTune){
		// Can only be set once
		if(this.theDiscreteParametersToTune == null){
			this.theDiscreteParametersToTune = new ArrayList<DiscreteTunableParameter>();
			this.theDiscreteParametersToTune.add(theDiscreteParameterToTune);
		}else if(this.theDiscreteParametersToTune.contains(theDiscreteParameterToTune)){
			GamerLogger.logError("SearchManagerCreation", "Trying to add duplicate DiscreteTunableParameter to theDiscreteParametersToTune multiple times!");
			throw new RuntimeException("Trying to add duplicate TunableParameter to theDiscreteParametersToTune multiple times!");
		}else{
			this.theDiscreteParametersToTune.add(theDiscreteParameterToTune);
		}
	}

	public List<DiscreteTunableParameter> getTheDiscreteParametersToTune(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.theDiscreteParametersToTune != null && !this.theDiscreteParametersToTune.isEmpty()){
			return this.theDiscreteParametersToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TheDiscreteParametersToTune that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TheDiscreteParametersToTune that have never been set!");
		}
	}

	public void addContinuousParameterToTune(ContinuousTunableParameter theContinuousParameterToTune){
		// Can only be set once
		if(this.theContinuousParametersToTune == null){
			this.theContinuousParametersToTune = new ArrayList<ContinuousTunableParameter>();
			this.theContinuousParametersToTune.add(theContinuousParameterToTune);
		}else if(this.theContinuousParametersToTune.contains(theContinuousParameterToTune)){
			GamerLogger.logError("SearchManagerCreation", "Trying to add duplicate ContinuousTunableParameter to theContinuousParametersToTune multiple times!");
			throw new RuntimeException("Trying to add duplicate ContinuousTunableParameter to theContinuousParametersToTune multiple times!");
		}else{
			this.theContinuousParametersToTune.add(theContinuousParameterToTune);
		}
	}

	public List<ContinuousTunableParameter> getTheContinuousParametersToTune(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.theContinuousParametersToTune != null && !this.theContinuousParametersToTune.isEmpty()){
			return this.theContinuousParametersToTune;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TheContinuousParametersToTune that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TheContinuousParametersToTune that have never been set!");
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

	public void setDiscreteParametersManager(DiscreteParametersManager discreteParametersManager){
		this.discreteParametersManager = discreteParametersManager;
	}

	public DiscreteParametersManager getDiscreteParametersManager(){
		return this.discreteParametersManager;
	}

	public void setContinuousParametersManager(ContinuousParametersManager continuousParametersManager){
		this.continuousParametersManager = continuousParametersManager;
	}

	public ContinuousParametersManager getContinuousParametersManager(){
		return this.continuousParametersManager;
	}

	public void setTranspositionTable(MctsTranspositionTable transpositionTable){
		// Can only be set once
		if(this.transpositionTable == null){
			this.transpositionTable = transpositionTable;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set TranspositionTable multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set TranspositionTable multiple times!");
		}
	}

	public MctsTranspositionTable getTranspositionTable(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.transpositionTable != null){
			return this.transpositionTable;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get TranspositionTable that has never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get TranspositionTable that has never been set!");
		}
	}

	public SimLimitedLsiParametersTuner getSimLimitedLsiParametersTuner() {
		return this.simLimitedLsiParametersTuner;
	}

	public void setSimLimitedLsiParametersTuner(SimLimitedLsiParametersTuner simLimitedLsiParametersTuner) {
		this.simLimitedLsiParametersTuner = simLimitedLsiParametersTuner;
	}

	public List<MctsJointMove> getCurrentSimulationJointMoves() {
		return this.currentSimulationJointMoves;
	}

	public void setCurrentSimulationJointMoves(List<MctsJointMove> currentSimulationJointMoves) {
		this.currentSimulationJointMoves = currentSimulationJointMoves;
	}

	public void setWeightsPerMove(List<Map<Move, Double>> weightsPerMove){
		// Can only be set once
		if(this.weightsPerMove == null){
			this.weightsPerMove = weightsPerMove;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to set WeightsPerMove multiple times! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to set WeightsPerMove multiple times!");
		}
	}

	public List<Map<Move, Double>> getWeightsPerMove(){
		// If a strategy looks for the reference then another strategy must have set it
		if(this.weightsPerMove != null){
			return this.weightsPerMove;
		}else{
			GamerLogger.logError("SearchManagerCreation", "Trying to get WeightsPerMove that have never been set! Probably a wrong combination of strategies has been set.");
			throw new RuntimeException("Trying to get WeightsPerMove that have never been set!");
		}
	}

	public void setPpaWeights(PpaWeights ppaWeights) {
		this.ppaWeights = ppaWeights;
	}

	public PpaWeights getPpaWeights() {
		return this.ppaWeights;
	}

	public void setNstStatistics(List<NGramTreeNode<MoveStats>> nstStatistics) {
		this.nstStatistics = nstStatistics;
	}

	public List<NGramTreeNode<MoveStats>> getNstStatistics() {
		return this.nstStatistics;
	}


}
