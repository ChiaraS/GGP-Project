/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparatePropnetCreationManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 * @author C.Sironi
 *
 */
public class SlowDUCTGamer extends StateMachineGamer {

	/**
	 * The player must complete the executions of methods with a timeout by the time
	 * [timeout - safetyMargin(ms)] to increase the certainty of answering to the Game
	 * Manager in time.
	 */
	private long safetyMargin;

	/**
	 * The monte
	 */

	/**
	 *
	 */
	public SlowDUCTGamer() {
		// TODO: change code so that the parameters can be set from outside.
		this.safetyMargin = 1000L;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#getInitialStateMachine()
	 */
	@Override
	public StateMachine getInitialStateMachine() {

		if(System.currentTimeMillis() < this.getMetagamingTimeout() - this.safetyMargin){

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        SeparatePropnetCreationManager manager = new SeparatePropnetCreationManager(getMatch().getGame().getRules(), this.getMetagamingTimeout());

	        // Start the manager
	  	  	executor.execute(manager);

	  	  	// Shutdown executor to tell it not to accept any more task to execute.
			// Note that this doesn't interrupt previously started tasks.
			executor.shutdown();

			// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
			try{
				executor.awaitTermination(this.getMetagamingTimeout() - System.currentTimeMillis() - this.safetyMargin, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){ // The thread running the gamer has been interrupted => stop playing.
				executor.shutdownNow(); // Interrupt everything
				GamerLogger.logError("Gamer", "Gamer interrupted while computing initial state machine.");
				GamerLogger.logStackTrace("Gamer", e);
				Thread.currentThread().interrupt();
				return new CachedStateMachine(new ProverStateMachine());
			}

			// Here the available time has elapsed, so we must interrupt the thread if it is still running.
			executor.shutdownNow();

			// If the thread is terminated, we can get the propnet, otherwise we return the prover.
			// TODO: if the thread isn't terminated, we don't wait for it to do so, we ignore the propnet
			// and give back the prover. A check must be added: it could be that the manager built the
			// propnet but it is still busy optimizing it. In this case the last completed optimization
			// of the propnet is usable so we should not discard it! For example, if the thread isn't
			// terminated, we could wait for half of the time still available and check again. We cannot
			// get the propnet before being sure that the manager has terminated, otherwise we risk getting
			// one in an inconsistent state. Also the manager must be fixed so that if it gets interrupted
			// while running an optimization it can return the propnet and its state at the previous optimization.
			if(executor.isTerminated()){

				// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
				ImmutablePropNet propnet = manager.getImmutablePropnet();
				ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

				if(propnet != null && propnetState != null){

					// Create the state machine giving it the propnet and the propnet state.
				    return new SeparateInternalPropnetStateMachine(propnet, propnetState);
				}
			}
		}

		return new CachedStateMachine(new ProverStateMachine());
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineMetaGame(long)
	 */
	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		// Create the MCTS tree and start simulations

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#preview(org.ggp.base.util.game.Game, long)
	 */
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
