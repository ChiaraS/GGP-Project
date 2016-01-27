package org.ggp.base.player.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.event.PlayerDroppedPacketEvent;
import org.ggp.base.player.event.PlayerReceivedMessageEvent;
import org.ggp.base.player.event.PlayerSentMessageEvent;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.statemachine.random.RandomGamer;
import org.ggp.base.player.request.factory.RequestFactory;
import org.ggp.base.player.request.grammar.AbortRequest;
import org.ggp.base.player.request.grammar.InfoRequest;
import org.ggp.base.player.request.grammar.PlayRequest;
import org.ggp.base.player.request.grammar.Request;
import org.ggp.base.player.request.grammar.StartRequest;
import org.ggp.base.player.request.grammar.StopRequest;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.http.HttpReader;
import org.ggp.base.util.http.HttpWriter;
import org.ggp.base.util.observer.Event;
import org.ggp.base.util.observer.Observer;
import org.ggp.base.util.observer.Subject;
import org.ggp.base.util.symbol.grammar.SymbolPool;


/**
 * ProxyGamePlayer starts a separate process running an instance of the Gamer
 * class that is passed in as a parameter. It serves as a proxy between this
 * Gamer process and the GGP server: it ensures that legal moves are sent back
 * to the server on time, accepts and stores working moves, and so on.
 *
 * This class is not necessary, unless you are interested in adding another
 * layer of bullet-proofing to your player in preparation for a tournament
 * or for running your player for long periods of time.
 *
 * There are advantages and disadvantages to this approach. The advantages are:
 *
 *  1. Even if the Gamer process stalls, for example due to garbage collection,
 *     you will always send a legal move back to the server in time.
 *
 *  2. You can send "working moves" to the proxy, so that if your Gamer process
 *     stalls, you can send back your best-guess move from before the stall.
 *
 * The disadvantage is very simple:
 *
 *  1. If the proxy breaks, you can revert to playing extremely poorly
 *     even though your real Gamer process is fully functional.
 *
 * The advantages are very important, and so my response to the disadvantage
 * has been to shake as many bugs out of the proxy as I can. While the code is
 * fairly complex, this proxy has proven to be decently reliable in my testing.
 * So, that's progress.
 *
 * @author Sam Schreiber
 */
public final class ProxyGamePlayer extends Thread implements Subject
{

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{

		LOGGER = LogManager.getRootLogger();

	}

	private final String gamerName;
	private ServerSocket listener;
	private ServerSocket clientListener;
	private final List<Observer> observers;
	private ClientManager theClientManager;
	private Gamer theDefaultGamer;

	private class ClientManager extends Thread {
	    private Process theClientProcess;
	    private Socket theClientConnection;
	    private PrintStream theOutput;
	    private BufferedReader theInput;

	    private StreamConnector outConnector, errConnector;

	    public volatile boolean pleaseStop = false;
	    public volatile boolean expectStop = false;
	    private Thread parentThread;

	    public ClientManager(Thread parentThread) {
	        this.parentThread = parentThread;

	        String command = GamerConfiguration.getCommandForJava();
	        List<String> processArgs = new ArrayList<String>();
	        processArgs.add(command);
	        processArgs.add("-mx" + GamerConfiguration.getMemoryForGamer() + "m");
	        processArgs.add("-server");
	        processArgs.add("-XX:-DontCompileHugeMethods");
	        processArgs.add("-XX:MinHeapFreeRatio=10");
	        processArgs.add("-XX:MaxHeapFreeRatio=10");
	        processArgs.add("-classpath");
	        processArgs.add(System.getProperty("java.class.path"));
	        processArgs.add("org.ggp.base.player.proxy.ProxyGamePlayerClient");
	        processArgs.add(gamerName);
	        processArgs.add("" + clientListener.getLocalPort());
	        if(GamerConfiguration.runningOnLinux()) {
	        	processArgs.add(0, "nice");
	        }
	        ProcessBuilder pb = new ProcessBuilder(processArgs);

	        try {
	        	LOGGER.info("[PROXY] Starting a new proxy client, using gamer " + gamerName + ".");

	            theClientProcess = pb.start();
	            outConnector = new StreamConnector(theClientProcess.getErrorStream(), System.err);
	            errConnector = new StreamConnector(theClientProcess.getInputStream(), System.out);
	            outConnector.start();
	            errConnector.start();

	            theClientConnection = clientListener.accept();

	            theOutput = new PrintStream(theClientConnection.getOutputStream());
	            theInput = new BufferedReader(new InputStreamReader(theClientConnection.getInputStream()));

	            LOGGER.info("[PROXY] Proxy client started.");
	        } catch(IOException e) {
	        	LOGGER.error("[PROXY] Error when setting up proxy.", e);
	        }
	    }

