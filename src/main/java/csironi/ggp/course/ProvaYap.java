package csironi.ggp.course;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
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
            	provaYap2(description);
            }catch(Exception e){
            	System.out.println("Error when testing game " + gameKey);
            	e.printStackTrace();
            	continue;
            }

        }
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
