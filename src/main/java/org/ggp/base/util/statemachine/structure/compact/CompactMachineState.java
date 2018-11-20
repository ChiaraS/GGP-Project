package org.ggp.base.util.statemachine.structure.compact;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.statemachine.structure.MachineState;

/**
 * @author C.Sironi
 *
 */
public class CompactMachineState extends MachineState{

	private final OpenBitSet truthValues;

	public CompactMachineState(){
		this.truthValues = null;
	}

	public CompactMachineState(OpenBitSet truthValues){
		this.truthValues = truthValues;
	}

	public OpenBitSet getTruthValues(){
		return this.truthValues;
	}

	@Override
	public CompactMachineState clone() {
		return new CompactMachineState(this.truthValues.clone());
	}

	/* Utility methods */
	/*
	 *  Implementation of hash code for openBitSet?????
	 *
  	 *	public int hashCode() {
     *  // Start with a zero hash and use a mix that results in zero if the input is zero.
     *  // This effectively truncates trailing zeros without an explicit check.
     *  long h = 0;
     *      for (int i = bits.length; --i>=0;) {
     *          h ^= bits[i];
     *          h = (h << 1) | (h >>> 63); // rotate left
     *      }
     *      // fold leftmost bits into right and add a constant to prevent
     *      // empty sets from returning 0, which is too common.
     *      return (int)((h>>32) ^ h) + 0x98761234;
     *  }
     *
	 */
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
		if ((o != null) && (o instanceof CompactMachineState)){
			CompactMachineState state = (CompactMachineState) o;
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