	    // TODO: remove this class if nothing is being sent over it
	    private class StreamConnector extends Thread {
	        private InputStream theInput;
	        private PrintStream theOutput;

	        public volatile boolean pleaseStop = false;

	        public StreamConnector(InputStream theInput, PrintStream theOutput) {
	            this.theInput = theInput;
	            this.theOutput = theOutput;
	        }

	        public boolean isPrintableChar( char c ) {
	            if(!Character.isDefined(c)) return false;
	            if(Character.isIdentifierIgnorable(c)) return false;
	            return true;
	        }

	        @Override
			public void run() {
	            try {
	                while(!pleaseStop) {
	                    int next = theInput.read();
	                    if(next == -1) break;
	                    if(!isPrintableChar((char)next))
	                        next = '@';
	                    theOutput.write(next);
	                }
	            } catch(IOException e) {
	            	LOGGER.error("[PROXY] Caught exception when redirecting output. Might be okay.", e);
	            } catch(Exception e) {
	            	LOGGER.error("[PROXY] Caught exception when redirecting output.", e);
	            } catch(Error e) {
	            	LOGGER.error("[PROXY] Caught error when redirecting output.", e);
                }
	        }
	    }

	    public void sendMessage(ProxyMessage theMessage) {
            if(theOutput != null) {
                theMessage.writeTo(theOutput);
                LOGGER.info("[PROXY] Wrote message to client: " + theMessage);
            }
	    }

	    @Override
		public void run() {
            while(theInput != null) {
                try {
                    ProxyMessage in = ProxyMessage.readFrom(theInput);
                    if(pleaseStop)
                        return;

                    LOGGER.info("[PROXY] Got message from client: " + in);
                    if(in == null)
                        continue;

                    processClientResponse(in, parentThread);
                } catch(SocketException se) {
                    if(expectStop)
                        return;

                    LOGGER.error("[PROXY] Shutting down reader as consequence of socket exception. Presumably this is because the gamer client crashed.", se);
                    break;
                } catch(Exception e) {
                	LOGGER.error("[PROXY] Caught exception while managing proxy messages.", e);
                } catch(Error e) {
                	LOGGER.error("[PROXY] Caught error while managing proxy messages.", e);
                }
            }
	    }

	    public void closeClient() {
	        try {
                outConnector.pleaseStop = true;
                errConnector.pleaseStop = true;

                theClientConnection.close();
                theInput = null;
                theOutput = null;
            } catch (IOException e) {
            	LOGGER.error("[PROXY] Caught exception while closing proxy client.", e);
            }

	        theClientProcess.destroy();
	    }
	}

	public final int myPort;
	private final String myPlayerID;

