package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class RandomSingleMoveSelector extends SingleMoveSelector{

	public RandomSingleMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {
		return this.gameDependentParameters.getTheMachine().getRandomMove(state, this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex));
	}

	@Override
	public String getComponentParameters() {
		return null;
	}



}
