/**
 *
 */
package org.ggp.base.util.statemachine.implementation.yapProlog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
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
	private File functionsFile;

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
	public void initialize(List<Gdl> description)/* throws StateMachineException*/{
		try{
			// Create the bridge between Java and YAP Prolog, trying to start the YAP Prolog program.
			this.yapProver = new YAPSubprocessEngine(this.yapCommand);

			////NEEDED???????
			this.functionsFile = new File(this.functionsFilePath);
			//this.functionsIdbFile = new File(this.functionsFileIdbPath);

			this.support = new YapEngineSupport();

			flushAndWrite(support.toProlog(description));


			/*
			 * NEEDED?
			 */
			/*
			if(IDB) engine.consultAbsolute(fileFunctionsIdb);
			else */ this.yapProver.consultAbsolute(new File(functionsFilePath));

			initializeQueries();
			randomizeProlog();

			// If creation succeeded, compute initial state and roles.
			this.initialState = computeInitialState();
			this.roles = computeRoles();

		}catch(Exception e){
			// Log the exception
			GamerLogger.logError("StateMachine", "[YAP] Exception during state machine initialization. Shutting down.");
			GamerLogger.logStackTrace("StateMachine", e);

			// Shutdown Yap Prolog and remove the reference to it, as it is now unusable.
			this.yapProver.shutdown();
			this.yapProver = null;

			// Throw an exception.
			//throw new StateMachineException(e);
		}
	}

	private MachineState computeInitialState()
	{
		Object[] bindings = yapProver.deterministicGoal("initialize_state(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");

		// If bindings is null => the initial state is empty because there are no true propositions in the initial state.
		if(bindings == null){
			this.currentYapState = new MachineState(new HashSet<GdlSentence>());
		}else{
			currentYapState = new MachineState(support.askToState((String[]) bindings[0]));
		}

		return currentYapState.clone();
	}

	private ImmutableList<Role> computeRoles() throws StateMachineException
	{
		try{
			List<Role> roles = support.askToRoles((String[]) yapProver.deterministicGoal("get_roles(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]") [0]);
			this.fakeRoles = support.getFakeRoles(roles);
		}catch(SymbolFormatException e){
			this.fakeRoles = null;
			throw new StateMachineException(e);
		}
		return ImmutableList.copyOf(roles);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int getGoal(MachineState state, Role role)
			throws GoalDefinitionException/*, StateMachineException*/ {
		computeState(state);
		int goal;
		goal = Integer.parseInt((String) yapProver.deterministicGoal("get_goal("+support.getFakeRole(role)+", S)", "[string(S)]") [0]);
		GamerLogger.log("Responsibility", "YAP(getGoal)");
		GamerLogger.log("StateMachine", "YAP(getGoal)");
		return goal;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		// TODO Auto-generated method stub
		return false;
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
			throws MoveDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
			throws TransitionDefinitionException {
		// TODO Auto-generated method stub
		return null;
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
		yapProver.realCommand("setrand(rand("+i+", "+j+", "+k+"))");
	}


	/**
	 * Compute the given MachineState in the Prolog side
	 * @throws StateMachineException
	 */
	private void computeState(MachineState state) /*throws StateMachineException*/{
		if(!currentYapState.equals(state)){

			if(!((String) yapProver.deterministicGoal("compute_state("+support.getFakeMachineState(state.getContents())+", S)", "[string(S)]") [0]).equals("d")){
				//State computation failed on Yap prolog side
				GamerLogger.logError("StateMachine", "[YAP] Computation of current state on YAP Prolog side failed!");
				//throw new StateMachineException("Computation on YAP Prolog side failed for state: " + state);
			}else{
				this.currentYapState = state;
			}
		}
	}

	/**
	 * Initialize the queries objects
	 */
	private void initializeQueries()
	{
		QUERYbIsTerminal = new QueryB();

		QUERYsComputeState = new QueryS("compute_state(");
		QUERYsGetGoal = new QueryS("get_goal(");

		QUERYaosGetNextState = new QueryAOS("get_next_state(");
		QUERYaosGetLegalMoves = new QueryAOS("get_legal_moves(");
		QUERYaosComputeRoles = new QueryAOS(true, "get_roles(_l), processList(_l, _ll), ipObjectTemplate('ArrayOfString',_lll,_,[_ll],_)");
		QUERYaosComputeInitialStateGdl = new QueryAOS(true, "initialize_state(_l), processList(_l, _ll), ipObjectTemplate('ArrayOfString',_lll,_,[_ll],_)");

		QUERYsGetRandomMove = new QueryS("get_random_move(");
		QUERYaosGetRandomJointMove1 = new QueryAOS("get_random_joint_move(");
		QUERYaosGetRandomJointMove2 = new QueryAOS("get_random_joint_moveg(");
		QUERYaosGetRandomNextState = new QueryAOS("get_random_next_state(");

		QUERYaosPerformDepthCharge = new QueryAOS("perform_depth_charge(");
	}

	/**
	 *  The queries objects used to call "deterministicGoal(_)"
	 */
	// boolean query
	private static QueryB QUERYbIsTerminal; // to "isTerminal" method

	// String queries
	private static QueryS QUERYsComputeState; // to "computeState" method
	private static QueryS QUERYsGetGoal; // to "getGoal" method

	// ArrayOfString queries
	private static QueryAOS QUERYaosGetNextState; // to "getNextState" method
	private static QueryAOS QUERYaosGetLegalMoves; // to "getLegalMoves" method
	private static QueryAOS QUERYaosComputeRoles; // to "computeRoles" method
	private static QueryAOS QUERYaosComputeInitialStateGdl; // to "computeInitialStateGdl" method

	// non-basic methods queries (Prolog methods)
	private static QueryS QUERYsGetRandomMove; // to "getRandomMove" method
	private static QueryAOS QUERYaosGetRandomJointMove1; // to "getRandomJointMove(_)" method
	private static QueryAOS QUERYaosGetRandomJointMove2; // to "getRandomJointMove(_,_,_)" method
	private static QueryAOS QUERYaosGetRandomNextState; // to"getRandomNextState" method
	private static QueryAOS QUERYaosPerformDepthCharge; // to "performDepthCharge" method

	/**
	 * Query to Boolean
	 */
	public class QueryB implements Callable<Boolean>
	{
		private String goal;
		public QueryB(){ goal = "is_terminal"; }

		public void setGoal(String newG){ goal = newG; }

		public String getGoal(){ return goal; }

		@Override
		public Boolean call()
		{
			if(yapProver.deterministicGoal(goal) == true) return true;
			else return false;
		}

	}



	/**
	 *  Query to String
	 */
	public class QueryS implements Callable<String>
	{
		private String subGoal;
		private static final String answer = "[string(_s)]";
		private static final String endOfGoal = ", _s)";
		private String beginingOfGoal;

		public QueryS(){}
		public QueryS(String g){ beginingOfGoal = g; }

		public void setSubGoal(String newG){ subGoal = newG; }

		public String getGoal(){ return beginingOfGoal+subGoal+endOfGoal; }

		@Override
		public String call()
		{
			return (String) yapProver.deterministicGoal(beginingOfGoal+subGoal+endOfGoal, answer) [0];
		}

	}



	/**
	 *  Query to ArrayOfString
	 */
	public class QueryAOS implements Callable<String[]>
	{
		private String goal;
		private boolean full;
		private String subGoal;
		private static final String answer = "[_lll]";
		private static final String endOfGoal = ", _l), processList(_l, _ll), ipObjectTemplate('ArrayOfString',_lll,_,[_ll],_)";
		private String beginingOfGoal;

		public QueryAOS(){}
		public QueryAOS(String g)
		{
			beginingOfGoal = g;
			full = false;
		}
		public QueryAOS(boolean b, String g)
		{
			if(b == true)
			{
				goal = g;
				full = b;
			}
		}

		public void setSubGoal(String newG){ subGoal = newG; }

		public String getGoal()
		{
			if(full) return goal;
			else return beginingOfGoal+subGoal+endOfGoal;
		}

		@Override
		public String[] call()
		{
			if(full) return (String[]) yapProver.deterministicGoal(goal, answer) [0];
			else return (String[]) yapProver.deterministicGoal(beginingOfGoal+subGoal+endOfGoal, answer) [0];
		}

	}
	///////////////////////////////////////////////////////////////////////////////

}
