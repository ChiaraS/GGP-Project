package org.ggp.base.util.statemachine.structure.fpga;

import org.ggp.base.util.statemachine.structure.Move;

@SuppressWarnings("serial")
public class FpgaMove extends Move{

	/**
	 * Substitute with the FPGA library representation for the move
	 */
    private final int moveIndex;

    public FpgaMove(int moveIndex){
        this.moveIndex = moveIndex;
    }

    @Override
    public boolean equals(Object o){
        if ((o != null) && (o instanceof FpgaMove)) {
        	FpgaMove move = (FpgaMove) o;
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
