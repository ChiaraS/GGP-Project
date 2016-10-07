/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet;

import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.MCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.AfterMoveStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.AfterSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation.BeforeSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSTranspositionTable;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.TreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

/**
 * @author C.Sironi
 *
 */
public class InternalPropnetMCTSManager extends MCTSManager {

	/**
	 * True if the manager must run the DUCT version of Monte Carlo Tree Search,
	 * false otherwise.
	 */
	//private boolean DUCT;

	/**
	 * Used to tell to the manager which version of Monte Carlo Tree Search to run.
	 *
	 * More precisely:
	 * - DUCT = the manager will run Monte Carlo Tree Search with the Decoupled UCT statistics.
	 *
	 * - SUCT = the manager will run Monte Carlo Tree Search with the Sequential UCT statistics.
	 *
	 * - SLOW_SUCT = like SUCT but using the slower version of the tree nodes structure.
	 *   (//TODO: This version of SUCT should disappear)
	 *
	 * @author C.Sironi
	 *
	 */
    //public enum MCTS_TYPE{
    //	DUCT, SUCT, SLOW_SUCT
    //}


    //private MCTS_TYPE mctsType;

	/**
	 * Strategies that the MCTSManger must use to perform the different MCTS phases.
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
	private BeforeSimulationStrategy beforeSimulationStrategy;

	private AfterSimulationStrategy afterSimulationStrategy;

	private AfterMoveStrategy afterMoveStrategy;

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
	private TreeNodeFactory theNodesFactory;

	/** NOT NEEDED FOR NOW SINCE ALL STRATEGIES ARE SEPARATE
	 * A set containing all the distinct concrete strategy classes only once.
	 * NOTE: two strategies might be implemented by the same concrete class implementing two
	 * different interfaces, this set allows to perform certain operations only once per class.
	 */
	//private Set<Strategy> strategies = new HashSet<Strategy>();

	/**
	 * The state machine that this MCTS manager uses to reason on the game
	 */
	private InternalPropnetStateMachine theMachine;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private MCTSTranspositionTable transpositionTable;

