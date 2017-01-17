package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.Arrays;

import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class represents a move as an array of indices.
 *
 * Used for parameters tuning. A move is a list of indices, one for each parameter
 * being tuned, indicating the index that the value being assigned to the parameter
 * has in the list of all possible values for the parameter.
 *
 * @author C.Sironi
 *
 */
@SuppressWarnings("serial")
public class CombinatorialCompactMove extends Move {

	/**
	 * The combinatorial move (i.e. the list of indices that the values to be assigned to the different
	 * parameters have in the corresponding list of available values for the parameter).
	 */
	private int[] combinatorialMove;

	public CombinatorialCompactMove(int[] combinatorialMove) {
		this.combinatorialMove = combinatorialMove;
	}

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof CombinatorialCompactMove)) {
        	CombinatorialCompactMove move = (CombinatorialCompactMove) o;
            return Arrays.equals(this.combinatorialMove, move.getIndices());
        }

        return false;
    }

    public int[] getIndices(){
        return this.combinatorialMove;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(this.combinatorialMove);
    }

    @Override
    public String toString(){

    	if(this.combinatorialMove != null){
    		String s = "[ ";
    		for(int i = 0; i < this.combinatorialMove.length; i++){
    			s += this.combinatorialMove[i] + " ";
    		}
    		s += "]";
    		return s;
    	}else{
    		return "null";
    	}

    }

}
