package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftergame.AfterGameStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermetagame.AfterMetagameStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.AfterMoveStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.AfterSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforemove.BeforeMoveStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesearch.BeforeSearchStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion.BeforeSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.LogTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsTranspositionTable;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.TreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AmafDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.statememorizingdecoupled.StateMemorizingMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;
import csironi.ggp.course.experiments.propnet.SingleValueLongStats;
import csironi.ggp.course.utils.MyPair;

/**
 * @author C.Sironi
 *
 * TODO: Change structure to have before/after-simulation, before/after-move and after-game
 * actions performed directly in all components instead of having strategies for those actions
 * too.
 *
 * TODO: change the gamer to always call the MctsManager to perform before and after move
 * actions even when there is not enough time to perform the search (this way the MctsManager
 * can keep the game step up to date and perform actions that should be performed per move,
 * e.g. clean the transposition table, decrease statistics, etc...).
 */
public class HybridMctsManager {

	//--- Parameters used to collect details about search (duration, number of iterations, ecc...) ---//

	/**
	 * Number of performed iterations during the search for the current game step.
	 */
	//private int iterations;
	/**
	 * Number of all visited states since the start of the search.
	 */
	//private int visitedNodes;
	/**
	 * Number of visited nodes in the current iteration so far.
	 */
	//private int currentIterationVisitedNodes;
	/**
	 * Start time of last performed search.
	 */
	//private long searchStart;
	/**
	 * End time of last performed search.
	 */
	//private long searchEnd;

	/**
	 * Random generator.
	 */
	private Random random;

	/**
	 * Memorizes the joint moves performed so far in the simulation in the tree.
	 */
	private List<MctsJointMove> currentSimulationJointMoves;

	//------------------------ Parameters to make the reward function noisy --------------------------//

	/**
	 * Probability of having a flip sample (i.e. probability that the goals that get propagated back
	 * in the three are flip).
	 */
	private double pFlip;
	/**
	 * True if I want the possibility to flip scores with probability pFlip only when my role is winning.
	 * False if I want the possibility to flip scores with probability pFlip always.
	 */
	private boolean flipWinsOnly;

	//-------------------------- Parameters needed to perform the search -----------------------------//

	/**
	 * Maximum depth that the MCTS algorithm must visit.
	 */
	private int maxSearchDepth;

	/**
	 * Number of simulations per search that this MCTS manager can perform.
	 * NOTE that if this number is set to a positive number then the manager
	 * will ignore any time limit and always perform the exact number of
	 * simulations specified by this parameter.
	 */
	private int numExpectedIterations;

	/**
	 * All the game-dependent and global parameters needed by the MctsManager and its strategies.
	 * Must be reset between games.
	 */
	private GameDependentParameters gameDependentParameters;

	//----------------------------------------- Strategies -------------------------------------------//

	/**
	 * Strategies that the MctsManger must use to perform the different MCTS phases.
	 */
	private SelectionStrategy selectionStrategy;

	private ExpansionStrategy expansionStrategy;

	private PlayoutStrategy playoutStrategy;

	private BackpropagationStrategy backpropagationStrategy;

	private MoveChoiceStrategy moveChoiceStrategy;

	/**
	 * Some MCTS strategies require additional work before/after every simulation has been performed
	 * or before/after every move has been played in the real game (e.g. change some parameters, clear
	 * or decay some statistics). The following strategies allow to specify the actions to be taken in
	 * such situations. If nothing has to be done, just set these strategies to null.
	 */
	/**
	 * Performs actions before every search, even if the search is being performed again for the same
	 * game step.
	 */
	private BeforeSearchStrategy beforeSearchStrategy;
	/**
	 * Performs actions after the end of the search for the metagame.
	 */
	private AfterMetagameStrategy afterMetagameStrategy;
	/**
	 * Performs actions before every simulation.
	 */
	private BeforeSimulationStrategy beforeSimulationStrategy;
	/**
	 * Performs actions after every simulation.
	 */
	private AfterSimulationStrategy afterSimulationStrategy;
	/**
	 * Performs actions after every move in the game.
	 * Not performed at the end of each call to the search() method, but only if after the search we
	 * change game step in the real game.
	 */
	private BeforeMoveStrategy beforeMoveStrategy;
	/**
	 * Performs actions after every move in the game.
	 * Not performed at the end of each call to the search() method, but only if after the search we
	 * change game step in the real game.
	 */
	private AfterMoveStrategy afterMoveStrategy;
	/**
	 * If any action needs to be performed the end of every game, this strategy takes care of it.
	 */
	private AfterGameStrategy afterGameStrategy;

	/**
	 * The factory that creates the tree nodes with the necessary structure that the strategies need.
	 * The factory will always return the same node interface with all the methods that the manager
	 * needs, hiding all the specific details of the structure that depend on what the single
	 * strategies need (e.g. the manager needs to only know if a node is terminal, what the goals
	 * of the players are in the node, the number of visits of the node and the game step stamp,
	 * however, if the selection implements Decoupled UCT, it will need a certain structure of the
	 * statistics).
	 * NOTE: always make sure when initializing the manager to assign it the correct node factory,
	 * that creates nodes containing all the information that the strategies need.
	 */
	private TreeNodeFactory treeNodeFactory;

	//------------------------------------ Transposition table -------------------------------------//

	/**
	 * The transposition table (implemented with HashMap that uses the state as key
	 * and solves collisions with linked lists).
	 */
	private MctsTranspositionTable transpositionTable;


	/***************************************************************************************************
	 * ATTENTION: the logging of the tree has been implemented quickly, therefore it is not guaranteed *
	 * to work correctly when the execution of the search is not smooth, e.g. no search is performed   *
	 * for a turn in the game because there was no time.                                               *
	 ***************************************************************************************************/
	/**
	 * Tree representation of the game that does not consider transpositions.
	 * This is used to log the tree built during the game. Set the parameter
	 * 'buildLogTree' to false if you don't want to log the tree and therefore
	 * not build this tree at all.
	 */
	private LogTreeNode logTree;
	/**
	 * If true the log tree is logged, otherwise not.
	 * Note that setting this to true automatically sets the log tree to be built.
	 */
	private boolean logLogTree;
	/**
	 * If true, after each move the branching factor and tree depth are logged, otherwise not.
	 * Note that setting this to true automatically sets the log tree to be built, but not necessarily logged.
	 * TODO: the LogTreeNodes are set up to memorize also the total number of legal joint moves in each node,
	 * so that they can be used to log the entropy of nodes when the distribution of visits over the actions is
	 * uniform or to log the branching factor of the game tree (instead of the effective branching factor of the
	 * search tree that is being logged now).
	 */
	private boolean logTreeBranchingAndDepthAndEntropy;
	/**
	 * Keeps track of the node that is the current root of the log tree
	 */
	private LogTreeNode currentLogRoot;
	/**
	 * Keeps track of the current node being visited in the log tree
	 */
	private LogTreeNode currentLogNode;
	/**
	 * For each step in which the search was performed, keeps track of the number
	 * of simulations performed up till the end of the step.
	 */
	private Map<Integer,Integer> accumSimulationsPerStep;


	/** NOT NEEDED FOR NOW SINCE ALL STRATEGIES ARE SEPARATE
	 * A set containing all the distinct concrete strategy classes only once.
	 * NOTE: two strategies might be implemented by the same concrete class implementing two
	 * different interfaces, this set allows to perform certain operations only once per class.
	 */
	//private Set<Strategy> strategies = new HashSet<Strategy>();

