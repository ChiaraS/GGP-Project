/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTTranspositionTable;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
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
	 * The role of the playing gamer.
	 */
	private InternalPropnetRole myRole;

	/**
	 * Maximum depth that the MCTS algorithm must visit.
	 */
	private int maxSearchDepth;

	/**
	 * The transposition table (implemented with HashMap that uses the internal propnet state as key
	 * and solves collisions with linked lists).
	 */
	private DUCTTranspositionTable transpositionTable;

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
	 *
	 */
	public InternalPropnetMCTSManager(SelectionStrategy selectionStrategy, ExpansionStrategy expansionStrategy,
			PlayoutStrategy playoutStrategy, BackpropagationStrategy backpropagationStrategy,
			MoveChoiceStrategy moveChoiceStrategy, InternalPropnetStateMachine theMachine,
			InternalPropnetRole myRole, int gameStepOffset, int maxSearchDepth) {

		this.selectionStrategy = selectionStrategy;
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.backpropagationStrategy = backpropagationStrategy;
		this.moveChoiceStrategy = moveChoiceStrategy;
		this.theMachine = theMachine;
		this.myRole = myRole;
		this.transpositionTable = new DUCTTranspositionTable(gameStepOffset);
		this.maxSearchDepth = maxSearchDepth;
	}

	public DUCTMove getBestMove(InternalPropnetMachineState initialState, long timeout, int gameStep) throws MCTSException{

		//System.out.println("Start DUCT\\MCTS getting best move on root state " + initialState + " with role " + this.myRole + ".");

		//System.out.println();

		InternalPropnetDUCTMCTreeNode initialNode = this.startSearch(initialState, timeout, gameStep);

		//System.out.println();
		//System.out.println();
		//System.out.println("Selecting best move on node: ");
		//System.out.println(initialNode);

		return this.moveChoiceStrategy.chooseBestMove(initialNode, this.myRole);
	}

	public void search(InternalPropnetMachineState initialState, long timeout, int gameStep) throws MCTSException{
		this.startSearch(initialState, timeout, gameStep);
	}

	private InternalPropnetDUCTMCTreeNode startSearch(InternalPropnetMachineState initialState, long timeout, int gameStep)  throws MCTSException{

		this.iterations = 0;
		this.visitedNodes = 0;

		this.transpositionTable.clean(gameStep);

		// If it's the first time during the game that we call this method the transposition table is empty
		// so we create the first node, otherwise we check if the node is already in the tree.

		InternalPropnetDUCTMCTreeNode initialNode = this.transpositionTable.getNode(initialState);

		if(initialNode == null){

			//System.out.println("Creating initial node...");

			initialNode = this.createNewNode(initialState);
			this.transpositionTable.putNode(initialState, initialNode);
		}

		//System.out.println("Initial node: ");
		//System.out.println(initialNode);

		// If the initial node to be used as a root to perform MCTS is terminal (or pseudo-terminal)
		// the search cannot be performed, so an exception is thrown.
		if(initialNode.isTerminal()){
			throw new MCTSException("Impossible to perform search using the given state as root.");
		}

		while(System.currentTimeMillis() < timeout){
			this.currentIterationVisitedNodes = 0;
			this.searchNext(initialState, initialNode);
			this.iterations++;
			this.visitedNodes += this.currentIterationVisitedNodes;

			//System.out.println("Iteration: " + this.iterations);
		}

		return initialNode;

	}

	private int[] searchNext(InternalPropnetMachineState currentState, InternalPropnetDUCTMCTreeNode currentNode) {

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

			//System.out.print("Reached depth limit.");

			// The state is not terminal, but we have reached the depth limit.
			// So we must return some goals. Try to return the goals of the non-terminal state
			// and if they cannot be computed return default tie-goals.

			// Try to get the goals of the state.
			try{
				goals = this.theMachine.getGoals(currentState);
			}catch(GoalDefinitionException e){

				GamerLogger.logError("MCTSManager", "Search interrupted (in the Monte Carlo tree) before reaching a treminal state. Impossible to compute real goals. Returning default tie goals.");
				GamerLogger.logStackTrace("MCTSManager", e);
				goals = this.theMachine.getTieGoals();

			}

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

			return goals;
		}

		this.currentIterationVisitedNodes++;

		//System.out.println("Node: " + this.currentIterationVisitedNodes);

		DUCTJointMove ductJointMove;
		InternalPropnetMachineState nextState;
		InternalPropnetDUCTMCTreeNode nextNode;

		// If the state is not terminal we must check if we have to expand it or if we have to continue the selection.
		// Depending on what needs to be done, get the joint move to be expanded/selected.
		boolean expansionRequired = this.expansionStrategy.expansionRequired(currentNode);
		if(expansionRequired){

			//System.out.println("Expanding.");

			ductJointMove = this.expansionStrategy.expand(currentNode);
		}else{

			//System.out.println("Selecting.");

			ductJointMove = this.selectionStrategy.select(currentNode);
		}

		//System.out.println("Chosen move: " + ductJointMove);

		//System.out.println("Computing next state and next node.");

		// Get the next state according to the joint move...
		nextState = this.theMachine.getInternalNextState(currentState, ductJointMove.getJointMove());
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
		if(nextNode == null){

			//System.out.println("Creating next node...");

			//System.out.println("Adding new node to table: " + nextState);

			nextNode = this.createNewNode(nextState);
			this.transpositionTable.putNode(nextState, nextNode);
		}

		// Now that we have the MCT node, if we are expanding:
		if(expansionRequired){
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
				goals = this.playoutStrategy.playout(nextState, this.myRole, playoutVisitedNodes, availableDepth);
				this.currentIterationVisitedNodes += playoutVisitedNodes[0];
				//System.out.println("Node: " + this.visitedNodes);
			}
		}else{

			//System.out.println("Searching next selected node.");

			// Otherwise, if we are selecting:
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

		this.backpropagationStrategy.update(currentNode, ductJointMove, goals);
		return goals;
	}

	/**
	 * This method creates a MC tree node corresponding to the given state.
	 *
	 * 1. If the state is terminal the corresponding node will memorize the goals
	 * for each player in the state and the fact that the state is terminal.
	 * The legal moves will be null since there are supposed to be none in a
	 * terminal state. If an error occurs in computing goals, the default goal
	 * values will be memorized.
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
	 * to explore any further state from here. Thus the node will memorize the legal moves
	 * that could be computed, the fact that the node is terminal (EVEN IF THE STATE ISN'T)
	 * and the goals for the players in the state (assigning default values if they cannot
	 * be computed).
	 *
	 * @param state the state for which to crate the tree node.
	 * @return the tree node corresponding to the state.
	 */
	private InternalPropnetDUCTMCTreeNode createNewNode(InternalPropnetMachineState state){

		int goals[];
		boolean terminal;

		// Terminal state:
		if(this.theMachine.isTerminal(state)){

			terminal = true;

			try{
				// Try to compute the goals for each player.
				goals = this.theMachine.getGoals(state);
			}catch(GoalDefinitionException e) {
				// If the computation of the goals fails assign to the state default goal values.
				// When computation fails in a terminal state we consider it a tie for all other
				// roles and a loss for ours.
				GamerLogger.logError("MCTSManager", "Failed to retrieve the goals while adding a terminal state to the tree. Setting default loss goals.");
				GamerLogger.logStackTrace("MCTSManager", e);
				goals = this.theMachine.getLossGoals(this.myRole);
			}

			return new InternalPropnetDUCTMCTreeNode(null, goals, terminal);
		}else{// Non-terminal state:

			InternalPropnetRole[] roles = this.theMachine.getInternalRoles();
			DUCTMove[][] moves = new DUCTMove[roles.length][];
			goals = null;
			terminal = false;

			for(int i = 0; i < roles.length; i++){

				List<InternalPropnetMove> legalMoves;

				try{
					// If the legal moves can be computed for every player, there is no need to compute the goals.
					legalMoves = this.theMachine.getInternalLegalMoves(state, roles[i]);

					moves[i] = new DUCTMove[legalMoves.size()];
					for(int j = 0; j < legalMoves.size(); j++){
						moves[i][j] = new DUCTMove(legalMoves.get(j));
					}

				}catch(MoveDefinitionException e) {
					// If for at least one player the legal moves cannot be computed, we consider
					// this node "pseudo-terminal" (i.e. the corresponding state is not terminal but
					// we cannot explore any of the next states, so we treat it as terminal). This
					// means that we will need the goal values in this node and they will not change
					// for all the rest of the search, so we compute them and memorize them.
					GamerLogger.logError("MCTSManager", "Failed to retrieve the legal actions for role " + this.theMachine.internalRoleToRole(roles[i]) + " while adding non-terminal state to the tree.");
					GamerLogger.logStackTrace("MCTSManager", e);

					moves[i] = null;
					terminal = true;

					// If it is the first time that we cannot compute the legal moves for a player in this
					// state we must compute the goals, otherwise we already computed them previously.
					if(goals == null){
						try{
							// Try to compute the goals for each player.
							goals = this.theMachine.getGoals(state);
						}catch(GoalDefinitionException gde) {
							// If the computation of the goals fails assign to the state default goal values.
							// When computation fails in a non-terminal state we consider it a tie for all the
							// roles. TODO: is this ok?
							GamerLogger.logError("MCTSManager", "Failed to retrieve the goals while adding a terminal state to the tree. Setting default loss goals.");
							GamerLogger.logStackTrace("MCTSManager", gde);
							goals = this.theMachine.getLossGoals(this.myRole);
						}
					}
				}
			}

			return new InternalPropnetDUCTMCTreeNode(moves, goals, terminal);
		}
	}

	public int getIterations(){
		return this.iterations;
	}

	public int getVisitedNodes(){
		return this.visitedNodes;
	}

}
