package org.ggp.base.util.statemachine.structure.fpga;

import org.ggp.base.util.placeholders.FpgaInternalMove;
import org.ggp.base.util.statemachine.structure.Move;

@SuppressWarnings("serial")
public class FpgaMove extends Move{

	/**
	 * Substitute with the FPGA library representation for the move
	 * TODO
	 */
    private final FpgaInternalMove internalMove;

    public FpgaMove(FpgaInternalMove internalMove){
        this.internalMove = internalMove;
    }

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof FpgaMove)) {
        	FpgaMove move = (FpgaMove) o;
            return this.internalMove.equals(move.getInternalMove());
        }

        return false;
    }

    public FpgaInternalMove getInternalMove(){
        return this.internalMove;
    }

    @Override
    public int hashCode()
    {
        return this.internalMove.hashCode();
    }

    @Override
    public String toString()
    {
        return "" + this.internalMove;
    }

}
