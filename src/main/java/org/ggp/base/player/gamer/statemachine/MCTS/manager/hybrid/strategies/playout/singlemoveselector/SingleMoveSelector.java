package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public abstract class SingleMoveSelector extends SearchManagerComponent{

	public SingleMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract Move getMoveForRole(MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException;

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "(SINGLE_MOVE_SEL = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(SINGLE_MOVE_SEL = " + this.getClass().getSimpleName() + ")";
		}
	}

}