	public ProxyGamePlayer(int port, Class<? extends Gamer> gamer) throws IOException
	{
		// Use a random gamer as our "default" gamer, that we fall back to
	    // in the event that we don't get a message from the client, or if
	    // we need to handle a simple request (START or STOP).
	    theDefaultGamer = new RandomGamer();

		observers = new ArrayList<Observer>();
		listener = null;
		while (listener == null) {
			try {
				listener = new ServerSocket(port);
			} catch (Exception ex) {
				listener = null;
				port++;
				LOGGER.error("[PROXY] Failed to start gamer on port: "+(port-1)+" trying port "+port + ".", ex);
			}
		}
		this.myPort = port;

		// Start up the socket for communicating with clients
		int clientPort = 17147;
		while(clientListener == null) {
		    try {
		        clientListener = new ServerSocket(clientPort);
		    } catch(Exception ex) {
		        clientListener = null;
		        clientPort++;
		    }
		}
		LOGGER.info("[PROXY] Opened client communication socket on port " + clientPort + ".");

		this.myPlayerID =  System.currentTimeMillis() + ".Proxy-" + this.theDefaultGamer.getName() + "." + this.myPort + "." + clientPort;

		LOGGER.info("[PROXY] Started proxy player " + this.myPlayerID + ". Writing logs to file " + this.myPlayerID + "\\logfile.log");

		// Start up the first ProxyClient
		gamerName = gamer.getSimpleName();
	}

