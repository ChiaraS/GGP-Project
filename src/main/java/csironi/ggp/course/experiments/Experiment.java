/**
 *
 */
package csironi.ggp.course.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.server.GameServer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;

/**
 * @author C.Sironi
 *
 */
public class Experiment {

	/**
	 * List of all the types of gamers that this experiment must match against each other.
	 */
	private String[] gamerTypes;

	/**
	 * Number of different gamer types
	 */
	private int numOfTypes;

	public int count = 0;

	/**
	 *
	 */
	public Experiment(int num) {
		this.numOfTypes = num;
	}


	public static void main(String []args) throws InstantiationException, IllegalAccessException, IOException, InterruptedException, GoalDefinitionException{

		/*Experiment e = new Experiment(2);

		ArrayList<Integer> unusedTypes = new ArrayList<Integer>();
		unusedTypes.add(new Integer(0));
		unusedTypes.add(new Integer(1));

		e.runAllTypesCombinations(3, unusedTypes, new int[3]);



		Experiment e2 = new Experiment(3);

		ArrayList<Integer> unusedTypes2 = new ArrayList<Integer>();
		unusedTypes2.add(new Integer(0));
		unusedTypes2.add(new Integer(1));
		unusedTypes2.add(new Integer(2));

		e2.runAllTypesCombinations(4, unusedTypes2, new int[4]);


		Experiment e3 = new Experiment(3);

		ArrayList<Integer> unusedTypes3 = new ArrayList<Integer>();
		unusedTypes3.add(new Integer(0));
		unusedTypes3.add(new Integer(1));
		unusedTypes3.add(new Integer(2));

		e3.runAllTypesCombinations(6, unusedTypes3, new int[6]);

		System.out.println(e3.count);*/

		List<String> hostNames = new ArrayList<String>();
		List<String> playerNames = new ArrayList<String>();
		List<Integer> portNumbers = new ArrayList<Integer>();

		int port = 9147;
		String name = "RandomGamer";
    	System.out.println("Starting up preconfigured player on port " + port + " using player class named " + name);
    	Class<?> chosenGamerClass = null;
    	List<String> availableGamers = new ArrayList<String>();
    	for (Class<?> gamerClass : ProjectSearcher.GAMERS.getConcreteClasses()) {
    		availableGamers.add(gamerClass.getSimpleName());
    		if (gamerClass.getSimpleName().equals(name)) {
    			chosenGamerClass = gamerClass;
    		}
    	}
    	if (chosenGamerClass == null) {
    		System.out.println("Could not find player class with that name. Available choices are: " + Arrays.toString(availableGamers.toArray()));
    		return;
    	}
    	Gamer gamer = (Gamer) chosenGamerClass.newInstance();
		GamePlayer first = new GamePlayer(port, gamer);
		first.start();

		port = first.getGamerPort();

		hostNames.add("127.0.0.1");
		playerNames.add(name);
		portNumbers.add(port);


		port += 1;
		name = "PhilUCT";
    	System.out.println("Starting up preconfigured player on port " + port + " using player class named " + name);
    	chosenGamerClass = null;
    	availableGamers = new ArrayList<String>();
    	for (Class<?> gamerClass : ProjectSearcher.GAMERS.getConcreteClasses()) {
    		availableGamers.add(gamerClass.getSimpleName());
    		if (gamerClass.getSimpleName().equals(name)) {
    			chosenGamerClass = gamerClass;
    		}
    	}
    	if (chosenGamerClass == null) {
    		System.out.println("Could not find player class with that name. Available choices are: " + Arrays.toString(availableGamers.toArray()));
    		return;
    	}
    	gamer = (Gamer) chosenGamerClass.newInstance();
		GamePlayer second = new GamePlayer(port, gamer);
		second.start();

		port = second.getGamerPort();

		hostNames.add("127.0.0.1");
		playerNames.add(name);
		portNumbers.add(port);


		// Extract the desired configuration from the command line.
		String tourneyName = "TTTProva";
		String gameKey = "ticTacToe";
		Game game = GameRepository.getDefaultRepository().getGame(gameKey);
		int startClock = 20;
		int playClock = 10;


		String matchName = tourneyName + "." + gameKey + "." + System.currentTimeMillis();

		int expectedRoles = Role.computeRoles(game.getRules()).size();
		if (hostNames.size() != expectedRoles) {
			throw new RuntimeException("Invalid number of players for game " + gameKey + ": " + hostNames.size() + " vs " + expectedRoles);
		}
		Match match = new Match(matchName, -1, startClock, playClock, game);
		match.setPlayerNamesFromHost(playerNames);

		// Actually run the match, using the desired configuration.
		GameServer server = new GameServer(match, hostNames, portNumbers);
		server.start();
		server.join();

		// Open up the directory for this tournament.
		// Create a "scores" file if none exists.
		File f = new File(tourneyName);
		if (!f.exists()) {
			f.mkdir();
			f = new File(tourneyName + "/scores");
			f.createNewFile();
		}


		BufferedWriter bw;

		/**
		 * Do not save the match history in an XML
		 */
		/*
		// Open up the XML file for this match, and save the match there.
		f = new File(tourneyName + "/" + matchName + ".xml");
		if (f.exists()) f.delete();
		bw = new BufferedWriter(new FileWriter(f));
		bw.write(match.toXML());
		bw.flush();
		bw.close();
		*/



		// Open up the JSON file for this match, and save the match there.
		f = new File(tourneyName + "/" + matchName + ".json");
		if (f.exists()) f.delete();
		bw = new BufferedWriter(new FileWriter(f));
		bw.write(match.toJSON());
		bw.flush();
		bw.close();


		// Save the goals in the "/scores" file for the tournament.
		bw = new BufferedWriter(new FileWriter(tourneyName + "/scores", true));
		List<Integer> goals = server.getGoals();
		String goalStr = "";
		String playerStr = "";
		for (int i = 0; i < goals.size(); i++)
		{
			Integer goal = server.getGoals().get(i);
			goalStr += Integer.toString(goal);
			playerStr += playerNames.get(i);
			if (i != goals.size() - 1)
			{
				playerStr += ",";
				goalStr += ",";
			}
		}
		bw.write("\n" + playerStr + "=" + goalStr);
		bw.flush();
		bw.close();
		
		first.shutdown();
		second.shutdown();


	}


