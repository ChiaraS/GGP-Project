package org.ggp.base.apps.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggp.base.player.L4J2GamePlayer;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.util.reflection.ProjectSearcher;

public final class L4J2PlayerRunner
{

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException
	{
		if (args.length != 2 || args[0].equals("${arg0}")) {
			System.out.println("L4J2PlayerRunner [port] [name]");
			System.out.println("example: ant L4J2PlayerRunner -Darg0=9147 -Darg1=TurboTurtle");
			return;
		}
    	int port = Integer.parseInt(args[0]);
    	String name = args[1];
    	System.out.println("[L4J2] Starting up preconfigured player on port " + port + " using player class named " + name);
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
		new L4J2GamePlayer(port, gamer).start();
	}
}
