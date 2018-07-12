package org.ggp.base.apps.player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.statemachine.ConfigurableStateMachineGamer;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

/**
 * This is a simple command line app for running players.
 *
 * @author schreib
 */
public final class PlayerRunner
{
	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException
	{
		if (args.length != 2 || args[0].equals("${arg0}")) {
			System.out.println("PlayerRunner [port] [name]");
			System.out.println("example: ant PlayerRunner -Darg0=9147 -Darg1=TurboTurtle");
			return;
		}
    	int port = Integer.parseInt(args[0]);

		String[] s = args[1].split("-");

		String name;
		String settings;

		if(s.length == 1){ // Internal gamer without settings
			name = s[0];
			settings = null;
		}else if(s.length == 2){ // Internal gamer with settings
			name = s[0];
			settings = s[1];
		}else{
			System.out.println("Impossible to start player, wrong input. Wrong definition of gamer type " + args[1] + ".");
			return;
		}

    	System.out.println("Starting up preconfigured player on port " + port + " using player class named " + name + " and " + (settings != null ? settings : "default") + " settings.");
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
    	Gamer gamer;
    	if(ConfigurableStateMachineGamer.class.isAssignableFrom(chosenGamerClass) && settings != null){
			try {
				gamer = (Gamer) chosenGamerClass.getConstructor(String.class).newInstance(GamerConfiguration.gamersSettingsFolderPath + "/" + settings);
			} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				System.out.println("Could not initialize player with settings " + settings);
				return;
			}
		}else{
			gamer = (Gamer) chosenGamerClass.newInstance();
		}

    	ThreadContext.put("LOG_FOLDER", System.currentTimeMillis() + ".PlayerRunner");

    	GamerLogger.startFileLogging();

		new GamePlayer(port, gamer).start();

		/*for(int i = 0; i < 30; i++){
			System.out.println("Threads ALL: " + ManagementFactory.getThreadMXBean().getThreadCount());
			System.out.println("Threads ACTIVE: " + Thread.activeCount());
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
}