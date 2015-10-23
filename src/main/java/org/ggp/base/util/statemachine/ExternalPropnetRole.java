package org.ggp.base.util.statemachine;

import java.io.Serializable;

/**
 * This class represents a role as the index it has in the ordered list of roles.
 */
@SuppressWarnings("serial")
public class ExternalPropnetRole implements Serializable{

    private final int index;

    public ExternalPropnetRole(int index){
        this.index = index;
    }

    @Override
    public boolean equals(Object o){

        if ((o != null) && (o instanceof ExternalPropnetRole)){
        	ExternalPropnetRole role = (ExternalPropnetRole) o;
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