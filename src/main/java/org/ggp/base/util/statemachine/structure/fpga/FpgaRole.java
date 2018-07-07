package org.ggp.base.util.statemachine.structure.fpga;

import org.ggp.base.util.statemachine.structure.Role;

@SuppressWarnings("serial")
public class FpgaRole extends Role {

	/**
	 * Substitute with the FPGA library representation of roles.
	 */
    private final int index;

    public FpgaRole(int index){
        this.index = index;
    }

    @Override
    public boolean equals(Object o){

        if ((o != null) && (o instanceof FpgaRole)){
        	FpgaRole role = (FpgaRole) o;
            return this.index == role.getIndex();
        }

        return false;
    }

    public int getIndex(){
        return this.index;
    }

    @Override
    public int hashCode()
    {
        return this.index;
    }

    @Override
    public String toString()
    {
        return "" + this.index;
    }

}
