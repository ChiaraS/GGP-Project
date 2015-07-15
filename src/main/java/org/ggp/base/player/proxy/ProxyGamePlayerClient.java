package org.ggp.base.player.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.ggp.base.player.event.PlayerDroppedPacketEvent;
import org.ggp.base.player.event.PlayerReceivedMessageEvent;
import org.ggp.base.player.event.PlayerSentMessageEvent;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.request.factory.RequestFactory;
import org.ggp.base.player.request.grammar.AbortRequest;
import org.ggp.base.player.request.grammar.Request;
import org.ggp.base.player.request.grammar.StopRequest;
import org.ggp.base.util.observer.Event;
import org.ggp.base.util.observer.Observer;
import org.ggp.base.util.observer.Subject;
import org.ggp.base.util.reflection.ProjectSearcher;

import com.google.common.collect.Lists;

public final class ProxyGamePlayerClient extends Thread implements Subject, Observer
{

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{

    	System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

		LOGGER = LogManager.getRootLogger();
	}

	private final Gamer gamer;
	private final int port;
	private final String playerID;
	private final List<Observer> observers;

	private Socket theConnection;
	private BufferedReader theInput;
	private PrintStream theOutput;

    /**
     * @param args
     * Command line arguments:
     *  ProxyGamePlayerClient gamer port
     */
    public static void main(String[] args) {

        LOGGER.info("[ProxyClient] Starting the ProxyGamePlayerClient program.");

        if (!(args.length == 2)) {
            LOGGER.error("[ProxyClient] Usage is: \n\tProxyGamePlayerClient gamer port");
            return;
        }

        int port = 9147;
        Gamer gamer = null;
        try {
            port = Integer.valueOf(args[1]);
        } catch(Exception e) {
            LOGGER.error("[ProxyClient] Caught exception when parsing the port number: " + args[1]+" is not a valid port.", e);
            return;
        }

        List<Class<? extends Gamer>> gamers = Lists.newArrayList(ProjectSearcher.GAMERS.getConcreteClasses());
        List<String> gamerNames = new ArrayList<String>();
        if(gamerNames.size()!=gamers.size())
        {
            for(Class<?> c : gamers)
                gamerNames.add(c.getName().replaceAll("^.*\\.",""));
        }

        int idx = gamerNames.indexOf(args[0]);
        if (idx == -1) {
        	String message = "[ProxyClient] " + args[0] + " is not a subclass of gamer. Valid options are:";
            for(String s : gamerNames)
                message += "\n" + s;
            LOGGER.error(message);
            return;
        }

        try {
            gamer = (Gamer)(gamers.get(idx).newInstance());
        } catch(Exception ex) {
            LOGGER.error("[ProxyClient] Caught exception when creating the gamer: cannot create instance of " + args[0] + ".", ex);
            return;
        }

        try {
            ProxyGamePlayerClient theClient = new ProxyGamePlayerClient(port, gamer);
            theClient.start();

        } catch (IOException e) {
            LOGGER.error("[ProxyClient] Caught exception when creating and starting the Proxy Game Player Client.", e);
        }
    }

	public ProxyGamePlayerClient(int port, Gamer gamer) throws IOException
	{
		observers = new ArrayList<Observer>();

		theConnection = new Socket("127.0.0.1", port);
        theOutput = new PrintStream(theConnection.getOutputStream());
        theInput = new BufferedReader(new InputStreamReader(theConnection.getInputStream()));

		this.gamer = gamer;
		this.port = port;
		this.playerID = System.currentTimeMillis() + "." + this.gamer.getName() + "." + this.port;

		LOGGER.info("[ProxyClient] Started player " + playerID + ". Writing logs to file logs\\" + this.playerID + "\\GamePlayer.log");

		gamer.addObserver(this);
	}

	@Override
	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}

	@Override
	public void notifyObservers(Event event)
	{
		for (Observer observer : observers)
		{
			observer.observe(event);
		}
	}

	private long theCode;

	@Override
	public void run()
	{
		// LOGGING DETAILS
		ThreadContext.put("PLAYER_ID", this.playerID);
		LOGGER.info("[ProxyClient] Starting logs for player " + this.playerID + ". Player available to play a match.");
		// LOGGING DETAILS

		while (!isInterrupted())
		{
			try
			{
			    ProxyMessage theMessage = ProxyMessage.readFrom(theInput);
			    LOGGER.info(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] [MESSAGE RECEIVED] " + theMessage, "GamePlayer"));
			    String in = theMessage.theMessage;
			    theCode = theMessage.messageCode;
			    long receptionTime = theMessage.receptionTime;
				notifyObservers(new PlayerReceivedMessageEvent(in));

				Request request = new RequestFactory().create(gamer, in);

				/* This part of code is needed only to get information about the match for which we want to start file
				 * logging in a specific file with the GamerLogger class. However, using Log4j2, file logging is started
				 * automatically by the StartRequest class every time a start request is processed.
				if(request instanceof StartRequest) {
				    RandomGamer theDefaultGamer = new RandomGamer();
				    new RequestFactory().create(theDefaultGamer, in).process(1);
				    GamerLogger.startFileLogging(theDefaultGamer.getMatch(), theDefaultGamer.getRoleName().toString());
				    GamerLogger.log("Proxy", "[ProxyClient] Got message: " + theMessage);
				}
				*/

				String out = request.process(receptionTime);

				ProxyMessage outMessage = new ProxyMessage("DONE:" + out, theCode, 0L);
				outMessage.writeTo(theOutput);
				LOGGER.info(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] [MESSAGE SENT] " + outMessage, "GamePlayer"));
				notifyObservers(new PlayerSentMessageEvent(out));

				if(request instanceof StopRequest) {
				    LOGGER.info(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] Got stop request, shutting down.", "GamePlayer"));
				    System.exit(0);
				}
                if(request instanceof AbortRequest) {
                    LOGGER.info(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] Got abort request, shutting down.", "GamePlayer"));
                    System.exit(0);
                }
			}
			catch (Exception e)
			{
			    LOGGER.error(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] Caught exception while running.", "GamePlayer"), e);
				notifyObservers(new PlayerDroppedPacketEvent());
			}
		}

		LOGGER.info(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] Got interrupted, shutting down.", "GamePlayer"));
		ThreadContext.remove("PLAYER_ID");
	}

    @Override
	public void observe(Event event) {
        if(event instanceof WorkingResponseSelectedEvent) {
            WorkingResponseSelectedEvent theWorking = (WorkingResponseSelectedEvent)event;
            ProxyMessage theMessage = new ProxyMessage("WORK:" + theWorking.getWorkingResponse(), theCode, 0L);
            theMessage.writeTo(theOutput);
            LOGGER.info(new StructuredDataMessage("ProxyGamePlayerClient", "[ProxyClient] [MESSAGE SENT] " + theMessage, "GamePlayer"));
        }
    }
}
