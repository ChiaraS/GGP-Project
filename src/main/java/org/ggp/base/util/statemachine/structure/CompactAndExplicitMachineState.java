/**
 *
 */
package org.ggp.base.util.statemachine.structure;

import java.util.Set;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

/**
 * @author C.Sironi
 *
 */
public class CompactAndExplicitMachineState extends ExplicitMachineState {

	private OpenBitSet basePropsTruthValue;

	/**
	 *
	 */
	public CompactAndExplicitMachineState() {
		super();
		this.basePropsTruthValue = null;
	}

	/**
	 * @param contents
	 */
	public CompactAndExplicitMachineState(Set<GdlSentence> contents) {
		super(contents);
		this.basePropsTruthValue = null;
	}

	/**
	 * @param contents
	 */
	public CompactAndExplicitMachineState(Set<GdlSentence> contents, OpenBitSet basePropsTruthValue) {
		super(contents);
		this.basePropsTruthValue = basePropsTruthValue;
	}

	public OpenBitSet getBasePropsTruthValue(){
		return this.basePropsTruthValue;
	}

	@Override
	public boolean equals(Object o)
    {
        if ((o != null) && (o instanceof ExplicitMachineState)){
        	if(o instanceof CompactAndExplicitMachineState){
        		CompactAndExplicitMachineState state = (CompactAndExplicitMachineState) o;
        		return state.getBasePropsTruthValue().equals(this.getBasePropsTruthValue());
        	}else{
        		ExplicitMachineState state = (ExplicitMachineState) o;
        		return state.getContents().equals(this.getContents());
        	}
        }

        return false;
    }

	@Override
	public String toString(){
		String state = "[";
		for(int i = 0; i < this.basePropsTruthValue.size(); i++){
			if(this.basePropsTruthValue.fastGet(i)){
				state +=("1");
			}else{
				state +=("0");
			}
		}

		return super.toString() + "\n" + state + "]";
	}

}
