package org.ggp.base.util.statemachine.structure.compact;

import org.ggp.base.util.statemachine.structure.Move;


/**
 * A Move represents a possible move that can be made by a role. Each
 * player makes exactly one move on every turn. This includes moves
 * that represent passing, or taking no action.
 * <p>
 * Note that Move objects are not intrinsically tied to a role. They
 * only express the action itself.
 *
 * This class represents a move as an index (i.e. the index that the move has
 * in a certain structure in which the move is used).
 *
 * For example, for a move in a game that is represented with a propnet the
 * index is the one that the move has in the input propositions array of the
 * propnet.
 *
 * For a move in the context of parameters tuning, the index is the one that
 * the value that the move assigns to the corresponding parameter has in the
 * list of all possible values of the parameter.
 */
@SuppressWarnings("serial")
public class CompactMove extends Move{
    private final int moveIndex;

    public CompactMove(int moveIndex){
        this.moveIndex = moveIndex;
    }

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof CompactMove)) {
        	CompactMove move = (CompactMove) o;
            return this.moveIndex == move.getIndex();
        }

        return false;
    }

    public int getIndex(){
        return this.moveIndex;
    }

    @Override
    public int hashCode()
    {
        return this.moveIndex;
    }

    @Override
    public String toString()
    {
        return "" + this.moveIndex;
    }
}