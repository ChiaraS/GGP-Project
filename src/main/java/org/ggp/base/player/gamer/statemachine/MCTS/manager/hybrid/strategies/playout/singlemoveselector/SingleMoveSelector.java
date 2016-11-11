package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public abstract class SingleMoveSelector extends SearchManagerComponent{

	public SingleMoveSelector(GameDependentParameters gameDependentParameters){
		super(gameDependentParameters);
	}

	public abstract Move getMoveForRole(MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException;

	public abstract String getSingleMoveSelectorParameters();

	public abstract String printSingleMoveSelector();

}
