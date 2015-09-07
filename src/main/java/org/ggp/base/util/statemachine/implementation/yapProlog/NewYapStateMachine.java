/**
 *
 */
package org.ggp.base.util.statemachine.implementation.yapProlog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.yapProlog.transform.YapEngineSupport;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

import com.declarativa.interprolog.YAPSubprocessEngine;
import com.google.common.collect.ImmutableList;

/**
 * ATTENTION: if the initialization method fails do not query this state machine
 * as its answers will not be consistent. (TODO: add a way to check if state
 * machine is an inconsistent state and thus cannot answer queries).
 *
 * On the contrary, if any other method fails throwing the StateMachineException,
 * it is possible to keep asking queries to the state machine since each method
 * that fails also makes sure to leave the state machine in a consistent state.
 *
 * @author C.Sironi
 *
 */
public class NewYapStateMachine extends StateMachine {

	/**
	 * Command that this state machine must use to run Yap.
	 */
	private String yapCommand;

	/**
	 * Yap engine that this state machine must use to answer queries on the game.
	 */
	private YAPSubprocessEngine yapProver;

	/**
	 * Initial state of the current game.
	 */
	private MachineState initialState;

	/**
	 * Ordered list of roles in the current game.
	 */
	private ImmutableList<Role> roles;


	/////////////////////////////////////////////////////////////////////////////////


	// The YapEngineSupport which handles the translations and the mapping
	private YapEngineSupport support;

	// Path of the file where to put the game description
	private String descriptionFilePath;

	// File with all the predefined Prolog functions
	private String functionsFilePath;

	// Second file with all the predefined Prolog functions
	// => using the Internal DataBase

	/*
	 * NEEDED?????
	 */
	/*
	private String functionsFileIdbPath;
	private File functionsIdbFile;
	*/

	// The state in the Prolog side
	// -> to avoid running "computeState(MachineState)" when it's useless
	/**
	 * This variable keeps track of the game state that currently is registered on
	 * YAP Prolog side.
	 * If null, it means that the game state on YAP Prolog side is unknown.
	 *
	 * This variable can be used to avoid running "computeState(MachineState)" on YAP
	 * Prolog side when the game state registered on it is already the one we need.
	 */
	private MachineState currentYapState;

	// The list of the roles
	private List<String> fakeRoles;


	////////////////////////////////////////////////////////////////////////////////


	/**
	 * Constructor that sets the command that this state machine must use to run YAP
	 * and the paths of the files that YAP prolog must use to the default values.
	 */
	public NewYapStateMachine() {
		this("/home/csironi/CadiaplayerInstallation/Yap/bin/yap", "/home/csironi/YAPplayer/prologFiles/description.pl", "/home/csironi/YAPplayer/prologFiles/prologFunctions.pl");
	}

