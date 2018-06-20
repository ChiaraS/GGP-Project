/**
 *
 */
package org.ggp.base.util.statemachine.implementation.yapProlog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.transforms.DistinctAndNotMover;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.yapProlog.prover.YapProver;
import org.ggp.base.util.statemachine.implementation.yapProlog.prover.YapProverException;
import org.ggp.base.util.statemachine.implementation.yapProlog.transform.YapEngineSupport;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

import com.google.common.collect.ImmutableList;

/**
 * This class implements a state machine based on YAP prolog. For this state
 * machine to be able to work correctly, YAP prolog needs to be installed and the
 * Interprolog.jar library needs to be imported (a version that includes the
 * YapSubprocessEngine class, removed from most recent versions).
 *
 * This state machine represents game states in YAP prolog and uses YAP prolog to
 * reason on the game rules, translated from the GDL description into prolog syntax.
 *
 * ATTENTION:
 * if the initialization method fails throwing the StateMachineInitializationException
 * do not query this state machine as its answers will not be consistent.
 * (TODO: add a way to check if state
 * machine is in an inconsistent state and thus cannot answer queries).
 * On the contrary, if any other method fails throwing the StateMachineException,
 * it is possible to keep asking queries to the state machine since each method
 * that fails also makes sure to leave the state machine in a consistent state.
 *
 * ATTENTION:
 * if the initialization method succeeds YAP prolog will be running, so when you
 * stop using this state machine, remember to shut it down. In case the state
 * machine is being used by a StateMachineGamer, it will already take care of
 * calling the shutdown method whenever the match it is playing is aborted or
 * stopped.
 * Moreover, even if the initialization method fails, Yap Prolog might be running,
 * because the initialization failed after starting Yap Prolog (e.g. while computing
 * the initial state or the roles). In this case, if you don't want to use this state
 * machine, remember to shut it down.
 *
 * @author C.Sironi
 *
 */
public class YapStateMachine extends StateMachine {

	// INTERFACE TO THE REAL YAP ENGINE TO WHICH THIS STATE MACHINE CAN ASK QUERIES

	/**
	 * The fake Yap Prolog engine to which this state machine can ask queries
	 * and that will mask the real Yap Prolog engine and its errors.
	 */
	private YapProver yapProver;

	// WAITING TIME FOR QUERY ANSWERS

	/**
	 * Maximum time that the Yap Prover can wait to get the answer for a query.
	 */
	private long waitingTime;

	// PARAMETERS USED TO REDUCE COMPUTATIONS WHEN ASKING QUERIES

	/**
	 * This variable keeps track of the game state that currently is registered on
	 * YAP Prolog side.
	 * If null, it means that the game state on YAP Prolog side is unknown.
	 *
	 * This variable can be used to avoid running "computeState(MachineState)" on YAP
	 * Prolog side when the game state registered on it is already the one we need.
	 */
	private ExplicitMachineState currentYapState;

	/**
	 * The list of the roles as strings. Used to build as a string the query goals
	 * that require the ordered list of roles. This will avoid computing them every
	 * time they are needed.
	 */
	private List<String> fakeRoles;

	// PARAMETERS OF THE STATE MACHINE MEMORIZING THE INITIAL STATE AND THE LIST OF ROLES OF THECURRENT GAME

	/**
	 * Initial state of the current game.
	 */
	private ExplicitMachineState initialState;

	/**
	 * Ordered list of roles in the current game.
	 */
	private ImmutableList<ExplicitRole> roles;

	// SUPPORT CLASS THAT TAKES CARE OF TRANSLATIONS BETWEEN GDL AND PROLOG SYNTAX

	/**
	 * Class that handles the translation and the mapping from GDL to prolog and vice versa.
	 */
	private YapEngineSupport support;

	// CONSTRUCTORS

	public YapStateMachine(Random random){
		this(random, 500L);
	}

