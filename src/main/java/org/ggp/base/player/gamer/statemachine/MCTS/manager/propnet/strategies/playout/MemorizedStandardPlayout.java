package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.JointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MemorizedStandardPlayout extends StandardPlayout {

    protected List<List<InternalPropnetMove>> allJointMoves;

	public MemorizedStandardPlayout(InternalPropnetStateMachine theMachine,
			JointMoveSelector jointMoveSelector, List<List<InternalPropnetMove>> allJointMoves) {
		super(theMachine, jointMoveSelector);

		this.allJointMoves = allJointMoves;
		this.allJointMoves.clear(); // Just to make sure that if the list given as input is not empty it will be before being used in this class.
	}

	@Override
	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{
		List<InternalPropnetMove> theChosenMove =  super.getJointMove(state);
		this.allJointMoves.add(theChosenMove);
		return theChosenMove;
	}

	public void clearLastMemorizedPlayout(){
		this.allJointMoves.clear();
	}

	/*
	public void printJM(){
		System.out.println("All joint moves: " + this.allJointMoves.size());

		System.out.println("[");

		for(List<InternalPropnetMove> jm : this.allJointMoves){

			System.out.print("( ");
			for(InternalPropnetMove i : jm){
				System.out.print(i + ", ");
			}
			System.out.println(")");
		}

		System.out.println("]");
	}
	*/

}
