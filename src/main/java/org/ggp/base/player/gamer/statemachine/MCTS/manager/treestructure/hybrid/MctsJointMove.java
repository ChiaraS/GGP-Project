package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import java.util.List;

import org.ggp.base.util.statemachine.structure.Move;


public class MctsJointMove {

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<Move> jointMove;

	public MctsJointMove(List<Move> jointMove) {
		this.jointMove = jointMove;
	}

	public List<Move> getJointMove() {
		return jointMove;
	}

	@Override
	public String toString(){
		String s = "[ ";
		for(Move m : this.jointMove){
			s += m + " ";
		}
		s += "]";

		return s;
	}

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof MctsJointMove)) {
        	MctsJointMove move = (MctsJointMove) o;
            return this.jointMove.equals(move.getJointMove());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.jointMove.hashCode();
    }


}
