package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public interface JointMoveSelector {

	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException;

	public String getJointMoveSelectorParameters();

	public String printJointMoveSelector();

}
