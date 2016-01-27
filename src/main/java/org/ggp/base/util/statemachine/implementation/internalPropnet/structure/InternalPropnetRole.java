package org.ggp.base.util.statemachine.implementation.internalPropnet.structure;

import java.io.Serializable;

/**
 * This class represents a role as the index it has in the ordered list of roles.
 */
@SuppressWarnings("serial")
public class InternalPropnetRole implements Serializable{

    private final int index;

    public InternalPropnetRole(int index){
        this.index = index;
    }

    @Override
    public boolean equals(Object o){

        if ((o != null) && (o instanceof InternalPropnetRole)){
        	InternalPropnetRole role = (InternalPropnetRole) o;
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