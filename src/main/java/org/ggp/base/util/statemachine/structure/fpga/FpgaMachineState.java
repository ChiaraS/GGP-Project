package org.ggp.base.util.statemachine.structure.fpga;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.statemachine.structure.MachineState;

public class FpgaMachineState extends MachineState {

	/**
	 * Substitute with FPGA state type.
	 */
	private OpenBitSet stateRepresentation;

	public FpgaMachineState(){
		this.stateRepresentation = null;
	}

	public FpgaMachineState(OpenBitSet state){
		this.stateRepresentation = state;
	}

	public OpenBitSet getStateRepresentation(){
		return this.stateRepresentation;
	}

	@Override
	public FpgaMachineState clone() {
		return new FpgaMachineState(this.stateRepresentation.clone());
	}

	/* Utility methods */
    @Override
	public int hashCode(){
        return this.stateRepresentation.hashCode();
    }

	@Override
	public String toString(){
		if(this.stateRepresentation == null){
			return "[MachineState with null values]";
		}else{
			String toReturn = "[";
			for(int i = 0; i < this.stateRepresentation.size(); i++){
				if(this.stateRepresentation.fastGet(i)){
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
		if ((o != null) && (o instanceof FpgaMachineState)){
			FpgaMachineState state = (FpgaMachineState) o;
			if(this.stateRepresentation == null){
				return state.getStateRepresentation() == null;
			}else{
				if(state.getStateRepresentation() == null){
					return false;
				}else{
					return state.getStateRepresentation().equals(this.stateRepresentation);
				}
			}
	    }
        return false;
    }

}
