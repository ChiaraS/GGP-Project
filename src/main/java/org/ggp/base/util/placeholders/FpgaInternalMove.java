package org.ggp.base.util.placeholders;

import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class FpgaInternalMove {

	private final CompactMove comapctMove;

    public FpgaInternalMove(CompactMove comapctMove){
        this.comapctMove = comapctMove;
    }

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof FpgaInternalMove)) {
        	FpgaInternalMove otherMove = (FpgaInternalMove) o;
            return this.comapctMove.equals(otherMove.getCompactMove());
        }

        return false;
    }

    public CompactMove getCompactMove(){
        return this.comapctMove;
    }

    @Override
    public int hashCode()
    {
        return this.comapctMove.hashCode();
    }

    @Override
    public String toString()
    {
        return "FIM[" + this.comapctMove.toString() + "]";
    }

}
