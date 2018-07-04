package org.ggp.base.util.placeholders;

import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;

public class FpgaInternalState {

	private final CompactMachineState compactState;

	public FpgaInternalState(){
		this.compactState = null;
	}

	public FpgaInternalState(CompactMachineState compactState){
		this.compactState = compactState;
	}

	public CompactMachineState getCompactMachineState(){
		return this.compactState;
	}

	@Override
	public FpgaInternalState clone() {
		return new FpgaInternalState(this.compactState.clone());
	}

	/* Utility methods */
    @Override
	public int hashCode(){
        return this.compactState.hashCode();
    }

	@Override
	public String toString(){
		return "FIS[" + this.compactState + "]";
	}

	@Override
	public boolean equals(Object o){
		if ((o != null) && (o instanceof FpgaInternalState)){
			FpgaInternalState state = (FpgaInternalState) o;
			if(this.compactState == null){
				return state.getCompactMachineState() == null;
			}else{
				if(state.getCompactMachineState() == null){
					return false;
				}else{
					return state.getCompactMachineState().equals(this.compactState);
				}
			}
	    }
        return false;
    }

}
