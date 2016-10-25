package org.ggp.base.util.statemachine.inernalPropnetStructure;

import org.ggp.base.util.statemachine.Mmove;

/**
 * A Move represents a possible move that can be made by a role. Each
 * player makes exactly one move on every turn. This includes moves
 * that represent passing, or taking no action.
 * <p>
 * Note that Move objects are not intrinsically tied to a role. They
 * only express the action itself.
 *
 * This class represents a move for the propnet with externalized satate.
 * It represents it with the index that the move has in the input propositions
 * array.
 */
@SuppressWarnings("serial")
public class InternalPropnetMove extends Mmove{
    private final int moveIndex;

    public InternalPropnetMove(int moveIndex){
        this.moveIndex = moveIndex;
    }

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof InternalPropnetMove)) {
        	InternalPropnetMove move = (InternalPropnetMove) o;
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