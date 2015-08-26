package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;

public class StateMachineInitializer extends Thread {

	/**
	 * Reference to the state machine that must be initialized.
	 */
	private StateMachine stateMachine;

	/**
	 * Reference to the GDL game description that this class must use to initialize the state machine.
	 */
	private List<Gdl> description;

	/**
	 * Total time taken for initialization.
	 */
	private long initializationTime;

	/**
	 * Constructor that initializes the state machine that this class must initialize and the GDL
	 * game description that this class must use to initialize the state machine.
	 *
	 * @param description the GDL game description from which this class must create the propnet.
	 */
	public StateMachineInitializer(StateMachine stateMachine, List<Gdl> description) {
		this.stateMachine = stateMachine;
		this.description = description;
	}

	/**
	 * This method tries to initialize the state machine with the GDL description.
	 * If initialization takes too long this thread can be interrupted.
	 */
	@Override
	public void run(){

		try{
			this.stateMachine.initialize(this.description);
		}catch(Exception e){
			this.stateMachine = null;
			GamerLogger.logError("StateMachine", "[StateMachineInitializer] Exception during state machine initialization.");
			GamerLogger.logStackTrace("StateMachine", e);
		}






		/*
		try{
			long startTime = System.currentTimeMillis();
			this.propNet = ForwardInterruptingPropNetFactory.create(this.description);
			this.constructionTime = System.currentTimeMillis() - startTime;
			GamerLogger.log("StateMachine", "[Propnet Creator] Propnet creation done. It took " + (this.constructionTime) + "ms.");
		}catch(InterruptedException ex){
			this.propNet = null;
			GamerLogger.logError("StateMachine", "[Propnet Creator] Propnet creation interrupted. Interrupted exception!");
			GamerLogger.logStackTrace("StateMachine", ex);
		}catch(OutOfMemoryError er){
			this.propNet = null;
			GamerLogger.logError("StateMachine", "[Propnet Creator] Propnet creation interrupted. Out of memory error!");
			GamerLogger.logStackTrace("StateMachine", er);
		}catch(Exception ex){
			this.propNet = null;
			GamerLogger.logError("StateMachine", "[Propnet Creator] Propnet creation interrupted. Exception during creation!");
			GamerLogger.logStackTrace("StateMachine", ex);
		}catch(Error er){
			this.propNet = null;
			GamerLogger.logError("StateMachine", "[Propnet Creator] Propnet creation interrupted. Error during creation!");
			GamerLogger.logStackTrace("StateMachine", er);
		}*/
	}


	/**
	 * Get method for the state machine initialization time.
	 *
	 * @return the initialization time of the state machine, -1 if it has not been initialized in time.
	 */
	public long getInitializationTime(){
		return this.initializationTime;
	}







}
