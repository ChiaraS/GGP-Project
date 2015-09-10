package csironi.ggp.course;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.yapProlog.YapStateMachine;
import org.ggp.base.util.statemachine.implementation.yapProlog.transform.YapRenderer;

import com.declarativa.interprolog.YAPSubprocessEngine;

public class ProvaYap {



	public static void main(String[] args) {
		GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;
            if(!gameKey.equals(args[0])) continue;
            List<Gdl> description = theRepository.getGame(gameKey).getRules();
            try{
            	//provaYap(description);
            	//provaYap2(description);
            	//provaDescription(description);
            	provaStepByStep(description);
            }catch(Exception e){
            	System.out.println("Error when testing game " + gameKey);
            	e.printStackTrace();
            	continue;
            }

        }
	}


	private static void provaStepByStep(List<Gdl> gdlDescription){

		GamerLogger.setSpilloverLogfile("ProvaStepByStep.log");

		YapStateMachine machine = new YapStateMachine();

		try {
			machine.initialize(gdlDescription);
		} catch (StateMachineException e) {
			GamerLogger.logError("ProvaStepByStep", "Inititlaization failed!");
			GamerLogger.logStackTrace("ProvaStepByStep", e);
			return;
		}

		MachineState currentState = machine.getInitialState();

		GamerLogger.log("ProvaStepByStep", "Initial state: " + currentState.toString());

		List<Role> roles = machine.getRoles();

		GamerLogger.log("ProvaStepByStep", "Roles: " + roles.toString());

		boolean terminal;
		try {
			terminal = machine.isTerminal(currentState);
		} catch (StateMachineException e) {
			GamerLogger.logError("ProvaStepByStep", "isTerminal failed!");
			GamerLogger.logStackTrace("ProvaStepByStep", e);
			return;
		}

		while(!terminal){

			List<Move> jointMove = new ArrayList<Move>();

			for(Role r : roles){
				List<Move> moves;
				try {
					moves = machine.getLegalMoves(currentState, r);
				} catch (MoveDefinitionException e) {
					GamerLogger.logError("ProvaStepByStep", "getLegalMoves failed for role \"" + r + "\" with a MoveDefinitionException!");
					GamerLogger.logStackTrace("ProvaStepByStep", e);
					return;
				} catch (StateMachineException e) {
					GamerLogger.logError("ProvaStepByStep", "getLegalMoves failed for role \"" + r + "\" with a StateMachineException!");
					GamerLogger.logStackTrace("ProvaStepByStep", e);
					return;
				}
				GamerLogger.log("ProvaStepByStep", "Legal moves for role \"" + r + "\": " + moves.toString());

				jointMove.add(moves.get(new Random().nextInt(moves.size())));
			}

			GamerLogger.log("ProvaStepByStep", "Chosen joint move: " + jointMove.toString());

			try {
				currentState = machine.getNextState(currentState, jointMove);
			} catch (TransitionDefinitionException e) {
				GamerLogger.logError("ProvaStepByStep", "getNextState failed with a TransitionDefinitionException!");
				GamerLogger.logStackTrace("ProvaStepByStep", e);
				return;
			} catch (StateMachineException e) {
				GamerLogger.logError("ProvaStepByStep", "getNextState failed with a StateMachineException!");
				GamerLogger.logStackTrace("ProvaStepByStep", e);
				return;
			}

			GamerLogger.log("ProvaStepByStep", "Current state: " + currentState.toString());

			try {
				terminal = machine.isTerminal(currentState);
			} catch (StateMachineException e) {
				GamerLogger.logError("ProvaStepByStep", "isTerminal failed!");
				GamerLogger.logStackTrace("ProvaStepByStep", e);
				return;
			}

		}

		for(Role r : roles){
			int goal;
			try {
				goal = machine.getGoal(currentState, r);
			} catch (GoalDefinitionException e) {
				GamerLogger.logError("ProvaStepByStep", "getGoal failed for role \"" + r + "\"with a GoalDefinitionException!");
				GamerLogger.logStackTrace("ProvaStepByStep", e);
				return;
			} catch (StateMachineException e) {
				GamerLogger.logError("ProvaStepByStep", "getGoal failed for role \"" + r + "\"with a StateMachineException!");
				GamerLogger.logStackTrace("ProvaStepByStep", e);
				return;
			}

			GamerLogger.log("ProvaStepByStep", "Goal for role \"" + r + "\": " + goal);
		}

		machine.shutdown();

		GamerLogger.log("ProvaStepByStep", "DONE!!");
	}

	private static void provaDescription(List<Gdl> gdlDescription){

		System.out.println(gdlDescription);

	}

	private static void provaYap(List<Gdl> gdlDescription) throws IOException{

		YAPSubprocessEngine yapProver = null;

		try{
			createAndSavePrologDescription(gdlDescription);

			String yapCommand = "/home/csironi/CadiaplayerInstallation/Yap/bin/yap";

			yapProver = new YAPSubprocessEngine(yapCommand);

			yapProver.consultAbsolute(new File("/home/csironi/YAPplayer/prologFiles/prologFunctions.pl"));

			Object[] bindings = yapProver.deterministicGoal("initialize_state(List), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_)", "[AS]");
			//Object[] bindings = yapProver.deterministicGoal("initialize_state(List), ipObjectTemplate('ArrayOfString',AS,_,[List],_)", "[AS]");

			if(bindings == null){
				System.out.println("No bindings");
			}else{
				System.out.println("There are " + bindings.length + " bindings!");
				Object firstBinding = bindings[0];
				if(firstBinding instanceof String[]){
					System.out.println("The first binding is a string array!");
					String[] results = ((String[]) firstBinding);
					System.out.println("The result saved in the binding is: ");
					System.out.print("[ ");
					for(String s : results){
						System.out.print(s + ", ");
					}
					System.out.println("]");
				}else{
					System.out.println("The first binding is of type " + firstBinding.getClass().getName());
				}
			}
		}finally{
			if(yapProver != null){
				yapProver.shutdown();
			}
		}


	}

	private static void createAndSavePrologDescription(List<Gdl> description) throws IOException{

			String yapDescription = "";

			for(Gdl gdl : description)
				yapDescription += YapRenderer.renderYap(gdl)+". \n";

			BufferedWriter out = null;
			try{
				out = new BufferedWriter(new FileWriter("/home/csironi/YAPplayer/prologFiles/description.pl"));
				out.write(yapDescription);
			}finally{
				if(out != null){
					out.close();
				}
			}

	}

	private static void provaYap2(List<Gdl> gdlDescription) throws IOException{

		YAPSubprocessEngine yapProver = null;

		try{
			createAndSavePrologDescription(gdlDescription);

			String yapCommand = "/home/csironi/CadiaplayerInstallation/Yap/bin/yap";

			yapProver = new YAPSubprocessEngine(yapCommand);

			yapProver.consultAbsolute(new File("/home/csironi/YAPplayer/prologFiles/prologFunctions.pl"));

			System.out.println("Prima della query.");

			Object[] bindings = yapProver.deterministicGoal("get_number_of_init(List, N), processList(List, LL), ipObjectTemplate('ArrayOfString',AS,_,[LL],_), ipObjectSpec('java.lang.Integer',I,[N],_)", "[AS, I]");

			System.out.println("Dopo la query.");

			if(bindings == null){
				System.out.println("No bindings");
			}else{
				System.out.println("There are " + bindings.length + " bindings!");
				Object firstBinding = bindings[0];
				if(firstBinding instanceof String[]){
					System.out.println("The first binding is a string array!");
					String[] results = ((String[]) firstBinding);
					System.out.println("The result saved in the binding is: ");
					System.out.print("[ ");
					for(String s : results){
						System.out.print(s + " ");
					}
					System.out.println("]");
				}else{
					System.out.println("The first binding is of type " + firstBinding.getClass().getName());
				}

				if(bindings.length > 1){
					Object secondBinding = bindings[1];
					if(secondBinding instanceof Integer){
						System.out.println("The second binding is an Integer!");
						Integer result = ((Integer) secondBinding);
						System.out.print("The result saved in the binding is: ");
						System.out.println(result.intValue());
					}else{
						System.out.println("The second binding is of type " + secondBinding.getClass().getName());
					}
				}else{
					System.out.println("Whyy??");
				}
			}
		}finally{
			if(yapProver != null){
				yapProver.shutdown();
			}
		}


	}

}