	/**
	 *
	 */
	public InternalPropnetMCTSManager(SelectionStrategy selectionStrategy,
			ExpansionStrategy expansionStrategy, PlayoutStrategy playoutStrategy,
			BackpropagationStrategy backpropagationStrategy, MoveChoiceStrategy moveChoiceStrategy,
			BeforeSimulationStrategy beforeSimulationStrategy, AfterSimulationStrategy afterSimulationStrategy,
			AfterMoveStrategy afterMoveStrategy,TreeNodeFactory theNodesFactory,
			InternalPropnetStateMachine theMachine, int gameStepOffset, int maxSearchDepth,
			boolean logTranspositionTable) {

		//this.mctsType = mctsType;
		this.selectionStrategy = selectionStrategy;
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.backpropagationStrategy = backpropagationStrategy;
		this.moveChoiceStrategy = moveChoiceStrategy;
		this.beforeSimulationStrategy = beforeSimulationStrategy;
		this.afterSimulationStrategy = afterSimulationStrategy;
		this.afterMoveStrategy = afterMoveStrategy;
		this.theNodesFactory = theNodesFactory;
		this.theMachine = theMachine;

		if(!(theNodesFactory instanceof PnAMAFDecoupledTreeNodeFactory)){
			logTranspositionTable = false;
		}

		this.transpositionTable = new MCTSTranspositionTable(gameStepOffset, logTranspositionTable);

		this.maxSearchDepth = maxSearchDepth;
		this.iterations = 0;
		this.visitedNodes = 0;
		this.currentIterationVisitedNodes = 0;
		this.searchStart = 0;
		this.searchEnd = 0;

		/*
		switch(this.mctsType){
		case DUCT:
			GamerLogger.log("MCTSManager", "MCTS manager initialized to perform DUCT MCTS with maximum search dept " + this.maxSearchDepth + ".");
			break;
		case SUCT:
			GamerLogger.log("MCTSManager", "MCTS manager initialized to perform SUCT MCTS with maximum search dept " + this.maxSearchDepth + ".");
			break;
		case SLOW_SUCT:
			GamerLogger.log("MCTSManager", "MCTS manager initialized to perform SLOW_SUCT MCTS with maximum search dept " + this.maxSearchDepth + ".");
			break;
		}
		*/

		//this.strategies.add(this.expansionStrategy);
		//this.strategies.add(this.selectionStrategy);
		//this.strategies.add(this.backpropagationStrategy);
		//this.strategies.add(this.playoutStrategy);
		//this.strategies.add(this.moveChoiceStrategy);

		String toLog = "MCTS manager initialized with the following state mahcine " + this.theMachine.getName();

		toLog += "\nMCTS manager initialized with the following parameters: [maxSearchDepth = " + this.maxSearchDepth + ", logTranspositionTable = " + logTranspositionTable + "]";

		toLog += "\nMCTS manager initialized with the following tree node factory: " + this.theNodesFactory.getClass().getSimpleName() + ".";

		toLog += "\nMCTS manager initialized with the following strategies: ";

		//for(Strategy s : this.strategies){
		//	toLog += "\n" + s.printStrategy();
		//}

		toLog += "\n" + this.selectionStrategy.printStrategy();
		toLog += "\n" + this.expansionStrategy.printStrategy();
		toLog += "\n" + this.playoutStrategy.printStrategy();
		toLog += "\n" + this.backpropagationStrategy.printStrategy();
		toLog += "\n" + this.moveChoiceStrategy.printStrategy();

		if(this.beforeSimulationStrategy != null){
			toLog += "\n" + this.beforeSimulationStrategy.printStrategy();
		}else{
			toLog += "\n[BEFORE_SIM_STRATEGY = null]";
		}

		if(this.afterSimulationStrategy != null){
			toLog += "\n" + this.afterSimulationStrategy.printStrategy();
		}else{
			toLog += "\n[AFTER_SIM_STRATEGY = null]";
		}

		if(this.afterMoveStrategy != null){
			toLog += "\n" + this.afterMoveStrategy.printStrategy();
		}else{
			toLog += "\n[AFTER_MOVE_STRATEGY = null]";
		}

		GamerLogger.log("MCTSManager", toLog);

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
	public CompleteMoveStats getBestMove(MCTSNode theNode)throws MCTSException{

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
	public MCTSNode search(InternalPropnetMachineState initialState, long timeout, int gameStep) throws MCTSException{

		MCTSNode initialNode = this.prepareForSearch(initialState, gameStep);

		// We can be sure that the node is not null, but if it is terminal we cannot perform any search.
		// Note that the node being terminal might mean that the state is not terminal but legal moves
		// couldn't be correctly computed for all roles.
		if(initialNode.isTerminal()){
			throw new MCTSException("Impossible to perform search using the given state as root.");
		}

		this.performSearch(initialState, initialNode, timeout);

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
	private MCTSNode prepareForSearch(InternalPropnetMachineState initialState, int gameStep){

		this.iterations = 0;
		this.visitedNodes = 0;
		// This is required in case the method that wants to prepare the manager for the search fails before actually
		// performing the search. In this way we can make sure that if someone tries to retrieve the search time after
		// the search failed it won't get the positive time of the search performed before this one.
		this.searchStart = 0L;
		this.searchEnd = 0L;

		// Every time a move is played in the actual game...
		if(this.transpositionTable.getLastGameStep() != gameStep){
			// ...nodes not visited recently are removed from the transposition table...

			//long ttStart = System.currentTimeMillis();

			this.transpositionTable.clean(gameStep);

			//System.out.println(this.selectionStrategy.getClass().getSimpleName() + " cleaning TT : " + (System.currentTimeMillis()-ttStart));

			// ...and each strategy performs some clean-up of its internal structures (if necessary).
			//for(Strategy s : this.strategies){
			//	s.afterMoveAction();
			//}

			// ...and all the actions that need to be taken after a move is performed in the real game are performed.
			// NOTE: we cannot perform such actions right after the end of the search, we must wait until the execution
			// gets here so that we can check the new game step and be sure that the actual game proceeded.
			// Otherwise we'll also perform the "AfterMoveActions" even after the initial search during metagame, but
			// the actual game won't have been advanced the next time the search will be performed.
			if(this.afterMoveStrategy != null){
				this.afterMoveStrategy.afterMoveActions();
			}
		}

		// If it's the first time during the game that we call this method the transposition table is empty
		// so we create the first node, otherwise we check if the node is already in the tree.

		MCTSNode initialNode = this.transpositionTable.getNode(initialState);

		if(initialNode == null){

			//System.out.println("Creating initial node...");

			initialNode = this.theNodesFactory.createNewNode(initialState);
			this.transpositionTable.putNode(initialState, initialNode);
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
	private void performSearch(InternalPropnetMachineState initialState, MCTSNode initialNode, long timeout){
		this.searchStart = System.currentTimeMillis();
		while(System.currentTimeMillis() < timeout){
			this.currentIterationVisitedNodes = 0;

			//System.out.println();
			//System.out.println("MyIteration " + this.iterations);

			if(this.beforeSimulationStrategy != null){
				this.beforeSimulationStrategy.beforeSimulationActions();
			}

			SimulationResult simulationResult = this.searchNext(initialState, initialNode);
			this.iterations++;
			this.visitedNodes += this.currentIterationVisitedNodes;

			//((PnAMAFDecoupledMCTSNode)initialNode).printAMAF();


			if(this.afterSimulationStrategy != null){
				this.afterSimulationStrategy.afterSimulationActions(simulationResult);
			}
			//System.out.println("Iteration: " + this.iterations);
			//System.out.println("Stats: " + ((MASTStrategy)this.playoutStrategy).getNumStats());
		}
		this.searchEnd = System.currentTimeMillis();
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
	 * @return the goals of all players, obtained by the current MCTS iteration and that
	 *         must be backpropagated.
	 */
	private SimulationResult searchNext(InternalPropnetMachineState currentState, MCTSNode currentNode) {

		//System.out.println();
		//System.out.println("Search step:");

		//System.out.println();

		//System.out.println("Current state(Terminal:" + this.theMachine.isTerminal(currentState) + "):");
		//System.out.println(currentState);
		//System.out.println("Current node");
		//System.out.println(currentNode);

		//System.out.println();

		//int[] goals;

		SimulationResult simulationResult;

		// Check if the node is terminal, and if so, return as result the final goals (saved in the node) for all players.
		// NOTE: even if the node is terminal the state might not be, but an error occurred when computing legal
		// moves, so we cannot search deeper and we return the goals saved in the node.
		if(currentNode.isTerminal()){

			//System.out.println("Reached terminal state.");

			// If a state in the tree is terminal, it must record the goals for every player.
			// If it doesn't there must be a programming error.
			if(currentNode.getGoals() == null){
				GamerLogger.logError("MCTSManager", "Detected null goals for a treminal node in the tree.");
				throw new RuntimeException("Detected null goals for a treminal node in the tree.");
			}

			/*
			System.out.println("Detected terminal.");
			System.out.print("Returning goals:");
			String s = "[";
			s += " ";
			for(int i = 0; i < goals.length; i++){
				s += goals[i] + " ";
			}
			s += "]\n";
			System.out.print(s);
			*/

			return new SimulationResult(currentNode.getGoals());
		}

		// If the state is not terminal (and no error occurred when computing legal moves),
		// it can be visited (i.e. one of its moves explored) only if the depth limit has not been reached.
		if(this.currentIterationVisitedNodes >= this.maxSearchDepth){

			GamerLogger.log("MCTSManager", "Reached search depth limit. Search interrupted (in the Monte Carlo tree) before reaching a treminal state.");

			//System.out.print("Reached depth limit.");

			// The state is not terminal, but we have reached the depth limit.
			// So we must return some goals. Try to return the goals of the non-terminal state
			// and if they cannot be computed return default tie-goals.

			/*
			goals = theMachine.getSafeGoals(currentState);
			System.out.print("Returning goals:");
			String s = "[";
			s += " ";
			for(int i = 0; i < goals.length; i++){
				s += goals[i] + " ";
			}
			s += "]\n";
			System.out.print(s);
			*/


			return new SimulationResult(this.theMachine.getSafeGoalsAvg(currentState));
		}

		this.currentIterationVisitedNodes++;

		//System.out.println("Node: " + this.currentIterationVisitedNodes);

		MCTSJointMove mctsJointMove;
		InternalPropnetMachineState nextState;
		MCTSNode nextNode;

		/*
		System.out.println("Printing current node: ");
		switch(this.mctsType){
		// DUCT version of MCTS.
		case DUCT:
			break;
		case SUCT: // SUCT version of MCTS.
			printSUCTMovesTree(((InternalPropnetSUCTMCTSNode)currentNode).getMovesStats(), "");
			break;
		case SLOW_SUCT: // Slow SUCT version of MCTS.
			printMovesTree(((InternalPropnetSlowSUCTMCTSNode)currentNode).getMovesStats(), "");
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

			//System.out.println("Expanding move " + mctsJointMove);

		}else{

			//System.out.println("Selecting.");

			mctsJointMove = this.selectionStrategy.select(currentNode);

			//System.out.println("Selecting move " + mctsJointMove);
		}

		//System.out.println("Chosen move: " + mctsJointMove);

		//System.out.println("Computing next state and next node.");

		// Get the next state according to the joint move...
		nextState = this.theMachine.getInternalNextState(currentState, mctsJointMove.getJointMove());
		// ...and get the corresponding MCT node from the transposition table.
		nextNode = this.transpositionTable.getNode(nextState);

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

			//System.out.println("Creating next node...");

			//System.out.println("Adding new node to table: " + nextState);

			nextNode = this.theNodesFactory.createNewNode(nextState);
			this.transpositionTable.putNode(nextState, nextNode);

			// No need to perform playout if the node is terminal, we just return the goals in the node.
			// Otherwise we perform the playout.
			if(nextNode.isTerminal()){

				//System.out.println("Expanded state is terminal.");

				if(nextNode.getGoals() == null){
					GamerLogger.logError("MCTSManager", "Detected null goals for a treminal node in the tree.");
					throw new RuntimeException("Detected null goals for a treminal node in the tree.");
				}

				simulationResult = new SimulationResult(nextNode.getGoals());
			}else{

				//System.out.println("Performing playout.");

				// Check how many nodes can be visited after the current one. At this point
				// "currentIterationVisitedNodes" can be at most equal to the "maxSearchDepth".
				int availableDepth = this.maxSearchDepth - this.currentIterationVisitedNodes;

				if(availableDepth == 0){

					simulationResult = new SimulationResult(this.theMachine.getSafeGoalsAvg(nextState));

				}else{

					int[] playoutVisitedNodes = new int[1];
					// Note that if no depth is left for the playout, the playout itself will take care of
					// returning the added-state goal values (if any) or the default tie goal values.
					simulationResult = this.playoutStrategy.playout(nextState, playoutVisitedNodes, availableDepth);
					this.currentIterationVisitedNodes += playoutVisitedNodes[0];

					this.backpropagationStrategy.processPlayoutResult(nextNode, simulationResult);
				}

				//System.out.print("After playout - ");
				//((MemorizedStandardPlayout)this.playoutStrategy).printJM();
			}
		}else{
			// Otherwise, if we continue selecting:
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


		this.backpropagationStrategy.update(currentNode, mctsJointMove, nextState, simulationResult);
		return simulationResult;
	}

	/**
	 * This method creates a Monte Carlo tree node corresponding to the given state
	 * for the DUCT (Decoupled UCT version of the MCTS algorithm).
	 *
	 * 1. If the state is terminal the corresponding node will memorize the goals
	 * for each player in the state and the fact that the state is terminal.
	 * The legal moves will be null since there are supposed to be none in a
	 * terminal state. If an error occurs in computing the goal for a player, its
	 * goal value will be set to the default value (0 at the moment).
	 *
	 * 2. If the state is not terminal and the legal moves can be computed for each
	 * role with no errors the corresponding node will memorize such moves, their
	 * statistics, null goals and the fact that the node is not terminal.
	 *
	 * 3. If the state is not terminal but at least for one role the legal moves cannot
	 * be computed correctly the corresponding node will be treated as a pseudo-terminal
	 * node. This means that during the rest of the search, whenever this node will be
	 * encountered, it will be treated as terminal even if the corresponding state isn't.
	 * This choice has been made because without complete joint moves it is not possible
	 * to explore any further state from here. Thus the node will memorize the fact that
	 * the node is terminal (EVEN IF THE STATE ISN'T) and the goals for the players in
	 * the state (assigning default values if they cannot be computed), while the moves
	 * will be set to null.
	 *
	 * @param state the state for which to crate the tree node.
	 * @return the tree node corresponding to the state.
	 */
/*	private PnMCTSNode createNewNode(InternalPropnetMachineState state){

		//System.out.println("Creating new node.");

		int goals[] = null;
		boolean terminal = false;
		List<List<InternalPropnetMove>> allLegalMoves = null;

		switch(this.mctsType){
		// DUCT version of MCTS.
		case DUCT:

			DUCTMCTSMoveStats[][] ductMovesStats = null;

			// Terminal state:
			if(this.theMachine.isTerminal(state)){

				goals = this.theMachine.getSafeGoals(state);
				terminal = true;

			}else{// Non-terminal state:

				ductMovesStats = this.createDUCTMCTSMoves(state);

				// Error when computing moves.
				// If for at least one player the legal moves cannot be computed (an thus the moves
				// are returned as a null value), we consider this node "pseudo-terminal" (i.e. the
				// corresponding state is not terminal but we cannot explore any of the next states,
				// so we treat it as terminal during the MCT search). This means that we will need
				// the goal values in this node and they will not change for all the rest of the
				// search, so we compute them and memorize them.
				if(ductMovesStats == null){
					// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
					// We use the state machine method that will return default goal values for the player for which goal
					// values cannot be computed in this state.
					goals = this.theMachine.getSafeGoals(state);
					terminal = true;
				}
				// If the legal moves can be computed for every player, there is no need to compute the goals.
			}

			return new PnDUCTMCTSNode(ductMovesStats, goals, terminal);
		case SUCT: // SUCT version of MCTS.

			SUCTMCTSMoveStats[] suctMovesStats = null;
			int unvisitedLeavesCount = 0;

			// Terminal state:
			if(this.theMachine.isTerminal(state)){

				goals = this.theMachine.getSafeGoals(state);
				terminal = true;

			}else{// Non-terminal state:

				try {
					allLegalMoves = this.theMachine.getAllLegalMoves(state);


					//int r = 0;
					//for(List<InternalPropnetMove> legalPerRole : allLegalMoves){
					//	System.out.println("Legal moves for role " + r);
					//	System.out.print("[ ");
					//	for(InternalPropnetMove m : legalPerRole){
					//		System.out.print("(" + m.getIndex() + ", " + this.theMachine.internalMoveToMove(m) + ") ");
					//	}
					//	System.out.println("]");
					//	r++;
					//}

					suctMovesStats = this.createSUCTMCTSMoves(allLegalMoves);
					unvisitedLeavesCount = suctMovesStats[0].getUnvisitedSubleaves() * suctMovesStats.length;
					//this.printSUCTMovesTree(suctMovesStats, "");
				} catch (MoveDefinitionException e) {
					// Error when computing moves.
					GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SUCT state to the tree.");
					GamerLogger.logStackTrace("MCTSManager", e);
					// If for at least one player the legal moves cannot be computed we consider this node
					// "pseudo-terminal" (i.e. the corresponding state is not terminal but we cannot explore
					// any of the next states, so we treat it as terminal during the MCT search). This means
					// that we will need the goal values in this node and they will not change for all the rest
					// of the search, so we compute them and memorize them.

					// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
					// We use the state machine method that will return default goal values for the player for which goal
					// values cannot be computed in this state.

					//System.out.println("Null moves in non terminal state!");

					allLegalMoves = null;
					goals = this.theMachine.getSafeGoals(state);
					terminal = true;
					unvisitedLeavesCount = 0;
				}
				// If the legal moves can be computed for every player, there is no need to compute the goals.
			}

			return new PnSUCTMCTSNode(allLegalMoves, suctMovesStats, goals, terminal, unvisitedLeavesCount);

		case SLOW_SUCT: // Slow SUCT version of MCTS.

			SlowSUCTMCTSMoveStats[] ssuctMovesStats = null;
			List<SlowSUCTMCTSMoveStats> unvisitedLeaves = null;

			// Terminal state:
			if(this.theMachine.isTerminal(state)){

				goals = this.theMachine.getSafeGoals(state);
				terminal = true;

			}else{// Non-terminal state:

				try {
					allLegalMoves = this.theMachine.getAllLegalMoves(state);
					// The method that creates the SlowSUCTMCTSMoves assumes that the unvisitedLeaves parameter
					// passed as input will be initialized to an empty list. The method will then fill it
					// with all the SUCTMoves that are leaves of the tree representing the move statistics.
					// Such leaf moves represent the end of a path that form a joint move and are included
					// in the unvisitedLeaves list if the corresponding joint move hasn't been visited yet
					// during the search.
					unvisitedLeaves = new ArrayList<SlowSUCTMCTSMoveStats>();
					ssuctMovesStats = this.createSlowSUCTMCTSMoves(allLegalMoves, unvisitedLeaves);
					//this.printMovesTree(ssuctMovesStats, "");
				} catch (MoveDefinitionException e) {
					// Error when computing moves.
					GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SlowSUCT state to the tree.");
					GamerLogger.logStackTrace("MCTSManager", e);
					// If for at least one player the legal moves cannot be computed we consider this node
					// "pseudo-terminal" (i.e. the corresponding state is not terminal but we cannot explore
					// any of the next states, so we treat it as terminal during the MCT search). This means
					// that we will need the goal values in this node and they will not change for all the rest
					// of the search, so we compute them and memorize them.

					// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
					// We use the state machine method that will return default goal values for the player for which goal
					// values cannot be computed in this state.

					//System.out.println("Null moves in non terminal state!");
					allLegalMoves = null;
					goals = this.theMachine.getSafeGoals(state);
					terminal = true;
					unvisitedLeaves = null;
				}

				// If the legal moves can be computed for every player, there is no need to compute the goals.
			}

			return new PnSlowSUCTMCTSNode(ssuctMovesStats, unvisitedLeaves, goals, terminal);
		default:
			throw new RuntimeException("Someone added a new MCTS Node type and forgot to del with it here, when creating a new tree node.");
		}

	}
*/

	/**
	 * This method creates the moves statistics to be put in the MC Tree node of the given state
	 * for the DUCT version of the MCTS player.
	 *
	 * @param state the state for which to create the moves statistics.
	 * @return the moves statistics, if the moves can be computed, null otherwise.
	 */
/*	private DUCTMCTSMoveStats[][] createDUCTMCTSMoves(InternalPropnetMachineState state){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();
		DUCTMCTSMoveStats[][] moves = new DUCTMCTSMoveStats[roles.length][];

		try{
			List<InternalPropnetMove> legalMoves;

			for(int i = 0; i < roles.length; i++){

				legalMoves = this.theMachine.getInternalLegalMoves(state, roles[i]);

				moves[i] = new DUCTMCTSMoveStats[legalMoves.size()];
				for(int j = 0; j < legalMoves.size(); j++){
					moves[i][j] = new DUCTMCTSMoveStats(legalMoves.get(j));
				}
			}
		}catch(MoveDefinitionException e){
			// If for at least one player the legal moves cannot be computed, we return null.
			GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal DUCT state to the tree.");
			GamerLogger.logStackTrace("MCTSManager", e);

			moves = null;
		}

		return moves;
	}
*/

/*	private SlowSUCTMCTSMoveStats[] createSlowSUCTMCTSMoves(List<List<InternalPropnetMove>> allLegalMoves, List<SlowSUCTMCTSMoveStats> unvisitedLeaves){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();

		// For all the moves of my role (i.e. the role actually performing the search)
		// create the SUCT move containing the move statistics.
		int myIndex = this.myRole.getIndex();

		List<InternalPropnetMove> myLegalMoves = allLegalMoves.get(myIndex);
		SlowSUCTMCTSMoveStats[] moves = new SlowSUCTMCTSMoveStats[myLegalMoves.size()];
		for(int i = 0; i < myLegalMoves.size(); i++){
			moves[i] = new SlowSUCTMCTSMoveStats(myLegalMoves.get(i), i, createSlowSUCTMCTSMoves((myIndex+1)%(roles.length), roles.length, allLegalMoves, unvisitedLeaves));
			if(moves[i].getNextRoleMovesStats() == null){
				unvisitedLeaves.add(moves[i]);
			}
		}

		return moves;
	}

	private SlowSUCTMCTSMoveStats[] createSlowSUCTMCTSMoves(int roleIndex, int numRoles, List<List<InternalPropnetMove>> legalMoves, List<SlowSUCTMCTSMoveStats> unvisitedLeaves){

		if(roleIndex == this.myRole.getIndex()){
			return null;
		}

		List<InternalPropnetMove> roleLegalMoves = legalMoves.get(roleIndex);
		SlowSUCTMCTSMoveStats[] moves = new SlowSUCTMCTSMoveStats[roleLegalMoves.size()];
		for(int i = 0; i <roleLegalMoves.size(); i++){
			moves[i] = new SlowSUCTMCTSMoveStats(roleLegalMoves.get(i), i, createSlowSUCTMCTSMoves((roleIndex+1)%(numRoles), numRoles, legalMoves, unvisitedLeaves));
			if(moves[i].getNextRoleMovesStats() == null){
				unvisitedLeaves.add(moves[i]);
			}
		}

		return moves;
	}



	private SUCTMCTSMoveStats[] createSUCTMCTSMoves(List<List<InternalPropnetMove>> allLegalMoves){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();

		// Get legal moves for all players.
		//try {
		//	for(int i = 0; i < roles.length; i++){
		//		legalMoves.add(this.theMachine.getInternalLegalMoves(state, roles[i]));
		//	}
		//}catch (MoveDefinitionException e) {
			// If for at least one player the legal moves cannot be computed, we return null.
		//	GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SUCT state to the tree.");
		//	GamerLogger.logStackTrace("MCTSManager", e);

		//	return null;
		//}

		// For all the moves of my role (i.e. the role actually performing the search)
		// create the SUCT move containing the move statistics.
		int myIndex = this.myRole.getIndex();

		List<InternalPropnetMove> myLegalMoves = allLegalMoves.get(myIndex);
		SUCTMCTSMoveStats[] moves = new SUCTMCTSMoveStats[myLegalMoves.size()];
		for(int i = 0; i < myLegalMoves.size(); i++){
			moves[i] = new SUCTMCTSMoveStats(createSUCTMCTSMoves((myIndex+1)%(roles.length), roles.length, allLegalMoves));
		}

		return moves;
	}

	private SUCTMCTSMoveStats[] createSUCTMCTSMoves(int roleIndex, int numRoles, List<List<InternalPropnetMove>> allLegalMoves){

		if(roleIndex == this.myRole.getIndex()){
			return null;
		}

		List<InternalPropnetMove> roleLegalMoves = allLegalMoves.get(roleIndex);
		SUCTMCTSMoveStats[] moves = new SUCTMCTSMoveStats[roleLegalMoves.size()];
		for(int i = 0; i < roleLegalMoves.size(); i++){
			moves[i] = new SUCTMCTSMoveStats(createSUCTMCTSMoves((roleIndex+1)%(numRoles), numRoles, allLegalMoves));
		}

		return moves;
	}
*/

/*
	private void printMovesTree(SlowSequentialMCTSMoveStats[] moves, String tab){

		if(moves == null){
			return;
		}

		for(SlowSequentialMCTSMoveStats m : moves){
			System.out.println(tab + this.theMachine.internalMoveToMove(m.getTheMove()));
			this.printMovesTree(m.getNextRoleMovesStats(), tab + "   ");
		}

	}

	private void printSUCTMovesTree(SequentialMCTSMoveStats[] moves, String tab){

		if(moves == null){
			return;
		}

		for(SequentialMCTSMoveStats m : moves){
			System.out.println(tab + "[" + m + "]");
			this.printSUCTMovesTree(m.getNextRoleMovesStats(), tab + "   ");
		}

	}
*/
}
