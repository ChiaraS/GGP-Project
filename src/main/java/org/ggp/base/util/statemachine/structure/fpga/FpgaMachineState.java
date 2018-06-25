package org.ggp.base.util.statemachine.structure.fpga;

import org.ggp.base.util.placeholders.FpgaInternalState;
import org.ggp.base.util.statemachine.structure.MachineState;

public class FpgaMachineState extends MachineState {

	/**
	 * Substitute with FPGA state type.
	 */
	private FpgaInternalState stateRepresentation;

	public FpgaMachineState(){
		this.stateRepresentation = null;
	}

	public FpgaMachineState(FpgaInternalState state){
		this.stateRepresentation = state;
	}

	public FpgaInternalState getStateRepresentation(){
		return this.stateRepresentation;
	}

	@Override
	public FpgaMachineState clone() {
		// TODO: fix! the clone method
		//return new FpgaMachineState(this.stateRepresentation.clone());
		return null;
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
			// TODO Implement depending on the state representation
			/*String toReturn = "[";
			for(int i = 0; i < this.stateRepresentation.size(); i++){
				if(this.stateRepresentation.fastGet(i)){
					toReturn += "1";
				}else{
					toReturn += "0";
				}
			}
			toReturn += "]";
			return toReturn;
			*/
			return null;
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