	/**
	 * Constructor that sets the command that this state machine must use to run YAP
	 * and the paths of the files that YAP must use to the given values.
	 */
	public NewYapStateMachine(String yapCommand, String descriptionFilePath, String functionsFilePath) {
		this.yapCommand = yapCommand;
		this.descriptionFilePath = descriptionFilePath;
		this.functionsFilePath = functionsFilePath;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 */
	@Override
	public void initialize(List<Gdl> description) throws StateMachineException{
		try{
			// Create the bridge between Java and YAP Prolog, trying to start the YAP Prolog program.
			this.yapProver = new YAPSubprocessEngine(this.yapCommand);

			////NEEDED???????
			//this.functionsFile = new File(this.functionsFilePath);
			//this.functionsIdbFile = new File(this.functionsFileIdbPath);

			this.support = new YapEngineSupport();

			flushAndWrite(support.toProlog(description));


			/*
			 * NEEDED?
			 */
			/*
			if(IDB) engine.consultAbsolute(fileFunctionsIdb);
			else */ this.yapProver.consultAbsolute(new File(functionsFilePath));

			randomizeProlog();

			// If creation succeeded, compute initial state and roles.
			this.initialState = computeInitialState();
			this.roles = computeRoles();

		}catch(RuntimeException re){
			throw re;
		}catch(Exception e){
			// Log the exception
			GamerLogger.logError("StateMachine", "[YAP] Exception during state machine initialization. Shutting down.");
			GamerLogger.logStackTrace("StateMachine", e);

			// Reset all the variables of the state machine to null to leave the state machine in a consistent
			// state, since initialization failed.
			this.roles = null;
			this.fakeRoles = null;
			this.currentYapState = null;
			this.initialState = null;
			// Shutdown Yap Prolog and remove the reference to it, as it is now unusable.
			this.yapProver.shutdown();
			this.yapProver = null;

			// Throw an exception.
			throw new StateMachineException("State machine initialization failure.", e);
		}
	}

	private MachineState computeInitialState() throws StateMachineException
	{
		Object[] bindings = yapProver.deterministicGoal("initialize_state(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

		// If bindings is null => something went wrong on Yap Prolog side during the computation.
		// Note that this should never happen, but an extra check won't hurt.
		if(bindings == null){
			// State computation failed on Yap prolog side.
			this.currentYapState = null;
			GamerLogger.logError("StateMachine", "[YAP] Computation of initial state on Yap Prolog side failed.");
			throw new StateMachineException("Computation of initial state on Yap Prolog side failed.");
		}

		// Compute the machine state using the Yap Prolog answer (note that it could be an empty array of strings in case
		// no propositions are true in the initial state. In this case the content of the machine state will be an empty HashSet)
		this.currentYapState = new MachineState(support.askToState((String[]) bindings[0]));

		return currentYapState.clone();
	}

	private ImmutableList<Role> computeRoles() throws StateMachineException	{
		List<Role> tmpRoles = new ArrayList<Role>();

		try{
			Object[] bindings = yapProver.deterministicGoal("get_roles(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

			if(bindings == null){
				GamerLogger.logError("StateMachine", "[YAP] Got no results for the computation of the game roles, while expecting at least one role.");
				throw new StateMachineException("Got no results for the computation of the game roles, while expecting at least one role.");
			}

			tmpRoles = support.askToRoles((String[]) bindings[0]);

			this.fakeRoles = support.getFakeRoles(tmpRoles);

		}catch(SymbolFormatException e){
			this.fakeRoles = null;
			throw new StateMachineException(e);
		}
		return ImmutableList.copyOf(tmpRoles);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int getGoal(MachineState state, Role role)
			throws GoalDefinitionException, StateMachineException {

		updateYapState(state);
		int goal;
		Object[] bindings = yapProver.deterministicGoal("get_goal("+support.getFakeRole(role)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[YAP] Got no goal when expecting one.");
			throw new GoalDefinitionException(state, role);
		}

		String[] goals = (String[]) bindings[0];

		if(goals.length != 1){
			GamerLogger.logError("StateMachine", "[YAP] Got goal results of size: " + goals.length + " when expecting size one.");
			throw new GoalDefinitionException(state, role);
		}

		try{
			goal = Integer.parseInt(goals[0]);
		}catch(NumberFormatException ex){
			GamerLogger.logError("StateMachine", "[YAP] Got goal results that is not a number.");
			GamerLogger.logStackTrace("StateMachine", ex);
			throw new GoalDefinitionException(state, role, ex);
		}

		return goal;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {

		updateYapState(state);

		return yapProver.deterministicGoal("is_terminal");
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<Role> getRoles() {
		return this.roles;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public MachineState getInitialState() {
		return this.initialState;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
			throws MoveDefinitionException, StateMachineException {

		updateYapState(state);

		Object[] bindings = yapProver.deterministicGoal("get_legal_moves("+support.getFakeRole(role)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

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

		List<Move> moves = support.askToMoves(yapMoves);

		return moves;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
			throws TransitionDefinitionException, StateMachineException {

		updateYapState(state);

		// Get the next state and assert it on Yap Prolog side.
		Object[] bindings = yapProver.deterministicGoal("get_next_state("+fakeRoles+", "+support.getFakeMoves(moves)+", List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

		if(bindings == null){
			GamerLogger.logError("StateMachine", "[YAP] Computation of next state on Yap Prolog side failed.");
			throw new StateMachineException("Computation of next state on Yap Prolog side failed for moves " + moves + " in state " + state + ".");
		}

		// Compute the machine state using the Yap Prolog answer (note that it could be an empty array of strings in case no
		// propositions are true in the computed next state. In this case the content of the machine state will be an empty HashSet)
		this.currentYapState = new MachineState(support.askToState((String[]) bindings[0]));

		return currentYapState.clone();
	}

	////////////////////////////////////////////////////////////////////////
	/**
	 *	Flush the description file and write the game description in it
	 * @param string: the description of the game
	 * @throws IOException
	 */
	private void flushAndWrite(String string) throws IOException{

		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new FileWriter(this.descriptionFilePath));
			out.write(string);
		}finally{
			if(out != null){
				out.close();
			}
		}
	}


	/**
	 * Change the Prolog random number generator
	 * using the Java random number generator
	 */
	private void randomizeProlog()
	{
		int i = (int)Math.min(Math.random()*(30268), 30268)+1;
		int j = (int)Math.min(Math.random()*(30307), 30307)+1;
		int k = (int)Math.min(Math.random()*(30323), 30323)+1;
		this.yapProver.realCommand("setrand(rand("+i+", "+j+", "+k+"))");
	}


	/**
	 * Compute the given MachineState in the Prolog side
	 * @throws StateMachineException
	 */
	private void updateYapState(MachineState state) throws StateMachineException{

		if(currentYapState==null||!currentYapState.equals(state)){

			boolean success = yapProver.deterministicGoal("update_state("+support.getFakeMachineState(state.getContents())+")");

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

	public void shutdown(){
		this.yapProver.shutdown();
	}

}
