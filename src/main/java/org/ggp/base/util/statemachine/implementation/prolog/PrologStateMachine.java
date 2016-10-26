package org.ggp.base.util.statemachine.implementation.prolog;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.transforms.DistinctAndNotMover;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prolog.prover.PrologProver;
import org.ggp.base.util.statemachine.implementation.prolog.prover.PrologProverException;
import org.ggp.base.util.statemachine.implementation.yapProlog.transform.YapEngineSupport;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

import com.google.common.collect.ImmutableList;

/** NOTE: THIS CLASS ONLY WORKS WITH YAP PROLOG ON LINUX - DON'T USE THIS CLASS BUT RATHER USE YapStateMachine
 * This class implements a state machine based on prolog. For this state machine
 * to be able to work correctly, at least one among YAP, SWI and XSB prolog needs
 * to be installed and the Interprolog.jar library needs to be imported (a version
 * that includes the XXXSubprocessEngine class corresponding to the type of prolog
 * program that we want to use: either YAPSubprocessEngine, SWISubprocessEngine or
 * XSBSubprocessEngine - Note that from InterProlog2.2a4 the YAPSubprocessEngine
 * and the SWISubprocessEngine have been removed).
 *
 * This state machine represents game states in prolog and uses it to reason on the
 * game rules, translated from the GDL description into prolog syntax.
 *
 * ATTENTION:
 * if the initialization method fails throwing the StateMachineInitializationException
 * do not query this state machine as its answers will not be consistent.
 * (TODO: add a way to check if state machine is in an inconsistent state and thus
 * cannot answer queries).
 * On the contrary, if any other method fails throwing the StateMachineException,
 * it is possible to keep asking queries to the state machine since each method
 * that fails also makes sure to leave the state machine in a consistent state.
 *
 * ATTENTION:
 * if the initialization method succeeds, prolog will be running, so when you
 * stop using this state machine, remember to shut it down. In case the state
 * machine is being used by a StateMachineGamer, it will already take care of
 * calling the shutdown method whenever the match it is playing is aborted or
 * stopped.
 * Moreover, even if the initialization method fails, prolog might be running,
 * because the initialization failed after starting Prolog (e.g. while computing
 * the initial state or the roles). In this case, if you don't want to use this state
 * machine, remember to shut it down.
 *
 * @author C.Sironi
 *
 */
public class PrologStateMachine extends StateMachine {

	// INTERFACE TO THE REAL PROLOG ENGINE TO WHICH THIS STATE MACHINE CAN ASK QUERIES

	private PrologProver.PROLOG_TYPE prologType;

	/**
	 * The fake Prolog engine to which this state machine can ask queries
	 * and that will mask the real Prolog engine and its errors.
	 */
	private PrologProver prologProver;

	// WAITING TIME FOR QUERY ANSWERS

	/**
	 * Maximum time that the Prolog Prover can wait to get the answer for a query.
	 */
	private long waitingTime;

	// PARAMETERS USED TO REDUCE COMPUTATIONS WHEN ASKING QUERIES

	/**
	 * This variable keeps track of the game state that currently is registered on
	 * Prolog side.
	 * If null, it means that the game state on Prolog side is unknown.
	 *
	 * This variable can be used to avoid running "computeState(MachineState)" on
	 * Prolog side when the game state registered on it is already the one we need.
	 */
	private ExplicitMachineState currentPrologState;

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
	 * TODO: Check the YapEngineSupport class and change the name to PrologEngineSupport,
	 * since now it is used by all types of prolog.
	 */
	private YapEngineSupport support;

	// CONSTRUCTORS

	public PrologStateMachine(){
		this(PrologProver.PROLOG_TYPE.YAP, 500L);
	}

	public PrologStateMachine(PrologProver.PROLOG_TYPE prologType){
		this(prologType, 500L);
	}

	public PrologStateMachine(long waitingTime){
		this(PrologProver.PROLOG_TYPE.YAP, waitingTime);
	}

	public PrologStateMachine(PrologProver.PROLOG_TYPE prologType, long waitingTime){
		this.prologType = prologType;
		this.waitingTime = waitingTime;
		this.prologProver = null;
		this.currentPrologState = null;
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
			// Create the interface with the Prover.
			this.prologProver = new PrologProver(this.support.toProlog(description), this.prologType, this.waitingTime);
		}catch(PrologProverException e){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during state machine initialization. " + this.prologType + " Prolog Prover creation and startup failed!");
			GamerLogger.logStackTrace("StateMachine", e);
			this.prologProver = null;
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to create and start up " + this.prologType + " Prolog Prover.", e);
		}

