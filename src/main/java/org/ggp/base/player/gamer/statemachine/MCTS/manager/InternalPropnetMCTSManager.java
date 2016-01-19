/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSTranspositionTable;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.InternalPropnetSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SUCTMCTSMove;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

/**
 * @author C.Sironi
 *
 */
public class InternalPropnetMCTSManager extends MCTSManager {

	/**
	 * True if the manager must run the DUCT version of Monte Carlo Tree Search,
	 * false otherwise.
	 */
	private boolean DUCT;

	/**
	 * Role of the player that is actually performing the search.
	 */
	private InternalPropnetRole myRole;

	/**
	 * Strategies that the MCTSManger must use to perform the different MCTS phases.
	 */
	private SelectionStrategy selectionStrategy;

	private ExpansionStrategy expansionStrategy;

	private PlayoutStrategy playoutStrategy;

	private BackpropagationStrategy backpropagationStrategy;

	private MoveChoiceStrategy moveChoiceStrategy;

	/**
	 * The state machine that this MCTS manager uses to reason on the game
	 */
	private InternalPropnetStateMachine theMachine;

	/**
	 * Maximum depth that the MCTS algorithm must visit.
	 */
	private int maxSearchDepth;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private MCTSTranspositionTable transpositionTable;

	/**
	 * Number of performed iterations.
	 */
	private int iterations;

	/**
	 * Number of all visited states since the start of the search.
	 */
	private int visitedNodes;

	/**
	 * Number of visited nodes in the current iteration so far.
	 */
	private int currentIterationVisitedNodes;

	/**
	 * Start time of last performed search.
	 */
	private long searchStart;

	/**
	 * End time of last performed search.
	 */
	private long searchEnd;

