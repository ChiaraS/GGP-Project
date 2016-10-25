/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

/**
 * Random gamer realized for the GGP course.
 * This gamer chooses at each step a random legal action.
 *
 * @author C.Sironi
 *
 */
public class RandomTerminal extends SampleGamer {

	long totalTime;


	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		this.totalTime = 0L;
		GamerLogger.startFileLogging(getMatch(), getRole().getName().getValue());
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ProverMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();

		List<ProverMove> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		ProverMove selection = (moves.get(new Random().nextInt(moves.size())));

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;

	}

	@Override
	public void stateMachineStop() {

		this.callGetGoals();

		GamerLogger.stopFileLogging();

		//not necessary to repeat for each match
		GamerLogger.setSpilloverLogfile("logs/TotalTimes-"+ this.getName());

		GamerLogger.log("Time", "Total getGoals() times:" + this.totalTime + " ms");

		GamerLogger.setSpilloverLogfile(null);

	}

	protected void callGetGoals() {

		long start = System.currentTimeMillis();

		List<Integer> goals = null;

		Boolean fail = false;

		try {
			goals = getStateMachine().getGoals(getCurrentState());
		} catch (GoalDefinitionException | StateMachineException e) {
			fail=true;
		}

		long stop = System.currentTimeMillis();

		this.totalTime += (stop-start);

		if(!fail){
			if(goals != null){
				GamerLogger.log("Goals", goals.toString());
			}
			GamerLogger.log("Times", "Single getGoals() call: " + (stop-start) + " ms");
		}else{
			GamerLogger.log("Times", "[FAILED] Single getGoals() call: " + (stop-start) + " ms");
		}

	}


}
