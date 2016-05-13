package ggpbasebenchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizationCaller;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SelfInitSeparateInternalPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class Benchmark {

	private StateMachine stateMachine;
	private ReasonerType reasonerType;
	private BenchmarkMethod method;
	private int playclock;
	private File gdlFile, traceFile;
	private long nbLegals, nbUpdates, nbGoals;
	private SearchAlgorithm algorithm;

	public Benchmark(ReasonerType reasonerType, BenchmarkMethod method, int playclock, File gdlFile, File traceFile) {
		this.reasonerType = reasonerType;
		this.method = method;
		this.playclock = playclock;
		this.gdlFile = gdlFile;
		this.traceFile = traceFile;
	}

	public void run() throws Exception {
		// Extract a "match id" from the name of the trace file and name of the reasoner.
		// This is used for the PropNetStructureFactory to save intermediate files in a place without collisions.
		String matchId = reasonerType.name().toLowerCase() + "_" + method.name().toLowerCase() + "_" + traceFile.getName();
		if (matchId.endsWith(".log")) matchId = matchId.substring(0, matchId.length()-4);
		if (matchId.endsWith(".trace")) matchId = matchId.substring(0, matchId.length()-6);

		switch (reasonerType) {

			case GGPBASEPROVER:
				stateMachine = new ProverStateMachine();
				break;
			case BACKWARDPROPNET:
				//stateMachine = new BackwardPropNetStateMachine(new GGPBasePropNetStructureFactory());
				break;
			case FORWARDPROPNET:
				//stateMachine = new ForwardPropNetStateMachine(new GGPBasePropNetStructureFactory());
				break;
			case FCPN_BASE:
				//stateMachine = new ForwardChangePropNetStateMachine(new GGPBasePropNetStructureFactory());
				break;
			case BPN_ASP:
				//stateMachine = new BackwardPropNetStateMachine(new ASPPropNetStructureFactory(matchId));
				break;
			case FPN_ASP:
				//stateMachine = new ForwardPropNetStateMachine(new ASPPropNetStructureFactory(matchId));
				break;
			case FCPN_ASP:
				//stateMachine = new ForwardChangePropNetStateMachine(new ASPPropNetStructureFactory(matchId));
				break;
			case TRAN_SIPN:
				stateMachine = new SelfInitSeparateInternalPropNetStateMachine(new OptimizationCaller[0]);
				break;
		}

		// load game
		String gameDescription = readFile(gdlFile);
		String preprocessedRules = Game.preprocessRulesheet(gameDescription);

		Game ggpBaseGame = Game.createEphemeralGame(preprocessedRules);

		//!
		//System.out.println("Descr: " + ggpBaseGame.getDescription());
		//System.out.println("Rulesheet: " + ggpBaseGame.getRulesheet());
		//System.out.println("Rulesheet: " + ggpBaseGame.getRules());

		long startTime = System.currentTimeMillis();
		stateMachine.initialize(ggpBaseGame.getRules());
		long endTime = System.currentTimeMillis();
		double secondsUsed = (endTime-startTime)/1000.0;
		System.out.println("time for stateMachine.initialize (in s): " + secondsUsed);

		// load trace
		Trace trace = Trace.loadFromFile(traceFile);

		//!
		//System.out.println("Trace: " + trace);

		// setup search algorithm
		switch (method) {
			case DFS:
				algorithm = new IterativeDeepeningSearch(stateMachine, playclock, Integer.MAX_VALUE);
				break;
			case FDFS:
				algorithm = new IterativeDeepeningSearch(stateMachine, Integer.MAX_VALUE, playclock);
				break;
			case MC:
				algorithm = new MonteCarloSearch(stateMachine, playclock);
				break;
		}

		// run trace
		startTime = System.currentTimeMillis();
		nbLegals=0; nbUpdates=0; nbGoals=0;
		runTrace(trace);
		endTime = System.currentTimeMillis();
		secondsUsed = (endTime-startTime)/1000.0;
		System.out.println("FINAL #legals: " + nbLegals + ", #updates: " + nbUpdates + ", #goals: " + nbGoals + ", seconds: " + secondsUsed);
	}

	public static String readFile(File gdlFile) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(gdlFile));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			sb.append(line + "\n"); // artificial EOLN marker
		}
		br.close();
		return sb.toString();
	}

	private void runTrace(Trace trace) {
		int stepCounter = 0;
		MachineState state = stateMachine.getInitialState();
		boolean isTerminal = false;
		try {
			isTerminal = stateMachine.isTerminal(state);
		} catch (StateMachineException e) {
			System.out.println("ERROR: impossible to check terminality: " + state + "\n" + e.getMessage());
		}
		while (!isTerminal && !trace.isEmpty()) {
			++stepCounter;
			System.out.println("step " + stepCounter);
			// System.out.println("state: " + state);
			// System.out.println("next move: " + trace.get(0));

			algorithm.run(state);
			nbLegals += algorithm.getNbLegals();
			nbUpdates += algorithm.getNbUpdates();
			nbGoals += algorithm.getNbGoals();

			try {
				System.out.println("Using SM EXT"); //!
				state = stateMachine.getNextState(state, trace.remove(0));

			} catch (TransitionDefinitionException | StateMachineException e) {
	        	System.out.println("ERROR: with state update: " + state + "\n" + e.getMessage());
			}
			++nbUpdates;
			try {
				isTerminal = stateMachine.isTerminal(state);
			} catch (StateMachineException e) {
				System.out.println("ERROR: impossible to check terminality: " + state + "\n" + e.getMessage());
			}
			System.out.println("MOVE " + stepCounter + " #legals: " + nbLegals + ", #updates: " + nbUpdates + ", #goals: " + nbGoals);
		}
		// System.out.println("final state: " + state);
	    if(isTerminal) {
	        if (!trace.isEmpty()) {
	        	System.out.println("ERROR: terminal state in middle of trace: " + state);
	        } else {
	        	try {
	        		System.out.println("Using SM EXT"); //!
					algorithm.evaluateGoals(state);
				} catch (StateMachineException e) {
					System.out.println("ERROR: with goals evaluation: " + state + "\n" + e.getMessage());
				}
				++nbGoals;
	        }
	    } else if (trace.isEmpty()) {
	    	System.out.println("ERROR: final state of trace is not terminal in the game: " + state);
	    }
	}
}