	public YapStateMachine(Random random, long waitingTime){
		super(random);
		this.waitingTime = waitingTime;
		this.yapProver = null;
		this.currentYapState = null;
		this.fakeRoles = null;
		this.initialState = null;
		this.roles = null;
		this.support = null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 *
	 * NOTE:
	 */
	@Override
	public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException{

		// Modify the description as for the GGP Base Prover
		description = DistinctAndNotMover.run(description);

		// Initialize the GDL <-> Prolog translator.
		this.support = new YapEngineSupport();

		try{
			// Create the interface with the Yap Prover.
			this.yapProver = new YapProver(this.support.toProlog(description), this.waitingTime);
		}catch(YapProverException e){
			GamerLogger.logError("StateMachine", "[YAP] Exception during state machine initialization. Yap Prover creation and startup failed!");
			GamerLogger.logStackTrace("StateMachine", e);
			this.yapProver = null;
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to create and start up Yap Prover.", e);
		}

		// If creation succeeded, compute initial state...
		try{
			this.initialState = computeInitialState();
		}catch(StateMachineException e){
			GamerLogger.logError("StateMachine", "[YAP] Exception during state machine initialization. Initial state computation failed!");
			GamerLogger.logStackTrace("StateMachine", e);
			this.initialState = null;
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to compute initial state.", e);
		}
		// ...and roles.
		try{
			this.roles = computeRoles();
		}catch(StateMachineException e){
			GamerLogger.logError("StateMachine", "[YAP] Exception during state machine initialization. Roles computation failed!");
			GamerLogger.logStackTrace("StateMachine", e);
			this.roles = null;
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to compute roles.", e);
		}
	}

	/**
	 * This method asks to prolog to compute the initial state of the game.
	 * When doing this, Yap Prolog also records the initial state as its
	 * current state.
	 *
	 * @return a machine state representing the initial state of the game.
	 * @throws StateMachineException if something went wrong and the initial
	 * state could not be computed.
	 */
	private ExplicitMachineState computeInitialState() throws StateMachineException
	{
		Object[] bindings;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.yapProver.askQueryResults("initialize_state(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("YAP-COMPUTE_INITIAL_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (YapProverException e) {

			//System.out.println("YAP-COMPUTE_INITIAL_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

			this.currentYapState = null;
			GamerLogger.logError("StateMachine", "[YAP] Exception during initial state computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			throw new StateMachineException("Exception during initial state computation.", e);
		}

		// If bindings is null => something went wrong on Yap Prolog side during the computation
		// or the initial state is not well defined in the game description or the game description
		// is not well defined for prolog and it cannot compute the correct answer.
		// Note that this should never happen, but an extra check won't hurt.
		if(bindings == null){
			// State computation failed on Yap prolog side.
			this.currentYapState = null;
			GamerLogger.logError("StateMachine", "[YAP] Computation of initial state on Yap Prolog side failed.");
			throw new StateMachineException("Computation of initial state on Yap Prolog side failed.");
		}

		// Compute the machine state using the Yap Prolog answer (note that it could be an empty array of strings in case
		// no propositions are true in the initial state. In this case the content of the machine state will be an empty HashSet)
		this.currentYapState = new ExplicitMachineState(support.askToState((String[]) bindings[0]));

		return currentYapState.clone();
	}

	/**
	 * This method asks to prolog to compute the roles of the game.
	 * When doing this, Yap Prolog also records the list of roles in the prolog
	 * syntax to speed up future queries.
	 *
	 * @return the roles of the game in the correct order.
	 * @throws StateMachineException if something went wrong and the game roles
	 * could not be computed.
	 */
	private ImmutableList<ExplicitRole> computeRoles() throws StateMachineException	{

		Object[] bindings;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.yapProver.askQueryResults("get_roles(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("YAP-COMPUTE_ROLES: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (YapProverException e) {

			//System.out.println("YAP-COMPUTE_ROLES: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[YAP] Exception during game roles computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a YapProverException we cannot be sure about the state
			// Yap Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentYapState = null;
			throw new StateMachineException("Exception during game roles computation.", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[YAP] Got no results for the computation of the game roles, while expecting at least one role.");
			throw new StateMachineException("Got no results for the computation of the game roles, while expecting at least one role.");
		}

		List<ExplicitRole> tmpRoles = new ArrayList<ExplicitRole>();

		try{
			tmpRoles = support.askToRoles((String[]) bindings[0]);

			this.fakeRoles = support.getFakeRoles(tmpRoles);

		}catch(SymbolFormatException e){
			GamerLogger.logError("StateMachine", "[YAP] Got exception while parsing the game roles.");
			GamerLogger.logStackTrace("StateMachine", e);
			this.fakeRoles = null;
			throw new StateMachineException("Impossible to parse the game roles.", e);
		}
		return ImmutableList.copyOf(tmpRoles);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role)
			throws StateMachineException {

		updateYapState(state);

		Object[] bindings = null;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.yapProver.askQueryResults("get_goal("+support.getFakeRole(role)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("YAP-GET_GOAL: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (YapProverException e) {

			//System.out.println("YAP-GET_GOAL: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[YAP] Exception during goal computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a YapProverException we cannot be sure about the state
			// Yap Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentYapState = null;
			throw new StateMachineException("Impossible to compute goal for role \"" + role + "\" in state " + state + ".", e);
		}

		if(bindings == null){
			//GamerLogger.logError("StateMachine", "[YAP] Got no goal when expecting at least one.");
			//throw new GoalDefinitionException(state, role);
			return new ArrayList<Integer>();
		}

		String[] goals = (String[]) bindings[0];

		/*
		if(goals.length != 1){
			GamerLogger.logError("StateMachine", "[YAP] Got goal results of size: " + goals.length + " when expecting size one.");
			//throw new GoalDefinitionException(state, role);
		}
		*/

		List<Integer> goalValues = new ArrayList<Integer>();

		for(String s : goals){
			try{
				int goal = Integer.parseInt(s);
				goalValues.add(new Integer(goal));
			}catch(NumberFormatException ex){
				GamerLogger.logError("StateMachine", "[YAP] Got goal results that is not a number.");
				GamerLogger.logStackTrace("StateMachine", ex);
				//throw new GoalDefinitionException(state, role, ex);
			}
		}

		return goalValues;

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException {

		updateYapState(state);

		boolean terminal = false;

		//long cancStart = System.currentTimeMillis();

		try {
			terminal = this.yapProver.askQuerySuccess("is_terminal");

			//System.out.println("YAP-IS_TERMINAL: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (YapProverException e) {

			//System.out.println("YAP-IS_TERMINAL: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[YAP] Exception during terminality computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a YapProverException we cannot be sure about the state
			// Yap Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentYapState = null;
			throw new StateMachineException("Impossible to compute terminality of state " + state + ".", e);
		}

		return terminal;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<ExplicitRole> getExplicitRoles() {
		return this.roles;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public ExplicitMachineState getExplicitInitialState() {
		return this.initialState;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role)
			throws MoveDefinitionException, StateMachineException {

		// TODO: should I catch the state machine exception before re-throwing it?
		updateYapState(state);

		Object[] bindings = null;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.yapProver.askQueryResults("get_legal_moves("+support.getFakeRole(role)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("YAP-GET_LEGAL_MOVES: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (YapProverException e) {

			//System.out.println("YAP-GET_LEGAL_MOVES: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[YAP] Exception during legal moves computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a YapProverException we cannot be sure about the state
			// Yap Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentYapState = null;
			throw new StateMachineException("Impossible to compute legal moves for role \"" + role + "\" in state " + state + ".", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[YAP] Got no legal moves when expecting at least one.");
			throw new MoveDefinitionException(state, role);
		}

		String[] yapMoves = (String[]) bindings[0];

		// Extra check, but this should never happen.
		if(yapMoves.length < 1){
			GamerLogger.logError("StateMachine", "[YAP] Got no legal moves when expecting at least one.");
			throw new MoveDefinitionException(state, role);
		}

		List<ExplicitMove> moves = support.askToMoves(yapMoves);

		return moves;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves)
			throws TransitionDefinitionException, StateMachineException {

		updateYapState(state);

		// Get the next state and assert it on Yap Prolog side.
		Object[] bindings = null;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.yapProver.askQueryResults("get_next_state("+fakeRoles+", "+support.getFakeMoves(moves)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("YAP-GET_NEXT_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (YapProverException e) {

			//System.out.println("YAP-GET_NEXT_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[YAP] Exception during next state computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			this.currentYapState = null;
			throw new StateMachineException("Impossible to compute next state for moves " + moves + " in state " + state + ".", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[YAP] Computation of next state on Yap Prolog side failed.");
			this.currentYapState = null;
			throw new StateMachineException("Computation of next state on Yap Prolog side failed for moves " + moves + " in state " + state + ".");
		}

		// Compute the machine state using the Yap Prolog answer (note that it could be an empty array of strings in case no
		// propositions are true in the computed next state. In this case the content of the machine state will be an empty HashSet)
		this.currentYapState = new ExplicitMachineState(support.askToState((String[]) bindings[0]));

		return this.currentYapState.clone();
	}

	/**
	 * Compute the given MachineState in the Prolog side.
	 *
	 * @throws StateMachineException if something went wrong and the state could not be updated.
	 */
	private void updateYapState(ExplicitMachineState state) throws StateMachineException{

		if(currentYapState==null||!currentYapState.equals(state)){

			boolean success = false;

			//long cancStart = System.currentTimeMillis();

			try {
				List<String> fakeState = support.getFakeMachineState(state.getContents());
				success = this.yapProver.askQuerySuccess("update_state("+ fakeState +")");

				//System.out.println("YAP-UPDATE_YAP_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

			} catch (YapProverException e) {

				//System.out.println("YAP-UPDATE_YAP_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

				this.currentYapState = null;
				GamerLogger.logError("StateMachine", "[YAP] Exception during prolog state update.");
				GamerLogger.logStackTrace("StateMachine", e);
				throw new StateMachineException("State update on YAP Prolog side failed for state: " + state, e);
			}

			// This should never happen, but you never know...
			if(!success){
				// State computation failed on Yap prolog side.
				this.currentYapState = null;
				GamerLogger.logError("StateMachine", "[YAP] Computation of current state on YAP Prolog side failed!");
				throw new StateMachineException("Computation on YAP Prolog side failed for state: " + state);
			}

			this.currentYapState = state;
		}
	}

	/**
	 * This method shuts down the Yap Prover.
	 * To be called ALWAYS when this state machine won't be used anymore to make sure that
	 * the fake Yap Prover will stop all running tasks and also the external Yap Prolog
	 * program.
	 *
	 * This method can also be called whenever we need to stop the external Yap Prolog
	 * program temporarily. The reason why we need to do this is the following: when the
	 * state machine is initialized by a certain thread, the external Yap Prolog program
	 * that is started will be linked to such thread, so whenever that thread is interrupted
	 * also the YAP program must be interrupted or the thread might get stuck forever or an
	 * instance of the external Yap Prolog program will keep running.
	 */
	@Override
	public void shutdown(){
		if(this.yapProver != null){
			this.yapProver.shutdown();
		}
		this.currentYapState = null;
	}


	// METHODS USED BY AN EXTERNAL CLASS TO FIX THE STATE OF THE STATE MACHINE IN CASE INITIALIZATION
	// FAILS WITH A RECOVERABLE FAILURE (I.E. INITIALIZATION OF YAP PROVER SUCCEEDED BUT SOMETHING ELSE
	// FAILED).

	/**
	 * This method checks if the state machine is completely unusable because the initialization
	 * of the Yap Prover failed or if it can still be used, probably with some fixing, because
	 * the Yap Prover is working.
	 *
	 * This method is needed because initialization fails both if the Yap Prover fails to be started
	 * and if the initial state and/or the roles fail to be computed. This is because if this state
	 * machine is the only one being used, it cannot give correct answers if one of the previously
	 * mentioned actions fails. However, if some external class has a way to check what exactly
	 * went wrong with initialization, it can fix it and still use this state machine.
	 *
	 * @return false if the state machine cannot be used at all because the Yap Prover failed to
	 * start, true otherwise.
	 */
	public boolean isUsable(){
		return this.yapProver != null;
	}

	/**
	 * Sets the initial state of this state machine.
	 *
	 * CONTRACT: only use this method in case the computation of the initial state failed and you
	 * want to still use this state machine passing it the initial state computed by another state
	 * machine.
	 *
	 * @param state the initial state.
	 */
	public void setInitialState(ExplicitMachineState state){
		this.initialState = state;
	}

	/**
	 * Sets the game roles of this state machine.
	 *
	 * CONTRACT: only use this method in case the computation of the game roles failed and you
	 * want to still use this state machine passing it the game roles computed by another state
	 * machine.
	 *
	 * @param roles the list of game roles.
	 */
	public void setRoles(List<ExplicitRole> roles){
		this.roles = ImmutableList.copyOf(roles);
		this.fakeRoles = support.getFakeRoles(roles);
	}

}