	public int getGamerPort() {
	    return myPort;
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

	private Random theRandomGenerator = new Random();
	private long currentMoveCode = 0L;
	private boolean receivedClientMove = false;
	private boolean needRestart = false;

	@Override
	public void run()
	{
		ThreadContext.put("LOG_FOLDER", this.myPlayerID);
		LOGGER.info("[PROXY] Starting logs for proxy player " + this.myPlayerID + ". Proxy player for " + this.gamerName + ".");

		GamerConfiguration.showConfiguration();

	    // Start up the client manager
	    theClientManager = new ClientManager(Thread.currentThread());
	    theClientManager.start();

	    // Start up the input queue listener
	    inputQueue = new ArrayBlockingQueue<ProxyMessage>(100);
	    inputConnectionQueue = new ArrayBlockingQueue<Socket>(100);
	    QueueListenerThread theListener = new QueueListenerThread();
	    theListener.start();

		while (true)
		{
			try
			{
			    // First, read a message from the server.
				ProxyMessage nextMessage = inputQueue.take();
				Socket connection = inputConnectionQueue.take();
				String in = nextMessage.theMessage;
				long receptionTime = nextMessage.receptionTime;
				notifyObservers(new PlayerReceivedMessageEvent(in));
				LOGGER.info("[PROXY] Got incoming message:" + in);

				// Formulate a request, and see how the legal gamer responds.
				String legalProxiedResponse;
				Request request = new RequestFactory().create(theDefaultGamer, in);
				try {
				    legalProxiedResponse = request.process(receptionTime);
				} catch(OutOfMemoryError e) {
				    // Something went horribly wrong -- our baseline prover failed.
				    System.gc();
				    LOGGER.error("[PROXY] Caught error while processing a server request with default gamer.", e);
				    legalProxiedResponse = "SORRY";
				}
				latestProxiedResponse = legalProxiedResponse;
				LOGGER.info("[PROXY] Selected fallback move:" + latestProxiedResponse);

				if (!(request instanceof InfoRequest)) {
					// Update the move codes and prepare to send the request on to the client.
					receivedClientMove = false;
			        currentMoveCode = 1 + theRandomGenerator.nextLong();
			        if(request instanceof StopRequest || request instanceof AbortRequest)
			            theClientManager.expectStop = true;

					// Send the request on to the client, along with the move code.
					ProxyMessage theMessage = new ProxyMessage(in, currentMoveCode, receptionTime);
					theClientManager.sendMessage(theMessage);
	                if(!(request instanceof PlayRequest))   // If we're not asked for a move, just let
	                    currentMoveCode = 0L;               // the default gamer handle it by switching move code.

	                // Wait the appropriate amount of time for the request.
					proxyProcessRequest(request, receptionTime);
				} else {
					receivedClientMove = true;
				}

				// Get the latest response, and complain if it's the default response, or isn't a valid response.
				String out = latestProxiedResponse;
				if(!receivedClientMove && (request instanceof PlayRequest)) {
					LOGGER.error("[PROXY] Did not receive any move information from client for this turn; falling back to first legal move.");
					LOGGER.error("[PROXY] [ExecutiveSummary] Proxy did not receive any move information from client this turn: used first legal move.");
				}

				// Cycle the move codes again so that we will ignore any more responses
				// that the client sends along to us.
                currentMoveCode = 0L;

                // And finally write the latest response out to the server.
                LOGGER.info("[PROXY] [MESSAGE SENT] " + out);
				HttpWriter.writeAsServer(connection, out);
				connection.close();
				notifyObservers(new PlayerSentMessageEvent(out));

				// Once everything is said and done, restart the client if we're
				// due for a restart (having finished playing a game).
				if(needRestart) {
					LOGGER.info("[PROXY] Cleaning up and restarting client after end of match.");

	                theClientManager.closeClient();
	                theClientManager.pleaseStop = true;

	                if(GamerConfiguration.runningOnLinux()) {
	                	// Clean up the working directory and terminate any orphan processes.
	                	Thread.sleep(500);
	                	LOGGER.info("[PROXY] Calling cleanup scripts.");
	                	try {
	                	    Runtime.getRuntime().exec("./cleanup.sh").waitFor();
	                	} catch(IOException e) {
	                		LOGGER.error("[PROXY] Caught exception while running cleanup scripts.", e);
	                	}
	                	Thread.sleep(500);
	                }

	                theClientManager = new ClientManager(Thread.currentThread());
	                theClientManager.start();

	                theDefaultGamer = new RandomGamer();
	                GdlPool.drainPool();
	                SymbolPool.drainPool();

                    long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    double usedMemoryInMegs = usedMemory / 1024.0 / 1024.0;
                    LOGGER.info("[PROXY] Before collection, using " + usedMemoryInMegs + "mb of memory as proxy.");

	                // Okay, seriously garbage collect please. As it turns out,
	                // this takes some convincing; Java isn't usually eager to do it.
	                for(int i = 0; i < 10; i++) {
	                    System.gc();
	                    Thread.sleep(100);
	                }

	                usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	                usedMemoryInMegs = usedMemory / 1024.0 / 1024.0;
	                LOGGER.info("[PROXY] After collection, using a non-transient " + usedMemoryInMegs + "mb of memory as proxy.");

	                System.out.println("Cleaned up completed match, with a residual " + usedMemoryInMegs + "mb of memory as proxy.");

                    needRestart = false;
				}
			}
			catch (Exception e)
			{
				LOGGER.error("[PROXY] [DATA DROPPED]", e);
				notifyObservers(new PlayerDroppedPacketEvent());
			}
			catch (Error e)
			{
				LOGGER.error("[PROXY] [DATA DROPPED]", e);
			    notifyObservers(new PlayerDroppedPacketEvent());
			}
		}
	}

	public static final long METAGAME_BUFFER = Gamer.PREFERRED_METAGAME_BUFFER + 100;
	public static final long PLAY_BUFFER = Gamer.PREFERRED_PLAY_BUFFER + 100;

    private void proxyProcessRequest(Request theRequest, long receptionTime) {
        long startSleeping = System.currentTimeMillis();
        long timeToFinish = receptionTime;
        long timeToSleep = 0L;

	    try {
    	    if(theRequest instanceof PlayRequest) {
    	    	if (theDefaultGamer.getMatch() != null) {
                  // They have this long to play
                  timeToFinish = receptionTime + theDefaultGamer.getMatch().getPlayClock() * 1000 - PLAY_BUFFER;
                } else {
                  // Respond immediately if we're not tracking this match (and so don't know the play clock).
                  timeToFinish = System.currentTimeMillis();
                }
    	        timeToSleep = timeToFinish - System.currentTimeMillis();
    	        LOGGER.info("[PROXY] Forwarded PlayRequest to the proxy client. Waiting for a response.");
    	        if(timeToSleep > 0)
    	            Thread.sleep(timeToSleep);
    	    } else if(theRequest instanceof StartRequest) {
    	    	LOGGER.info("[PROXY] Forwarded StartRequest for match " + theDefaultGamer.getMatch().getMatchId() + " to the proxy client.");

    	    	ThreadContext.put("LOG_FILE", theDefaultGamer.getMatch().getMatchId() + "-" + theDefaultGamer.getRoleName().toString() + "-Proxy");

    	        System.out.println("Started playing " + theDefaultGamer.getMatch().getMatchId() + ".");

    	        // They have this long to metagame
    	        timeToFinish = receptionTime + theDefaultGamer.getMatch().getStartClock() * 1000 - METAGAME_BUFFER;
                timeToSleep = timeToFinish - System.currentTimeMillis();
                if(timeToSleep > 0)
                    Thread.sleep(timeToSleep);
    	   } else if(theRequest instanceof StopRequest || theRequest instanceof AbortRequest) {
    		   LOGGER.info("[PROXY] Forwarded " + theRequest.getClass().getSimpleName() + "message to the proxy client.");
    		   ThreadContext.remove("LOG_FILE");
    	       needRestart = true;
    	   }
	    } catch(InterruptedException ie) {
	        // Rise and shine!
	    	LOGGER.info("[PROXY] Got woken up by final move!");
	    }

	    LOGGER.info("[PROXY] Proxy slept for " + (System.currentTimeMillis() - startSleeping) + ", and woke up " + (System.currentTimeMillis() - timeToFinish) + "ms late (started " + (startSleeping - receptionTime) + "ms after receiving message).");
	}

	private String latestProxiedResponse;
	private void processClientResponse(ProxyMessage in, Thread toWakeUp) {
	    String theirTag = in.theMessage.substring(0,5);
	    String theirMessage = in.theMessage.substring(5);

	    // Ignore their message unless it has an up-to-date move code.
	    if(!(in.messageCode == currentMoveCode)) {
	        if(currentMoveCode > 0)
	        	LOGGER.error("[PROXY] CODE MISMATCH: " + currentMoveCode + " vs " + in.messageCode);
	        return;
	    }

	    if(theirTag.equals("WORK:")) {
            latestProxiedResponse = theirMessage;
            LOGGER.info("[PROXY] Got latest working move: " + latestProxiedResponse);
            receivedClientMove = true;
	    } else if(theirTag.equals("DONE:")) {
            latestProxiedResponse = theirMessage;
            LOGGER.info("[PROXY] Got a final move: " + latestProxiedResponse);
            receivedClientMove = true;
            currentMoveCode = 0L;
            toWakeUp.interrupt();
	    }
    }

	private BlockingQueue<ProxyMessage> inputQueue;
	private BlockingQueue<Socket> inputConnectionQueue;
	private class QueueListenerThread extends Thread {
	    @Override
		public void run() {
	        while(true) {
	            try {
                    // First, read a message from the server.
                    Socket connection = listener.accept();
                    String in = HttpReader.readAsServer(connection).replace('\n', ' ').replace('\r', ' ');
                    long receptionTime = System.currentTimeMillis();
                    if(inputQueue.remainingCapacity() > 0) {
                        inputQueue.add(new ProxyMessage(in, 0L, receptionTime));
                        inputConnectionQueue.add(connection);

                        LOGGER.info("[PROXY] [PROXY QueueListener] Got incoming message from game server: " + in + ". Added to queue in position " + inputQueue.size() + ".");
                    } else {
                    	LOGGER.error("[PROXY] [PROXY QueueListener] Got incoming message from game server: " + in + ". Could not add to queue, because queue is full!");
                    }
	            } catch(Exception e) {
	            	LOGGER.error("[PROXY] [PROXY QueueListener] Exception.", e);
	            } catch(Error e) {
	            	LOGGER.error("[PROXY] [PROXY QueueListener] Error.", e);
                }
	        }
	    }
	}
}