	public HybridMctsManager(Random random, GamerSettings gamerSettings, String gamerType) {

		GamerLogger.log("SearchManagerCreation", "Creating search manager for gamer " + gamerType + ".");

		this.random = random;

		// List of joint moves played during selection. Used by the multiple playout strategy.
		this.currentSimulationJointMoves = new ArrayList<MctsJointMove>();

		if(gamerSettings.specifiesProperty("SearchManager.pFlip")) {
			this.pFlip = gamerSettings.getDoublePropertyValue("SearchManager.pFlip");
		}else {
			this.pFlip = 0.0;
		}
		if(gamerSettings.specifiesProperty("SearchManager.flipWinsOnly")) {
			this.flipWinsOnly = gamerSettings.getBooleanPropertyValue("SearchManager.flipWinsOnly");
		}else {
			this.flipWinsOnly = true;
		}

		this.maxSearchDepth = gamerSettings.getIntPropertyValue("SearchManager.maxSearchDepth");

		if(gamerSettings.specifiesProperty("SearchManager.numExpectedIterations")){
			this.numExpectedIterations = gamerSettings.getIntPropertyValue("SearchManager.numExpectedIterations");
		}else{
			this.numExpectedIterations = -1;
		}
		this.gameDependentParameters = new GameDependentParameters();

		// Create strategies according to the types specified in the gamer configuration
		SharedReferencesCollector sharedReferencesCollector = new SharedReferencesCollector();
		sharedReferencesCollector.setCurrentSimulationJointMoves(currentSimulationJointMoves);

		String propertyValue;
		String[] multiPropertyValue;

		propertyValue = gamerSettings.getPropertyValue("SearchManager.selectionStrategyType");
		try {
			this.selectionStrategy = (SelectionStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.SELECTION_STRATEGIES.getConcreteClasses(),
					propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating SelectionStrategy " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		propertyValue = gamerSettings.getPropertyValue("SearchManager.expansionStrategyType");
		try {
			this.expansionStrategy = (ExpansionStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.EXPANSION_STRATEGIES.getConcreteClasses(),
					propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ExpansionStrategy " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		multiPropertyValue = gamerSettings.getIDPropertyValue("SearchManager.playoutStrategyType");

		try {
			this.playoutStrategy = (PlayoutStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PLAYOUT_STRATEGIES.getConcreteClasses(),
					multiPropertyValue[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, multiPropertyValue[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating PlayoutStrategy " + gamerSettings.getIDPropertyValue("SearchManager.playoutStrategyType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		sharedReferencesCollector.setPlayoutStrategy(this.playoutStrategy);

		propertyValue = gamerSettings.getPropertyValue("SearchManager.backpropagationStrategyType");
		try {
			this.backpropagationStrategy = (BackpropagationStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.BACKPROPAGATION_STRATEGIES.getConcreteClasses(),
					propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating BackpropagationStrategy " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		propertyValue = gamerSettings.getPropertyValue("SearchManager.moveChoiceStrategyType");
		try {
			this.moveChoiceStrategy = (MoveChoiceStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.MOVE_CHOICE_STRATEGIES.getConcreteClasses(),
					propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating MoveChoiceStrategy " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		if(gamerSettings.specifiesProperty("SearchManager.beforeSearchStrategyType")){

			propertyValue = gamerSettings.getPropertyValue("SearchManager.beforeSearchStrategyType");
			try {
				this.beforeSearchStrategy = (BeforeSearchStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.BEFORE_SEARCH_STRATEGIES.getConcreteClasses(),
						propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating BeforeSearchStrategy " + propertyValue + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}

		if(gamerSettings.specifiesProperty("SearchManager.afterMetagameStrategyType")){

			propertyValue = gamerSettings.getPropertyValue("SearchManager.afterMetagameStrategyType");
			try {
				this.afterMetagameStrategy = (AfterMetagameStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_METAGAME_STRATEGIES.getConcreteClasses(),
						propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating AfterMetagameStrategy " + propertyValue + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}

		if(gamerSettings.specifiesProperty("SearchManager.beforeSimulationStrategyType")){

			propertyValue = gamerSettings.getPropertyValue("SearchManager.beforeSimulationStrategyType");
			try {
				this.beforeSimulationStrategy = (BeforeSimulationStrategy) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.BEFORE_SIMULATION_STRATEGIES.getConcreteClasses(),
						propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating BeforeSimulationStrategy " + propertyValue + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}

		if(gamerSettings.specifiesProperty("SearchManager.afterSimulationStrategyType")){

			String[] idPropertyValue = gamerSettings.getIDPropertyValue("SearchManager.afterSimulationStrategyType");
			try {
				this.afterSimulationStrategy = (AfterSimulationStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_SIMULATION_STRATEGIES.getConcreteClasses(),
						idPropertyValue[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, idPropertyValue[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating AfterSimulationStrategy " + gamerSettings.getPropertyValue("SearchManager.afterSimulationStrategyType") + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}

		if(gamerSettings.specifiesProperty("SearchManager.beforeMoveStrategyType")){

			String[] idPropertyValue = gamerSettings.getIDPropertyValue("SearchManager.beforeMoveStrategyType");
			try {
				this.beforeMoveStrategy = (BeforeMoveStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.BEFORE_MOVE_STRATEGIES.getConcreteClasses(),
						idPropertyValue[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, idPropertyValue[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating BeforeMoveStrategy " + gamerSettings.getPropertyValue("SearchManager.beforeMoveStrategyType") + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}

		}

		if(gamerSettings.specifiesProperty("SearchManager.afterMoveStrategyType")){

			String[] idPropertyValue = gamerSettings.getIDPropertyValue("SearchManager.afterMoveStrategyType");
			try {
				this.afterMoveStrategy = (AfterMoveStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_MOVE_STRATEGIES.getConcreteClasses(),
						idPropertyValue[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, idPropertyValue[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating AfterMoveStrategy " + gamerSettings.getPropertyValue("SearchManager.afterMoveStrategyType") + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}

		if(gamerSettings.specifiesProperty("SearchManager.afterGameStrategyType")){

			String[] idPropertyValue = gamerSettings.getIDPropertyValue("SearchManager.afterGameStrategyType");
			try {
				this.afterGameStrategy = (AfterGameStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.AFTER_GAME_STRATEGIES.getConcreteClasses(),
						idPropertyValue[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, idPropertyValue[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating AfterGameStrategy " + gamerSettings.getPropertyValue("SearchManager.afterGameStrategyType") + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}

		propertyValue = gamerSettings.getPropertyValue("SearchManager.treeNodeFactoryType");
		try {
			this.treeNodeFactory = (TreeNodeFactory) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TREE_NODE_FACTORIES.getConcreteClasses(),
					propertyValue)).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TreeNodeFactory " + propertyValue + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.transpositionTable = new MctsTranspositionTable(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		if(!(this.treeNodeFactory instanceof AmafDecoupledTreeNodeFactory)){
			this.transpositionTable.turnOffLogging();
		}

		sharedReferencesCollector.setTranspositionTable(transpositionTable);

		if(gamerSettings.specifiesProperty("SearchManager.logLogTree")) {
			this.logLogTree = gamerSettings.getBooleanPropertyValue("SearchManager.logLogTree");
		}else {
			this.logLogTree = false;
		}

		if(gamerSettings.specifiesProperty("SearchManager.logTreeBranchingAndDepthAndEntropy")) {
			this.logTreeBranchingAndDepthAndEntropy = gamerSettings.getBooleanPropertyValue("SearchManager.logTreeBranchingAndDepthAndEntropy");
		}else {
			this.logTreeBranchingAndDepthAndEntropy = false;
		}

		// Let all strategies set references if needed.
		this.selectionStrategy.setReferences(sharedReferencesCollector);
		this.expansionStrategy.setReferences(sharedReferencesCollector);
		this.playoutStrategy.setReferences(sharedReferencesCollector);
		this.backpropagationStrategy.setReferences(sharedReferencesCollector);
		this.moveChoiceStrategy.setReferences(sharedReferencesCollector);
		if(this.beforeSearchStrategy != null){
			this.beforeSearchStrategy.setReferences(sharedReferencesCollector);
		}
		if(this.afterMetagameStrategy != null){
			this.afterMetagameStrategy.setReferences(sharedReferencesCollector);
		}
		if(this.beforeSimulationStrategy != null){
			this.beforeSimulationStrategy.setReferences(sharedReferencesCollector);
		}
		if(this.afterSimulationStrategy != null){
			this.afterSimulationStrategy.setReferences(sharedReferencesCollector);
		}
		if(this.beforeMoveStrategy != null){
			this.beforeMoveStrategy.setReferences(sharedReferencesCollector);
		}
		if(this.afterMoveStrategy != null){
			this.afterMoveStrategy.setReferences(sharedReferencesCollector);
		}
		if(this.afterGameStrategy != null){
			this.afterGameStrategy.setReferences(sharedReferencesCollector);
		}
		this.treeNodeFactory.setReferences(sharedReferencesCollector);

		GamerLogger.log("SearchManagerCreation", "Creation of search manager for gamer " + gamerType + " ended successfully.");
		//this.strategies.add(this.expansionStrategy);
		//this.strategies.add(this.selectionStrategy);
		//this.strategies.add(this.backpropagationStrategy);
		//this.strategies.add(this.playoutStrategy);
		//this.strategies.add(this.moveChoiceStrategy);
	}

	public String printSearchManager(){

		String toLog = "MCTS_MANAGER_TYPE = " + this.getClass().getSimpleName();

		//toLog += "\nMCTS manager initialized with the following state machine: " + this.theMachine.getName();

		toLog += "\n\nFLIP_PROBABILITY = " + this.pFlip + "\nFLIP_WINS_ONLY = " + this.flipWinsOnly +
				"\nMAX_SEARCH_DEPTH = " + this.maxSearchDepth + "\nNUM_EXPECTED_ITERATIONS = " + numExpectedIterations;

		toLog += "\nTREE_NODE_FACTORY = " + this.treeNodeFactory.printComponent("\n  ");

		//for(Strategy s : this.strategies){
		//	toLog += "\n" + s.printStrategy();
		//}

		toLog += "\nSELECTION_STRATEGY = " + this.selectionStrategy.printComponent("\n  ");
		toLog += "\nEXPANSION_STRATEGY = " + this.expansionStrategy.printComponent("\n  ");
		toLog += "\nPLAYOUT_STRATEGY = " + this.playoutStrategy.printComponent("\n  ");
		toLog += "\nBACKPROPAGATION_STRATEGY = " + this.backpropagationStrategy.printComponent("\n  ");
		toLog += "\nMOVE_CHOICE_STRATEGY = " + this.moveChoiceStrategy.printComponent("\n  ");

		if(this.beforeSearchStrategy != null){
			toLog += "\nBEFORE_SEARCH_STRATEGY = " + this.beforeSearchStrategy.printComponent("\n  ");
		}else{
			toLog += "\nBEFORE_SEARCH_STRATEGY = null";
		}

		if(this.afterMetagameStrategy != null){
			toLog += "\nAFTER_METAGAME_STRATEGY = " + this.afterMetagameStrategy.printComponent("\n  ");
		}else{
			toLog += "\nAFTER_METAGAME_STRATEGY = null";
		}

		if(this.beforeSimulationStrategy != null){
			toLog += "\nBEFORE_SIM_STRATEGY = " + this.beforeSimulationStrategy.printComponent("\n  ");
		}else{
			toLog += "\nBEFORE_SIM_STRATEGY = null";
		}

		if(this.afterSimulationStrategy != null){
			toLog += "\nAFTER_SIM_STRATEGY = " + this.afterSimulationStrategy.printComponent("\n  ");
		}else{
			toLog += "\nAFTER_SIM_STRATEGY = null";
		}

		if(this.beforeMoveStrategy != null){
			toLog += "\nBEFORE_MOVE_STRATEGY = " + this.beforeMoveStrategy.printComponent("\n  ");
		}else{
			toLog += "\nBEFORE_MOVE_STRATEGY = null";
		}

		if(this.afterMoveStrategy != null){
			toLog += "\nAFTER_MOVE_STRATEGY = " + this.afterMoveStrategy.printComponent("\n  ");
		}else{
			toLog += "\nAFTER_MOVE_STRATEGY = null";
		}

		if(this.afterGameStrategy != null){
			toLog += "\nAFTER_GAME_STRATEGY = " + this.afterGameStrategy.printComponent("\n  ");
		}else{
			toLog += "\nAFTER_GAME_STRATEGY = null";
		}

		toLog += "\nTRANSPOSITION_TABLE = " + this.transpositionTable.printComponent("\n  ");

		toLog += "\nLOG_LOG_TREE = " + this.logLogTree;

		toLog += "\nLOG_TREE_BRANCHING_AND_DEPTH_AND_ENTROPY = " + this.logTreeBranchingAndDepthAndEntropy;

		toLog += "\nabstract_state_machine = " + (this.gameDependentParameters.getTheMachine() == null ? "null" : this.gameDependentParameters.getTheMachine().getName());
		toLog += "\nnum_roles = " + this.gameDependentParameters.getNumRoles();
		toLog += "\nmy_role_index = " + this.gameDependentParameters.getMyRoleIndex();
		toLog += "\ncurrent_game_step = " + this.gameDependentParameters.getGameStep();
		String stepScoreSumForRoleStirng;
		if(this.gameDependentParameters.getStepScoreSumForRoles() != null){
			stepScoreSumForRoleStirng = "[ ";
			for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getStepScoreSumForRoles().length; roleIndex++){
				stepScoreSumForRoleStirng += (this.gameDependentParameters.getStepScoreSumForRoles()[roleIndex] + " ");
			}
			stepScoreSumForRoleStirng += "]";
		}else{
			stepScoreSumForRoleStirng =	"null";
		}
		toLog += "\nstep_score_sum_for_role = " + stepScoreSumForRoleStirng;
		toLog += "\nstep_iterations = " + this.gameDependentParameters.getStepIterations();
		toLog += "\nstep_visited_nodes = " + this.gameDependentParameters.getStepVisitedNodes();
		toLog += "\nstep_added_nodes = " + this.gameDependentParameters.getStepAddedNodes();
		toLog += "\nstep_memorized_states = " + this.gameDependentParameters.getMemorizedStates();
		toLog += "\nstep_search_duration = " + this.gameDependentParameters.getStepSearchDuration();
		toLog += "\ncurrent_iteration_visited_nodes = " + this.gameDependentParameters.getCurrentIterationVisitedNodes();

		return toLog;

	}

	public void clearManager(){

		this.selectionStrategy.clearComponent();
		this.expansionStrategy.clearComponent();
		this.playoutStrategy.clearComponent();
		this.backpropagationStrategy.clearComponent();
		this.moveChoiceStrategy.clearComponent();
		if(this.beforeSearchStrategy != null){
			this.beforeSearchStrategy.clearComponent();
		}
		if(this.afterMetagameStrategy != null){
			this.afterMetagameStrategy.clearComponent();
		}
		if(this.beforeSimulationStrategy != null){
			this.beforeSimulationStrategy.clearComponent();
		}
		if(this.afterSimulationStrategy != null){
			this.afterSimulationStrategy.clearComponent();
		}
		if(this.beforeMoveStrategy != null){
			this.beforeMoveStrategy.clearComponent();
		}
		if(this.afterMoveStrategy != null){
			this.afterMoveStrategy.clearComponent();
		}
		if(this.afterGameStrategy != null){
			this.afterGameStrategy.clearComponent();
		}
		this.treeNodeFactory.clearComponent();

		this.transpositionTable.clearComponent();

		this.logTree = null;
		this.currentLogRoot = null;
		this.currentLogNode = null;

		this.gameDependentParameters.clearGameDependentParameters();

	}

	public void setUpManager(AbstractStateMachine theMachine, int numRoles, int myRoleIndex, long actualPlayClock){

		this.gameDependentParameters.resetGameDependentParameters(theMachine, numRoles, myRoleIndex, actualPlayClock);

		this.selectionStrategy.setUpComponent();
		this.expansionStrategy.setUpComponent();
		this.playoutStrategy.setUpComponent();
		this.backpropagationStrategy.setUpComponent();
		this.moveChoiceStrategy.setUpComponent();
		if(this.beforeSearchStrategy != null){
			this.beforeSearchStrategy.setUpComponent();
		}
		if(this.afterMetagameStrategy != null){
			this.afterMetagameStrategy.setUpComponent();
		}
		if(this.beforeSimulationStrategy != null){
			this.beforeSimulationStrategy.setUpComponent();
		}
		if(this.afterSimulationStrategy != null){
			this.afterSimulationStrategy.setUpComponent();
		}
		if(this.beforeMoveStrategy != null){
			this.beforeMoveStrategy.setUpComponent();
		}
		if(this.afterMoveStrategy != null){
			this.afterMoveStrategy.setUpComponent();
		}
		if(this.afterGameStrategy != null){
			this.afterGameStrategy.setUpComponent();
		}
		this.treeNodeFactory.setUpComponent();

		this.transpositionTable.setUpComponent();

		if(this.logLogTree || this.logTreeBranchingAndDepthAndEntropy) {
			this.accumSimulationsPerStep = new HashMap<Integer,Integer>();
		}

		if(this.logTreeBranchingAndDepthAndEntropy) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BranchingLogs", "Step;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "DepthLogs", "Step;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EntropyLogs", "Step;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EntropyDiffLogs", "Step;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");
		}

	}


	/**
	 * This method computes the best move in a state, given the corresponding MCT node.
	 *
	 * @param theNode the tree node for which to choose the best move.
	 * @return the selected best move.
	 * @throws MCTSException if the best move cannot be computed for the state because
	 * it is either terminal or there is some problem with the computation of legal
	 * moves (and thus corresponding statistics).
	 */
	public CompleteMoveStats getBestMove(MctsNode theNode)throws MCTSException{

		// If the node is null or terminal we cannot return any move.
		// Note that the node being terminal might mean that the state is not terminal but legal moves
		// couldn't be correctly computed for all roles.
		if(theNode.isTerminal()){
			throw new MCTSException("Impossible to return a move using the given state as root.");
		}

		//System.out.println();

		//System.out.println();
		//System.out.println();
		//System.out.println("Selecting best move on node: ");
		//System.out.println(node);

		return this.moveChoiceStrategy.chooseBestMove(theNode);
	}

	/**
	 * This method takes care of performing the MCT search on the given state.
	 * First prepares the manager for the search and then actually performs the search.
	 * It also takes care of checking if the search can actually be performed on the given state.
	 *
	 * Note that if there is no time to perform the search the method will just retrieve (or
	 * create if it doesn't exist) the MCT node.
	 *
	 * @param initialState the state from which to start the search.
	 * @param timeout the time by when the method must return.
	 * @param gameStep the game step currently being played.
	 * @return the tree node corresponding to the given initial state.
	 * @throws MCTSException if the search cannot be performed on the state because the
	 * state is either terminal or there is some problem with the computation of legal
	 * moves (and thus corresponding statistics).
	 */
	public MctsNode search(MachineState initialState, long timeout, int currentGameStep) throws MCTSException{

		MctsNode initialNode = this.prepareForSearch(initialState, currentGameStep);

		// We can be sure that the node is not null, but if it is terminal we cannot perform any search.
		// Note that the node being terminal might mean that the state is not terminal but legal moves
		// couldn't be correctly computed for all roles.
		if(initialNode.isTerminal()){
			throw new MCTSException("Impossible to perform search using the given state as root, state is terminal.");
		}

		/*
		 * NOTE! This is executed before every new search even if it's still for the same step.
		 */
		if(this.beforeSearchStrategy != null){
			this.beforeSearchStrategy.beforeMoveActions(timeout);
		}

		this.performSearch(initialState, initialNode, timeout);

		// Put here AfterSearchStrategies

		return initialNode;

	}

	/**
	 * This method prepares the manager to perform MCTS from a given game state.
	 * More precisely, resets the count of visited nodes and performed iterations,
	 * cleans the transposition table according to the game step that is going to
	 * be searched and gets (or creates if it doesn't exist yet) the tree node
	 * corresponding to the given game state.
	 *
	 * @param initialState the state of the game to be used as starting state for
	 * 					   to perform the search.
	 * @param gameStep the current game step being played (needed to clean the transposition
	 * 				   table and be used as time stamp for tree nodes). The manager considers
	 * 				   the steps as starting from 1. 0 or less are not valid!
	 * @return the tree node corresponding to the given initial state.
	 */
	private MctsNode prepareForSearch(MachineState initialState, int currentGameStep){

		// If it's the first time during the game that we call this method the transposition table is empty
		// so we create the first node, otherwise we check if the node is already in the tree.

		MctsNode initialNode = this.transpositionTable.getNode(initialState);

		if(initialNode == null){

			//System.out.println("Creating initial node...");

			initialNode = this.treeNodeFactory.createNewNode(initialState);
			this.transpositionTable.putNode(initialState, initialNode);

		}

		if(this.logLogTree || this.logTreeBranchingAndDepthAndEntropy) {
			if(this.currentLogRoot.getNumLegalJointMoves() == -1) {
				this.currentLogRoot.setNumLegalJointMoves(initialNode.getNumJointMoves());
			}
		}

		return initialNode;
	}

	/**
	 * This method performs the Monte Carlo Tree Search.
	 *
	 * @param initialState the state from where to start the search.
	 * @param initialNode the tree node corresponding to the state from where to start
	 * 					  the search (making it the root of the currently searched tree).
	 * @param timeout the time (in milliseconds) by when the search must end.
	 */
	private void performSearch(MachineState initialState, MctsNode initialNode, long timeout){
		long searchStart = System.currentTimeMillis();

		while(! this.timeToStopSearch(timeout)){

			//System.out.println();

			this.gameDependentParameters.resetIterationStatistics();

			//System.out.println();
			//System.out.println();
			//System.out.println("MyIteration " + this.iterations);

			if(this.beforeSimulationStrategy != null){
				this.beforeSimulationStrategy.beforeSimulationActions();
			}

			//System.out.println();
			//System.out.println("Inizio iterazione");

			// List of joint moves played during selection. Used by the multiple playout strategy.
			this.currentSimulationJointMoves.clear();

			if(this.logLogTree || this.logTreeBranchingAndDepthAndEntropy) {
				this.currentLogNode = this.currentLogRoot;
				this.currentLogNode.increaseVisits();
			}

			SimulationResult[] simulationResult = this.searchNext(initialState, initialNode);
			for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){
				this.gameDependentParameters.increaseStepIterations();
				this.gameDependentParameters.increaseStepScoreSumForRoles(simulationResult[resultIndex].getTerminalGoals());
			}
			this.gameDependentParameters.increaseStepVisitedNodes(this.gameDependentParameters.getCurrentIterationVisitedNodes());

			//((AMAFDecoupledMCTSNode)initialNode).printAMAF();

			if(this.afterSimulationStrategy != null){
				this.afterSimulationStrategy.afterSimulationActions(simulationResult);
			}
			//System.out.println("Iteration: " + this.iterations);
			//System.out.println("Stats: " + ((MASTStrategy)this.playoutStrategy).getNumStats());
		}

		long searchEnd = System.currentTimeMillis();

		this.gameDependentParameters.increaseStepSearchDuration(searchEnd-searchStart);
	}

	/**
	 * This method performs the search on a single tree node.
	 *
	 * More precisely:
	 * - If the node is terminal: stop the search and backpropagate the terminal goals.
	 * - If the search depth limit has been reached: stop the search and backpropagate
	 *   intermediate goals, if they exist, or default goals.
	 * - If the node requires expansion: expand the node and backpropagate the goals
	 *   obtained by performing a playout.
	 * - In any other case: select the next node to visit and backpropagate the goals
	 *   obtained by recursively calling this method.
	 *
	 * @param currentState the state being visited.
	 * @param currentNode the tree node corresponding to the visited state.
	 * @return a list with the result(s) obtained by the current MCTS iteration and that
	 *         must be backpropagated (the list will have more than one result if multiple
	 *         playouts were performed).
	 */
	private SimulationResult[] searchNext(MachineState currentState, MctsNode currentNode) {

		/*for(GdlSentence s : ((ExplicitMachineState) currentState).getContents()) {
			if(s instanceof GdlProposition) {
				System.out.println("GdlProposition " + s);
			}else if(s instanceof GdlRelation) {
				System.out.println("GdlRelation " + s);
			}else {
				System.out.println("Unidentified " + s);
			}

		}*/

		//System.out.println();
		//System.out.println("Search step:");

		//System.out.println();

		/*
		try {
			System.out.println("Current state(Terminal:" + this.gameDependentParameters.getTheMachine().isTerminal(currentState) + "):");
		} catch (StateMachineException e1) {
			System.out.println("Cannot compute if state is terminal.");
		}
		System.out.println(this.gameDependentParameters.getTheMachine().convertToExplicitMachineState(currentState));
		*/

		//System.out.println("Current node");
		//System.out.println(currentNode);

		//System.out.println();

		//int[] goals;

		SimulationResult[] simulationResult;


		/*
		try {
			boolean terminalState = this.gameDependentParameters.getTheMachine().isTerminal(currentState);

			if(terminalState != currentNode.isTerminal()){
				System.out.println("NODE AND STATE DISAGREE ON TERMINALITY: NODE=" + currentNode.isTerminal() + ", STATE = " + terminalState);
			}

		} catch (StateMachineException e1) {
			System.out.println("Cannot compute if state is terminal.");
		}
		*/



		// Check if the node is terminal, and if so, return as result the final goals (saved in the node) for all players.
		// NOTE: even if the node is terminal the state might not be, but an error occurred when computing legal
		// moves, so we cannot search deeper and we return the goals saved in the node.
		if(currentNode.isTerminal()){

			//System.out.println("Reached terminal state.");

			double[] goals = currentNode.getGoals();
			// If a state in the tree is terminal, it must record the goals for every player.
			// If it doesn't there must be a programming error.
			if(goals == null){
				GamerLogger.logError("MctsManager", "Detected null goals for a treminal node in the tree.");
				throw new RuntimeException("Detected null goals for a treminal node in the tree.");
			}

			/*
			System.out.println("Reached terminal state.");
			System.out.print("Returning goals:");
			String s = "[";
			s += " ";
			for(int i = 0; i < currentNode.getGoals().length; i++){
				s += currentNode.getGoals()[i] + " ";
			}
			s += "]\n";
			System.out.print(s);
			*/

			// If current node is terminal and we are in the tree, the game length corresponds to the
			// number of visited nodes in the current iteration
			this.gameDependentParameters.increaseStepGameLengthSum(this.gameDependentParameters.getCurrentIterationVisitedNodes());

			simulationResult = new SimulationResult[1];
			simulationResult[0] = new SimulationResult(goals);
			this.flip(simulationResult); // If I have to flip, I flip the scores.

			return simulationResult;
		}


		//System.out.println("currentIterationVisitedNodes = " + this.currentIterationVisitedNodes);

		// If the state is not terminal (and no error occurred when computing legal moves),
		// it can be visited (i.e. one of its moves explored) only if the depth limit has not been reached.
		if(this.gameDependentParameters.getCurrentIterationVisitedNodes() >= this.maxSearchDepth){

			GamerLogger.log("MctsManager", "Reached search depth limit. Search interrupted (in the Monte Carlo tree) before reaching a treminal state.");

			//System.out.print("Reached depth limit.");

			// The state is not terminal, but we have reached the depth limit.
			// So we must return some goals. Try to return the goals of the non-terminal state
			// and if they cannot be computed return default tie-goals.

			/*
			System.out.print("Returning goals:");

			int[] goals = this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(currentState);
			String s = "[";
			s += " ";
			for(int i = 0; i < goals.length; i++){
				s += goals[i] + " ";
			}
			s += "]\n";
			System.out.print(s);
			*/

			// If we reached the depth limit while we are in the tree, we assume that the game length corresponds to the
			// number of visited nodes in the current iteration.
			// TODO: if we use this sample we might underestimate the game length. Should this sample be ignored? Note that
			// if we want to ignore this sample we must remember that when computing the average length we have to decrease
			// by 1 the number of iterations by which we divide the total game length sum.
			this.gameDependentParameters.increaseStepGameLengthSum(this.gameDependentParameters.getCurrentIterationVisitedNodes());

			simulationResult = new SimulationResult[1];
			simulationResult[0] = new SimulationResult(this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(currentState));
			this.flip(simulationResult);

			return simulationResult;
			//return new SimulationResult(goals);
		}

		this.gameDependentParameters.increaseCurrentIterationVisitedNodes();

		//System.out.println("Node: " + this.currentIterationVisitedNodes);

		MctsJointMove mctsJointMove;
		MachineState nextState;
		MctsNode nextNode;

		/*
		System.out.println("Printing current node: ");
		switch(this.mctsType){
		// DUCT version of MCTS.
		case DUCT:
			break;
		case SUCT: // SUCT version of MCTS.
			printSUCTMovesTree(((SUCTMCTSNode)currentNode).getMovesStats(), "");
			break;
		case SLOW_SUCT: // Slow SUCT version of MCTS.
			printMovesTree(((SlowSUCTMCTSNode)currentNode).getMovesStats(), "");
			break;
		default:
			throw new RuntimeException("Someone added a new MCTS Node type and forgot to deal with it here, when creating a new tree node.");
		}
		System.out.println("Finished printing current node.");
		*/

		// If the state is not terminal we must check if we have to expand it or if we have to continue the selection.
		// Depending on what needs to be done, get the joint move to be expanded/selected.
		boolean expansionRequired = this.expansionStrategy.expansionRequired(currentNode);
		if(expansionRequired){

			//System.out.println("Expanding.");

			mctsJointMove = this.expansionStrategy.expand(currentNode);

			/*
			String s = "[ ";
			for(Move m : mctsJointMove.getJointMove()){
				s += this.gameDependentParameters.getTheMachine().convertToExplicitMove(m);
			}
			s += "]";
			System.out.println("Expanding move " + s);
			*/

		}else{

			//System.out.println("Selecting.");
			this.selectionStrategy.preSelectionActions(currentNode);
			mctsJointMove = this.selectionStrategy.select(currentNode, currentState);

			/*
			String s = "[ ";
			for(Move m : mctsJointMove.getJointMove()){
				s += this.gameDependentParameters.getTheMachine().convertToExplicitMove(m);
			}
			s += "]";
			System.out.println("Selecting move " + s);
			System.out.println("Selecting move " + mctsJointMove.getJointMove());
			*/
		}

		//System.out.println("Chosen move: " + mctsJointMove);

		//System.out.println("Computing next state and next node.");

		// Get the next state according to the joint move...
		try {
			nextState = this.getNextState(currentNode, currentState, mctsJointMove);
		} catch (TransitionDefinitionException | StateMachineException e) {
			GamerLogger.logError("MctsManager", "Cannot compute next state. Stopping iteration and returning safe goals.");

			this.gameDependentParameters.decreaseCurrentIterationVisitedNodes();

			// If something goes wrong in advancing with the current move, assume that the game length corresponds to the
			// number of visited nodes so far in the current iteration
			// TODO: if we use this sample we might underestimate the game length. Should this sample be ignored? Note that
			// if we want to ignore this sample we must remember that when computing the average length we have to decrease
			// by 1 the number of iterations by which we divide the total game length sum.
			this.gameDependentParameters.increaseStepGameLengthSum(this.gameDependentParameters.getCurrentIterationVisitedNodes());

			simulationResult = new SimulationResult[1];
			simulationResult[0] = new SimulationResult(this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(currentState));
			this.flip(simulationResult);

			return simulationResult;
		}

		// Once we advanced to the next state, we can add the joint move to the list of joint moves for the current simulation.
		this.currentSimulationJointMoves.add(mctsJointMove);

		//System.out.println("Next state = [ " + this.gameDependentParameters.getTheMachine().convertToExplicitMachineState(nextState) + " ]");
		//System.out.println("Next state = [ " + nextState + " ]");

		// ...and get the corresponding MCT node from the transposition table.
		nextNode = this.transpositionTable.getNode(nextState);

		if(this.logLogTree || this.logTreeBranchingAndDepthAndEntropy) {
			if(this.currentLogNode.getChild(mctsJointMove) == null) {

				int totIterations = this.gameDependentParameters.getTotIterations();
				if(totIterations < 0) {
					GamerLogger.logError("MctsManager", "[searchNext] TotIterations = " + totIterations + ". Expected to be at least 0.");
				}

				this.currentLogNode.addChild(mctsJointMove, new LogTreeNode(this.gameDependentParameters.getTotIterations(), nextNode.getNumJointMoves()));
			}
			this.currentLogNode = this.currentLogNode.getChild(mctsJointMove);
			this.currentLogNode.increaseVisits();
		}

		// If we cannot find such tree node we create it and add it to the table.
		// NOTE: there are 3 situations when the next node might not be in the tree yet:
		// 1. If we are expanding the current node, the chosen joint move will probably
		// lead to an unexplored state (depends on the choice the expansion strategy makes
		// and on the fact that the state might have been visited already from a different
		// sequence of actions).
		// 2. If the expansion doesn't look at unexplored joint moves to choose the joint
		// move to expand, but only at unexplored single moves for each player, it might
		// be that all single moves for each player have been explored already, but the
		// selection picks a combination of them that has not been explored yet and the
		// corresponding next state hasn't thus been added to the tree yet.
		// 3. It might also be the case that the selection chooses a joint move whose
		// corresponding state has been already visited in a previous run of the MCTS,
		// but since the corresponding MCT node hasn't been visited in recent runs anymore
		// it has been removed from the transposition table during the "cleaning" process.
		//
		// If the node doesn't exists, after creation we perform a playout on it, both
		// in the case when we were performing selection and in the case when we were performing
		// expansion. If the node exists, we continue searching on it, even if we were performing
		// expansion.
		if(nextNode == null){

			//System.out.println("Next node not found. Creating next node...");

			//System.out.println("Adding new node to table: " + nextState);

			nextNode = this.treeNodeFactory.createNewNode(nextState);
			this.transpositionTable.putNode(nextState, nextNode);

			this.gameDependentParameters.increaseStepAddedNodes();

			// No need to perform playout if the node is terminal, we just return the goals in the node.
			// Otherwise we perform the playout.
			if(nextNode.isTerminal()){

				//System.out.println("Expanded state is terminal.");

				if(nextNode.getGoals() == null){
					GamerLogger.logError("MctsManager", "Detected null goals for a treminal node in the tree.");
					throw new RuntimeException("Detected null goals for a treminal node in the tree.");
				}

				/*
				System.out.print("Returning goals:");
				String s = "[";
				s += " ";
				for(int i = 0; i < nextNode.getGoals().length; i++){
					s += nextNode.getGoals()[i] + " ";
				}
				s += "]\n";
				System.out.print(s);
				*/

				// If the next node is terminal, the game length corresponds to the number of visited nodes in the
				// current iteration
				this.gameDependentParameters.increaseStepGameLengthSum(this.gameDependentParameters.getCurrentIterationVisitedNodes());

				simulationResult = new SimulationResult[1];
				simulationResult[0] = new SimulationResult(nextNode.getGoals());
				this.flip(simulationResult);
			}else{

				//System.out.println("Performing playout.");

				// Check how many nodes can be visited after the current one. At this point
				// "currentIterationVisitedNodes" can be at most equal to the "maxSearchDepth".
				int availableDepth = this.maxSearchDepth - (int) Math.round(this.gameDependentParameters.getCurrentIterationVisitedNodes());

				if(availableDepth == 0){

					/*
					System.out.println("No depth available to perform playout.");

					System.out.print("Returning goals:");

					int[] goals = this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(nextState);
					String s = "[";
					s += " ";
					for(int i = 0; i < goals.length; i++){
						s += goals[i] + " ";
					}
					s += "]\n";
					System.out.print(s);

					simulationResult = new SimulationResult(goals);

					*/

					// If we reached the depth limit, assume that the game length corresponds to the
					// number of visited nodes so far in the current iteration
					// TODO: if we use this sample we might underestimate the game length. Should this sample be ignored?  Note that
					// if we want to ignore this sample we must remember that when computing the average length we have to decrease
					// by 1 the number of iterations by which we divide the total game length sum.
					this.gameDependentParameters.increaseStepGameLengthSum(this.gameDependentParameters.getCurrentIterationVisitedNodes());

					simulationResult = new SimulationResult[1];
					simulationResult[0] = new SimulationResult(this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(nextState));
					this.flip(simulationResult);

				}else{

					//int[] playoutVisitedNodes = new int[1];
					// Note that if no depth is left for the playout, the playout itself will take care of
					// returning the added-state goal values (if any) or the default tie goal values.
					simulationResult = this.playoutStrategy.playout(nextNode, mctsJointMove.getJointMove(), nextState, availableDepth);
					this.flip(simulationResult);

					// IMPORTANT NOTE! Here we increment the number of nodes visited for the current iteration
					// using ALL the performed playouts lengths. If more than one playout was performed this
					// number will be higher than normal. Keep this in mind when looking at speed statistics.
					// When using multiple playouts the average number of nodes per iteration will increase,
					// while the average number of iterations per second will decrease.
					// OTHER IMPORTANT NOTE! Here we also save the number of nodes visited so far in the tree for this
					// iteration. We use this number to compute the length of the game simulated in this iteration. If
					// for this iteration multiple playouts have been performed, the length of each playout will be
					// summed to the number of nodes visited in the tree to obtain the total length of the simulated game.
					double gameLengthInTheTree = this.gameDependentParameters.getCurrentIterationVisitedNodes();
					for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){
						// TODO: increase currentIterationVisitedNodes directly from the playout strategy every time a new node is visited.
						this.gameDependentParameters.increaseCurrentIterationVisitedNodes(simulationResult[resultIndex].getPlayoutLength());
						this.gameDependentParameters.increaseStepGameLengthSum(gameLengthInTheTree + simulationResult[resultIndex].getPlayoutLength());
					}

					/*
					System.out.println("Finished performing playout.");

					System.out.print("Returning goals:");

					int[] goals = simulationResult.getTerminalGoals();
					String s = "[";
					s += " ";
					for(int i = 0; i < goals.length; i++){
						s += goals[i] + " ";
					}
					s += "]\n";
					System.out.print(s);
					*/

					this.backpropagationStrategy.processPlayoutResult(nextNode, nextState, simulationResult);
				}

				//System.out.print("After playout - ");
				//((MemorizedStandardPlayout)this.playoutStrategy).printJM();
			}
		}else{
			// Otherwise, if we continue selecting:
			//System.out.println("Found next node, continuing search.");
			simulationResult = this.searchNext(nextState, nextNode);
		}


		/*
		System.out.println("Backpropagating goals:");
		String s = "[";
		s += " ";
		for(int i = 0; i < goals.length; i++){
			s += goals[i] + " ";
		}
		s += "]\n";
		System.out.print(s);
		*/


		this.backpropagationStrategy.update(currentNode, currentState, mctsJointMove, simulationResult);
		return simulationResult;
	}

	/**
	 * Method that checks when it's time to stop the search.
	 */
	private boolean timeToStopSearch(long timeout){

		if(this.numExpectedIterations > 0){

			return this.gameDependentParameters.getStepIterations() == this.numExpectedIterations;

		}else{

			return System.currentTimeMillis() >= timeout;

		}

	}

	/**
	 * With probability pFlip, flip the terminal goals in a SimulationResult.
	 *
	 * @param simulationResult
	 */
	private void flip(SimulationResult[] simulationResults) {

		if(this.pFlip > 0.0) {
			for(SimulationResult simulationResult : simulationResults) {
				//System.out.println();
				//System.out.println(Arrays.toString(simulationResult.getTerminalGoals()));

				if((!this.flipWinsOnly || simulationResult.getTerminalWins()[this.gameDependentParameters.getMyRoleIndex()] == 1.0) && // If I have to flip in any situation, or if I have to flip only if my role is winning and it is winning...
						this.random.nextDouble() < this.pFlip) { // ...I check if I have to flip according to the pFlip probability.
					simulationResult.flipTerminalScores(); // If I have to flip, I flip the scores.
				}

				//System.out.println(Arrays.toString(simulationResult.getTerminalGoals()));
			}
		}
	}

	/**
	 * Given the current state, the associated node and a joint move, this method returns the next state.
	 * If the tree node memorizes next states, it will use the memorized map to get the next state, otherwise
	 * it will use the state machine.
	 *
	 * @param currentNode
	 * @param currentState
	 * @param mctsJointMove
	 * @return
	 * @throws TransitionDefinitionException
	 * @throws StateMachineException
	 */
	private MachineState getNextState(MctsNode currentNode, MachineState currentState, MctsJointMove mctsJointMove) throws TransitionDefinitionException, StateMachineException {

		MachineState nextState;

		if(currentNode instanceof StateMemorizingMctsNode) {
			nextState = ((StateMemorizingMctsNode) currentNode).getNextState(mctsJointMove.getJointMove());
			if(nextState == null) {
				throw new TransitionDefinitionException(this.gameDependentParameters.getTheMachine().convertToExplicitMachineState(currentState), this.gameDependentParameters.getTheMachine().convertToExplicitMoves(mctsJointMove.getJointMove()));
			}
		}else {
			nextState = this.gameDependentParameters.getTheMachine().getNextState(currentState, mctsJointMove.getJointMove());
		}

		return nextState;

	}

	public void beforeMoveActions(int currentGameStep, boolean metagame, List<Move> internalLastJointMove){

		int previousGameStep = this.gameDependentParameters.getGameStep();

		this.gameDependentParameters.setGameStep(currentGameStep);

		this.gameDependentParameters.setMetagame(metagame);

		// Assume that the game step changed, because we are going to search for a new game step in the game.

		this.transpositionTable.clean();

		if(this.beforeMoveStrategy != null){
			this.beforeMoveStrategy.beforeMoveActions();
		}

		this.transpositionTable.logTable("Start");

		if(this.logLogTree || this.logTreeBranchingAndDepthAndEntropy) {

			if(this.logTree == null) {
				int totIterations = this.gameDependentParameters.getTotIterations();
				if(totIterations < 0) {
					GamerLogger.logError("MctsManager", "[beforeMoveActions] TotIterations = " + totIterations + ". Expected to be at least 0.");
				}
				this.logTree = new LogTreeNode(totIterations, -1);
				this.currentLogRoot = this.logTree;
				this.currentLogNode = this.currentLogRoot;
				this.logTree.setOnPath(0); // Assume the root is always on path and was added in the step previous to 1.
			}else {
				// The current log root always refers to the root of the previous step, because it gets updated even if no search is performed in a step.
				if(this.currentLogRoot == null) {
					GamerLogger.logError("MctsManager", "Unexpected null root for log tree in step " + currentGameStep + ". Stopping tree logging for this agent (also for any future game).");
					this.logLogTree = false;
					this.logTreeBranchingAndDepthAndEntropy = false;
				}else {
					if(internalLastJointMove == null) {
						GamerLogger.logError("MctsManager", "The last performed joint move is null, cannot advance the root state of the log tree. Stopping tree logging and branching and depth logging for this agent (also for any future game).");
						this.logLogTree = false;
						this.logTreeBranchingAndDepthAndEntropy = false;
					}else {
						MctsJointMove mctsJointMove = new MctsJointMove(internalLastJointMove);
						if(this.currentLogRoot.getChild(mctsJointMove) == null) { // If the edge of the selected action was not in the tree yet, we have to add it here so we can log that this action was selected, even if in the transposition table this edge is not present and might be discovered later.
							this.currentLogRoot.addChild(mctsJointMove, new LogTreeNode(-1, -1)); // Since the action was never added to the actual tree, we do not give it an insertion iteration, we will only add it to the plotted tree when it gets selected.
						}
						this.currentLogRoot = this.currentLogRoot.getChild(mctsJointMove);
						this.currentLogNode = this.currentLogRoot;
						this.currentLogRoot.setOnPath(previousGameStep);
					}
				}

			}
		}

	}

	public void afterMoveActions(){
		if(this.afterMoveStrategy != null){
			this.afterMoveStrategy.afterMoveActions();
		}

		if(this.logLogTree || this.logTreeBranchingAndDepthAndEntropy) {
			this.accumSimulationsPerStep.put(this.gameDependentParameters.getGameStep(), this.gameDependentParameters.getTotIterations());
		}

		if(this.logTreeBranchingAndDepthAndEntropy) {
			this.logTreeBranchingAndDepthAndEntropy();
		}

		this.transpositionTable.logTable("End");
	}

	public void afterMetagameActions(){
		if(this.afterMetagameStrategy != null){
			this.afterMetagameStrategy.afterMetagameActions();
		}
		this.gameDependentParameters.setMetagame(false);
	}

	public void afterGameActions(List<Double> goals, List<Move> internalLastJointMove){
		// Call again the AfterMoveAction because for the search of the last move it hasn't been
		// performed yet, because it is always performed at the beginning of a new search.
		//if(this.afterMoveStrategy != null){
		//	this.afterMoveStrategy.afterMoveActions();
		//}
		if(this.afterGameStrategy != null){
			this.afterGameStrategy.afterGameActions(goals);
		}
		// If we are building the tree as well, log it.
		if(this.logLogTree) {

			// At the end of the game we have to advance to the final game node in the tree and set it on the path of game nodes.
			if(this.logTree == null) {
				GamerLogger.logError("MctsManager", "Unexpected null log tree. Terminating match without logging the tree.");
			}else {
				// Here we assume that the current log root refers to the root of the last game step.
				if(this.currentLogRoot == null) {
					GamerLogger.logError("MctsManager", "Unexpected null root for log tree in the last step. Logging tree without terminal node.");
				}else {
					if(internalLastJointMove == null) {
						GamerLogger.logError("MctsManager", "The last performed joint move is null, cannot advance to the terminal node of the log tree. Logging tree without terminal node.");
					}else {
						MctsJointMove mctsJointMove = new MctsJointMove(internalLastJointMove);
						if(this.currentLogRoot.getChild(mctsJointMove) == null) {

							int totIterations = this.gameDependentParameters.getTotIterations();
							if(totIterations < 0) {
								GamerLogger.logError("MctsManager", "[afterGameActions] TotIterations = " + totIterations + ". Expected to be at least 0.");
							}

							this.currentLogRoot.addChild(mctsJointMove, new LogTreeNode(this.gameDependentParameters.getTotIterations(), -1)); // Here we don't care about the number of joint actions available in the node, because if it is being added now it has no children and thus it won't be used to compute the entropy difference.
						}
						this.currentLogRoot = this.currentLogRoot.getChild(mctsJointMove);
						this.currentLogNode = this.currentLogRoot;
						this.currentLogRoot.setOnPath(this.gameDependentParameters.getGameStep());
					}
				}
				this.logLogTree();
			}

		}

	}

	public double[] getStepScoreSumForRoles(){
		return this.gameDependentParameters.getStepScoreSumForRoles();
	}

	public int getStepIterations(){
		return this.gameDependentParameters.getStepIterations();
	}

	public int getStepAddedNodes(){
		return this.gameDependentParameters.getStepAddedNodes();
	}

	public int getStepMemorizedStates(){
		return this.gameDependentParameters.getMemorizedStates();
	}

	public double getStepVisitedNodes(){
		return this.gameDependentParameters.getStepVisitedNodes();
	}

	public long getStepSearchDuration(){
		return this.gameDependentParameters.getStepSearchDuration();
	}

	/**
	 * ATTENTION! This method has to be used ONLY when testing the propnet speed, NEVER
	 * change this parameter otherwise.
	 */
	public void setNumExpectedIterations(int numExpectedIterations) {
		this.numExpectedIterations = numExpectedIterations;
	}

	public int getNumExpectedIterations() {
		return this.numExpectedIterations;
	}

	private void logLogTree() {

		if(this.logLogTree) {

			int[] count = new int[1];
			count[0] = 0;

			DFS(this.logTree, "", count);

			System.out.println("Nodes in the tree: " + count[0]);

			// Perform BFS to assign coordinates to the nodes.
			// Nodes in a given level of BFS will have the same x coordinate and
			// be distributed at equal distance over the y coordinate.
			List<LogTreeNode> currentLevel = new ArrayList<LogTreeNode>();
			List<LogTreeNode> nextLevel = new ArrayList<LogTreeNode>();
			//currentLevel.add(this.logTree);

			// The minimum values for x is always 0, while the maximum is the depth of the tree,
			// that can be recorded after visiting all the tree.
			int xMin = 0;
			//int xMax;

			// yMin (i.e. the minimum value of y) is equal to -yMax. Any of the two can be computed
			// while assigning coordinates to the nodes. The level with most nodes will determine them.
			double yMax = 0;

			int x = 0;

			double y;

			// Distance in the y coordinate between two nodes on the same level.
			double d = 1;

			// Keep a map that for each entry contains a list of nodes that have to be printed for the corresponding game turn.
			// Note that before printing the nodes of each game turn must be ordered
			Map<Integer,List<LogTreeNode>> nodesPerStep = new HashMap<Integer,List<LogTreeNode>>();

			// Keep an array that contains an entry for each step of the game. Each entry contains the node that is reached by
			// performing the action selected at the end of the corresponding step. Note that the action selected at the end of
			// step i is memorized at index i-1.
			LogTreeNode[] selectedActionNodes = new LogTreeNode[this.gameDependentParameters.getGameStep()];

			// First set the coordinates of the root and put all its children in the current level
			MyPair<Double,Double> coordinates = new MyPair<Double,Double>(0.0,0.0);
			this.logTree.setCoordinates(coordinates);
			for(LogTreeNode child : this.logTree.getChildren().values()) {
				child.setParentCoordinates(coordinates);
				currentLevel.add(child);
			}

			// The root is not included in the .
			// We want to print only nodes that have a parent, because printing a node corresponds to printing
			// the edge that leads to it.

			while(!currentLevel.isEmpty()) {

				x++; // Increment the x coordinate

				y = ((currentLevel.size()-1)/2.0)*d; // Max y for this level
				// Check if it's the maximum so far
				if(y > yMax) {
					yMax = y;
				}

				y = -y; // Start with the lowest value for y

				for(LogTreeNode node : currentLevel) {
					coordinates = new MyPair<Double,Double>((double)x,y);
					node.setCoordinates(coordinates);
					for(LogTreeNode child : node.getChildren().values()) {
						child.setParentCoordinates(coordinates);
						nextLevel.add(child);
					}
					// Compute in which step the node has been added
					int insertionStep = this.getInsertionStep(node.getInsertionIteration());
					if(insertionStep != -1) {
						List<LogTreeNode> stepNodes = nodesPerStep.get(insertionStep);
						if(stepNodes == null) {
							stepNodes = new ArrayList<LogTreeNode>();
							nodesPerStep.put(insertionStep, stepNodes);
						}
						stepNodes.add(node);
					}
					if(node.isOnPath()) {
						int index = node.getSelectionStep() - 1; // The root is the only node selected for step 0, but the root is not considered in the while loop, so we can memorize the selected nodes from steps 1 to n (n=num steps) in an array that goes from 0 to n-1
						if(selectedActionNodes[index] == null) {
							selectedActionNodes[index] = node;
						}else {
							GamerLogger.logError("MctsManager", "Error logging the tree. There are multiple selected action for game step " + index+1 + ". Stopping logging on file.");
							return;
						}
					}
					y+=d;
				}

				currentLevel = nextLevel;
				nextLevel = new ArrayList<LogTreeNode>();

			}

			//System.out.println("Ordered nodes: " + orderedNodes.size());

			// Log the tree with the format required by the matlab function that will print it (use space as separator)

			// The first line has 1 integer value representing the total number of steps of the search,
			// 1 integer value representing the total number of simulations performed during the search and
			// 4 double values that represent the minimum and maximum value for the x and y axis respectively
			// (i.e. step iterartions xMin xMax yMin yMax)
			String newline = "\n";
			String toLog = this.gameDependentParameters.getGameStep() + " " + this.gameDependentParameters.getTotIterations() + " " + xMin + " " + x + " " + (-yMax-d) + " " + (yMax+d) + newline;


			//// Assuming that steps start at 1 and increase by 1 after every turn.
			//int currentStep = 1;
			//toLog += (currentStep + newline);

			//System.out.println(currentStep);

			int firstIteration;
			int lastIteration;
			List<LogTreeNode> stepNodes;

			// First log edges for step 1
			firstIteration = 0;
			lastIteration = this.accumSimulationsPerStep.get(1)-1;

			if(firstIteration <= lastIteration) { // At least one iteration was performed in the step
				stepNodes = nodesPerStep.get(1);
				if(stepNodes == null) {
					stepNodes = new ArrayList<LogTreeNode>();
				}
				toLog += this.stepLog(1, firstIteration, lastIteration, stepNodes, newline);
			}else {
				if(nodesPerStep.get(1) != null) {
					GamerLogger.logError("MctsManager", "Error logging the tree. Detected nodes to log for step 1, which had no performed simulations. Some nodes are being lost and not logged!");
				}
			}

			// For each step > 1, log the step and the action chosen for the previous step, then log all the edges discovered in the step.
			for(int step = 2; step <= this.gameDependentParameters.getGameStep(); step ++) {

				LogTreeNode selectedNode = selectedActionNodes[step-2]; // Get action selected for step-1 which is memorized at index step-2

				toLog += (step + " " + selectedNode.getParentCoordinates().getFirst() + " " + selectedNode.getCoordinates().getFirst() + " " +
						selectedNode.getParentCoordinates().getSecond() + " " + selectedNode.getCoordinates().getSecond() + newline);

				firstIteration = this.accumSimulationsPerStep.get(step-1);
				lastIteration = this.accumSimulationsPerStep.get(step)-1;

				if(firstIteration <= lastIteration) { // At least one iteration was performed in the step
					stepNodes = nodesPerStep.get(step);
					if(stepNodes == null) {
						stepNodes = new ArrayList<LogTreeNode>();
					}
					toLog += this.stepLog(step, firstIteration, lastIteration, stepNodes, newline);
				}else {
					if(nodesPerStep.get(step) != null) {
						GamerLogger.logError("MctsManager", "Error logging the tree. Detected nodes to log for step " + step + ", which had no performed simulations. Some nodes are being lost and not logged!");
					}
				}

			}

			// Log the action selected for the last step
			LogTreeNode selectedNode = selectedActionNodes[this.gameDependentParameters.getGameStep()-1]; // Get action selected for the very last step, which is memorized at index lastStep-1

			toLog += ((this.gameDependentParameters.getGameStep()+1) + " " + selectedNode.getParentCoordinates().getFirst() + " " + selectedNode.getCoordinates().getFirst() + " " +
					selectedNode.getParentCoordinates().getSecond() + " " + selectedNode.getCoordinates().getSecond() + newline);

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TreePlot", toLog);

		}


	}

	private int getInsertionStep(int insertionIteration) {
		if(insertionIteration >= 0) {
			for(int step = 1; step <= this.gameDependentParameters.getGameStep(); step++) {
				Integer cumulativeNumIterations = this.accumSimulationsPerStep.get(step);
				if(cumulativeNumIterations == null) {
					GamerLogger.logError("MctsManager", "Error logging the tree. There are no recorded cumulative simulations for step " + step + ".");
					return -1;
				}
				if(cumulativeNumIterations.intValue() > insertionIteration) {
					return step;
				}
			}
		}
		return -1;
	}

	/**
	 * Call this method ONLY if there have been simulations performed in the step.
	 *
	 *
	 * @param step
	 * @param stepNodes
	 * @param newline
	 * @return
	 */
	private String stepLog(int step, int firstIteration, int lastIteration, List<LogTreeNode> stepNodes, String newline) {

		String toLog = "";

		// 1. Order the nodes
		Collections.sort(stepNodes,	new Comparator<LogTreeNode>(){

			@Override
			public int compare(LogTreeNode o1, LogTreeNode o2) {
				// Sort from smallest to largest
				if(o1.getInsertionIteration() < o2.getInsertionIteration()){
					return -1;
				}else if(o1.getInsertionIteration() > o2.getInsertionIteration()){
					return 1;
				}else{
					return 0;
				}
			}

		});

		// 2. Start logging the nodes, iterating over all the iterations performed in the step.
		int currentIteration = firstIteration; // Keeps track of the current iteration that we are logging
		int currentNodeIndex = 0; // Keeps track of the current node that still has to be logged in the list of nodes to log
		int iterationsWithNoInsertions = 0;

		boolean problem = true;

		while(currentIteration <= lastIteration && currentNodeIndex < stepNodes.size()) { // Iterate until all game iterations have been logged
			// 2.1 First check if there are iterations where no node was added and sum them
			while(currentIteration < stepNodes.get(currentNodeIndex).getInsertionIteration()) { // We assume that if there is a node then its insertion iteration must be < than the
				// If no node was added during the iteration, increment the number of iterations with no insertions
				iterationsWithNoInsertions++;
				currentIteration++;
			}
			// 2.2 We found a node to log for the current iteration, but first log the number of iterations without insertions (if any)
			if(iterationsWithNoInsertions > 0) {
				toLog += iterationsWithNoInsertions + newline;
				iterationsWithNoInsertions = 0;
				problem = false;
			}
			// 2.3 Log in the same line all nodes inserted in the same iteration
			while(currentNodeIndex < stepNodes.size() && stepNodes.get(currentNodeIndex).getInsertionIteration() == currentIteration) {
				toLog += stepNodes.get(currentNodeIndex).getParentCoordinates().getFirst() + " " + stepNodes.get(currentNodeIndex).getCoordinates().getFirst() + " " +
						stepNodes.get(currentNodeIndex).getParentCoordinates().getSecond() + " " + stepNodes.get(currentNodeIndex).getCoordinates().getSecond() + " ";
				currentNodeIndex++;
				problem = false;
			}
			if(problem == true) {
				GamerLogger.logError("MctsManager", "Error logging the tree. Step = " + step + ", FirstIteration = " + firstIteration + ", LastIteration = " + lastIteration + ", NumStepNodes = " + stepNodes.size() + (stepNodes.size() > 0 ? (", InsertionIterationOfFirstNode = " + stepNodes.get(0).getInsertionIteration()) : "."));
			}
			toLog += newline;
			currentIteration++;
		}

		// 2.4 Check if there are other simulations to log where no node was added to the tree.
		//Note that it shouldn't be possible to finish iterating over all iterations without having logged all nodes first.
		if(lastIteration - currentIteration + 1 > 0) {
			toLog += (lastIteration - currentIteration + 1) + newline;
		}

		return toLog;

	}

	private void DFS(LogTreeNode node, String indent, int[] count) {

		count[0]++;

		if(!node.getChildren().isEmpty()) {
			for(Entry<MctsJointMove,LogTreeNode> action : node.getChildren().entrySet()) {
				//System.out.println(indent + action.getKey());
				DFS(action.getValue(), indent + "  ", count);
			}
		}

	}

	private void logTreeBranchingAndDepthAndEntropy() {

		SingleValueLongStats branchingStats = new SingleValueLongStats();
		SingleValueLongStats depthStats = new SingleValueLongStats();
		SingleValueDoubleStats entropyStats = new SingleValueDoubleStats();

		this.DFSlogTreeBranchingAndDepthAndEntropy(this.currentLogRoot, branchingStats, depthStats, entropyStats, 0);

		if(branchingStats.getNumSamples() > 1) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BranchingLogs", this.gameDependentParameters.getGameStep() + ";" +
					branchingStats.getNumSamples() + ";" + branchingStats.getLongMinValue() + ";" + branchingStats.getLongMaxValue() + ";" +
					branchingStats.getMedian() + ";" + branchingStats.getValuesStandardDeviation() + ";" + branchingStats.getValuesSEM() +
					";" + branchingStats.getAvgValue() + ";" + branchingStats.get95ConfidenceInterval() + ";");
		}else if (branchingStats.getNumSamples() == 1) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BranchingLogs", this.gameDependentParameters.getGameStep() + ";" +
					branchingStats.getNumSamples() + ";" + branchingStats.getLongMinValue() + ";" + branchingStats.getLongMaxValue() + ";" +
					branchingStats.getMedian() + ";" + 0 + ";" + 0 +
					";" + branchingStats.getAvgValue() + ";" + 0 + ";");
		}else if (branchingStats.getNumSamples() == 0) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BranchingLogs", this.gameDependentParameters.getGameStep() + ";" +
					branchingStats.getNumSamples() + ";N/A;N/A;N/A;N/A;N/A;N/A;N/A;");
		}

		if(depthStats.getNumSamples() > 1) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "DepthLogs", this.gameDependentParameters.getGameStep() + ";" +
					depthStats.getNumSamples() + ";" + depthStats.getLongMinValue() + ";" + depthStats.getLongMaxValue() + ";" +
					depthStats.getMedian() + ";" + depthStats.getValuesStandardDeviation() + ";" + depthStats.getValuesSEM() +
					";" + depthStats.getAvgValue() + ";" + depthStats.get95ConfidenceInterval() + ";");
		}else if(depthStats.getNumSamples() == 1) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "DepthLogs", this.gameDependentParameters.getGameStep() + ";" +
					depthStats.getNumSamples() + ";" + depthStats.getLongMinValue() + ";" + depthStats.getLongMaxValue() + ";" +
					depthStats.getMedian() + ";" + 0 + ";" + 0 +
					";" + depthStats.getAvgValue() + ";" + 0 + ";");
		}else if(depthStats.getNumSamples() == 0) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "DepthLogs", this.gameDependentParameters.getGameStep() + ";" +
					depthStats.getNumSamples() + ";N/A;N/A;N/A;N/A;N/A;N/A;N/A;");
		}

		if(entropyStats.getNumSamples() > 1) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EntropyLogs", this.gameDependentParameters.getGameStep() + ";" +
					entropyStats.getNumSamples() + ";" + entropyStats.getMinValue() + ";" + entropyStats.getMaxValue() + ";" +
					entropyStats.getMedian() + ";" + entropyStats.getValuesStandardDeviation() + ";" + entropyStats.getValuesSEM() +
					";" + entropyStats.getAvgValue() + ";" + entropyStats.get95ConfidenceInterval() + ";");
		}if(entropyStats.getNumSamples() == 1) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EntropyLogs", this.gameDependentParameters.getGameStep() + ";" +
					entropyStats.getNumSamples() + ";" + entropyStats.getMinValue() + ";" + entropyStats.getMaxValue() + ";" +
					entropyStats.getMedian() + ";" + 0 + ";" + 0 +
					";" + entropyStats.getAvgValue() + ";" + 0 + ";");
		}else if(depthStats.getNumSamples() == 0) {
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EntropyLogs", this.gameDependentParameters.getGameStep() + ";" +
					entropyStats.getNumSamples() + ";N/A;N/A;N/A;N/A;N/A;N/A;N/A;");
		}

	}

	private void DFSlogTreeBranchingAndDepthAndEntropy(LogTreeNode currentNode,
			SingleValueLongStats branchingStats, SingleValueLongStats depthStats,
			SingleValueDoubleStats entropyStats, int depth) {

		if(currentNode.getChildren().isEmpty()) { // Leaf node => memorize the depth
			depthStats.addValue(depth);
		}else {
			branchingStats.addValue(currentNode.getChildren().size());
			entropyStats.addValue(getEntropy(currentNode));
			for(LogTreeNode child : currentNode.getChildren().values()) {
				this.DFSlogTreeBranchingAndDepthAndEntropy(child, branchingStats, depthStats, entropyStats, depth+1);
			}
		}

	}

	private double getEntropy(LogTreeNode node) {

		double totVisits = 0;
		double entropy = 0;
		double probability;

		for(LogTreeNode child : node.getChildren().values()) {

			totVisits += child.getVisits();

		}

		// Check that the sum of visits is equal to (visits of parent -1) or (visits of parent)
		//if(totVisits != node.getVisits() && totVisits+1 != node.getVisits()) {
		//	GamerLogger.logError("MctsManager", "Error computing entropy. Total visits of children = " + totVisits + ", and total visits of parent = " + node.getVisits() + ".");
		//}

		for(LogTreeNode child : node.getChildren().values()) {

			probability = child.getVisits()/totVisits;
			entropy += (probability*(this.log2(probability)));

		}

		return -entropy;

	}

	private double log2(double number) {
		return Math.log(number)/Math.log(2.0);
	}

}
