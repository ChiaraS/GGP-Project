package csironi.ggp.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicPropNet;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

import csironi.ggp.course.experiments.propnet.SingleValueDoubleStats;
import csironi.ggp.course.statsSummarizer.StatsUtils;

public class BranchingFactorEstimator {

	public static void main(String[] args){

		String[] gameKeys = args[0].split(";");

		String folder;

		if(args.length == 2){
			folder = args[1];
		}else{
			folder = System.currentTimeMillis() + ".SearchTreeStats";
		}

		for(String key : gameKeys){
			computeBranchingFactorAndDepthAndWinsForGame(key, folder);
		}

	}

	public static void computeBranchingFactorAndDepthAndWinsForGame(String gameKey, String folder){

		// Get game description

		GameRepository gameRepo = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath + "/" + GamerConfiguration./*defaultStanfordRepo*/defaultGGPBaseRepo);

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		System.out.println("Specified game " + gameKey + " not found in the repository.");
			return;
    	}

    	List<Gdl> description = game.getRules();

    	SeparateInternalPropnetManager manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + 420000L, null);

		PropNetManagerRunner.runPropNetManager(manager, 420000L);

		DynamicPropNet dynamicPropnet = manager.getDynamicPropnet();
		ImmutablePropNet immutablePropnet = manager.getImmutablePropnet();
		ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

		if(dynamicPropnet != null && immutablePropnet != null && propnetState != null){

			System.out.println("Propnet creation successful for game " + gameKey + ".");

			System.out.println("Num inputs = " + dynamicPropnet.getNumInputs() + ".");
			System.out.println("Num legals = " + dynamicPropnet.getNumLegals() + ".");

			InternalPropnetStateMachine thePropnetMachine;
			Random random;

			// Create the state machine giving it the propnet and the propnet state.
			// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
			// and this will be detected by the state machine during initialization.

			propnetState = manager.getInitialPropnetState();

			random = new Random();

			thePropnetMachine = new SeparateInternalPropnetStateMachine(random, immutablePropnet, propnetState);

			try {
				thePropnetMachine.initialize(description, System.currentTimeMillis() + 420000L);

				AbstractStateMachine abstractStateMachine = new CompactStateMachine(thePropnetMachine);

				MachineState root = abstractStateMachine.getInitialState();

				Long timeout = System.currentTimeMillis() + 60000;

				SingleValueDoubleStats branches = new SingleValueDoubleStats();
				SingleValueDoubleStats[] branchesPerRole = new SingleValueDoubleStats[abstractStateMachine.getRoles().size()];
				//double branches = 0;
				//double[] branchesPerRole = new double[abstractStateMachine.getRoles().size()];
				for(int i = 0; i < branchesPerRole.length; i++){
					branchesPerRole[i] = new SingleValueDoubleStats();
				}
				SingleValueDoubleStats[] branchesPerRoleWithoutNoop = new SingleValueDoubleStats[abstractStateMachine.getRoles().size()];
				//double branches = 0;
				//double[] branchesPerRole = new double[abstractStateMachine.getRoles().size()];
				for(int i = 0; i < branchesPerRoleWithoutNoop.length; i++){
					branchesPerRoleWithoutNoop[i] = new SingleValueDoubleStats();
				}
				double visitedStates = 0;
				double numSimulations = 0;
				SingleValueDoubleStats depth = new SingleValueDoubleStats();
				//double depthSum = 0;
				SingleValueDoubleStats[] scores = new SingleValueDoubleStats[abstractStateMachine.getRoles().size()];
				//double[] goalSums = new double[abstractStateMachine.getRoles().size()];
				SingleValueDoubleStats[] wins = new SingleValueDoubleStats[abstractStateMachine.getRoles().size()];

				for(int i = 0; i < scores.length; i++){
					scores[i] = new SingleValueDoubleStats();
					wins[i] = new SingleValueDoubleStats();
				}

				int maxDepth = 500;

				for(Role r : abstractStateMachine.getRoles()){
					StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-" + abstractStateMachine.convertToExplicitRole(r) + "-SimulationStats.csv", "#Sim;#Samples;Min;Max;Avg;STD;SEM;95%CI;Score;Wins");
				}

				SingleValueDoubleStats[] simulationStats = new SingleValueDoubleStats[abstractStateMachine.getRoles().size()];

				while(System.currentTimeMillis() < timeout) {

					for(int i = 0; i < abstractStateMachine.getRoles().size(); i++){
						simulationStats[i] = new SingleValueDoubleStats();
					}

					MachineState currentState = root;

					boolean terminal;

					int nDepth = 0;

				    do{

						nDepth++;

						try {
							branches.addValue(abstractStateMachine.getLegalJointMoves(currentState).size());
						} catch (MoveDefinitionException | StateMachineException e) {
							System.out.println("Exception getting the legal joint moves while performing a playout.");
							e.printStackTrace();
							break;
						}

						int i = 0;
						for(Role r : abstractStateMachine.getRoles()){
							try {
								List<Move> legalMoves = abstractStateMachine.getLegalMoves(currentState, r);
								String moveString = abstractStateMachine.convertToExplicitMove(legalMoves.get(0)).getContents().toString();
								if(legalMoves.size() != 1 || !(moveString.equals("noop"))){
									//System.out.println(moveString);
									branchesPerRoleWithoutNoop[i].addValue(legalMoves.size());
									simulationStats[i].addValue(legalMoves.size());
								}
								branchesPerRole[i].addValue(legalMoves.size());
							} catch (MoveDefinitionException | StateMachineException e) {
								System.out.println("Exception getting the legal moves for a role while performing a playout.");
								e.printStackTrace();
								break;
							}
							i++;
						}

						visitedStates++;

						try {
							currentState = abstractStateMachine.getRandomNextState(currentState);
						} catch (MoveDefinitionException | TransitionDefinitionException | StateMachineException e) {
							System.out.println("Exception getting the next state while performing a playout.");
							e.printStackTrace();
							break;
						}

				        try {
							terminal = abstractStateMachine.isTerminal(currentState);
						} catch (StateMachineException e) {
							System.out.println("Exception computing state terminality while performing a playout.");
							e.printStackTrace();
							terminal = true;
							break;
						}

				    }while(nDepth < maxDepth && !terminal);

				    depth.addValue(nDepth);
				    numSimulations++;

				    double[] goals = abstractStateMachine.getSafeGoalsAvgForAllRoles(currentState);

				    double[] winsArray = getTerminalWinsIn0_1(goals);
				    for(int roleIndex = 0; roleIndex < goals.length; roleIndex++){
				    	scores[roleIndex].addValue(goals[roleIndex]);
				    	wins[roleIndex].addValue(winsArray[roleIndex]);
				    }

				    int i = 0;
				    for(Role r : abstractStateMachine.getRoles()){
						StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-" + abstractStateMachine.convertToExplicitRole(r) + "-SimulationStats.csv", numSimulations + ";" + simulationStats[i].getNumSamples() + ";" + simulationStats[i].getMinValue() + ";" + simulationStats[i].getMaxValue() + ";" + simulationStats[i].getAvgValue() + ";" + simulationStats[i].getValuesStandardDeviation() + ";" + simulationStats[i].getValuesSEM() + ";" + simulationStats[i].get95ConfidenceInterval() + ";" + goals[i] + ";" + winsArray[i] + ";");
						i++;
					}

				}

				StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv", "Measure;#Samples;Min;Max;Avg;STD;SEM;95%CI");

				StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Visited states;" + visitedStates);
				StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Num simulations;" + numSimulations);
				StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Joint branches;" + branches.getNumSamples() + ";" + branches.getMinValue() + ";" + branches.getMaxValue() + ";" + branches.getAvgValue() + ";" + branches.getValuesStandardDeviation() + ";" + branches.getValuesSEM() + ";" + branches.get95ConfidenceInterval() + ";");
				//System.out.println("Visited states = " + visitedStates);
				//System.out.println("Average joint branches = " + branches/visitedStates);
				//System.out.println("Average branches per role:");
				int i = 0;
				for(Role r : abstractStateMachine.getRoles()){
					StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Branches for role \"" + abstractStateMachine.convertToExplicitRole(r) + "\";" + branchesPerRole[i].getNumSamples() + ";" + branchesPerRole[i].getMinValue() + ";" + branchesPerRole[i].getMaxValue() + ";" + branchesPerRole[i].getAvgValue() + ";" + branchesPerRole[i].getValuesStandardDeviation() + ";" + branchesPerRole[i].getValuesSEM() + ";" + branchesPerRole[i].get95ConfidenceInterval() + ";");
					//System.out.println("    " + abstractStateMachine.convertToExplicitRole(r) + " = " + branchesPerRole[i]/visitedStates);
					i++;
				}

				i = 0;
				for(Role r : abstractStateMachine.getRoles()){
					StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Branches for role \"" + abstractStateMachine.convertToExplicitRole(r) + "\" without noop;" + branchesPerRoleWithoutNoop[i].getNumSamples() + ";" + branchesPerRoleWithoutNoop[i].getMinValue() + ";" + branchesPerRoleWithoutNoop[i].getMaxValue() + ";" + branchesPerRoleWithoutNoop[i].getAvgValue() + ";" + branchesPerRoleWithoutNoop[i].getValuesStandardDeviation() + ";" + branchesPerRoleWithoutNoop[i].getValuesSEM() + ";" + branchesPerRoleWithoutNoop[i].get95ConfidenceInterval() + ";");
					//System.out.println("    " + abstractStateMachine.convertToExplicitRole(r) + " = " + branchesPerRole[i]/visitedStates);
					i++;
				}

				StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Depth;" + depth.getNumSamples() + ";" + depth.getMinValue() + ";" + depth.getMaxValue() + ";" + depth.getAvgValue() + ";" + depth.getValuesStandardDeviation() + ";" + depth.getValuesSEM() + ";" + depth.get95ConfidenceInterval() + ";");

				//System.out.println("Average depth = " + depthSum/numSimulations);

				//System.out.println("Average goals per role:");
				i = 0;
				for(Role r : abstractStateMachine.getRoles()){
					StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Scores for role\"" + abstractStateMachine.convertToExplicitRole(r) + "\";" + scores[i].getNumSamples() + ";" + scores[i].getMinValue() + ";" + scores[i].getMaxValue() + ";" + scores[i].getAvgValue() + ";" + scores[i].getValuesStandardDeviation() + ";" + scores[i].getValuesSEM() + ";" + scores[i].get95ConfidenceInterval() + ";");
					//System.out.println("    " + abstractStateMachine.convertToExplicitRole(r) + " = " + goalSums[i]/numSimulations);
					i++;
				}

				i = 0;
				for(Role r : abstractStateMachine.getRoles()){
					StatsUtils.writeToFileMkParentDir(folder + "/" + gameKey + "-SearchTree.csv","Wins for role\"" + abstractStateMachine.convertToExplicitRole(r) + "\";" + wins[i].getNumSamples() + ";" + wins[i].getMinValue() + ";" + wins[i].getMaxValue() + ";" + wins[i].getAvgValue() + ";" + wins[i].getValuesStandardDeviation() + ";" + wins[i].getValuesSEM() + ";" + wins[i].get95ConfidenceInterval() + ";");
					//System.out.println("    " + abstractStateMachine.convertToExplicitRole(r) + " = " + goalSums[i]/numSimulations);
					i++;
				}

			} catch (StateMachineInitializationException e) {
	        	System.out.println("State machine " + thePropnetMachine.getName() + " initialization failed, impossible to compute branching factor for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	e.printStackTrace();
			}

			System.out.println();

		}


	}


	/**
	 * This method looks at the terminal scores and returns the corresponding wins. To compute the wins
	 * 1 point is split equally among all the agents that have the highest score. If it's a single player
	 * game the only role gets the fraction of 1 point proportional to its score: (score/100)*1
	 * Examples:
	 * Scores		Wins
	 * [100]		[1]
	 * [80]			[0.8]
	 * [50]			[0.5]
	 * [100 0]		[1 0]
	 * [30 70]		[0 1]
	 * [30 30 30]	[0.33 0.33 0.33]
	 * [70 70 50]	[0.5 0.5 0]
	 *
	 * @return
	 */
	public static double[] getTerminalWinsIn0_1(double[] goals) {

		double[] wins = new double[goals.length];

		if(goals.length == 1) {
			wins[0] = goals[0]/100.0;
		}else {
			List<Integer> bestIndices = new ArrayList<Integer>();
			double max = -1;
			for(int roleIndex = 0; roleIndex < goals.length; roleIndex++) {
				if(goals[roleIndex] > max) {
					max = goals[roleIndex];
					bestIndices.clear();
					bestIndices.add(roleIndex);
				}else if(goals[roleIndex] == max){
					bestIndices.add(roleIndex);
				}
			}
			if(bestIndices.size() == 0) {
				GamerLogger.logError("MctsManager", "Found no best score when computing wins for a SimulationResult.");
				throw new RuntimeException("MctsManager - Found no best score when computing wins for a SimulationResult.");
			}
			// Wins is already initialized to all 0s, so we just change the wins for the bestIndices
			double splitPoint = 1.0/((double)bestIndices.size());
			for(Integer roleIndex : bestIndices) {
				wins[roleIndex] = splitPoint;
			}
		}
		return wins;

	}

}