	/**
	 *
	 */
	public InternalPropnetMCTSManager(boolean DUCT, InternalPropnetRole myRole,
			SelectionStrategy selectionStrategy, ExpansionStrategy expansionStrategy,
			PlayoutStrategy playoutStrategy, BackpropagationStrategy backpropagationStrategy,
			MoveChoiceStrategy moveChoiceStrategy, InternalPropnetStateMachine theMachine,
			int gameStepOffset, int maxSearchDepth) {

		this.DUCT = DUCT;
		this.myRole = myRole;
		this.selectionStrategy = selectionStrategy;
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.backpropagationStrategy = backpropagationStrategy;
		this.moveChoiceStrategy = moveChoiceStrategy;
		this.theMachine = theMachine;
		this.transpositionTable = new MCTSTranspositionTable(gameStepOffset);
		this.maxSearchDepth = maxSearchDepth;
		this.iterations = 0;
		this.visitedNodes = 0;
		this.currentIterationVisitedNodes = 0;
		this.searchStart = 0;
		this.searchEnd = 0;

		if(this.DUCT){
			GamerLogger.log("MCTSManager", "MCTS manager initialized to perform DUCT MCTS with maximum search dept " + this.maxSearchDepth + ".");
		}else{
			GamerLogger.log("MCTSManager", "MCTS manager initialized to perform SUCT MCTS with maximum search dept " + this.maxSearchDepth + ".");
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
	public MCTSMove getBestMove(InternalPropnetMCTSNode theNode)throws MCTSException{

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
	public InternalPropnetMCTSNode search(InternalPropnetMachineState initialState, long timeout, int gameStep) throws MCTSException{

		InternalPropnetMCTSNode initialNode = this.prepareForSearch(initialState, gameStep);

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
	 * 				   table and be used as time stamp for tree nodes).
	 * @return the tree node corresponding to the given initial state.
	 */
	private InternalPropnetMCTSNode prepareForSearch(InternalPropnetMachineState initialState, int gameStep){

		this.iterations = 0;
		this.visitedNodes = 0;
		// This is required in case the method that wants to prepare the manager for the search fails before actually
		// performing the search. In this way we can make sure that if someone tries to retrieve the search time after
		// the search failed it won't get the positive time of the search performed before this one.
		this.searchStart = 0L;
		this.searchEnd = 0L;

		this.transpositionTable.clean(gameStep);

		// If it's the first time during the game that we call this method the transposition table is empty
		// so we create the first node, otherwise we check if the node is already in the tree.

		InternalPropnetMCTSNode initialNode = this.transpositionTable.getNode(initialState);

		if(initialNode == null){

			//System.out.println("Creating initial node...");

			initialNode = this.createNewNode(initialState);
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
	private void performSearch(InternalPropnetMachineState initialState, InternalPropnetMCTSNode initialNode, long timeout){
		this.searchStart = System.currentTimeMillis();
		while(System.currentTimeMillis() < timeout){
			this.currentIterationVisitedNodes = 0;
			this.searchNext(initialState, initialNode);
			this.iterations++;
			this.visitedNodes += this.currentIterationVisitedNodes;
			//System.out.println("Iteration: " + this.iterations);
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
	private int[] searchNext(InternalPropnetMachineState currentState, InternalPropnetMCTSNode currentNode) {

		//System.out.println();
		//System.out.println("Search step:");

		//System.out.println();

		//System.out.println("Current state(Terminal:" + this.theMachine.isTerminal(currentState) + "):");
		//System.out.println(currentState);
		//System.out.println("Current node");
		//System.out.println(currentNode);

		//System.out.println();

		int[] goals;

		// Check if the node is terminal, and if so, return the final goals (saved in the node) for all players.
		// NOTE: even if the node is terminal the state might not be, but an error occurred when computing legal
		// moves, so we cannot search deeper and we return the goals saved in the node.
		if(currentNode.isTerminal()){

			//System.out.println("Reached terminal state.");

			goals = currentNode.getGoals();
			// If a state in the tree is terminal, it must record the goals for every player.
			// If it doesn't there must be a programming error.
			if(goals == null){
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

			return goals;
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
			System.out.print("Returning goals:");
			String s = "[";
			s += " ";
			for(int i = 0; i < goals.length; i++){
				s += goals[i] + " ";
			}
			s += "]\n";
			System.out.print(s);
			*/

			return theMachine.getSafeGoals(currentState);
		}

		this.currentIterationVisitedNodes++;

		//System.out.println("Node: " + this.currentIterationVisitedNodes);

		MCTSJointMove mctsJointMove;
		InternalPropnetMachineState nextState;
		InternalPropnetMCTSNode nextNode;

		// If the state is not terminal we must check if we have to expand it or if we have to continue the selection.
		// Depending on what needs to be done, get the joint move to be expanded/selected.
		boolean expansionRequired = this.expansionStrategy.expansionRequired(currentNode);
		if(expansionRequired){

			//System.out.println("Expanding.");

			mctsJointMove = this.expansionStrategy.expand(currentNode);
		}else{

			//System.out.println("Selecting.");

			mctsJointMove = this.selectionStrategy.select(currentNode);
		}

		//System.out.println("Chosen move: " + ductJointMove);

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
		// TODO: if the node doesn't exists, after creation we perform a playout on it, both
		// in the case when we were performing selection and in the case when we were performing
		// expansion. If the node exists, we continue searching on it, even if we were performing
		// expansion. Is this ok?
		if(nextNode == null){

			//System.out.println("Creating next node...");

			//System.out.println("Adding new node to table: " + nextState);

			nextNode = this.createNewNode(nextState);
			this.transpositionTable.putNode(nextState, nextNode);

			// No need to perform playout if the node is terminal, we just return the goals in the node.
			// Otherwise we perform the playout.
			if(nextNode.isTerminal()){

				//System.out.println("Expanded state is terminal.");

				goals = nextNode.getGoals();
			}else{

				//System.out.println("Performing playout.");

				// Check how many nodes can be visited after the current one. At this point
				// "currentIterationVisitedNodes" can be at most equal to the "maxSearchDepth".
				int availableDepth = this.maxSearchDepth - this.currentIterationVisitedNodes;

				int[] playoutVisitedNodes = new int[1];
				// Note that if no depth is left for the playout, the playout itself will take care of
				// returning the added-state goal values (if any) or the default tie goal values.
				goals = this.playoutStrategy.playout(nextState, playoutVisitedNodes, availableDepth);
				this.currentIterationVisitedNodes += playoutVisitedNodes[0];
				//System.out.println("Node: " + this.visitedNodes);
			}
		}else{
			// Otherwise, if we continue selecting:
			goals = this.searchNext(nextState, nextNode);
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

		this.backpropagationStrategy.update(currentNode, mctsJointMove, goals);
		return goals;
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
	 * role with no errors the corresponding node will memorize such moves, null goals
	 * and the fact that the node is not terminal.
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
	private InternalPropnetMCTSNode createNewNode(InternalPropnetMachineState state){

		int goals[] = null;
		boolean terminal = false;

		// DUCT version of MCTS.
		if(this.DUCT){

			DUCTMCTSMove[][] moves = null;

			// Terminal state:
			if(this.theMachine.isTerminal(state)){

				goals = this.theMachine.getSafeGoals(state);
				terminal = true;

			}else{// Non-terminal state:
				moves = this.createDUCTMCTSMoves(state);

				// Error when computing moves.
				// If for at least one player the legal moves cannot be computed (an thus the moves
				// are returned as a null value), we consider this node "pseudo-terminal" (i.e. the
				// corresponding state is not terminal but we cannot explore any of the next states,
				// so we treat it as terminal during the MCT search). This means that we will need
				// the goal values in this node and they will not change for all the rest of the
				// search, so we compute them and memorize them.
				if(moves == null){
					// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
					// We use the state machine method that will return default goal values for the player for which goal
					// values cannot be computed in this state.
					goals = this.theMachine.getSafeGoals(state);
					terminal = true;
				}
				// If the legal moves can be computed for every player, there is no need to compute the goals.
			}

			return new InternalPropnetDUCTMCTSNode(moves, goals, terminal);

		}else{// SUCT version of MCTS.

			SUCTMCTSMove[] moves = null;
			List<SUCTMCTSMove> unvisitedLeaves = null;

			// Terminal state:
			if(this.theMachine.isTerminal(state)){

				goals = this.theMachine.getSafeGoals(state);
				terminal = true;

			}else{// Non-terminal state:
				// The method that creates the SUCTMCTSMoves assumes that the unvisitedLeaves parameter
				// passed as input will be initialized to an empty list. The method will then fill it
				// with all the SUCTMoves that are leaves of the tree representing the move statistics.
				// Such leaf moves represent the end of a path that form a joint move and are included
				// in the unvisitedLeaves list if the corresponding joint move hasn't been visited yet
				// during the search.
				unvisitedLeaves = new ArrayList<SUCTMCTSMove>();
				moves = this.createSUCTMCTSMoves(state, unvisitedLeaves);

				// Error when computing moves.
				// If for at least one player the legal moves cannot be computed (an thus the moves
				// are returned as a null value), we consider this node "pseudo-terminal" (i.e. the
				// corresponding state is not terminal but we cannot explore any of the next states,
				// so we treat it as terminal during the MCT search). This means that we will need
				// the goal values in this node and they will not change for all the rest of the
				// search, so we compute them and memorize them.
				if(moves == null){
					// Compute the goals for each player. We are in a non terminal state so the goal might not be defined.
					// We use the state machine method that will return default goal values for the player for which goal
					// values cannot be computed in this state.
					goals = this.theMachine.getSafeGoals(state);
					terminal = true;
					unvisitedLeaves = null;
				}
				// If the legal moves can be computed for every player, there is no need to compute the goals.
			}

			return new InternalPropnetSUCTMCTSNode(moves, unvisitedLeaves, goals, terminal);

		}
	}

	/**
	 * This method creates the moves statistics to be put in the MC Tree node of the given state
	 * for the DUCT version of the MCTS player.
	 *
	 * @param state the state for which to create the moves statistics.
	 * @return the moves statistics, if the moves can be computed, null otherwise.
	 */
	private DUCTMCTSMove[][] createDUCTMCTSMoves(InternalPropnetMachineState state){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();
		DUCTMCTSMove[][] moves = new DUCTMCTSMove[roles.length][];

		try{
			List<InternalPropnetMove> legalMoves;

			for(int i = 0; i < roles.length; i++){

				legalMoves = this.theMachine.getInternalLegalMoves(state, roles[i]);

				moves[i] = new DUCTMCTSMove[legalMoves.size()];
				for(int j = 0; j < legalMoves.size(); j++){
					moves[i][j] = new DUCTMCTSMove(legalMoves.get(j));
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

	private SUCTMCTSMove[] createSUCTMCTSMoves(InternalPropnetMachineState state, List<SUCTMCTSMove> unvisitedLeaves){

		InternalPropnetRole[] roles = this.theMachine.getInternalRoles();

		List<List<InternalPropnetMove>> legalMoves = new ArrayList<List<InternalPropnetMove>>();

		// Get legal moves for all players.
		try {
			for(int i = 0; i < roles.length; i++){
				legalMoves.add(this.theMachine.getInternalLegalMoves(state, roles[i]));
			}
		}catch (MoveDefinitionException e) {
			// If for at least one player the legal moves cannot be computed, we return null.
			GamerLogger.logError("MCTSManager", "Failed to retrieve the legal moves while adding non-terminal SUCT state to the tree.");
			GamerLogger.logStackTrace("MCTSManager", e);

			return null;
		}

		// For all the moves of my role (i.e. the role actually performing the search)
		// create the SUCT move containing the move statistics.
		int myIndex = this.myRole.getIndex();

		List<InternalPropnetMove> myLegalMoves = legalMoves.get(myIndex);
		SUCTMCTSMove[] moves = new SUCTMCTSMove[myLegalMoves.size()];
		for(int i = 0; i < myLegalMoves.size(); i++){
			moves[i] = new SUCTMCTSMove(myLegalMoves.get(i), i, createSUCTMCTSMoves((myIndex+1)%(roles.length), roles.length, legalMoves, unvisitedLeaves));
			if(moves[i].getNextRoleMoves() == null){
				unvisitedLeaves.add(moves[i]);
			}
		}

		return moves;
	}

	private SUCTMCTSMove[] createSUCTMCTSMoves(int roleIndex, int numRoles, List<List<InternalPropnetMove>> legalMoves, List<SUCTMCTSMove> unvisitedLeaves){

		if(roleIndex == this.myRole.getIndex()){
			return null;
		}

		List<InternalPropnetMove> roleLegalMoves = legalMoves.get(roleIndex);
		SUCTMCTSMove[] moves = new SUCTMCTSMove[roleLegalMoves.size()];
		for(int i = 0; i <roleLegalMoves.size(); i++){
			moves[i] = new SUCTMCTSMove(roleLegalMoves.get(i), i, createSUCTMCTSMoves((roleIndex+1)%(numRoles), numRoles, legalMoves, unvisitedLeaves));
			if(moves[i].getNextRoleMoves() == null){
				unvisitedLeaves.add(moves[i]);
			}
		}

		return moves;
	}

	public int getIterations(){
		return this.iterations;
	}

	public int getVisitedNodes(){
		return this.visitedNodes;
	}

	public long getSearchTime(){
		return (this.searchEnd - this.searchStart);
	}

}