	public void runAllTypesCombinations(int remainingPlayers, ArrayList<Integer> unusedTypes, int[] typesCombination){

		/*
		System.out.println();

		System.out.println("Remaining players: " + remainingPlayers);

		System.out.print("Unused types: [ ");
		for(Integer i : unusedTypes){
			System.out.print(i.intValue() + " ");
		}
		System.out.println("]");

		System.out.print("Types combination: [ ");
		for(int i = 0; i < typesCombination.length; i++){
			System.out.print(typesCombination[i] + " ");
		}
		System.out.println("]");*/



		// If all the players in the considered game have been assigned a type,
		// it's possible to run the match with this combination of types.
		if(remainingPlayers == 0){

			/**this.runMatch(typesCombination);**/
			this.print(typesCombination);

		// Check if the number of players in the game that still don't have a gamer type assigned is more than
		// the number of gamer types that have not been assigned to anyone yet.
		// If yes, we must consider all the possible configurations obtained by assigning to the next player
		// all possible gamer types...
		}else if(remainingPlayers > unusedTypes.size()){

			// Consider all players configurations obtained by assigning to the current player every possible gamer type.
			for(int i = 0; i < this.numOfTypes; i++){

				typesCombination[remainingPlayers-1] = i;

				// Copy the unused types list and, if not yet done, remove the current type from the unused types list.
				ArrayList<Integer> unusedTypesCopy = new ArrayList<Integer>(unusedTypes);
				unusedTypesCopy.remove(new Integer(i));

				// Recursive call. NOTE!: It is not necessary to copy also the array with the typesCombination
				// since as soon as it will contain one of the possible combinations of types it will be used
				// to run a match. Then it will be modified so that every time a match will be run it will contain
				// a different valid combination of types.
				runAllTypesCombinations((remainingPlayers-1), unusedTypesCopy, typesCombination);

			}

		// ...if not, then we must consider only the configurations that we can obtain by assigning to the next
		// player all the gamer types not yet assigned so far. This is to make sure that each configuration that
		// we will obtain will contain all possible gamer types at least once.
		}else{

			// Consider all players configurations obtained by assigning to the current player only the gamer types
			// not used so far.
			for(Integer i : unusedTypes){

				typesCombination[remainingPlayers-1] = i.intValue();

				// Copy the unused types list and remove the current type from the unused types list.
				// It will be removed for sure since we are certain that it is still in the list because it
				// has not been used yet.
				ArrayList<Integer> unusedTypesCopy = new ArrayList<Integer>(unusedTypes);
				unusedTypesCopy.remove(i);

				// Recursive call
				runAllTypesCombinations((remainingPlayers-1), unusedTypesCopy, typesCombination);

			}
		}

	}



	private void runMatch(int[] typesCombination){

	}

	private void print(int[] combinations){

		this.count++;

		System.out.println();
		System.out.print("[ ");

		for(int i = 0; i < combinations.length; i++){
			System.out.print(combinations[i] + " ");
		}

		System.out.println("]");
	}

}
