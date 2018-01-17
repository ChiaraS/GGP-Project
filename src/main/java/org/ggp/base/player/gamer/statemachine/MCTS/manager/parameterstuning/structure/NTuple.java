package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.Arrays;

public class NTuple {

	/**
	 * Indices in the fixed parameter oder of the parameters that form the n-tuple.
	 */
	private int[] paramIndices;

	public NTuple(int[] paramIndices) {
		this.paramIndices = paramIndices;
	}

	public int[] getParamIndices() {
		return this.paramIndices;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.paramIndices);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
        if ((o != null) && (o instanceof NTuple)) {
        	NTuple ntuple = (NTuple) o;
            return Arrays.equals(this.paramIndices, ntuple.getParamIndices());
        }
        return false;
	}

    @Override
    public String toString(){

    	if(this.paramIndices != null){
    		String s = "[ ";
    		for(int i = 0; i < this.paramIndices.length; i++){
    			s += this.paramIndices[i] + " ";
    		}
    		s += "]";
    		return s;
    	}else{
    		return "null";
    	}

    }

}
