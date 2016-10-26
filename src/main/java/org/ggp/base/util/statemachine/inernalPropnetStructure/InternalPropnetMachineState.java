package org.ggp.base.util.statemachine.inernalPropnetStructure;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.structure.MachineState;

/**
 * @author C.Sironi
 *
 */
public class InternalPropnetMachineState implements MachineState{

	private final OpenBitSet truthValues;

	public InternalPropnetMachineState(){
		this.truthValues = null;
	}

	public InternalPropnetMachineState(OpenBitSet truthValues){
		this.truthValues = truthValues;
	}

	public OpenBitSet getTruthValues(){
		return this.truthValues;
	}

	@Override
	public InternalPropnetMachineState clone() {
		return new InternalPropnetMachineState(this.truthValues.clone());
	}

	/* Utility methods */
    @Override
	public int hashCode(){
        return this.truthValues.hashCode();
    }

	@Override
	public String toString(){
		if(this.truthValues == null){
			return "[MachineState with null values]";
		}else{
			String toReturn = "[";
			for(int i = 0; i < this.truthValues.size(); i++){
				if(this.truthValues.fastGet(i)){
					toReturn += "1";
				}else{
					toReturn += "0";
				}
			}
			toReturn += "]";
			return toReturn;
		}
	}

	@Override
	public boolean equals(Object o){
		if ((o != null) && (o instanceof InternalPropnetMachineState)){
			InternalPropnetMachineState state = (InternalPropnetMachineState) o;
			if(this.truthValues == null){
				return state.getTruthValues() == null;
			}else{
				if(state.getTruthValues() == null){
					return false;
				}else{
					return state.getTruthValues().equals(this.truthValues);
				}
			}
	    }
        return false;
    }

}