		// If creation succeeded, compute initial state...
		try{
			this.initialState = computeInitialState();
		}catch(StateMachineException e){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during state machine initialization. Initial state computation failed!");
			GamerLogger.logStackTrace("StateMachine", e);
			this.initialState = null;
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to compute initial state.", e);
		}
		// ...and roles.
		try{
			this.roles = computeRoles();
		}catch(StateMachineException e){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during state machine initialization. Roles computation failed!");
			GamerLogger.logStackTrace("StateMachine", e);
			this.roles = null;
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to compute roles.", e);
		}
	}

	/**
	 * This method asks to prolog to compute the initial state of the game.
	 * When doing this, Prolog also records the initial state as its
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
			bindings = this.prologProver.askQueryResults("initialize_state(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("PROLOG-COMPUTE_INITIAL_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (PrologProverException e) {

			//System.out.println("PROLOG-COMPUTE_INITIAL_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

			this.currentPrologState = null;
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during initial state computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			throw new StateMachineException("Exception during initial state computation.", e);
		}

		// If bindings is null => something went wrong on Prolog side during the computation
		// or the initial state is not well defined in the game description or the game description
		// is not well defined for prolog and it cannot compute the correct answer.
		// Note that this should never happen, but an extra check won't hurt.
		if(bindings == null){
			// State computation failed on prolog side.
			this.currentPrologState = null;
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Computation of initial state on " + this.prologType + " Prolog side failed.");
			throw new StateMachineException("Computation of initial state on " + this.prologType + " Prolog side failed.");
		}

		// Compute the machine state using the Prolog answer (note that it could be an empty array of strings in case
		// no propositions are true in the initial state. In this case the content of the machine state will be an empty HashSet)
		this.currentPrologState = new ExplicitMachineState(support.askToState((String[]) bindings[0]));

		return currentPrologState.clone();
	}

	/**
	 * This method asks to prolog to compute the roles of the game.
	 * When doing this, Prolog also records the list of roles in the prolog
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
			bindings = this.prologProver.askQueryResults("get_roles(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("PROLOG-COMPUTE_ROLES: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (PrologProverException e) {

			//System.out.println("PROLOG-COMPUTE_ROLES: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during game roles computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a PrologProverException we cannot be sure about the state
			// Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentPrologState = null;
			throw new StateMachineException("Exception during game roles computation.", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got no results for the computation of the game roles, while expecting at least one role.");
			throw new StateMachineException("Got no results for the computation of the game roles, while expecting at least one role.");
		}

		List<ExplicitRole> tmpRoles = new ArrayList<ExplicitRole>();

		try{
			tmpRoles = support.askToRoles((String[]) bindings[0]);

			this.fakeRoles = support.getFakeRoles(tmpRoles);

		}catch(SymbolFormatException e){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got exception while parsing the game roles.");
			GamerLogger.logStackTrace("StateMachine", e);
			this.fakeRoles = null;
			throw new StateMachineException("Impossible to parse the game roles.", e);
		}
		return ImmutableList.copyOf(tmpRoles);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	/**
	 * TODO: ATTNETION! This method has never been tested.
	 */
	@Override
	public List<Integer> getOneRoleGoals(ExplicitMachineState state, ExplicitRole role)	throws StateMachineException {

		updatePrologState(state);

		Object[] bindings = null;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.prologProver.askQueryResults("get_goal("+support.getFakeRole(role)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("PROLOG-GET_GOAL: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (PrologProverException e) {

			//System.out.println("PROLOG-GET_GOAL: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during goal computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a PrologProverException we cannot be sure about the state
			// Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentPrologState = null;
			throw new StateMachineException("Impossible to compute goal for role \"" + role + "\" in state " + state + ".", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got no goal when expecting at least one.");
			return new ArrayList<Integer>();
		}

		String[] goals = (String[]) bindings[0];

		if(goals.length != 1){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got goal results of size: " + goals.length + " when expecting size one.");
		}

		List<Integer> goalValues = new ArrayList<Integer>();

		for(String s : goals){
			try{
				int goal = Integer.parseInt(s);
				goalValues.add(new Integer(goal));
			}catch(NumberFormatException ex){
				GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got goal results that is not a number.");
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

		updatePrologState(state);

		boolean terminal = false;

		//long cancStart = System.currentTimeMillis();

		try {
			terminal = this.prologProver.askQuerySuccess("is_terminal");

			//System.out.println("PROLOG-IS_TERMINAL: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (PrologProverException e) {

			//System.out.println("PROLOG-IS_TERMINAL: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during terminality computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a PrologProverException we cannot be sure about the state
			// Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentPrologState = null;
			throw new StateMachineException("Impossible to compute terminality of state " + state + ".", e);
		}

		return terminal;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<ExplicitRole> getRoles() {
		return this.roles;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public ExplicitMachineState getInitialState() {
		return this.initialState;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role)
			throws MoveDefinitionException, StateMachineException {

		// TODO: should I catch the state machine exception before re-throwing it?
		updatePrologState(state);

		Object[] bindings = null;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.prologProver.askQueryResults("get_legal_moves("+support.getFakeRole(role)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("PROLOG-GET_LEGAL_MOVES: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (PrologProverException e) {

			//System.out.println("PROLOG-GET_LEGAL_MOVES: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during legal moves computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Everytime a query fails throwing a PrologProverException we cannot be sure about the state
			// Prolog is in (it's highly likely that it was reset so no state is currently set).
			this.currentPrologState = null;
			throw new StateMachineException("Impossible to compute legal moves for role \"" + role + "\" in state " + state + ".", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got no legal moves when expecting at least one.");
			throw new MoveDefinitionException(state, role);
		}

		String[] prologMoves = (String[]) bindings[0];

		// Extra check, but this should never happen.
		if(prologMoves.length < 1){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Got no legal moves when expecting at least one.");
			throw new MoveDefinitionException(state, role);
		}

		List<ExplicitMove> moves = support.askToMoves(prologMoves);

		return moves;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves)
			throws TransitionDefinitionException, StateMachineException {

		updatePrologState(state);

		// Get the next state and assert it on Prolog side.
		Object[] bindings = null;

		//long cancStart = System.currentTimeMillis();

		try {
			bindings = this.prologProver.askQueryResults("get_next_state("+fakeRoles+", "+support.getFakeMoves(moves)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			//System.out.println("PROLOG-GET_NEXT_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

		} catch (PrologProverException e) {

			//System.out.println("PROLOG-GET_NEXT_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during next state computation.");
			GamerLogger.logStackTrace("StateMachine", e);
			this.currentPrologState = null;
			throw new StateMachineException("Impossible to compute next state for moves " + moves + " in state " + state + ".", e);
		}

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Computation of next state on " + this.prologType + " Prolog side failed.");
			this.currentPrologState = null;
			throw new StateMachineException("Computation of next state on " + this.prologType + " Prolog side failed for moves " + moves + " in state " + state + ".");
		}

		// Compute the machine state using the Prolog answer (note that it could be an empty array of strings in case no
		// propositions are true in the computed next state. In this case the content of the machine state will be an empty HashSet)
		this.currentPrologState = new ExplicitMachineState(support.askToState((String[]) bindings[0]));

		return this.currentPrologState.clone();
	}

	/**
	 * Compute the given MachineState in the Prolog side.
	 *
	 * @throws StateMachineException if something went wrong and the state could not be updated.
	 */
	private void updatePrologState(ExplicitMachineState state) throws StateMachineException{

		if(currentPrologState==null||!currentPrologState.equals(state)){

			boolean success = false;

			//long cancStart = System.currentTimeMillis();

			try {
				List<String> fakeState = support.getFakeMachineState(state.getContents());
				success = this.prologProver.askQuerySuccess("update_state("+ fakeState +")");

				//System.out.println("PROLOG-UPDATE_PROLOG_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

			} catch (PrologProverException e) {

				//System.out.println("PROLOG-UPDATE_PROLOG_STATE: " + (System.currentTimeMillis() - cancStart) + "ms");

				this.currentPrologState = null;
				GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Exception during prolog state update.");
				GamerLogger.logStackTrace("StateMahcine", e);
				throw new StateMachineException("State update on " + this.prologType + " Prolog side failed for state: " + state, e);
			}

			// This should never happen, but you never know...
			if(!success){
				// State computation failed on prolog side.
				this.currentPrologState = null;
				GamerLogger.logError("StateMachine", "[" + this.prologType + " PROLOG] Computation of current state on " + this.prologType + " Prolog side failed!");
				throw new StateMachineException("Computation on " + this.prologType + " Prolog side failed for state: " + state);
			}

			this.currentPrologState = state;
		}
	}

	/**
	 * This method shuts down the Prolog Prover.
	 * To be called ALWAYS when this state machine won't be used anymore to make sure that
	 * the fake Prolog Prover will stop all running tasks and also the external Prolog
	 * program.
	 *
	 * This method can also be called whenever we need to stop the external Prolog
	 * program temporarily. The reason why we need to do this is the following: when the
	 * state machine is initialized by a certain thread, the external Prolog program
	 * that is started will be linked to such thread, so whenever that thread is interrupted
	 * also the Prolog program must be interrupted or the thread might get stuck forever or an
	 * instance of the external Prolog program will keep running.
	 */
	@Override
	public void shutdown(){
		if(this.prologProver != null){
			this.prologProver.shutdown();
		}
		this.currentPrologState = null;
	}


	// METHODS USED BY AN EXTERNAL CLASS TO FIX THE STATE OF THE STATE MACHINE IN CASE INITIALIZATION
	// FAILS WITH A RECOVERABLE FAILURE (I.E. INITIALIZATION OF PROLOG PROVER SUCCEEDED BUT SOMETHING ELSE
	// FAILED).

	/**
	 * This method checks if the state machine is completely unusable because the initialization
	 * of the Prolog Prover failed or if it can still be used, probably with some fixing, because
	 * the Prolog Prover is working.
	 *
	 * This method is needed because initialization fails both if the Prolog Prover fails to be started
	 * and if the initial state and/or the roles fail to be computed. This is because if this state
	 * machine is the only one being used, it cannot give correct answers if one of the previously
	 * mentioned actions fails. However, if some external class has a way to check what exactly
	 * went wrong with initialization, it can fix it and still use this state machine.
	 *
	 * @return false if the state machine cannot be used at all because the Prolog Prover failed to
	 * start, true otherwise.
	 */
	public boolean isUsable(){
		return this.prologProver != null;
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

	@Override
	public String getName(){
		return this.prologType + "PrologStateMachine";
	}

}
