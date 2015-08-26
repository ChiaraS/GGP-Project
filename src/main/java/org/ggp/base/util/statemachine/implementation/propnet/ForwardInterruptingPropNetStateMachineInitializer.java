package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingPropNet;
import org.ggp.base.util.propnet.factory.ForwardInterruptingPropNetFactory;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.PropnetCreationException;

public class ForwardInterruptingPropNetStateMachineInitializer extends Thread {

	/**
	 * Reference to the state machine that must be initialized.
	 */
	private StateMachine stateMachine;

	/**
	 * Reference to the GDL game description that this class must use to initialize the state machine.
	 */
	private List<Gdl> description;

	/**
	 * Constructor that initializes the state machine that this class must initialize and the GDL
	 * game description that this class must use to initialize the state machine.
	 *
	 * @param description the GDL game description from which this class must create the propnet.
	 */
	public ForwardInterruptingPropNetStateMachineInitializer(StateMachine stateMachine, List<Gdl> description) {
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
			this.stateMachine.initialize(description);
		}catch(PropnetCreationException e){

		}





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
		}
	}

	/**
	 * Get method for the propnet.
	 *
	 * @return the propnet if it has been created in time, NULL otherwise.
	 */
	public ForwardInterruptingPropNet getPropNet(){
		return this.propNet;
	}

	/**
	 * Get method for the propnet construction time.
	 *
	 * @return the construction time of the propnet, -1 if it has not been created in time.
	 */
	public long getConstructionTime(){
		return this.constructionTime;
	}







}
