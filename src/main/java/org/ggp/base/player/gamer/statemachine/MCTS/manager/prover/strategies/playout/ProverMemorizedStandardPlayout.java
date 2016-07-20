package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector.ProverJointMoveSelector;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;

public class ProverMemorizedStandardPlayout extends ProverStandardPlayout {

    protected List<List<Move>> allJointMoves;

	public ProverMemorizedStandardPlayout(StateMachine theMachine,
			ProverJointMoveSelector jointMoveSelector, List<List<Move>> allJointMoves) {
		super(theMachine, jointMoveSelector);

		this.allJointMoves = allJointMoves;
		this.allJointMoves.clear(); // Just to make sure that if the list given as input is not empty it will be before being used in this class.
	}

	@Override
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException{
		List<Move> theChosenMove =  super.getJointMove(state);
		this.allJointMoves.add(theChosenMove);
		return theChosenMove;
	}

	public void clearLastMemorizedPlayout(){
		this.allJointMoves.clear();
	}

}
