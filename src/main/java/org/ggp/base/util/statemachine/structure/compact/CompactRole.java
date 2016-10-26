package org.ggp.base.util.statemachine.structure.compact;

import java.io.Serializable;

import org.ggp.base.util.statemachine.structure.Role;



/**
 * This class represents a role as the index it has in the ordered list of roles.
 */
@SuppressWarnings("serial")
public class CompactRole implements Serializable, Role{

    private final int index;

    public CompactRole(int index){
        this.index = index;
    }

    @Override
    public boolean equals(Object o){

        if ((o != null) && (o instanceof CompactRole)){
        	CompactRole role = (CompactRole) o;
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