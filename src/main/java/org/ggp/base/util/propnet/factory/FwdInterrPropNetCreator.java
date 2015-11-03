package org.ggp.base.util.propnet.factory;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingPropNet;

/**
 * This class is used to run the creation of the propnet in a separate thread that can be
 * interrupted if taking too long. This class is used only by the CheckFwdInterrPropnetStateMachine
 * that internally checks during initialization that the building time of the propnet doesn't
 * exceed the given time limit.
 *
 * @author C.Sironi
 *
 */
public class FwdInterrPropNetCreator extends Thread {

	/**
	 * Reference to the (hopefully) created propnet.
	 */
	private ForwardInterruptingPropNet propNet;

	/**
	 * Total time (in milliseconds) taken to construct the propnet.
	 * If it is negative it means that the propnet didn't build in time.
	 */
	long constructionTime;

	/**
	 * Reference to the GDL game description to be used to create the propnet.
	 */
	private List<Gdl> description;

	/**
	 * Constructor that initializes the GDL game description from which this class must create the propnet.
	 *
	 * @param description the GDL game description from which this class must create the propnet.
	 */
	public FwdInterrPropNetCreator(List<Gdl> description) {
		this.propNet = null;
		this.constructionTime = -1L;
		this.description = description;
	}

	/**
	 * This method tries to create the propnet from the GDL description. If interrupted it just leaves
	 * the propnet initialized to null.
	 */
	@Override
	public void run(){
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


	public long getInitTime(){
		if(this.propNet == null){
			return -1L;
		}else{
			return this.propNet.getInitTime();
		}
	}


}
