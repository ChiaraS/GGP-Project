package csironi.ggp.course.gamers.old;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public final class MonteCarloUCTGamer extends StateMachineGamer {

	public MonteCarloUCTGamer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractStateMachine getInitialStateMachine() {
		return new ExplicitStateMachine(new ProverStateMachine(this.random));
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		// TODO Auto-generated method stub

	}

	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		long start = System.currentTimeMillis();

		List<ExplicitMove> moves = getStateMachine().convertToExplicitMoves(getStateMachine().getLegalMoves(getCurrentState(), getRole()));
		ExplicitMove selection = (moves.get(this.random.nextInt(moves.size())));

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {

		return "MCTS UCT Gamer";

	}

}
