package csironi.ggp.course;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
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
import org.ggp.base.util.statemachine.structure.Role;

public class BranchingFactorEstimator {

	public static void main(String[] args){

		String[] gameKeys = args[0].split(";");

		for(String key : gameKeys){
			computeBranchingFactorAndDepthAndWinsForGame(key);
		}

	}

	public static void computeBranchingFactorAndDepthAndWinsForGame(String gameKey){

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

				double branches = 0;
				double[] branchesPerRole = new double[abstractStateMachine.getRoles().size()];
				for(int i = 0; i < branchesPerRole.length; i++){
					branchesPerRole[i] = 0;
				}
				double visitedStates = 0;

				double numSimulations = 0;
				double depthSum = 0;
				double[] goalSums = new double[abstractStateMachine.getRoles().size()];

				int maxDepth = 500;

				while(System.currentTimeMillis() < timeout) {

					MachineState currentState = root;

					boolean terminal;

					int nDepth = 0;

				    do{

						nDepth++;

						try {
							branches += abstractStateMachine.getLegalJointMoves(currentState).size();
						} catch (MoveDefinitionException | StateMachineException e) {
							System.out.println("Exception getting the legal joint moves while performing a playout.");
							e.printStackTrace();
							break;
						}

						int i = 0;
						for(Role r : abstractStateMachine.getRoles()){
							try {
								branchesPerRole[i] += abstractStateMachine.getLegalMoves(currentState, r).size();
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

				    depthSum += nDepth;
				    numSimulations++;

				    double[] goals = abstractStateMachine.getSafeGoalsAvgForAllRoles(currentState);

				    for(int roleIndex = 0; roleIndex < goals.length; roleIndex++){
				    	goalSums[roleIndex] +=goals[roleIndex];
				    }

				}

				System.out.println("Visited states = " + visitedStates);
				System.out.println("Average joint branches = " + branches/visitedStates);
				System.out.println("Average branches per role:");
				int i = 0;
				for(Role r : abstractStateMachine.getRoles()){
					System.out.println("    " + abstractStateMachine.convertToExplicitRole(r) + " = " + branchesPerRole[i]/visitedStates);
					i++;
				}

				System.out.println("Average depth = " + depthSum/numSimulations);

				System.out.println("Average goals per role:");
				i = 0;
				for(Role r : abstractStateMachine.getRoles()){
					System.out.println("    " + abstractStateMachine.convertToExplicitRole(r) + " = " + goalSums[i]/numSimulations);
					i++;
				}

			} catch (StateMachineInitializationException e) {
	        	System.out.println("State machine " + thePropnetMachine.getName() + " initialization failed, impossible to compute branching factor for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	e.printStackTrace();
			}

			System.out.println();

		}


	}

}
