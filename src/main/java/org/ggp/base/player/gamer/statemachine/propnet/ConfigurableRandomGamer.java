package org.ggp.base.player.gamer.statemachine.propnet;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class ConfigurableRandomGamer extends InternalPropnetGamer {

	public ConfigurableRandomGamer() {
		this(GamerConfiguration.gamersSettingsFolderPath + "/" + defaultSettingsFileName);
	}

	public ConfigurableRandomGamer(String settingsFilePath) {
		super(settingsFilePath);
	}

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		// Do nothing

	}

	@Override
	public ExplicitMove stateMachineSelectMove(long timeout) throws TransitionDefinitionException,
			MoveDefinitionException, GoalDefinitionException, StateMachineException {
		long start = System.currentTimeMillis();

		List<ExplicitMove> moves = getStateMachine().convertToExplicitMoves(getStateMachine().getLegalMoves(getCurrentState(), getRole()));
		ExplicitMove selection = (moves.get(this.random.nextInt(moves.size())));

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

}
